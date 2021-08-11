package com.vsoft.goodmankotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.database.subscribeOnBackground
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.videoUploadSaveRespose
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import com.vsoft.goodmankotlin.utils.NetworkUtils
import com.vsoft.goodmankotlin.utils.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DashBoardActivity : AppCompatActivity(), View.OnClickListener, CustomDialogCallback {
    private lateinit var addOperator: LinearLayout
    private lateinit var addDie: LinearLayout
    private lateinit var sync: LinearLayout
    private lateinit var skip: LinearLayout
    private lateinit var logout: LinearLayout
    private lateinit var progressDialog: ProgressDialog
    private lateinit var vm: VideoViewModel
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        init()
        initProgress()
    }

    private fun init() {
        addOperator = findViewById(R.id.addOperator)
        addDie = findViewById(R.id.addDie)
        sync = findViewById(R.id.sync)
        skip = findViewById(R.id.skip)
        logout = findViewById(R.id.logout)
        addOperator.setOnClickListener(this)
        addDie.setOnClickListener(this)
        sync.setOnClickListener(this)
        skip.setOnClickListener(this)
        logout.setOnClickListener(this)
        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]
    }

    private fun initProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(this@DashBoardActivity.resources.getString(R.string.progress_dialog_message_sync))
    }

    override fun onClick(v: View?) {
        if (v?.id == addOperator.id) {
            showCustomAlert(
                this@DashBoardActivity.resources.getString(R.string.add_operator_alert_message),
                CommonUtils.NO_OPERATOR_FUNCTIONALITY_IMPLEMENTED_DIALOG,
                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
            )
        }

        if (v?.id == addDie.id) {
            navigateToAddDie()
        }
        if (v?.id == sync.id) {
            sync()
        }
        if (v?.id == skip.id) {
            navigateToOperatorSelection()
        }
        if (v?.id == logout.id) {
            showCustomAlert(
                this@DashBoardActivity.resources.getString(R.string.logout_alert_message),
                CommonUtils.LOGOUT_DIALOG,
                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok), this@DashBoardActivity.resources.getString(R.string.alert_cancel))
            )
        }
    }

    private fun showCustomAlert(
        alertMessage: String,
        functionality: String,
        buttonList: List<String>
    ) {
        var customDialogModel = CustomDialogModel(
            getString(R.string.app_name), alertMessage, null,
            buttonList
        )
        DialogUtils.showCustomAlert(this, customDialogModel, this, functionality)
    }

    override fun onBackPressed() {
        showCustomAlert(this@DashBoardActivity.resources.getString(R.string.exit_app_alert_message), CommonUtils.BACK_PRESSED_DIALOG, listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok), this@DashBoardActivity.resources.getString(R.string.alert_cancel)))
    }

    private fun navigateToOperatorSelection() {
        /*val mainIntent = Intent(this, OperatorSelectActivityWithWebservice::class.java)
        startActivity(mainIntent)*/

        val mainIntent = Intent(this, AddDieOperatorSelectActivity::class.java)
        mainIntent.putExtra(CommonUtils.IS_NEW_DIE,false)
        startActivity(mainIntent)
    }

    private fun navigateToLogin() {
        val mainIntent = Intent(this, LoginActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun navigateToAddDie() {
      /*  val mainIntent = Intent(this@DashBoardActivity, AddDieActivityNew::class.java)
        startActivity(mainIntent)*/

        val mainIntent = Intent(this, AddDieOperatorSelectActivity::class.java)
        mainIntent.putExtra(CommonUtils.IS_NEW_DIE,true)
        startActivity(mainIntent)
    }

    private fun sync() {
        var videosList: List<VideoModel>? = null
        subscribeOnBackground {
            videosList = vm.getVideos()
            if (videosList!!.isEmpty()) {
                runOnUiThread(Runnable {
                    showCustomAlert(
                        this@DashBoardActivity.resources.getString(R.string.no_videos_available), CommonUtils.VIDEO_SYNC_DIALOG,
                        listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                    )
                })
            } else {
                Log.i("Videos observed size", "${videosList?.size}")
                val iterator = videosList!!.listIterator()
                if (iterator.hasNext()) {
                    val item = iterator.next()
                    if (!item.status) {
                        save(this, item)
                    }
                } else {
                    runOnUiThread(Runnable {
                        showCustomAlert(
                            this@DashBoardActivity.resources.getString(R.string.sync_videos_alert_message_success), CommonUtils.VIDEO_SYNC_DIALOG,
                            listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                        )
                    })
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun save(context: Context, item: VideoModel) {
        Log.i("Id:", "${item.id}")
        Log.i("Status:", "${item.status}")
        Log.i("video die type :", item.die_top_bottom)
        val jsonObject = JsonObject()
        val gson = Gson()
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_DIE_ID, item.die_id)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_PART_ID, item.part_id)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_DIE_TOP_BOTTOM, item.die_top_bottom)
        val path = item.video_path;
        val filename: String = path.substring(path.lastIndexOf("/") + 1)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_FILE_NAME, filename)

        val jsonString = gson.toJson(jsonObject)

        Log.i("save jsonString ", jsonString)
        Log.i("save path ", path)
        val rootFolder: File? = context.getExternalFilesDir(null)
        val jsonFile = File(rootFolder, "post.json")
        val writer = FileWriter(jsonFile)
        writer.write(jsonString)
        writer.close()
        val metaDataFilePart = MultipartBody.Part.createFormData(
            CommonUtils.SYNC_VIDEO_API_META_DATA,
            jsonFile.name,
            RequestBody.create(MediaType.parse("*/*"), jsonFile)
        )

        val file = File(path) // initialize file here
        val videoFilePart = MultipartBody.Part.createFormData(
            CommonUtils.SYNC_VIDEO_API_FILE,
            file.name,
            RequestBody.create(MediaType.parse("image/*"), file)
        )
        saveVideoToServer(item, metaDataFilePart, videoFilePart)
    }

    private fun saveVideoToServer(
        item: VideoModel,
        metaData: MultipartBody.Part,
        videoFile: MultipartBody.Part) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog.show()
            }
            val call: Call<videoUploadSaveRespose?>? =
                RetrofitClient.getInstance()!!.getMyApi1()!!.saveVideo(metaData, videoFile)
            call!!.enqueue(object : Callback<videoUploadSaveRespose?> {
                override fun onResponse(
                    call: Call<videoUploadSaveRespose?>,
                    response: Response<videoUploadSaveRespose?>
                ) {
                    try {
                        Log.i("response  ", "$response")
                        val statusCode = response.body()!!.statusCode
                        if (statusCode == 200) {
                            runOnUiThread(Runnable {
                                item.status = true
                                val status: Int = vm.update(item)
                                Log.i("response update status ", "$status")
                                sync()
                            })
                        } else if (statusCode == 401) {
                            runOnUiThread(Runnable {
                                item.status = true
                                val status: Int = vm.update(item)
                                Log.i("response update status ", "$status")
                                sync()
                            })
                        } else {
                            showCustomAlert(this@DashBoardActivity.resources.getString(R.string.api_server_alert_message), CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401, listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok)))
                        }
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                }

                override fun onFailure(call: Call<videoUploadSaveRespose?>, t: Throwable) {
                    showCustomAlert(this@DashBoardActivity.resources.getString(R.string.api_failure_alert_title),CommonUtils.WEB_SERVICE_CALL_FAILED,
                        listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok)))
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            })
        } else {
            runOnUiThread(Runnable {
                showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                    CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                )
            })

        }
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(this@DashBoardActivity.resources.getString(R.string.alert_ok), true)) {
            if (functionality.equals(CommonUtils.NO_OPERATOR_FUNCTIONALITY_IMPLEMENTED_DIALOG, true)) {
                //No action required.
            }
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                super.onBackPressed()
            }
            if (functionality.equals(CommonUtils.LOGOUT_DIALOG, true)) {
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.clear()
                editor.apply()
                navigateToLogin()
            }
            if (functionality.equals(CommonUtils.VIDEO_SYNC_DIALOG, true)) {
                //No action required on sync
            }
            if (functionality.equals(CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401, true)) {
                //No action required on sync
            }
            if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required on internet connection error
            }
            if(functionality.equals(CommonUtils.WEB_SERVICE_CALL_FAILED,true)){
                //No action required
            }
        }
        if (buttonName.equals(this@DashBoardActivity.resources.getString(R.string.alert_cancel), true)) {
            //No action required. Just exit dialog.
        }
    }
}