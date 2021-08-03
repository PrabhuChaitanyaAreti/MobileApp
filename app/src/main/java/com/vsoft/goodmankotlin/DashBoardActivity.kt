package com.vsoft.goodmankotlin

import android.R.attr
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import android.R.attr.path




class DashBoardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var addOperator: TextView
    private lateinit var addDie: TextView
    private lateinit var sync: TextView
    private lateinit var skip: TextView

    private lateinit var vm: VideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        init()
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
                    save(this,gson.toJson(jsonObject))
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
    fun save(context: Context, jsonString: String?) {
        Log.i("save jsonString::", "$jsonString")
        val rootFolder: File? = context.getExternalFilesDir(null)
        val jsonFile = File(rootFolder, "details.json")
//        val writer = FileWriter(jsonFile)
//        writer.write(jsonString)
//        writer.close()
        //or IOUtils.closeQuietly(writer);
    }
}