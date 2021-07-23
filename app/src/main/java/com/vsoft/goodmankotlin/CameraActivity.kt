package com.vsoft.goodmankotlin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.media.MediaScannerConnection
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vsoft.goodmankotlin.model.PunchResponse
import com.vsoft.goodmankotlin.utils.*
import com.vsoft.goodmankotlin.utils.CameraPreview.Companion.getOptimalPreviewSize
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class CameraActivity : AppCompatActivity() ,View.OnClickListener{
    companion object {
    var screenWidth:Int = 0
    var screenHeight:Int = 0
    }

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mPicture: PictureCallback? = null

    private var capture: Button? = null
    private  var btnSendEdge:android.widget.Button? = null
    private var cameraPreview: LinearLayout? = null
    private var imgSettings: ImageView? =
        null
    private  var imgCapture:android.widget.ImageView? = null
    private  var imgPreview:android.widget.ImageView? = null

    private var myContext: Context? = null

    private var imagePath = ""

   // private val cameraSizesArray: Array<String> = TODO()
    private var progressDialog: ProgressDialog? = null

    /**
     * Background and Countdown timer variables
     */
    private var Counter: CountDownTimer? = null
    private val minutesToGo: Long = 1
    private val initialMillisToGo = minutesToGo * 1000 * 60
    private var alertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

      /*  val displaymetrics = DisplayMetrics()
        this@CameraActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics)
        screenHeight = displaymetrics.heightPixels
        screenWidth = displaymetrics.widthPixels

        Log.d(
            "TAG",
            "CameraActivity device width and height " + screenWidth + "x" + screenHeight
        )*/
        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@CameraActivity)

        Log.d("TAG", "getBatteryPercentage  batterLevel $batterLevel")

        if (batterLevel >= 15) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            myContext = this
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            // Checking availability of the camera
            if (!CameraUtils.isDeviceSupportCamera(applicationContext)) {
                Toast.makeText(
                    applicationContext,
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG
                ).show()
                // will close the app if the device doesn't have camera
                finish()
            }
            if (CameraUtils.checkPermissions(applicationContext)) {
                initScreen()
            } else {
                requestCameraPermission()
            }
        } else {
            batterLevelAlert()
        }
    }

    private fun batterLevelAlert() {
        val builder = AlertDialog.Builder(this@CameraActivity)
        builder.setCancelable(false)
        builder.setTitle("Low Battery")
        builder.setMessage("15% of battery remaining.Please piugin charger")
        builder.setNeutralButton("Exit") { dialog, which ->
            dialog.dismiss()
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
            dialog.dismiss()
            try {
                val previewIntent = Intent()
                setResult(RESULT_CANCELED, previewIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        alertDialog = builder.create()
        if (!this@CameraActivity.isFinishing()) {
            try {
                alertDialog!!.show()
            } catch (e: BadTokenException) {
                Log.e("BadTokenException", e.toString())
            }
        }
    }

    private fun initScreen() {
        /*
         * Background timer initialize
         */
        backgroundTimer()
        imgSettings = findViewById(R.id.settings_icon)
        imgCapture = findViewById(R.id.capture_icon)
        imgPreview = findViewById(R.id.imgPreview)
        val displaymetrics = DisplayMetrics()
        this@CameraActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics)
       CameraActivity.screenHeight = displaymetrics.heightPixels
       CameraActivity.screenWidth = displaymetrics.widthPixels
        Log.d(
            "TAG",
            "device width and height " +CameraActivity.screenWidth + "x" +CameraActivity.screenHeight
        )
        mCamera = Camera.open()
        val parameters = mCamera!!.getParameters()
        val mSupportedPreviewSizes = parameters
            .supportedPreviewSizes
        val previewSize: Camera.Size? = getOptimalPreviewSize(
            mSupportedPreviewSizes, screenWidth, screenHeight)
        if (previewSize != null) {
            Log.d("TAG", "previewSize width and height " + previewSize.width + "x" + previewSize.height)
            parameters.setPreviewSize(previewSize.width, previewSize.height)
            parameters.setPictureSize(previewSize.width, previewSize.height)
        }

        //  parameters.setZoom(Camera.Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX);
        val focusModes = parameters.supportedFocusModes
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        }
        //mCamera?.setParameters(parameters)
        cameraPreview = findViewById(R.id.cPreview)
        mPreview = CameraPreview(myContext, mCamera)
        cameraPreview!!.addView(mPreview)
        capture = findViewById(R.id.btnCam)
        btnSendEdge = findViewById(R.id.btnSendEdge)
        btnSendEdge!!.setOnClickListener(this)
        capture!!.setOnClickListener(this)
        imgSettings!!.setOnClickListener(this)
        imgCapture!!.setOnClickListener(this)
        btnSendEdge!!.setVisibility(View.GONE)
        imgCapture!!.setVisibility(View.GONE)
        imgSettings!!.setVisibility(View.GONE)
        imgPreview!!.setVisibility(View.GONE)
        mPicture = getPictureCallback()
        mPreview!!.refreshCamera(mCamera)
        mCamera!!.startPreview()
        val size = mCamera!!.getParameters().previewSize
        Log.d("TAG", "oncreate getPreviewSize   " + size.width + "x" + size.height)
    }

    /**
     * Requesting permissions using Dexter library
     */
    private fun requestCameraPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            .withListener(object : MultiplePermissionsListener {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        initScreen()
                    } else if (report.isAnyPermissionPermanentlyDenied()) {
                        showPermissionsAlert()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private fun showPermissionsAlert() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Permissions required!")
            .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
            .setPositiveButton(
                "GOTO SETTINGS"
            ) { dialog, which -> CameraUtils.openSettings(this@CameraActivity) }
            .setNegativeButton(
                "CANCEL"
            ) { dialog, which -> }.show()
    }

    override fun onClick(view: View) {
        if (view === capture) {
            if (Counter != null) {
                Counter!!.cancel()
                Counter = null
            }
            Log.d("TAG", "capture onClick ")
            if (mCamera != null) {
                val size = mCamera!!.getParameters().previewSize
                Log.d("TAG", "oncreate getPreviewSize   " + size.width + "x" + size.height)
                mPicture = getPictureCallback()
                mCamera!!.takePicture(null, null, mPicture)
            }
        } else if (view === imgSettings) {
            if (Counter != null) {
                Counter!!.cancel()
                Counter = null
            }
            //showDialog()
        } else if (view === imgCapture) {
            reCapture()
        } else if (view === btnSendEdge) {
            if (Counter != null) {
                Counter!!.cancel()
                Counter = null
            }
            Log.d("TAG", "btnSendEdge onClick imagePath::: $imagePath")
            progressDialog = ProgressDialog(this)
            progressDialog!!.setCancelable(false)
            progressDialog!!.setMessage("Please wait .. Processing image may take some time.")
            if (NetworkUtils.isNetworkAvailable(this)) {
                Handler().post { progressDialog!!.show() }
                val file = File(imagePath) // initialize file here
                val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    RequestBody.create(MediaType.parse("image/*"), file)
                )
                val call: Call<PunchResponse?>? =
                    RetrofitClient.getInstance()!!.getMyApi()!!.uploadDyeImage(filePart)
                call!!.enqueue(object : Callback<PunchResponse?> {
                    override fun onResponse(
                        call: Call<PunchResponse?>?,
                        response: Response<PunchResponse?>
                    ) {
                        try {
                            if (response.isSuccessful()) {
                                // Storing data into SharedPreferences
                                val sharedPreferences =
                                    getSharedPreferences("MySharedPref", MODE_PRIVATE)
                                // Creating an Editor object to edit(write to the file)
                                val myEdit = sharedPreferences.edit()
                                // Storing the key and its value as the data fetched from edittext
                                // Once the changes have been made,
                                // we need to commit to apply those changes made,
                                // otherwise, it will throw an error
                                val gson = Gson()
                                val json: String = gson.toJson(response.body())
                                myEdit.putString("response", json)
                                myEdit.apply()
                                if (progressDialog != null && progressDialog!!.isShowing()) progressDialog!!.dismiss()
                                val intent = Intent(
                                    this@CameraActivity,
                                    MaskingActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            } else {
                                if (progressDialog!!.isShowing()) {
                                    progressDialog!!.dismiss()
                                }
                                DialogUtils.showNormalAlert(
                                    this@CameraActivity,
                                    "Error!!",
                                    "Data mismatch, Take an image and try again."
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (progressDialog!!.isShowing()) {
                                progressDialog!!.dismiss()
                            }
                        }
                    }

                    override fun onFailure(call: Call<PunchResponse?>?, t: Throwable) {
                        DialogUtils.showNormalAlert(this@CameraActivity, "Alert!!", "" + t)
                        if (progressDialog!!.isShowing()) {
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

    private fun reCapture() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(this@CameraActivity.getResources().getString(R.string.app_name))
            .setMessage("Are you sure to want to go ReCapture?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                backgroundTimer()
                btnSendEdge!!.visibility = View.GONE
                imgCapture!!.visibility = View.GONE
                imgPreview!!.visibility = View.GONE
                capture!!.visibility = View.VISIBLE
                cameraPreview!!.visibility = View.VISIBLE
                imgSettings!!.visibility = View.GONE
                mCamera = Camera.open()
                mPicture = getPictureCallback()
                mPreview!!.refreshCamera(mCamera)
                dialog.dismiss()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> }).show()
    }

   /* fun showDialog() {
        val inflater = layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.camera_settings_dialog_new, null)
        val spResolution1 = alertLayout.findViewById<Spinner>(R.id.spResolution)
        val langAdapter1 = ArrayAdapter<CharSequence>(
            this@CameraActivity,
            R.layout.spinner_text,
            cameraSizesArray
        )
        langAdapter1.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        spResolution1.adapter = langAdapter1
        val alert = androidx.appcompat.app.AlertDialog.Builder(this)
        alert.setTitle("Image Size")
        alert.setView(alertLayout)
        alert.setCancelable(false)
        alert.setNegativeButton(
            "Cancel"
        ) { dialog, which ->
            Toast.makeText(baseContext, "Cancel Clicked", Toast.LENGTH_SHORT).show()
        }
        alert.setPositiveButton(
            "OK"
        ) { dialog, which ->
            val size = spResolution1.selectedItem.toString()
            Log.d("TAG", "spinner ok click size $size")
            val separated = size.split("x").toTypedArray()
            Log.d("TAG", "spinner ok click separated.length " + separated.size)
            Log.d(
                "TAG",
                "spinner ok click Integer.parseInt(separated[0]) " + separated[0].toInt()
            )
            Log.d(
                "TAG",
                "spinner ok click Integer.parseInt(separated[1]) " + separated[1].toInt()
            )
            if (Counter != null) {
                Counter!!.cancel()
                Counter = null
            }
            backgroundTimer()
            mPicture = getPictureCallback()
            mPreview!!.refreshCamera(mCamera)
        }
        val dialog = alert.create()
        dialog.show()
    }*/

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d("TAG", "onPause: ")
        //when on Pause, release camera in order to be used from other applications
        releaseCamera()
        if (Counter != null) {
            Counter!!.cancel()
            Counter = null
        }
    }

    private fun releaseCamera() {
        Log.d("TAG", "releaseCamera: ")
        // stop and release camera
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
        }
    }

    private fun getPictureCallback(): PictureCallback {
        Log.d("TAG", "getPictureCallback: ")
        return PictureCallback { data, camera ->
            Log.d("TAG", "onPictureTaken: ")
            /// Toast.makeText(CameraActivity.this,"getPictureCallback onPictureTaken",Toast.LENGTH_LONG).show();
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            imagePath = saveImage(bitmap)
            Log.d("TAG", "onPictureTaken imagePath::: $imagePath")
            capture!!.visibility = View.GONE
            cameraPreview!!.visibility = View.GONE
            imgSettings!!.visibility = View.GONE
            btnSendEdge!!.visibility = View.VISIBLE
            imgCapture!!.visibility = View.VISIBLE
            imgPreview!!.visibility = View.VISIBLE
            imgPreview!!.setImageBitmap(bitmap)
            releaseCamera()
        }
    }

    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val IMAGE_DIRECTORY = "/Goodman/images"
        val wallpaperDirectory =
            File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs())
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .timeInMillis.toString() + ".png"
            )
            f.createNewFile() //give read write permission
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(f.path), arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)
            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    /**
     * Background timer initialize
     */
    private fun backgroundTimer() {
        Counter = object : CountDownTimer(initialMillisToGo, 1000) {
            override fun onTick(millisUntilFinished1: Long) {
                val secs = (millisUntilFinished1 / 1000).toInt() % 60
                val minutes = (millisUntilFinished1 / (1000 * 60) % 60).toInt()
                Log.e("secs", secs.toString() + "")
                Log.e("minutes", minutes.toString() + "")
            }

            override fun onFinish() {
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                val builder = AlertDialog.Builder(this@CameraActivity)
                builder.setTitle(
                    this@CameraActivity.getResources().getString(R.string.app_name)
                )
                builder.setCancelable(false)
                builder.setMessage("No activity detected. Capture screen will close.Select Continue to continue capturing image.")
                builder.setPositiveButton(
                    "Continue"
                ) { dialog, which ->
                    dialog.dismiss()
                    if (alertDialog!!.isShowing) {
                        alertDialog!!.dismiss()
                    }
                    if (Counter != null) {
                        Counter!!.cancel()
                        Counter = null
                    }
                    backgroundTimer()
                }
                builder.setNegativeButton(
                    "Exit"
                ) { dialog, which ->
                    dialog.dismiss()
                    try {
                        val previewIntent = Intent()
                        setResult(RESULT_CANCELED, previewIntent)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                alertDialog = builder.create()
                if (!this@CameraActivity.isFinishing()) {
                    try {
                        alertDialog!!.show()
                    } catch (e: BadTokenException) {
                        Log.e("BadTokenException", e.toString())
                    }
                }
            }
        }
        Counter!!.start()
    }
}
