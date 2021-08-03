package com.vsoft.goodmankotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DashBoardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var addOperator: TextView
    private lateinit var addDie: TextView
    private lateinit var sync: TextView
    private lateinit var skip: TextView
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
    }

    override fun onClick(v: View?) {
        if(v?.id==addOperator.id){

        }
        if(v?.id==addDie.id){

        }
        if(v?.id==sync.id){
            var jsonObject= JsonObject()
            val gson = Gson()
            jsonObject.addProperty("Die Id","Die Id")
            jsonObject.addProperty("Part Id","Part Id")
            jsonObject.addProperty("file_name","check1.mp4")
            save(this,gson.toJson(jsonObject))
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
        val rootFolder: File? = context.getExternalFilesDir(null)
        val jsonFile = File(rootFolder, "details.json")
//        val writer = FileWriter(jsonFile)
//        writer.write(jsonString)
//        writer.close()
        //or IOUtils.closeQuietly(writer);
    }
}