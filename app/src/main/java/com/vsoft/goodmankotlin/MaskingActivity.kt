package com.vsoft.goodmankotlin

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.util.Log
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
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.interfaces.RightMenuItemClickCallBack
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.LeftMenuDataModel
import com.vsoft.goodmankotlin.model.RightMenuDataModel
import com.vsoft.goodmankotlin.model.UniqueResults
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import com.vsoft.goodmankotlin.video_response.*
import java.io.InputStream


class MaskingActivity : AppCompatActivity(), View.OnTouchListener, RightMenuItemClickCallBack,
    CustomDialogCallback {
    private lateinit var ivCapturedDie: ImageView
    private lateinit var ivOriginalDie: ImageView
    private lateinit var regionArrayList: ArrayList<Region>
    private lateinit var groundTruthImage: ImageView
    private lateinit var leftSideMenu: RecyclerView
    private lateinit var rightSideMenu: RecyclerView
    private lateinit var context: Context
    private lateinit var response: VideoAnnotationResponse
    private lateinit var bitmap: Bitmap
    private lateinit var shapes: ArrayList<Shapes>
    private lateinit var uniqueShapes: ArrayList<Unique_results>
    private lateinit var correctShapes: ArrayList<Shapes>
    private lateinit var missedShapes: ArrayList<Shapes>
    private lateinit var undetectedShapes: ArrayList<Shapes>
    private lateinit var incorrectShapes: ArrayList<Shapes>
    private lateinit var uniqueCorrectShapes: ArrayList<Unique_results>
    private lateinit var uniqueMissedShapes: ArrayList<Unique_results>
    private lateinit var uniqueUndetectedShapes: ArrayList<Unique_results>
    private lateinit var uniqueIncorrectShapes: ArrayList<Unique_results>
    private lateinit var uniqueRightMenuShapes: ArrayList<Unique_results>
    private lateinit var leftMenu: ArrayList<LeftMenuDataModel>
    private lateinit var shapesToBeDisplayed: ArrayList<Shapes>
    private lateinit var uniqueDiesDisplayed: ArrayList<Unique_results>
    private lateinit var dialogShowInfo: Dialog
    private lateinit var optionsMenu: Menu


    private var sharedPreferences: SharedPreferences? = null

    private var dieIdStr = ""
    private var partIdStr = ""
    private var dieTypeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburg_menu_icon)// set drawable icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_masking)
        init()
    }

    private fun init() {
        groundTruthImage = findViewById(R.id.punchTypeImage)
        leftSideMenu = findViewById(R.id.leftMenuRecycler)
        rightSideMenu = findViewById(R.id.rightMenuRecycler)
        context = this
        loadLeftSidemenu()
        try {
            sharedPreferences = this.getSharedPreferences(
                CommonUtils.SHARED_PREF_FILE,
                Context.MODE_PRIVATE
            )

            dieIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_ID, "").toString()
            partIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_PART_ID, "").toString()
            dieTypeStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_TYPE, "").toString()


            if (sharedPreferences!!.contains(CommonUtils.RESPONSE)) {
                val gson = Gson()
                val json = sharedPreferences!!.getString(CommonUtils.RESPONSE, "")
                response = gson.fromJson(json, VideoAnnotationResponse::class.java)
                processData(response)
            } else {
                val assetResponse: String? = readJSONFromAsset("res.json")
                val g = Gson()
                response = g.fromJson(assetResponse, VideoAnnotationResponse::class.java)
                processData(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processData(videoAnnotationResponse: VideoAnnotationResponse) {
        initMaskData(videoAnnotationResponse)
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun populateImageView(base64Image: String, imageWidth: Int, imageHeight: Int) {
        val layoutParams = FrameLayout.LayoutParams(imageWidth, imageHeight)
        groundTruthImage.layoutParams = layoutParams
        groundTruthImage.setOnTouchListener(this)
        val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        groundTruthImage.setImageBitmap(bitmap)
    }

    private fun initMaskData(videoAnnotationResponse: VideoAnnotationResponse) {
        shapes = arrayListOf()
        uniqueShapes = arrayListOf()
        val uniqueResults = videoAnnotationResponse.unique_results
        correctShapes = arrayListOf()
        missedShapes = arrayListOf()
        incorrectShapes = arrayListOf()
        undetectedShapes = arrayListOf()
        uniqueCorrectShapes = arrayListOf()
        uniqueMissedShapes = arrayListOf()
        uniqueIncorrectShapes = arrayListOf()
        uniqueUndetectedShapes = arrayListOf()
        shapes.addAll(videoAnnotationResponse.gt?.shapes!!)
        uniqueShapes.addAll(videoAnnotationResponse.unique_results)
        if (shapes.size > 0) {
            if (shapes.size == uniqueResults.size) {
                for (i in shapes.indices) {
                    if (uniqueResults[i].correct == "false" && (uniqueResults[i].ground_truth.equals(
                            CommonUtils.NO_PUNCH,
                            false
                        ) && uniqueResults[i].prediction.equals(CommonUtils.PUNCH, false))
                    ) {
                        //Incorrect Punches
                        incorrectShapes.add(shapes[i])
                        uniqueIncorrectShapes.add(uniqueResults[i])
                    } else if (uniqueResults[i].correct == "false" && (uniqueResults[i].ground_truth.equals(
                            CommonUtils.PUNCH,
                            false
                        ) && uniqueResults[i].prediction.equals(CommonUtils.NO_PUNCH, false))
                    ) {
                        //Missed Punches
                        missedShapes.add(shapes[i])
                        uniqueMissedShapes.add(uniqueResults[i])
                    } else if (uniqueResults[i].correct == "false" && uniqueResults[i].prediction.equals(
                            CommonUtils.UNDETECTED,
                            false
                        )
                    ) {
                        //Undetected Punches
                        undetectedShapes.add(shapes[i])
                        uniqueUndetectedShapes.add(uniqueResults[i])
                    } else {
                        //Correct Punches
                        correctShapes.add(shapes[i])
                        uniqueCorrectShapes.add(uniqueResults[i])
                    }
                }
            }
        }
        updateCountInRecyclerView(
            shapes.size,
            missedShapes.size,
            incorrectShapes.size,
            undetectedShapes.size
        )
        drawMask()
    }

    fun drawMask() {
        populateImageView(
            response.gt?.gt_image!!,
            response.gt?.gt_image_width!!,
            response.gt?.gt_image_height!!
        )
        shapesToBeDisplayed = ArrayList()
        uniqueDiesDisplayed = ArrayList()
        if (leftMenu[0].toggleMenu) {
            shapesToBeDisplayed.addAll(shapes)
            uniqueDiesDisplayed.addAll(uniqueShapes)
        } else {
            if (leftMenu[1].toggleMenu) {
                shapesToBeDisplayed.addAll(missedShapes)
                uniqueDiesDisplayed.addAll(uniqueMissedShapes)
            }
            if (leftMenu[2].toggleMenu) {
                shapesToBeDisplayed.addAll(incorrectShapes)
                uniqueDiesDisplayed.addAll(uniqueIncorrectShapes)
            }
            if (leftMenu[3].toggleMenu) {
                shapesToBeDisplayed.addAll(undetectedShapes)
                uniqueDiesDisplayed.addAll(uniqueUndetectedShapes)
            }
        }
        if (shapesToBeDisplayed.size > 0) {
            regionArrayList = ArrayList()
            val canvas = Canvas(bitmap)
            groundTruthImage.setImageBitmap(bitmap)
            for (i in 0 until shapesToBeDisplayed.size) {
                val path = Path()
                val paintLine = Paint()
                val paintLabel = Paint()
                paintLine.strokeWidth = 8f
                if (incorrectShapes.contains(shapesToBeDisplayed[i])) {
                    paintLine.color = resources.getColor(R.color.in_correct_punch_color)
                } else if (missedShapes.contains(shapesToBeDisplayed[i])) {
                    paintLine.color = resources.getColor(R.color.missed_punch_color)
                } else if (undetectedShapes.contains(shapesToBeDisplayed[i])) {
                    paintLine.color = resources.getColor(R.color.un_detected_punch_color)
                } else {
                    paintLine.color = Color.GREEN
                }
                paintLine.style = Paint.Style.STROKE
                paintLabel.strokeWidth = 8f
                paintLabel.textSize = 40f
                paintLabel.color = Color.WHITE
                paintLabel.isFakeBoldText = true
                paintLabel.textAlign = Paint.Align.CENTER
                val x = shapesToBeDisplayed[i].gt_points?.x
                val y = shapesToBeDisplayed[i].gt_points?.y
                if (x?.size == y?.size) {
                    for (j in x!!.indices) {
                        if (j == 0) {
                            path.moveTo(x[0].toFloat(), y!![0].toFloat())
                        } else if (j == x.size - 1) {
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
                            canvas.drawText(
                                shapesToBeDisplayed[i].label_id!!,
                                rectF.centerX(),
                                rectF.centerY(),
                                paintLabel
                            )
                        } else {
                            path.lineTo(x[j].toFloat(), y!![j].toFloat())
                        }
                        groundTruthImage.invalidate()
                    }
                }
            }
        }
    }

    private fun readJSONFromAsset(filename: String): String? {
        val json: String?
        try {
            val inputStream: InputStream = assets.open(filename)
            json = inputStream.bufferedReader().use { it.readText() }
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
                leftSideMenu.context,
                DividerItemDecoration.VERTICAL
            )
        )
        leftMenu = ArrayList()
        //adding some dummy data to the list
        leftMenu.add(
            LeftMenuDataModel(
                CommonUtils.ALL_PUNCHES,
                ContextCompat.getColor(context, R.color.app_green),
                true,
                0
            )
        )
        leftMenu.add(
            LeftMenuDataModel(
                CommonUtils.MISSED_PUNCHES,
                ContextCompat.getColor(context, R.color.missed_punch_color),
                false,
                0
            )
        )
        leftMenu.add(
            LeftMenuDataModel(
                CommonUtils.INCORRECT_PUNCHES,
                ContextCompat.getColor(context, R.color.in_correct_punch_color),
                false,
                0
            )
        )
        leftMenu.add(
            LeftMenuDataModel(
                CommonUtils.UNDETECTED_PUNCHES,
                ContextCompat.getColor(context, R.color.un_detected_punch_color),
                false,
                0
            )
        )
        //creating our adapter
        val adapter = LeftMenuAdapter(this, leftMenu, leftSideMenu)
        //now adding the adapter to recyclerview
        leftSideMenu.adapter = adapter
    }

    private fun updateCountInRecyclerView(
        allShapesCount: Int,
        missedShapesCount: Int,
        incorrectShapesCount: Int,
        undetectedShapesCount: Int
    ) {
        for (i in 0 until leftMenu.size) {
            val leftMenuDataModel = leftMenu[i]
            if (i == 0)
                leftMenuDataModel.count = allShapesCount
            if (i == 1)
                leftMenuDataModel.count = missedShapesCount
            if (i == 2)
                leftMenuDataModel.count = incorrectShapesCount
            if (i == 3)
                leftMenuDataModel.count = undetectedShapesCount
        }
        leftSideMenu.adapter?.notifyDataSetChanged()
    }

    private fun loadRightSideMenu(
        incorrectShapes: ArrayList<Shapes>,
        missedShapes: ArrayList<Shapes>
    ) {
        uniqueRightMenuShapes = ArrayList()
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
        val rightMenuItems = ArrayList<RightMenuDataModel>()
        if (incorrectShapes.isNotEmpty() || missedShapes.isNotEmpty()) {
            if (incorrectShapes.isNotEmpty()) {
                for (i in incorrectShapes) {
                    val rightMenuDataModel =
                        RightMenuDataModel(CommonUtils.INCORRECT_PUNCH, i.label_id!!)
                    rightMenuItems.add(rightMenuDataModel)
                }
                uniqueRightMenuShapes.addAll(uniqueIncorrectShapes)
            }
            if (missedShapes.isNotEmpty()) {
                for (i in missedShapes) {
                    val rightMenuDataModel =
                        RightMenuDataModel(CommonUtils.MISSED_PUNCH, i.label_id!!)
                    rightMenuItems.add(rightMenuDataModel)
                }
                uniqueRightMenuShapes.addAll(uniqueMissedShapes)
            }
            //creating our adapter
            val adapter = RightMenuAdapter(rightMenuItems, this)
            //now adding the adapter to recyclerview
            rightSideMenu.adapter = adapter
        } else {
            try {
                val item: MenuItem = optionsMenu.findItem(R.id.rightMenu)
                item.isVisible = false
                rightSideMenu.adapter = null
                rightSideMenu.visibility = View.GONE
            } catch (e: Exception) {
                println(e.printStackTrace())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        optionsMenu = menu!!
        menuInflater.inflate(R.menu.right_menu, menu)
        try {
            loadRightSideMenu(incorrectShapes, missedShapes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.rightMenu -> {
                if (rightSideMenu.visibility != View.VISIBLE) {
                    rightSideMenu.visibility = View.VISIBLE
                } else {
                    rightSideMenu.visibility = View.GONE
                }
                true
            }
            android.R.id.home -> {
                if (leftSideMenu.visibility != View.VISIBLE) {
                    leftSideMenu.visibility = View.VISIBLE
                } else {
                    leftSideMenu.visibility = View.GONE
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
        when (motionEvent?.action) {
            MotionEvent.ACTION_UP -> {
                rightSideMenu.visibility = View.GONE
                leftSideMenu.visibility = View.GONE
                val point = Point(motionEvent.x.toInt(), motionEvent.y.toInt())
                for ((clickedPos, r) in regionArrayList.withIndex()) {
                    if (r.contains(point.x, point.y)) {

                        displayClickedDieDetails(uniqueDiesDisplayed[clickedPos])
                        break
                    }
                }
            }
        }

        return true
    }


    private fun displayClickedDieDetails(uniqueDiesDisplayed: Unique_results) {
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.die_detail_dialog, null, false)
        //mLinearLayout = view.findViewById(R.id.legend_dialog_fragment_linearlayout) as LinearLayout
        dialogShowInfo = Dialog(this)
        dialogShowInfo.setContentView(dialogView)
        dialogShowInfo.setCancelable(false)
        ivCapturedDie = dialogView.findViewById(R.id.iv_captured_die)
        ivOriginalDie = dialogView.findViewById(R.id.iv_original_die)
        val accept: Button = dialogView.findViewById(R.id.btn_accept)
        val reject: Button = dialogView.findViewById(R.id.btn_reject)
        val labelId: TextView = dialogView.findViewById(R.id.labelId)
        val groundTruth: TextView = dialogView.findViewById(R.id.groundTruth)
        val correct: TextView = dialogView.findViewById(R.id.correct)
        val instructions: TextView = dialogView.findViewById(R.id.instructions)
        val llFeedBack: LinearLayout = dialogView.findViewById(R.id.llFeedBack)
        labelId.text =
            Html.fromHtml(this@MaskingActivity.resources.getString(R.string.label) + uniqueDiesDisplayed.label_id)
        groundTruth.text = Html.fromHtml(
            this@MaskingActivity.resources.getString(R.string.ground_truth) + uniqueDiesDisplayed.ground_truth
        )
        correct.text =
            Html.fromHtml(this@MaskingActivity.resources.getString(R.string.correct) + uniqueDiesDisplayed.correct)
        val decodedString: ByteArray =
            Base64.decode(uniqueDiesDisplayed.base64_image_segment, Base64.DEFAULT)
        val capturedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        val originalBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        if (capturedBitmap != null) {
            ivCapturedDie.setImageBitmap(capturedBitmap)
        }
        if (originalBitmap != null) {
            ivOriginalDie.setImageBitmap(originalBitmap)
        }
        if (uniqueDiesDisplayed.correct == "false" && uniqueDiesDisplayed.prediction!!.trim()
                .equals(
                    this@MaskingActivity.resources.getString(R.string.punch),
                    true
                ) && uniqueDiesDisplayed.ground_truth!!.trim()
                .equals(this@MaskingActivity.resources.getString(R.string.no_punch), true)
        ) {
            //Incorrect Punch
            llFeedBack.visibility = View.VISIBLE
            instructions.visibility = View.VISIBLE
            instructions.text =
                Html.fromHtml(this@MaskingActivity.resources.getString(R.string.instructions))
        } else if (uniqueDiesDisplayed.correct == "false" && uniqueDiesDisplayed.prediction!!.trim()
                .equals(
                    this@MaskingActivity.resources.getString(R.string.no_punch),
                    true
                ) && uniqueDiesDisplayed.ground_truth!!
                .trim().equals(this@MaskingActivity.resources.getString(R.string.punch), true)
        ) {
            //Missed Punch
            llFeedBack.visibility = View.VISIBLE
            instructions.visibility = View.VISIBLE
            instructions.text =
                Html.fromHtml(this@MaskingActivity.resources.getString(R.string.instructions))
        } else {
            llFeedBack.visibility = View.GONE
            instructions.visibility = View.GONE
        }
        accept.setOnClickListener {
            updateJson(uniqueDiesDisplayed)
            dialogShowInfo.dismiss()
        }
        reject.setOnClickListener {
            updateJson(uniqueDiesDisplayed)
            dialogShowInfo.dismiss()
        }
        val layoutParams = WindowManager.LayoutParams()
        val windowAlDl: Window? = dialogShowInfo.window

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        windowAlDl?.attributes = layoutParams
        dialogShowInfo.show()
    }

    private fun updateJson(uniqueResults: Unique_results) {
        if (response != null) {
            uniqueResults.correct = "true"
            processData(response)
        }
    }

    fun dismissListener(v: View?) {
        dialogShowInfo.dismiss()
    }

    override fun onRightMenuItemClickCallBack(clickedPos: Int) {
        displayClickedDieDetails(uniqueRightMenuShapes[clickedPos])
    }

    override fun onBackPressed() {
        DialogUtils.showCustomAlert(
            this, CustomDialogModel(
                this.resources.getString(R.string.app_name),
                this.resources.getString(R.string.dashboard_navigation_alert_message),
                null,
                listOf(
                    this.resources.getString(R.string.alert_ok),
                    this.resources.getString(R.string.alert_cancel)
                )
            ), this, CommonUtils.BACK_PRESSED_DIALOG
        )
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(this.resources.getString(R.string.alert_ok), true)) {
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                navigateToDashBoard()
            }
        }
        if (buttonName.equals(this.resources.getString(R.string.alert_cancel), true)) {
            //No action required. Just exit dialog.
        }
    }

    private fun navigateToDashBoard() {
        val mainIntent = Intent(this, DashBoardActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainIntent)
    }
}