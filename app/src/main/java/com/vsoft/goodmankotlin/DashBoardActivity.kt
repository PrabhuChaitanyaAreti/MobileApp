package com.vsoft.goodmankotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProviders
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.database.subscribeOnBackground
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.DieIdDetailsModel
import com.vsoft.goodmankotlin.model.VideoUploadSaveResponse
import com.vsoft.goodmankotlin.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DashBoardActivity : AppCompatActivity(), View.OnClickListener, CustomDialogCallback {
    private lateinit var addOperator: LinearLayout
    private lateinit var addDie: LinearLayout
    private lateinit var sync: LinearLayout
    private lateinit var skip: LinearLayout
    private lateinit var logout: LinearLayout
    private lateinit var syncDie:LinearLayout
    private lateinit var progressDialog: ProgressDialog
    private lateinit var vm: VideoViewModel
    private var sharedPreferences: SharedPreferences? = null
    private var isSyncing=false
    private var isDieDataAvailable = false
    private var dieData = ""
    private var dieDataSyncTime = ""
    private lateinit var versionDetails:TextView
    private var totalVideoCount:Int = 0
    private var currentIndex:Int=0
    // The TransferUtility is the primary class for managing transfer to S3
    var transferUtility: TransferUtility? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        initProgress()
        init()
        initAws()
    }

    private fun initAws() {
        // Initializes TransferUtility, always do this before using it.
        val util = AwsUtil()
        transferUtility = util.getTransferUtility(this)
    }

    private fun init() {

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        addOperator = findViewById(R.id.addOperator)
        addDie = findViewById(R.id.addDie)
        sync = findViewById(R.id.sync)
        skip = findViewById(R.id.skip)
        logout = findViewById(R.id.logout)
        syncDie=findViewById(R.id.syncDie)

        addOperator.setOnClickListener(this)
        addDie.setOnClickListener(this)
        sync.setOnClickListener(this)
        skip.setOnClickListener(this)
        logout.setOnClickListener(this)
        syncDie.setOnClickListener(this)
        versionDetails=findViewById(R.id.versionDetails)
        versionDetails.text = HtmlCompat.fromHtml("<B>Version:</B>"+BuildConfig.VERSION_CODE+"("+BuildConfig.VERSION_NAME+")", HtmlCompat.FROM_HTML_MODE_LEGACY)

       val str="select * from video_table where status="+"'"+false+"'"
        Log.d("TAG", "strstrstrstr: $str")

            vm = ViewModelProviders.of(this)[VideoViewModel::class.java]

        var videosList: List<VideoModel>?
        subscribeOnBackground {
            videosList = vm.getVideos()
                Log.d("TAG", "DashBoardActivity  videosList!!.size ${videosList!!.size}")
            runOnUiThread({
                totalVideoCount=videosList!!.size
//                Toast.makeText(
//                    applicationContext,
//                    "llVideosList size " + videosList!!.size,
//                    Toast.LENGTH_LONG
//                ).show()
            })
//           if(videosList!!.size>0) {
//                val iterator = videosList!!.listIterator()
//                if (iterator.hasNext()) {
//                    val item = iterator.next()
//                    CommonUtils.deletePath(item.video_path)
//                }
//            }
        }

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )

        isDieDataAvailable = sharedPreferences!!.getBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
        dieData = sharedPreferences!!.getString(CommonUtils.DIE_DATA, "").toString()
        dieDataSyncTime = sharedPreferences!!.getString(CommonUtils.DIE_DATA_SYNC_TIME, "").toString()

        Log.d("TAG", "DashBoardActivity  sharedPreferences  isDieDataAvailable $isDieDataAvailable")
        Log.d("TAG", "DashBoardActivity  sharedPreferences  dieData $dieData")
        Log.d("TAG", "DashBoardActivity  sharedPreferences  dieDataSyncTime $dieDataSyncTime")

        if(!isDieDataAvailable){
            getDieAndPartData()
        }else{
            try {
                val dateFormat = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss"
                )

                val oldDate: Date = dateFormat.parse(dieDataSyncTime)

                val currentDate = Date()

                Log.d("TAG", "DashBoardActivity currentDate $currentDate")

                val diff = currentDate.time - oldDate.time
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                Log.e("DateAndTime ", "days:::: $days")
                Log.e("DateAndTime ", "hours::::  $hours")
                Log.e("DateAndTime ", "minutes:::: $minutes")
                Log.e("DateAndTime ", "seconds:::: $seconds")

                if(days>=CommonUtils.DIE_DATA_SYNC_DAYS){
                    getDieAndPartData()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    private fun initProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
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
        if(v?.id==syncDie.id){
            getDieAndPartData()
        }
        if (v?.id == sync.id) {
            sync()
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
            }else{
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
                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok), this@DashBoardActivity.resources.getString(R.string.alert_cancel))
            )
        }
    }

    private fun showCustomAlert(
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
                if(!isSyncing) {
                    runOnUiThread({
                        showCustomAlert(
                            this@DashBoardActivity.resources.getString(R.string.no_videos_available),
                            CommonUtils.VIDEO_SYNC_DIALOG,
                            listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                        )
                    })
                }else{
                    isSyncing=false
                    currentIndex=0
                    runOnUiThread({
                        showCustomAlert(
                            this@DashBoardActivity.resources.getString(R.string.sync_videos_alert_message_success), CommonUtils.VIDEO_SYNC_DIALOG,
                            listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok))
                        )
                    })
                }
            } else {
                isSyncing=true
                Log.i("Videos observed size", "${videosList?.size}")
                runOnUiThread({
                    progressDialog.setMessage("Syncing ...${++currentIndex}/$totalVideoCount")
//                    Toast.makeText(
//                        applicationContext,
//                        "syncvideos list size " + videosList!!.size,
//                        Toast.LENGTH_LONG
//                    ).show()
                })
                val iterator = videosList!!.listIterator()
                if (iterator.hasNext()) {
                    val item = iterator.next()
                    if (!item.status) {
                        save(this, item)
                    }
                } else {
                    runOnUiThread({
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
        val path = item.video_path
        val filename: String = path.substring(path.lastIndexOf("/") + 1)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_FILE_NAME, filename)

        val jsonString = gson.toJson(jsonObject)

        Log.i("save jsonString ", jsonString)
        Log.i("save path ", path)
        val fileSize: File = File(path)
        val file_size = (fileSize.length() / 1024)/1024
        Log.i("video file_size  ", ""+file_size)
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
        beginUpload(file,item)
       // saveVideoToServer(item, metaDataFilePart, videoFilePart)
    }

    private fun saveVideoToServer(
        item: VideoModel,
        metaData: MultipartBody.Part,
        videoFile: MultipartBody.Part) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog.show()
            }
            val call: Call<VideoUploadSaveResponse?>? =
                RetrofitClient.getInstance()!!.getMyApi()!!.saveVideo(metaData, videoFile)
            call!!.enqueue(object : Callback<VideoUploadSaveResponse?> {
                override fun onResponse(
                    call: Call<VideoUploadSaveResponse?>,
                    response: Response<VideoUploadSaveResponse?>
                ) {
                    try {
                        Log.i("response  ", "$response")
                        val statusCode = response.body()!!.statusCode
                        if (statusCode == 200) {
                            runOnUiThread({
                               // Toast.makeText(applicationContext, "video upload success 200", Toast.LENGTH_LONG).show()
                                item.status = true
                                val status: Int = vm.update(item)
                                Log.i("response update status ", "$status")
//                                CommonUtils.deletePath(item.video_path)
//                                vm.delete(item)
                                sync()
                            })
                        } else if (statusCode == 401) {
                            runOnUiThread({
                                //Toast.makeText(applicationContext, "video upload exists 200", Toast.LENGTH_LONG).show()
                                item.status = true
                                val status: Int = vm.update(item)
                                Log.i("response update status ", "$status")
                                sync()
                            })
                        } else {
                            --currentIndex
                            runOnUiThread({
                            //Toast.makeText(applicationContext, "video upload fail", Toast.LENGTH_LONG).show()
                            showCustomAlert(this@DashBoardActivity.resources.getString(R.string.api_server_alert_message), CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401, listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok)))
                            })
                        }
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    } catch (e: Exception) {
                        --currentIndex
                        e.printStackTrace()
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                }

                override fun onFailure(call: Call<VideoUploadSaveResponse?>, t: Throwable) {
                    Log.i("onFailure  ", "${t.printStackTrace()}")
                    --currentIndex
                    runOnUiThread({
//                        Toast.makeText(applicationContext, "video upload failure", Toast.LENGTH_LONG).show()
                        if(t.localizedMessage.equals("timeout",true)){
                            //sync()
                        }else{
                            showCustomAlert(t.localizedMessage,CommonUtils.WEB_SERVICE_CALL_FAILED,
                                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok)))
                        }
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    })

                }



            })
        } else {
            runOnUiThread({
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
            if(functionality.equals(CommonUtils.NO_DIE_DATA_DIALOG,true)){
                //No action required
            }
        }
        if (buttonName.equals(this@DashBoardActivity.resources.getString(R.string.alert_cancel), true)) {
            //No action required. Just exit dialog.
        }
    }
    private fun getDieAndPartData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            progressDialog.setMessage(this@DashBoardActivity.resources.getString(R.string.progress_dialog_message_dies_parts))
            progressDialog.show()
            val call = RetrofitClient().getMyApi()!!.doGetListDieDetails()
            call!!.enqueue(object : Callback<DieIdDetailsModel?> {
                override fun onResponse(
                    call: Call<DieIdDetailsModel?>,
                    response: Response<DieIdDetailsModel?>
                ) {
                    val resourceData = response.body()
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    isDieDataAvailable=true

                    val gson = Gson()
                    val dieIdDetailsModelStr = gson.toJson(resourceData)
                    val timeStamp =
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, true)
                    editor.putString(CommonUtils.DIE_DATA, dieIdDetailsModelStr)
                    editor.putString(CommonUtils.DIE_DATA_SYNC_TIME, timeStamp)
                    editor.apply()
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
                this@DashBoardActivity.resources.getString(R.string.network_alert_message),CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                listOf(this@DashBoardActivity.resources.getString(R.string.alert_ok)))
        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private fun beginUpload(file: File,item: VideoModel) {
        val observer: TransferObserver =
            transferUtility!!.upload(
                file.name,
                file
            )

        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        observer.setTransferListener(UploadListener(item))
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    internal class UploadListener(item1: VideoModel) : TransferListener {
        // TAG for logging;
        val TAG = "UploadActivity"
        val item=item1
        // Simply updates the UI list when notified.
        override fun onError(id: Int, e: java.lang.Exception) {
            Log.e(
                TAG,
                "Error during upload: $id", e
            )
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            Log.d(
                TAG, String.format(
                    "onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent
                )
            )
        }

        override fun onStateChanged(id: Int, newState: TransferState) {
            Log.d(
                TAG,
                "onStateChanged: $id, $newState"
            )
            if(newState.equals("COMPLETED")){
                //runOnUiThread({
                    //Toast.makeText(applicationContext, "video upload exists 200", Toast.LENGTH_LONG).show()

                    //item.status = true
                    //val status: Int = vm.update(item)
                   // Log.i("response update status ", "$status")
                 //   sync()
                //})
            }
        }
    }
}