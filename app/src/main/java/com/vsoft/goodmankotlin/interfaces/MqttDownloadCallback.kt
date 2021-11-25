package com.vsoft.goodmankotlin.interfaces

interface MqttDownloadCallback {
    fun onDownloadStarted()
    fun onDownloadCompleted()
    fun onInstallStarted()
    fun onInstallCompleted()
}