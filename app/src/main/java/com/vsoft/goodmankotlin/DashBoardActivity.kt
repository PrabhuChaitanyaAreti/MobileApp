package com.vsoft.goodmankotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.database.subscribeOnBackground
import com.vsoft.goodmankotlin.model.videoUploadSaveRespose
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import com.vsoft.goodmankotlin.utils.DialogUtils.Companion.showNormalAlert
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

class DashBoardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var addOperator: LinearLayout
    private lateinit var addDie: LinearLayout
    private lateinit var sync: LinearLayout
    private lateinit var skip: LinearLayout
    private lateinit var logout: LinearLayout
    private lateinit var progressDialog: ProgressDialog
    private lateinit var vm: VideoViewModel
    private var alertDialog: android.app.AlertDialog? = null
    var sharedPreferences: SharedPreferences?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        init()
        initProgress()
    }
    private fun init(){
        addOperator=findViewById(R.id.addOperator)
        addDie=findViewById(R.id.addDie)
        sync=findViewById(R.id.sync)
        skip=findViewById(R.id.skip)
        logout=findViewById(R.id.logout)
        addOperator.setOnClickListener(this)
        addDie.setOnClickListener(this)
        sync.setOnClickListener(this)
        skip.setOnClickListener(this)
        logout.setOnClickListener(this)
        sharedPreferences = this.getSharedPreferences(CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE)
        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]
    }
    private fun initProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(this@DashBoardActivity.resources.getString(R.string.progress_dialog_message_sync))
    }
    override fun onClick(v: View?) {
        if(v?.id==addOperator.id){
            DialogUtils.showNormalAlert(
                this@DashBoardActivity,
                this@DashBoardActivity.resources.getString(R.string.alert_title),
                this@DashBoardActivity.resources.getString(R.string.add_operator_alert_message)
            )
        }

        if(v?.id==addDie.id){
            val mainIntent = Intent(this@DashBoardActivity, AddDieActivityNew::class.java)
            startActivity(mainIntent)
        }
        if(v?.id==sync.id){
                sync()
        }
        if(v?.id==skip.id){
            navigateToOperatorSelection()
        }
        if(v?.id==logout.id){
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle(
                this.resources.getString(R.string.app_name)
            )

            builder.setCancelable(false)
            builder.setMessage(this@DashBoardActivity.resources.getString(R.string.logout_alert_message))
            builder.setPositiveButton(
                this@DashBoardActivity.resources.getString(R.string.alert_ok)
            ) { dialog, which ->
                val editor: SharedPreferences.Editor =  sharedPreferences!!.edit()
                editor.clear()
                editor.apply()
                navigateToLogin()
                dialog.dismiss()
                if (alertDialog!!.isShowing) {
                    alertDialog!!.dismiss()
                }
            }
            builder.setNegativeButton(
                this@DashBoardActivity.resources.getString(R.string.alert_cancel)
            ) { dialog, which ->
                dialog.dismiss()
            }
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }
    override fun onBackPressed() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(
            this.resources.getString(R.string.app_name)
        )

        builder.setCancelable(false)
        builder.setMessage(this@DashBoardActivity.resources.getString(R.string.exit_app_alert_message))
        builder.setPositiveButton(
            this@DashBoardActivity.resources.getString(R.string.alert_ok)
        ) { dialog, which ->
            dialog.dismiss()
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
            super.onBackPressed()
        }
        builder.setNegativeButton(
            this@DashBoardActivity.resources.getString(R.string.alert_cancel)
        ) { dialog, which ->
            dialog.dismiss()
        }
        alertDialog = builder.create()
        alertDialog?.show()
    }
    private fun navigateToOperatorSelection() {
        val mainIntent = Intent(this, OperatorSelectActivityWithWebservice::class.java)
        startActivity(mainIntent)
    }
    private fun navigateToLogin() {
        val mainIntent = Intent(this, LoginActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
private fun sync(){
    var  videosList: List<VideoModel>? =null
//    vm.getAllVideos().observe(this, Observer {
//        videosList=it
    subscribeOnBackground {
        videosList=vm.getVideos()
        Log.i("Videos observed size", "${videosList?.size}")
        val iterator = videosList!!.listIterator()
       if(iterator.hasNext()){
          val item=iterator.next()
           if(!item.status){
               save(this, item)
           }
       }else{
           runOnUiThread(Runnable {
               DialogUtils.showNormalAlert(
                   this@DashBoardActivity,
                   this@DashBoardActivity.resources.getString(R.string.alert_title),
                   this@DashBoardActivity.resources.getString(R.string.sync_videos_alert_message),
               )
           })
       }
    }

   // })
}
    @Throws(IOException::class)
    private fun save(context: Context,item:VideoModel) {
        Log.i("Id:", "${item.id}")
        Log.i("Status:", "${item.status}")
        Log.i("video die type :", item.die_top_bottom)
        val jsonObject= JsonObject()
        val gson = Gson()
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_DIE_ID,item.die_id)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_PART_ID,item.part_id)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_DIE_TOP_BOTTOM,item.die_top_bottom)
        val path=item.video_path;
        val filename: String = path.substring(path.lastIndexOf("/") + 1)
        jsonObject.addProperty(CommonUtils.SYNC_VIDEO_API_FILE_NAME,filename)

        val jsonString=  gson.toJson(jsonObject)

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
        saveVideoToServer(item,metaDataFilePart,videoFilePart)
    }
    private fun saveVideoToServer(item:VideoModel,metaData:MultipartBody.Part,videoFile:MultipartBody.Part){
        if (NetworkUtils.isNetworkAvailable(this)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog.show()
            }
            val call: Call<videoUploadSaveRespose?>? =
                RetrofitClient.getInstance()!!.getMyApi1()!!.saveVideo(metaData,videoFile)
            call!!.enqueue(object : Callback<videoUploadSaveRespose?> {
                override fun onResponse(
                    call: Call<videoUploadSaveRespose?>,
                    response: Response<videoUploadSaveRespose?>
                ) {
                    try {
                        Log.i("response  ", "$response")
                        val statusCode=response.body()!!.statusCode
                        if(statusCode==200){
                            runOnUiThread(Runnable {
                                item.status=true
                                val status: Int = vm.update(item)
                                Log.i("response update status ", "$status")
                                sync()
                            })
//                            DialogUtils.showNormalAlert(
//                                this@DashBoardActivity,
//                                "Alert!!",
//                                "Data saved successfully"
//                            )
                        }else if(statusCode==401){
                            runOnUiThread(Runnable {
                                item.status=true
                                val status: Int = vm.update(item)
                                Log.i("response update status ", "$status")
                                sync()
                            })
//                            DialogUtils.showNormalAlert(
//                                this@DashBoardActivity,
//                                "Alert!!",
//                                "File Exists"
//                            )
                        }else{
                            DialogUtils.showNormalAlert(
                                this@DashBoardActivity,
                                this@DashBoardActivity.resources.getString(R.string.alert_title),
                                this@DashBoardActivity.resources.getString(R.string.api_server_alert_message)
                            )
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
                    DialogUtils.showNormalAlert(
                        this@DashBoardActivity,
                        this@DashBoardActivity.resources.getString(R.string.alert_title),
                        this@DashBoardActivity.resources.getString(R.string.api_failure_alert_title)
                    )
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            })
        } else {
            runOnUiThread(Runnable {
                DialogUtils.showNormalAlert(
                    this,
                    this@DashBoardActivity.resources.getString(R.string.alert_title),
                    this@DashBoardActivity.resources.getString(R.string.network_alert_message)
                )
            })

        }
    }
}