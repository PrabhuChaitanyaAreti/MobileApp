package com.cumulocitydemo

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vsoft.goodmankotlin.BuildConfig
import com.vsoft.goodmankotlin.cumulocity.ApkDownloaderCallBack

import java.io.File

class DownloadController(
    private val context: Context,
    private val url: String,
    private var FILE_NAME: String,
    private val apkDesc: String,

) {
    private lateinit var destination: String
    private lateinit var uri:Uri
    companion object {
       // private const val FILE_NAME = this.appName
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "application/vnd.android.package-archive"
    }

    fun enqueueDownload(apkDownloaderCallBack: ApkDownloaderCallBack) {
        FILE_NAME
        destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME

        uri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists()) file.delete()
        val username = "rpittala@vsoftconsulting.com"
        val password = "Ram@12345"
        val credentials = "$username:$password"
        val base64EncodedCredentials =
            Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        request.setMimeType(MIME_TYPE)
        request.setTitle(FILE_NAME)
        request.setDescription(apkDesc)
        request.addRequestHeader("Authorization", "Basic $base64EncodedCredentials")
        // set destination
        request.setDestinationUri(uri)

        showInstallOption(destination, uri,apkDownloaderCallBack)
        // Enqueue a new download and same the referenceId
        downloadManager.enqueue(request)
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG)
            .show()


    }

    private fun showInstallOption(
        destination: String,
        uri: Uri,
        apkDownloaderCallBack: ApkDownloaderCallBack
    ) {

        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
               // apkDownloaderCallBack.onDownloadCompleted()
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.d("DownloadController","-----> installing apk1")
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                        File(destination)
                    )
                    Log.d("DownloadController","----->URI: "+contentUri)
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                  //  install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                } else {
                    Log.d("DownloadController","-----> installing apk1")
                    val install = Intent(Intent.ACTION_VIEW)
                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    install.setDataAndType(
                        uri,
                        APP_INSTALL_PATH
                    )
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                }*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.d("DownloadController","-----> installing apk1")
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                        File(destination)
                    )
//                    Log.d("DownloadController","----->URI: "+contentUri)
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//                    install.data = contentUri
//                    context.startActivity(install)
//                    context.unregisterReceiver(this)
//                    // finish()
                }
                else {
//                    Log.d("DownloadController","-----> installing apk1")
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    install.setDataAndType(
//                        uri,
//                        APP_INSTALL_PATH
//                    )
//                    context.startActivity(install)
                    context.unregisterReceiver(this)
//                    // finish()
                }
                 apkDownloaderCallBack.onDownloadCompleted()

            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    public fun showInstallAPK() {

        // apkDownloaderCallBack.onDownloadCompleted()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d("DownloadController","-----> installing apk1")
            val contentUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                File(destination)
            )
            Log.d("DownloadController","----->URI: "+contentUri)
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = contentUri
            context.startActivity(install)


            // finish()
        } else {
            Log.d("DownloadController","-----> installing apk1")
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(
                uri,
                APP_INSTALL_PATH
            )
            context.startActivity(install)
            // finish()
        }

    }

}
