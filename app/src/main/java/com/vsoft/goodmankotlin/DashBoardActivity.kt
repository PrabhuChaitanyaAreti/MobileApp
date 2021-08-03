package com.vsoft.goodmankotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
            vm.getAllVideos().observe(this, Observer {
                Log.i("Videos observed size", "${it.size}")
                var videosList:List<VideoModel> =it
                val iterator = videosList.listIterator()
                for (item in iterator) {
                    val jsonObject= JsonObject()
                    val gson = Gson()
                    jsonObject.addProperty("Die Id",item.die_id)
                    jsonObject.addProperty("Part Id",item.part_id)
                    val path=item.video_path;
                    val filename: String = path.substring(path.lastIndexOf("/") + 1)
                    jsonObject.addProperty("file_name",filename)
                    save(this,gson.toJson(jsonObject),path)
                }
            })
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
    fun save(context: Context, jsonString: String?,path:String?) {
        val rootFolder: File? = context.getExternalFilesDir(null)
        val jsonFile = File(rootFolder, "details.json")
        val writer = FileWriter(jsonFile)
        writer.write(jsonString)
        writer.close()
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
        saveVideoToServer(metaDataFilePart,videoFilePart)
    }
    private fun saveVideoToServer(metaData:MultipartBody.Part,videoFile:MultipartBody.Part){
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
                        val statusCode=response.body()!!.statusCode
                        if(statusCode==200){
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