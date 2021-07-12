package com.vsoft.goodmankotlin

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vsoft.goodmankotlin.adapter.LeftMenuAdapter
import com.vsoft.goodmankotlin.model.LeftMenuDataModel
import com.vsoft.goodmankotlin.model.PunchResponse
import com.vsoft.goodmankotlin.model.Shapes
import java.io.InputStream


class MaskingActivity : AppCompatActivity() {
    private lateinit var groundTruthImage:ImageView
    private lateinit var leftSideMenu:RecyclerView
    private lateinit var rightSideMenu:RecyclerView
    private lateinit var context: Context
    private lateinit var response: PunchResponse
    private lateinit var bitmap:Bitmap
    private lateinit var correctShapes:ArrayList<Shapes>
    private lateinit var missedShapes:ArrayList<Shapes>
    private lateinit var undetectedShapes:ArrayList<Shapes>
    private lateinit var incorrectShapes:ArrayList<Shapes>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }
    private fun init(){
        groundTruthImage= findViewById(R.id.punchTypeImage)
        leftSideMenu = findViewById(R.id.ivPunchColor)
        rightSideMenu = findViewById(R.id.toggleMenu)
        context=this
        loadLeftSidemenu()
        try {
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            if(sharedPreferences.contains("response")){
                val gson = Gson()
                val json = sharedPreferences.getString("response", "")
                response = gson.fromJson(json, PunchResponse::class.java)
                processData(response)
            }else{
                val response: String? = readJSONFromAsset("response.json")
                val g = Gson()
                val punchResponse = g.fromJson(response, PunchResponse::class.java)
                processData(punchResponse)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processData(punchResponse: PunchResponse) {
        populateImageView(punchResponse.getGt()?.gt_image!!,punchResponse.getGt()?.gt_image_width!!,punchResponse.getGt()?.gt_image_height!!)
        initMaskData(punchResponse)
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun populateImageView(base64Image:String, imageWidth:String, imageHeight:String) {
        val layoutParams = FrameLayout.LayoutParams(imageWidth.toInt(), imageHeight.toInt())
        groundTruthImage.setLayoutParams(layoutParams)
        val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        groundTruthImage.setImageBitmap(bitmap)
    }

    private fun initMaskData(punchResponse: PunchResponse) {
        val shapes=punchResponse.getGt()?.shapes
        val uniqueResults=punchResponse.unique_results
         correctShapes= arrayListOf()
         missedShapes= arrayListOf()
         incorrectShapes= arrayListOf()
         undetectedShapes= arrayListOf()
        if(shapes?.size!!>0){
            if(shapes.size==uniqueResults.size) {
                for (i in shapes.indices) {
                    if(uniqueResults[i].correct==false && (uniqueResults[i].ground_truth.equals("NoPunch",false) && uniqueResults[i].prediction.equals("Punch",false))){
                        //Incorrect Punches
                        incorrectShapes.add(shapes[i])
                    }else if(uniqueResults[i].correct==false && (uniqueResults[i].ground_truth.equals("Punch",false) && uniqueResults[i].prediction.equals("NoPunch",false))){
                        //Missed Punches
                        missedShapes.add(shapes[i])
                    }else if(uniqueResults[i].correct==false && uniqueResults.get(i).prediction.equals("UNDETECTED",false)){
                        //Undetected Punches
                        undetectedShapes.add(shapes[i])
                    }else{
                        //Correct Punches
                        correctShapes.add(shapes[i])
                    }
                }
            }
        }
        drawMask(shapes.toCollection(ArrayList()))
    }

    private fun drawMask(shapesData: ArrayList<Shapes>){
        println("Incorrect Punches:"+incorrectShapes.size)
        println("Missed Punches:"+missedShapes.size)
        println("Undetected Punches:"+undetectedShapes.size)
        if(shapesData.size>0) {
            val canvas = Canvas(bitmap)
            groundTruthImage.setImageBitmap(bitmap)
            for(i in 0..shapesData.size) {
                val path = Path()
                val paintLine = Paint()
                val paintLabel = Paint()
                paintLine.strokeWidth = 8f
                if(incorrectShapes.contains(shapesData[i])){
                    paintLine.color = Color.RED
                }else if(missedShapes.contains(shapesData[i])){
                    paintLine.color = Color.RED
                }else if(undetectedShapes.contains(shapesData[i])){
                    paintLine.color = Color.BLACK
                }else{
                    paintLine.color = Color.GREEN
                }
                paintLine.style = Paint.Style.STROKE
                paintLabel.strokeWidth = 8f
                paintLabel.textSize = 40f
                paintLabel.color = Color.WHITE
                paintLabel.isFakeBoldText = true
                paintLabel.textAlign = Paint.Align.CENTER
                val x= shapesData[i].gt_points?.x
                val y= shapesData[i].gt_points?.y
                if(x?.size==y?.size){
                    for(j in 0..x!!.size-1){
                        if(j==0){
                            path.moveTo(x[0].toFloat(),y!![0].toFloat())
                        }else if(j == x.size - 1){
                            path.lineTo(x[j].toFloat(), y!![j].toFloat())
                            path.close()
                            canvas.drawPath(path, paintLine)
                            val rectF = RectF()
                            path.computeBounds(rectF, false)
                            val r = Region()
                            r.setPath(
                                path, Region(
                                    rectF.left.toInt(),
                                    rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt()
                                )
                            )
                            //regionArrayList.add(r)
                            canvas.drawText(shapesData[i].label_id!!, rectF.centerX(), rectF.centerY(), paintLabel)
                        }else{
                            path.lineTo(x[j].toFloat(), y!![j].toFloat())
                        }
                        groundTruthImage.invalidate()
                    }
                }
            }
        }
    }
    private fun readJSONFromAsset(filename:String): String? {
        var json: String?
        try {
            val  inputStream:InputStream = assets.open(filename)
            json = inputStream.bufferedReader().use{it.readText()}
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }
    private fun loadLeftSidemenu() {
        //adding a layoutmanager
        leftSideMenu.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //incorrectPunchesRecyclerView.addItemDecoration(new DividerItemDecoration(incorrectPunchesRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        leftSideMenu.addItemDecoration(
            DividerItemDecoration(
                leftSideMenu.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        //creating an arraylist to store users using the data class user
        val leftMenu = ArrayList<LeftMenuDataModel>()
        //adding some dummy data to the list
        leftMenu.add(LeftMenuDataModel("All Punches", ContextCompat.getColor(context,R.color.app_green),true))
        leftMenu.add(LeftMenuDataModel("Missed Punches", ContextCompat.getColor(context,R.color.missed_punch_color),false))
        leftMenu.add(LeftMenuDataModel("Incorrect Punches", ContextCompat.getColor(context,R.color.in_correct_punch_color),false))
        leftMenu.add(LeftMenuDataModel("Undetected Punches", ContextCompat.getColor(context,R.color.un_detected_punch_color),false))
        //creating our adapter
        val adapter = LeftMenuAdapter(leftMenu,leftSideMenu)
        //now adding the adapter to recyclerview
        leftSideMenu.adapter = adapter
    }
}