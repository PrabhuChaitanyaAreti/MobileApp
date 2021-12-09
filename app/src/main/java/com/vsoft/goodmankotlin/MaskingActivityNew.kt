package com.vsoft.goodmankotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.util.Base64
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import com.vsoft.goodmankotlin.video_response.Gt_points
import com.vsoft.goodmankotlin.video_response.Shapes
import org.json.JSONObject
import java.io.InputStream


class MaskingActivityNew : AppCompatActivity(), CustomDialogCallback {
    private lateinit var groundTruthImageView:ImageView
    private lateinit var bitmap:Bitmap
    private lateinit var regionArrayList: ArrayList<Region>
    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburg_menu_icon)// set drawable icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_masking)
        init()
    }
    private fun init() {
        groundTruthImageView=findViewById(R.id.punchTypeImage)
        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
        try {
            if (sharedPreferences!!.contains(CommonUtils.RESPONSE)) {
                val json = sharedPreferences!!.getString(CommonUtils.RESPONSE, "")
                parseResponse(json!!)
            } else {
                val assetResponse: String? = readJSONFromAsset("response_video.json")
                parseResponse(assetResponse!!)
            }
        }catch(ex:Exception){
            val assetResponse: String? = readJSONFromAsset("response_video.json")
            parseResponse(assetResponse!!)
        }

    }

    private fun parseResponse(assetResponse:String) {
        val inferencingObject=JSONObject(assetResponse)
        val groundTruthObject:JSONObject=inferencingObject.getJSONObject("gt")
        val groundTruthBase64:String=groundTruthObject.getString("image")
        val groundTruthWidth:Int=groundTruthObject.getInt("width")
        val groundTruthHeight:Int=groundTruthObject.getInt("height")
        val shapesToBeDisplayed= arrayListOf<Shapes>()
        val xArray= arrayListOf<Double>()
        val yArray= arrayListOf<Double>()
        //val firstObject=inferencingObject.getJSONObject("36")
        val keys: Iterator<String> = inferencingObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (inferencingObject.get(key) is JSONObject) {
                // do something with jsonObject here
                if((inferencingObject.get(key) as JSONObject).has("segmentation")) {
                    val segmentation =
                        (inferencingObject.get(key) as JSONObject).getJSONArray("segmentation")
                    for (i in 0 until segmentation.length()) {
                        val pointArray = segmentation.getJSONArray(i)
                        xArray.add(pointArray[0] as Double)
                        yArray.add(pointArray[1] as Double)
                    }
                    shapesToBeDisplayed.add(Shapes(key, Gt_points(xArray, yArray)))
                }
            }
        }

        populateImageView(groundTruthBase64,groundTruthWidth,groundTruthHeight, shapesToBeDisplayed)
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

    private fun populateImageView(base64Image: String, imageWidth: Int, imageHeight: Int, shapesToBeDisplayed: ArrayList<Shapes>) {
        val layoutParams = FrameLayout.LayoutParams(imageWidth, imageHeight)
        groundTruthImageView.layoutParams = layoutParams
        //groundTruthImageView.setOnTouchListener(this)
        val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        groundTruthImageView.setImageBitmap(bitmap)
        drawInferencing(shapesToBeDisplayed)
    }
    private fun drawInferencing(shapesToBeDisplayed:ArrayList<Shapes>){
        if (shapesToBeDisplayed.size > 0) {
            regionArrayList = ArrayList()
            val canvas = Canvas(bitmap)
//            groundTruthImageView.setImageBitmap(bitmap)
            for (i in 0 until shapesToBeDisplayed.size) {
                val path = Path()
                val paintLine = Paint()
                val paintFill = Paint()
                val paintLabel = Paint()
                paintLine.color = Color.BLUE
                paintLine.style = Paint.Style.STROKE
                paintLabel.strokeWidth = 2f
                paintLabel.textSize = 40f
                paintLabel.color = Color.WHITE
                paintLabel.isFakeBoldText = true
                paintLabel.textAlign = Paint.Align.CENTER
                paintFill.color = Color.GREEN
                paintFill.style = Paint.Style.FILL
                val x = shapesToBeDisplayed[i].gt_points.x
                val y = shapesToBeDisplayed[i].gt_points.y
                if (x.size == y.size) {
                    for (j in x.indices) {
                        when (j) {
                            0 -> {
                                path.moveTo(x[0].toFloat(), y[0].toFloat())
                            }
                            x.size - 1 -> {
                                path.lineTo(x[j].toFloat(), y[j].toFloat())
                                path.close()
                                canvas.drawPath(path, paintLine)
                                //canvas.drawPath(path,paintFill)
                                val rectF = RectF()
                                path.computeBounds(rectF, true)
                                val r = Region()
                                r.setPath(
                                    path, Region(
                                        rectF.left.toInt(),
                                        rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt()
                                    )
                                )
                                regionArrayList.add(r)
                                canvas.drawText(
                                    shapesToBeDisplayed[i].label_id,
                                    rectF.centerX(),
                                    rectF.centerY(),
                                    paintLabel
                                )
                            }
                            else -> {
                                path.lineTo(x[j].toFloat(), y[j].toFloat())
                            }
                        }
                        groundTruthImageView.invalidate()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        DialogUtils.showCustomAlert(
            this, CustomDialogModel(
                this.resources.getString(R.string.app_name),
                this.resources.getString(R.string.dashboard_navigation_alert_message),
                null,
                listOf(
                    this.resources.getString(R.string.alert_yes),
                    this.resources.getString(R.string.alert_no)
                )
            ), this, CommonUtils.BACK_PRESSED_DIALOG
        )
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(this.resources.getString(R.string.alert_yes), true)) {
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                navigateToDashBoard()
            }
        } else if (buttonName.equals(this.resources.getString(R.string.alert_no), true)) {
            //No action required. Just exit dialog.
        }
    }

    private fun navigateToDashBoard() {
        val mainIntent = Intent(this, DashBoardActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainIntent)
    }
}