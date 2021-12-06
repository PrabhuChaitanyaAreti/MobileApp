@file:Suppress("ControlFlowWithEmptyBody")

package com.vsoft.goodmankotlin

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProviders
import com.microsoft.appcenter.analytics.Analytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vsoft.goodmankotlin.cumulocity.MqttService
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.database.subscribeOnBackground
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.DieIdDetailsModel
import com.vsoft.goodmankotlin.model.VideoUploadSaveResponse
import com.vsoft.goodmankotlin.model.OperatorList
import com.vsoft.goodmankotlin.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class DashBoardActivity : AppCompatActivity(), View.OnClickListener, CustomDialogCallback {
    private lateinit var addDie: LinearLayout
    private lateinit var sync: RelativeLayout
    private lateinit var skip: LinearLayout
    private lateinit var download_latest_version: LinearLayout
    private lateinit var logout: LinearLayout
    private lateinit var syncDie: LinearLayout
    private lateinit var syncVideosCount: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var vm: VideoViewModel
    private var sharedPreferences: SharedPreferences? = null
    private var isSyncing = false
    private var isDieDataAvailable = false
    private var dieData = ""
    private var dieDataSyncTime = ""
    private lateinit var versionDetails: TextView
    private var totalVideoCount: Int = 0
    private var currentIndex: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        initProgress()
        init()

        // Start the MQTT Service
       // MqttService.activityContext=this;
        //val i = Intent(this@DashBoardActivity, MqttService::class.java)
        //startService(i)
    }

    @SuppressLint("SimpleDateFormat")
    private fun init() {

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        addDie = findViewById(R.id.addDie)
        sync = findViewById(R.id.sync)
        skip = findViewById(R.id.skip)
        logout = findViewById(R.id.logout)
        download_latest_version = findViewById(R.id.download_latest_version)
        syncDie=findViewById(R.id.syncDie)
        syncVideosCount=findViewById(R.id.syncVideosCount)
        syncVideosCount.visibility=View.GONE
        syncDie = findViewById(R.id.syncDie)
        syncVideosCount = findViewById(R.id.syncVideosCount)
        syncVideosCount.visibility = View.GONE


        addDie.setOnClickListener(this)
        sync.setOnClickListener(this)
        skip.setOnClickListener(this)
        logout.setOnClickListener(this)
        download_latest_version.setOnClickListener(this)
        syncDie.setOnClickListener(this)
        versionDetails=findViewById(R.id.versionDetails)
        versionDetails.text = HtmlCompat.fromHtml("<B>Version:</B>"+BuildConfig.VERSION_CODE+"("+ BuildConfig.VERSION_NAME+")", HtmlCompat.FROM_HTML_MODE_LEGACY)


//         isDownload = (MqttService.getDownloader() as Nothing?).toString();


/*
        var isDownload = MqttService.getDownloader()
        if(isDownload.contains("fail")){
            download_latest_version.visibility = View.GONE
        }else{
            download_latest_version.visibility = View.VISIBLE
        }
*/

       val str="select * from video_table where status="+"'"+false+"'"
        Log.d("TAG", "strstrstrstr: $str")
        versionDetails = findViewById(R.id.versionDetails)
        versionDetails.text = HtmlCompat.fromHtml(
            "<B>Version:</B>" + BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]

        var videosList: List<VideoModel>?
        subscribeOnBackground {
            removeEmptyVideos()
            removeSyncVideos()
            videosList = vm.getVideos()
            Log.i("DashBoardActivity videosList.size::: ", videosList!!.size.toString())
            runOnUiThread {
                totalVideoCount = videosList!!.size
                syncVideoCountDisplay(videosList!!.size)
            }
        }

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )

        isDieDataAvailable =
            sharedPreferences!!.getBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
        dieData = sharedPreferences!!.getString(CommonUtils.DIE_DATA, "").toString()
        dieDataSyncTime =
            sharedPreferences!!.getString(CommonUtils.DIE_DATA_SYNC_TIME, "").toString()

        if (!isDieDataAvailable) {
            getDieAndPartData()
        } else {
            try {
                val dateFormat = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss"
                )

                val oldDate: Date = dateFormat.parse(dieDataSyncTime)

                val currentDate = Date()

                val diff = currentDate.time - oldDate.time
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                /*
                  If days are more than 2, operators, die and part id api's are calling
                 */
                if (days >= CommonUtils.DIE_DATA_SYNC_DAYS) {
                    getDieAndPartData()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onResume() {
        super.onResume()
  /*     var isDownload = MqttService.getDownloader()
        if(isDownload.contains("fail")){
            download_latest_version.visibility = View.GONE
        }else{
            download_latest_version.visibility = View.VISIBLE
        }*/
    }
    private fun removeSyncVideos() {
        try {
            val videosList = vm.getSyncedVideos()
            if (videosList.isNotEmpty()) {
                videosList.forEach {
                    CommonUtils.deletePath(it.video_path)
                    vm.delete(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeEmptyVideos() {
        try {
            val videosList = vm.getVideos()
            videosList.forEach {
                val fileSize = File(it.video_path)
                if (fileSize.length() <= 0) {
                    val fileSizeKB = fileSize.length() / 1024
                    val fileSizeMB = (fileSize.length() / 1024) / 1024
                    it.status = true
                    val status: Int = vm.update(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
    }

    override fun onClick(v: View?) {

        if (v?.id == addDie.id) {
            if (CommonUtils.checkMemory()) {
                if (isDieDataAvailable) {
                    navigateToAddDie()
                } else {
                    showCustomAlert(
                        this@DashBoardActivity.resources.getString(R.string.no_die_id_data),
                        CommonUtils.NO_DIE_DATA_DIALOG,
                        listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                    )
                }
            } else {
                showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.memory_message),
                    CommonUtils.MEMORY_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                )
            }
        }
        if (v?.id == syncDie.id) {
            getDieAndPartData()
        }
        if (v?.id == sync.id) {
            if (NetworkUtils.isNetworkAvailable(this)) {
                sync()
            } else {
                showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                    CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                )
            }
        }
        if (v?.id == skip.id) {
            if (CommonUtils.checkMemory()) {
                if (isDieDataAvailable) {
                    navigateToOperatorSelection()
                } else {
                    showCustomAlert(
                        this@DashBoardActivity.resources.getString(R.string.no_die_id_data),
                        CommonUtils.NO_DIE_DATA_DIALOG,
                        listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                    )
                }
            } else {
                showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.memory_message),
                    CommonUtils.MEMORY_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                )
            }
        }
        if (v?.id == logout.id) {
            showCustomAlert(
                this@DashBoardActivity.resources.getString(R.string.logout_alert_message),
                CommonUtils.LOGOUT_DIALOG,
                listOf(
                    this@DashBoardActivity.resources.getString(R.string.alert_yes),
                    this@DashBoardActivity.resources.getString(R.string.alert_no)
                )
            )
        }

        if (v?.id == download_latest_version.id) {



           // MqttService.showInstallAPK()
        }
    }

     fun showCustomAlert(
        alertMessage: String,
        functionality: String,
        buttonList: List<String>
    ) {
        val customDialogModel = CustomDialogModel(
            getString(R.string.app_name), alertMessage, null,
            buttonList
        )
        DialogUtils.showCustomAlert(this, customDialogModel, this, functionality)
    }

    override fun onBackPressed() {
        showCustomAlert(
            this@DashBoardActivity.resources.getString(R.string.exit_app_alert_message),
            CommonUtils.BACK_PRESSED_DIALOG,
            listOf(
                this@DashBoardActivity.resources.getString(R.string.alert_yes),
                this@DashBoardActivity.resources.getString(R.string.alert_no)
            )
        )
    }

    private fun navigateToOperatorSelection() {
        val mainIntent = Intent(this, OperatorSelectActivity::class.java)
        mainIntent.putExtra(CommonUtils.IS_NEW_DIE, false)
        startActivity(mainIntent)
    }

    private fun navigateToLogin() {
        val mainIntent = Intent(this, LoginActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun navigateToAddDie() {
        val mainIntent = Intent(this, OperatorSelectActivity::class.java)
        mainIntent.putExtra(CommonUtils.IS_NEW_DIE, true)
        startActivity(mainIntent)
    }

    private fun sync() {
        try {
            val batterLevel: Int = BatteryUtil.getBatteryPercentage(this)
            if (batterLevel >= CommonUtils.BATTERY_LEVEL_PERCENTAGE) {
                var videosList: List<VideoModel>? = null
                subscribeOnBackground {
                    videosList = vm.getVideos()
                    syncVideoCountDisplay(videosList!!.size)
                    if (videosList!!.isEmpty()) {
                        if (!isSyncing) {
                            runOnUiThread {
                                showCustomAlert(
                                    this@DashBoardActivity.resources.getString(R.string.no_videos_available),
                                    CommonUtils.VIDEO_SYNC_DIALOG,
                                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                                )
                            }
                        } else {
                            isSyncing = false
                            currentIndex = 0
                            runOnUiThread {
                                showCustomAlert(
                                    this@DashBoardActivity.resources.getString(R.string.sync_videos_alert_message_success),
                                    CommonUtils.VIDEO_SYNC_DIALOG,
                                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                                )
                            }
                        }
                    } else {
                        isSyncing = true
                        currentIndex = totalVideoCount - videosList!!.size
                        runOnUiThread {
                            progressDialog.setMessage("Syncing ...${++currentIndex}/$totalVideoCount")
                        }
                        val iterator = videosList!!.listIterator()
                        if (iterator.hasNext()) {
                            val item = iterator.next()
                            if (!item.status) {
                                save(this, item)
                            }
                        } else {
                            runOnUiThread {
                                showCustomAlert(
                                    this@DashBoardActivity.resources.getString(R.string.sync_videos_alert_message_success),
                                    CommonUtils.VIDEO_SYNC_DIALOG,
                                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                                )
                            }
                        }
                    }
                }
            } else {
                showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.battery_alert_message),
                    CommonUtils.BATTERY_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_exit))
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun syncVideoCountDisplay(size: Int) {
        runOnUiThread {
            if (size > 0) {
                syncVideosCount.text = size.toString()
                syncVideosCount.visibility = View.VISIBLE
            } else {
                syncVideosCount.visibility = View.GONE
            }
        }
    }

    fun save(context: Context, item: VideoModel) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            val jsonObject = JsonObject()
            val gson = Gson()
            jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_DIE_ID, item.die_id)
            jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_PART_ID, item.part_id)
            jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_DIE_TOP_BOTTOM, item.die_top_bottom)

            if (item.operator_id == "Unknown") {
                jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_OPERATOR_ID, "Operator 1")
            } else {
                jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_OPERATOR_ID, item.operator_id)
            }

            if (item.user_id == "Unknown") {
                jsonObject.addProperty(
                    CommonUtils.SYNC_VIDEO_API_USER_ID,
                    sharedPreferences!!.getString(CommonUtils.LOGIN_USER_ID, "").toString()
                )
            } else {
                jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_USER_ID, item.user_id)
            }

            val path = item.video_path
            val filename: String = path.substring(path.lastIndexOf("/") + 1)
            jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_FILE_NAME, filename)

            val jsonString = gson.toJson(jsonObject)

            val fileSize = File(path)
            if (fileSize.length() > 0) {
                val fileSizeKB = fileSize.length() / 1024
                val fileSizeMB = (fileSize.length() / 1024) / 1024
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
                    RequestBody.create(MediaType.parse("video/*"), file)
                )
                saveVideoToServer(item, metaDataFilePart, videoFilePart)
            } else {
                item.status = true
                val status: Int = vm.update(item)
                CommonUtils.deletePath(item.video_path)
                vm.delete(item)
                sync()
            }
        } else {
            showCustomAlert(
                this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
            )
        }
    }

    private fun saveVideoToServer(
        item: VideoModel,
        metaData: MultipartBody.Part,
        videoFile: MultipartBody.Part
    ) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog.show()
            }
            val call: Call<VideoUploadSaveResponse?>? =
                RetrofitClient.getInstance(this)!!.getMyApi()!!.saveVideo(metaData, videoFile)
            call!!.enqueue(object : Callback<VideoUploadSaveResponse?> {
                override fun onResponse(
                    call: Call<VideoUploadSaveResponse?>,
                    response: Response<VideoUploadSaveResponse?>
                ) {
                    try {
                        when (response.body()!!.statusCode) {
                            200 -> {
                                runOnUiThread {
                                    CommonUtils.deletePath(item.video_path)
                                    vm.delete(item)
                                    sync()
                                }
                            }
                            401 -> {
                                runOnUiThread {
                                    CommonUtils.deletePath(item.video_path)
                                    vm.delete(item)
                                    sync()
                                }
                            }
                            else -> {
                                --currentIndex
                                runOnUiThread {
                                    showCustomAlert(
                                        this@DashBoardActivity.resources.getString(R.string.api_server_alert_message),
                                        CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401,
                                        listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                                    )
                                }
                            }
                        }
                        if (progressDialog != null) {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                        }
                    } catch (e: Exception) {
                        --currentIndex
                        e.printStackTrace()
                        if (progressDialog != null) {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<VideoUploadSaveResponse?>, t: Throwable) {
                    val str =
                        "" + BuildConfig.VERSION_CODE + "(" + BuildConfig.VERSION_NAME + ")" + "" + t.printStackTrace() + "" + t.toString() + "" + t.localizedMessage
                    Analytics.trackEvent("Sync Video: $str")
                    --currentIndex
                    runOnUiThread {
                        if (t.localizedMessage.equals("timeout", true)) {
                            sync()
                        } else if (t.localizedMessage.contains(
                                "Failed to connect to",
                                true
                            ) || t.localizedMessage.contains("connection", true)
                        ) {
                            showCustomAlert(
                                this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                                CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                            )
                        } else {
                            showCustomAlert(
                                t.localizedMessage, CommonUtils.WEB_SERVICE_CALL_FAILED,
                                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                            )
                        }
                        if (progressDialog != null) {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                        }
                    }

                }


            })
        } else {
            runOnUiThread {
                showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                    CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                )
            }

        }
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(
                this@DashBoardActivity.resources.getString(R.string.alert_yes),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.LOGOUT_DIALOG, true)) {
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.remove(CommonUtils.LOGIN_STATUS)
                //editor.clear()
                editor.apply()
                navigateToLogin()
            } else if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                super.onBackPressed()
            }

        }else if (buttonName.equals(
            this@DashBoardActivity.resources.getString(R.string.alert_no),
            true
        )
                ) {
            //No action required. Just exit dialog.
        } else if (buttonName.equals(
                this@DashBoardActivity.resources.getString(R.string.alert_ok),
                true
            )
        ) {
            if (functionality.equals(
                    CommonUtils.NO_OPERATOR_FUNCTIONALITY_IMPLEMENTED_DIALOG,
                    true
                )
            ) {
                //No action required.
            } else if (functionality.equals(CommonUtils.LOGOUT_DIALOG, true)) {
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.remove(CommonUtils.LOGIN_STATUS)
                //editor.clear()
                editor.apply()
                navigateToLogin()
            } else if (functionality.equals(CommonUtils.VIDEO_SYNC_DIALOG, true)) {
                //No action required on sync
            } else if (functionality.equals(CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401, true)) {
                //No action required on sync
            } else  if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required on internet connection error
            } else if (functionality.equals(CommonUtils.WEB_SERVICE_CALL_FAILED, true)) {
                //No action required
            } else  if (functionality.equals(CommonUtils.NO_DIE_DATA_DIALOG, true)) {
                //No action required
            }
        }

    }

    private fun getDieAndPartData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            progressDialog.setMessage(this@DashBoardActivity.resources.getString(R.string.progress_dialog_message_dies_parts))
            progressDialog.show()
            val call = RetrofitClient!!.getInstance(this)!!.getMyApi()!!.doGetListDieDetails()
            call!!.enqueue(object : Callback<DieIdDetailsModel?> {
                override fun onResponse(
                    call: Call<DieIdDetailsModel?>,
                    response: Response<DieIdDetailsModel?>
                ) {
                    val resourceData = response.body()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    isDieDataAvailable = true

                    val gson = Gson()
                    val dieIdDetailsModelStr = gson.toJson(resourceData)
                    val timeStamp =
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, true)
                    editor.putString(CommonUtils.DIE_DATA, dieIdDetailsModelStr)
                    editor.putString(CommonUtils.DIE_DATA_SYNC_TIME, timeStamp)
                    editor.apply()

                     getOperatorsListData()
                }

                override fun onFailure(call: Call<DieIdDetailsModel?>, t: Throwable) {
                    call.cancel()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
                    editor.apply()

                }
            })
        } else {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
            editor.apply()

            showCustomAlert(
                this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
            )
        }
    }


    private fun getOperatorsListData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            progressDialog.setMessage(this@DashBoardActivity.resources.getString(R.string.progress_dialog_message_operators))
            progressDialog.show()
            val call = RetrofitClient!!.getInstance(this)!!.getMyApi()!!.getOperatorsList()
            call!!.enqueue(object : Callback<OperatorList?> {
                override fun onResponse(
                        call: Call<OperatorList?>,
                        response: Response<OperatorList?>
                ) {
                    val resourceData = response.body()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    isDieDataAvailable = true

                    val gson = Gson()
                    val dieIdDetailsModelStr = gson.toJson(resourceData)
                    val timeStamp =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, true)
                    editor.putString(CommonUtils.OPERATORS_DATA, dieIdDetailsModelStr)
                    editor.putString(CommonUtils.DIE_DATA_SYNC_TIME, timeStamp)
                    editor.apply()
                }

                override fun onFailure(call: Call<OperatorList?>, t: Throwable) {
                    call.cancel()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
                    editor.apply()

                }
            })
        } else {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
            editor.apply()

            showCustomAlert(
                    this@DashBoardActivity.resources.getString(R.string.network_alert_message),
                    CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                    listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
            )
        }
    }

}