package com.vsoft.goodmankotlin

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vsoft.goodmankotlin.adapter.LeftMenuAdapter
import com.vsoft.goodmankotlin.adapter.RightMenuAdapter
import com.vsoft.goodmankotlin.model.*
import java.io.InputStream


class MaskingActivity : AppCompatActivity(),View.OnTouchListener {
    private lateinit var regionArrayList: ArrayList<Region>
    private lateinit var groundTruthImage:ImageView
    private lateinit var leftSideMenu:RecyclerView
    private lateinit var rightSideMenu:RecyclerView
    private lateinit var context: Context
    private lateinit var response: PunchResponse
    private lateinit var bitmap:Bitmap
    private lateinit var shapes:ArrayList<Shapes>
    private lateinit var uniqueShapes:ArrayList<Unique_results>
    private lateinit var correctShapes:ArrayList<Shapes>
    private lateinit var missedShapes:ArrayList<Shapes>
    private lateinit var undetectedShapes:ArrayList<Shapes>
    private lateinit var incorrectShapes:ArrayList<Shapes>
    private lateinit var uniqueCorrectShapes:ArrayList<Unique_results>
    private lateinit var uniqueMissedShapes:ArrayList<Unique_results>
    private lateinit var uniqueUndetectedShapes:ArrayList<Unique_results>
    private lateinit var uniqueIncorrectShapes:ArrayList<Unique_results>
    private lateinit var leftMenu:ArrayList<LeftMenuDataModel>
    private lateinit var shapesToBeDisplayed:ArrayList<Shapes>
    private lateinit var uniqueDiesDisplayed:ArrayList<Unique_results>
    private lateinit var dialogShowInfo:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }
    private fun init(){
        groundTruthImage= findViewById(R.id.punchTypeImage)
        leftSideMenu = findViewById(R.id.leftMenuRecycler)
        rightSideMenu = findViewById(R.id.rightMenuRecycler)
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
                val assetResponse: String? = readJSONFromAsset("response.json")
                val g = Gson()
                response = g.fromJson(assetResponse, PunchResponse::class.java)
                processData(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processData(punchResponse: PunchResponse) {
        initMaskData(punchResponse)
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun populateImageView(base64Image:String, imageWidth:String, imageHeight:String) {
        val layoutParams = FrameLayout.LayoutParams(imageWidth.toInt(), imageHeight.toInt())
        groundTruthImage.layoutParams = layoutParams
        groundTruthImage.setOnTouchListener(this)
        val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        groundTruthImage.setImageBitmap(bitmap)
    }

    private fun initMaskData(punchResponse: PunchResponse) {
        shapes=arrayListOf()
        uniqueShapes= arrayListOf()
        val uniqueResults=punchResponse.unique_results
         correctShapes= arrayListOf()
         missedShapes= arrayListOf()
         incorrectShapes= arrayListOf()
         undetectedShapes= arrayListOf()
        uniqueCorrectShapes= arrayListOf()
        uniqueMissedShapes= arrayListOf()
        uniqueIncorrectShapes= arrayListOf()
        uniqueUndetectedShapes= arrayListOf()
        shapes.addAll(punchResponse.getGt()?.shapes!!.toCollection(ArrayList()))
        uniqueShapes.addAll(punchResponse.unique_results)
        if(shapes?.size!!>0){
            if(shapes.size==uniqueResults.size) {
                for (i in shapes.indices) {
                    if(uniqueResults[i].correct==false && (uniqueResults[i].ground_truth.equals("NoPunch",false) && uniqueResults[i].prediction.equals("Punch",false))){
                        //Incorrect Punches
                        incorrectShapes.add(shapes[i])
                        uniqueCorrectShapes.add(uniqueResults[i])
                    }else if(uniqueResults[i].correct==false && (uniqueResults[i].ground_truth.equals("Punch",false) && uniqueResults[i].prediction.equals("NoPunch",false))){
                        //Missed Punches
                        missedShapes.add(shapes[i])
                        uniqueMissedShapes.add(uniqueResults[i])
                    }else if(uniqueResults[i].correct==false && uniqueResults.get(i).prediction.equals("UNDETECTED",false)){
                        //Undetected Punches
                        undetectedShapes.add(shapes[i])
                        uniqueUndetectedShapes.add(uniqueResults[i])
                    }else{
                        //Correct Punches
                        correctShapes.add(shapes[i])
                        uniqueCorrectShapes.add(uniqueResults[i])
                    }
                }
            }
        }
        updateCountInRecyclerView(shapes.size,missedShapes.size,incorrectShapes.size,undetectedShapes.size)
        loadRightSideMenu(incorrectShapes,missedShapes)
        drawMask()
    }
     fun drawMask(){
         populateImageView(response.getGt()?.gt_image!!,response.getGt()?.gt_image_width!!,response.getGt()?.gt_image_height!!)
         shapesToBeDisplayed= ArrayList()
         uniqueDiesDisplayed=ArrayList()
        if(leftMenu[0].toggleMenu){
            shapesToBeDisplayed.addAll(shapes)
            uniqueDiesDisplayed.addAll(uniqueShapes)
        }else{
            if(leftMenu[1].toggleMenu){
                shapesToBeDisplayed.addAll(missedShapes)
                uniqueDiesDisplayed.addAll(uniqueMissedShapes)
            }
            if(leftMenu[2].toggleMenu){
                shapesToBeDisplayed.addAll(incorrectShapes)
                uniqueDiesDisplayed.addAll(uniqueIncorrectShapes)
            }
            if(leftMenu[3].toggleMenu){
                shapesToBeDisplayed.addAll(undetectedShapes)
                uniqueDiesDisplayed.addAll(uniqueUndetectedShapes)
            }
        }
        if(shapesToBeDisplayed.size>0) {
            regionArrayList= ArrayList()
            val canvas = Canvas(bitmap)
            groundTruthImage.setImageBitmap(bitmap)
            for(i in 0 until shapesToBeDisplayed.size) {
                val path = Path()
                val paintLine = Paint()
                val paintLabel = Paint()
                paintLine.strokeWidth = 8f
                if(incorrectShapes.contains(shapesToBeDisplayed[i])){
                    paintLine.color = resources.getColor(R.color.in_correct_punch_color)
                }else if(missedShapes.contains(shapesToBeDisplayed[i])){
                    paintLine.color = resources.getColor(R.color.missed_punch_color)
                }else if(undetectedShapes.contains(shapesToBeDisplayed[i])){
                    paintLine.color = resources.getColor(R.color.un_detected_punch_color)
                }else{
                    paintLine.color = Color.GREEN
                }
                paintLine.style = Paint.Style.STROKE
                paintLabel.strokeWidth = 8f
                paintLabel.textSize = 40f
                paintLabel.color = Color.WHITE
                paintLabel.isFakeBoldText = true
                paintLabel.textAlign = Paint.Align.CENTER
                val x= shapesToBeDisplayed[i].gt_points?.x
                val y= shapesToBeDisplayed[i].gt_points?.y
                if(x?.size==y?.size){
                    for(j in x!!.indices){
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
                            regionArrayList.add(r)
                            canvas.drawText(shapesToBeDisplayed[i].label_id!!, rectF.centerX(), rectF.centerY(), paintLabel)
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
        leftMenu= ArrayList()
        //adding some dummy data to the list
        leftMenu.add(LeftMenuDataModel("All Punches", ContextCompat.getColor(context,R.color.app_green),true,0))
        leftMenu.add(LeftMenuDataModel("Missed Punches", ContextCompat.getColor(context,R.color.missed_punch_color),false,0))
        leftMenu.add(LeftMenuDataModel("Incorrect Punches", ContextCompat.getColor(context,R.color.in_correct_punch_color),false,0))
        leftMenu.add(LeftMenuDataModel("Undetected Punches", ContextCompat.getColor(context,R.color.un_detected_punch_color),false,0))
        //creating our adapter
        val adapter = LeftMenuAdapter(this,leftMenu,leftSideMenu)
        //now adding the adapter to recyclerview
        leftSideMenu.adapter = adapter
    }
    private fun updateCountInRecyclerView(allShapesCount:Int,missedShapesCount:Int,incorrectShapesCount:Int,undetectedShapesCount:Int){
        for(i in 0 until leftMenu.size){
            var leftMenuDataModel=leftMenu[i]
            if(i==0)
            leftMenuDataModel.count=allShapesCount
            if(i==1)
                leftMenuDataModel.count=missedShapesCount
            if(i==2)
                leftMenuDataModel.count=incorrectShapesCount
            if(i==3)
                leftMenuDataModel.count=undetectedShapesCount
        }
        leftSideMenu.adapter?.notifyDataSetChanged()
    }
    private fun loadRightSideMenu(incorrectShapes: ArrayList<Shapes>, missedShapes: ArrayList<Shapes>) {
        //adding a layoutManager
        rightSideMenu.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //incorrectPunchesRecyclerView.addItemDecoration(new DividerItemDecoration(incorrectPunchesRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        rightSideMenu.addItemDecoration(
            DividerItemDecoration(
                rightSideMenu.context,
                DividerItemDecoration.VERTICAL
            )
        )
        //creating an arraylist to store users using the data class user
        val rightMenu = ArrayList<RightMenuDataModel>()
        if(incorrectShapes.isNotEmpty() && missedShapes.isNotEmpty()) {
            if (incorrectShapes.isNotEmpty()) {
                for (i in incorrectShapes) {
                    val rightMenuDataModel = RightMenuDataModel("Incorrect Punch", i.label_id!!)
                    rightMenu.add(rightMenuDataModel)
                }
            }
            if (missedShapes.isNotEmpty()) {
                for (i in missedShapes) {
                    val rightMenuDataModel = RightMenuDataModel("Missed Punch", i.label_id!!)
                    rightMenu.add(rightMenuDataModel)
                }
            }
            //creating our adapter
            val adapter = RightMenuAdapter(rightMenu,rightSideMenu)
            //now adding the adapter to recyclerview
            rightSideMenu.adapter = adapter
        }else{
            rightSideMenu.visibility=View.GONE
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.right_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.rightMenu -> {
                if(rightSideMenu.visibility!= View.VISIBLE){
                    rightSideMenu.visibility=View.VISIBLE
                }else{
                    rightSideMenu.visibility=View.GONE
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN ->{
                var point = Point(motionEvent?.x!!.toInt() , motionEvent.y.toInt())
            for ((clickedPos, r) in regionArrayList.withIndex()) {
                if (r.contains(point.x, point.y)) {
                    displayClickedDieDetails(shapesToBeDisplayed,uniqueDiesDisplayed, clickedPos)
                    break
                }
            }
            }
        }

        return true
    }

    private fun displayClickedDieDetails(shapesToBeDisplayed: ArrayList<Shapes>,uniqueDiesDisplayed:ArrayList<Unique_results>, clickedPos: Int) {
        println("Clicked at:"+shapesToBeDisplayed[clickedPos].label_id)
        val shapeClicked=shapesToBeDisplayed[clickedPos]
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.die_detail_dialog, null)

        dialogShowInfo = Dialog(this)
        dialogShowInfo.setContentView(dialogView)
        dialogShowInfo.setCancelable(false)
        val iv_captured_die = dialogView.findViewById<ImageView>(R.id.iv_captured_die)
        val iv_original_die = dialogView.findViewById<ImageView>(R.id.iv_original_die)
        val accept: Button = dialogView.findViewById(R.id.btn_accept)
        val reject: Button = dialogView.findViewById(R.id.btn_reject)
        val labelId: TextView = dialogView.findViewById(R.id.labelId)
        val groundTruth: TextView = dialogView.findViewById(R.id.groundTruth)
        val correct: TextView = dialogView.findViewById(R.id.correct)
        val instructions: TextView = dialogView.findViewById(R.id.instructions)
        val llFeedBack: LinearLayout =dialogView.findViewById(R.id.llFeedBack)
        labelId.text =
            Html.fromHtml("<b>Label : </b>" + shapeClicked.label_id)
        groundTruth.text = Html.fromHtml(
            "<b>Ground Truth : </b>" + uniqueDiesDisplayed[clickedPos].ground_truth
        )
        correct.text =
            Html.fromHtml("<b>Correct : </b>" + uniqueDiesDisplayed[clickedPos].correct)
        val decodedString: ByteArray =
            Base64.decode(uniqueDiesDisplayed[clickedPos].base64_image_segment, Base64.DEFAULT)
        var capturedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        var originalBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        if(capturedBitmap!=null)
        iv_captured_die.setImageBitmap(capturedBitmap)
        if(originalBitmap!=null)
        iv_original_die.setImageBitmap(originalBitmap)
        if (uniqueDiesDisplayed[clickedPos].correct==false && uniqueDiesDisplayed[clickedPos].prediction!!.trim()
                .equals("Punch",true) && uniqueDiesDisplayed[clickedPos].ground_truth!!.trim()
                .equals("NoPunch",true)
        ) {
            //Incorrect Punch
            llFeedBack.setVisibility(View.VISIBLE)
            instructions.visibility = View.VISIBLE
            instructions.text =
                Html.fromHtml("<b>Instructions :</b> Please remove the punch from the displayed die.")
        } else if (uniqueDiesDisplayed[clickedPos].correct==false && uniqueDiesDisplayed[clickedPos].prediction!!.trim()
                .equals("NoPunch",true) && uniqueDiesDisplayed[clickedPos].ground_truth!!
                .trim().equals("Punch",true)
        ) {
            //Missed Punch
            llFeedBack.setVisibility(View.VISIBLE)
            instructions.visibility = View.VISIBLE
            instructions.text =
                Html.fromHtml("<b>Instructions :</b> Please insert a punch into the displayed die.")
        } else {
            llFeedBack.setVisibility(View.GONE)
            instructions.visibility = View.GONE
        }
        accept.setOnClickListener {
            dialogShowInfo.dismiss()
        }
        reject.setOnClickListener {
            dialogShowInfo.dismiss()
        }
        val layoutParams = WindowManager.LayoutParams()
        val windowAlDl: Window? = dialogShowInfo.window

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT

        windowAlDl?.attributes = layoutParams
        dialogShowInfo.show()
    }
     fun dismissListener(v: View?){
        dialogShowInfo.dismiss()
    }
}