package com.vsoft.goodmankotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.model.UserAuthRequest
import com.vsoft.goodmankotlin.model.UserAuthResponse
import com.vsoft.goodmankotlin.model.videoUploadSaveRespose
import com.vsoft.goodmankotlin.utils.DialogUtils
import com.vsoft.goodmankotlin.utils.NetworkUtils
import com.vsoft.goodmankotlin.utils.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashBoardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var addOperator: TextView
    private lateinit var addDie: TextView
    private lateinit var sync: TextView
    private lateinit var skip: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var vm: VideoViewModel

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

        addOperator.setOnClickListener(this)
        addDie.setOnClickListener(this)
        sync.setOnClickListener(this)
        skip.setOnClickListener(this)

        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]
    }
    private fun initProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Please wait .. Saving video will take time..")
    }
    override fun onClick(v: View?) {
        if(v?.id==addOperator.id){

        }
        if(v?.id==addDie.id){
            val mainIntent = Intent(this@DashBoardActivity, AddDieActivity::class.java)
            startActivity(mainIntent)
            finish()
        }
        if(v?.id==sync.id){

          /*  var jsonObject= JsonObject()
            val gson = Gson()
            jsonObject.addProperty("Die Id","Die Id")
            jsonObject.addProperty("Part Id","Part Id")
            jsonObject.addProperty("file_name","check1.mp4")
            save(this,gson.toJson(jsonObject))*/


            //vm.getAllVideos().observe(this, Observer {
               var videosList:List<VideoModel>? =vm.getVideos()
            Log.i("Videos observed size", "${videosList?.size}")
            val iterator = videosList!!.listIterator()
                for (item in iterator) {
                    Log.i("Id:", "${item.id}")
                    Log.i("Status:", "${item.status}")
                    val jsonObject= JsonObject()
                    val gson = Gson()
                    jsonObject.addProperty("Die Id",item.die_id)
                    jsonObject.addProperty("Part Id",item.part_id)
                    val path=item.video_path;
                    val filename: String = path.substring(path.lastIndexOf("/") + 1)
                    jsonObject.addProperty("file_name",filename)
                    save(this,item.id,gson.toJson(jsonObject),path)
                }
            //})
        }
        if(v?.id==skip.id){
            navigateToOperatorSelection()
        }
    }
    private fun navigateToOperatorSelection() {
        val mainIntent = Intent(this, OperatorSelectActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    @Throws(IOException::class)
    fun save(context: Context,id:Int?,jsonString: String?,path:String?) {
        Log.i("save jsonString ", "$jsonString")
        Log.i("save path ", "$path")
        val rootFolder: File? = context.getExternalFilesDir(null)
        val jsonFile = File(rootFolder, "post.json")
        val writer = FileWriter(jsonFile)
        writer.write(jsonString)
        writer.close()
//
//        val mediaStorageDir: File
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            mediaStorageDir = File(
//                Environment
//                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + "/Goodman/Videos"
//            )
//        } else {
//            mediaStorageDir = File(
//                Environment
//                    .getExternalStorageDirectory().path + "/Goodman/Videos"
//            )
//        }
//        val jsonFile: File = File(mediaStorageDir, "post.json")
//        val writer = FileWriter(jsonFile)
//        writer.append(jsonString)
//        writer.flush()
//        writer.close()

        val metaDataFilePart = MultipartBody.Part.createFormData(
            "meta_data",
            jsonFile.name,
            RequestBody.create(MediaType.parse("*/*"), jsonFile)
        )

        val file = File(path) // initialize file here
        val videoFilePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            RequestBody.create(MediaType.parse("image/*"), file)
        )
        saveVideoToServer(id,metaDataFilePart,videoFilePart)
    }
    private fun saveVideoToServer(id:Int?,metaData:MultipartBody.Part,videoFile:MultipartBody.Part){
        if (NetworkUtils.isNetworkAvailable(this)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog!!.show()
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
                               val status:Int= vm.updateSyncStatus(id)
                            })
                            DialogUtils.showNormalAlert(
                                this@DashBoardActivity,
                                "Alert!!",
                                "Data saved successfully"
                            )
                        }else if(statusCode==401){
                            DialogUtils.showNormalAlert(
                                this@DashBoardActivity,
                                "Alert!!",
                                "File Exists"
                            )
                        }else{
                            DialogUtils.showNormalAlert(
                                this@DashBoardActivity,
                                "Alert!!",
                                "File Exists"
                            )
                        }
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                    }
                }
                override fun onFailure(call: Call<videoUploadSaveRespose?>, t: Throwable) {
                    DialogUtils.showNormalAlert(
                        this@DashBoardActivity,
                        "Alert!!",
                        "Unable to communicate with server"
                    )
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                }
            })
        } else {
            DialogUtils.showNormalAlert(
                this,
                "Alert!!",
                "Please check your internet connection and try again"
            )
        }
    }
}