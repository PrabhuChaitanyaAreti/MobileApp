package com.vsoft.goodmankotlin.cumulocity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cumulocitydemo.DownloadController;import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MqttService extends Service {

    // client, user and device details
    final String serverUrl   = "tcp://mqtt.us.cumulocity.com";     /* ssl://mqtt.cumulocity.com:8883 for a secure connection */
    String clientId;//
    String device_name;
    final String tenant      = "vsoftconsultingai";
    final String username    = "rpittala@vsoftconsulting.com";
    final String password    = "Ram@12345";
    String appName="",appVersion="",appUrl="";
    DownloadController downloadController=null;
    TextView tv_message;
    private GpsTracker gpsTracker;
    double latitude,longitude;
    private static MqttClient mqttlient;
    private MqttConnectOptions mMqttConnectOptions;
    public static MqttClient client;
    public static final int QOS=0;
    public static Context activityContext;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            init();
            getLocation();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void getLocation(){
       /* gpsTracker = new GpsTracker(getApplicationContext());
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();


        }else{
            gpsTracker.showSettingsAlert();
        }*/
    }

    private void init() throws MqttException {

        Log.i("SERVICE-CALL","service started");
        String deiceinfo = DeviceManagement.getDeviceInfo(getApplicationContext());
        String[] parts = deiceinfo.split(",");

        clientId = parts[0];
        device_name = parts[1];

        final MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(tenant + "/" + username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(30);
        // connect the client to Cumulocity IoT
        client = new MqttClient(serverUrl, clientId, null);
        client.connect(options);

        registerDevice();


    }

    private void registerDevice() throws MqttException{
        // register a new device
        client.publish("s/us", ("100," + device_name + ",c8y_MQTTDevice").getBytes(), QOS, false);

        // set device's hardware information
        client.publish("s/us", ("110,"+clientId+",MQTT test model,Rev0.1").getBytes(), QOS, false);

        // add restart operation
        //client.publish("s/us", "114,c8y_Restart,c8y_Command,c8y_Configuration,c8y_Software,c8y_Firmware,c8y_SoftwareList".getBytes(), QOS, false);
        client.publish("s/us", "114,c8y_Command,c8y_Configuration,c8y_Software,c8y_Firmware,c8y_SoftwareList".getBytes(), QOS, false);

        client.subscribe("s/ds",mqttMessageListener);

        publishDeviceMemory(client);

        publishDeviceBattery(client);

        publishDeviceTemp(client);

        publishDeviceLocation(client);

    }







    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

      IMqttMessageListener mqttMessageListener = new IMqttMessageListener() {
          @Override
          public void messageArrived(String topic, MqttMessage message) throws Exception {


              String payload = new String(message.getPayload());

              if(payload.contains(clientId)){ // Checking device id in callback

              if (payload.startsWith("516")) {// software install
                  Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                      public void run() {
                          try {

//                              client.publish("s/us", "501,c8y_SoftwareList".getBytes(), QOS, false);//status - Executing..
                              String[] appDetails=payload.split(",");
                              if(appDetails !=null && appDetails.length>3) {
                                  appName = appDetails[2];
                                  appVersion = appDetails[3];
                                  appUrl = appDetails[4];

                                  //TODO --> check the current device app version and this version

                                  Log.i("INSTALL-MQTT","-----> "+appName+" : "+appVersion+" : "+appUrl);

                                  downloadController=new DownloadController(activityContext,appUrl,appName+".apk",appVersion);
                                  downloadController.enqueueDownload(new ApkDownloaderCallBack() {
                                      @Override
                                      public void onDownloadCompleted() {
                                          try {
                                              client.publish("s/us", "501,c8y_SoftwareList".getBytes(), QOS, false);//status - Executing..
                                              client.publish("s/us", "503,c8y_SoftwareList".getBytes(), QOS, false);

                                          } catch (MqttException e) {
                                              e.printStackTrace();
                                          }
                                      }

                                      @Override
                                      public void onDownloadFailed() {
                                          System.out.println("Download failed.");
                                          //  client.publish("s/us", "502,c8y_SoftwareList,networkissue".getBytes(), QOS, false);

                                      }
                                  });
                              }else{
                                  client.publish("s/us", "503,c8y_SoftwareList".getBytes(), QOS, false);
                              }


                          } catch (MqttException e) {
                              e.printStackTrace();
                          }
                      }
                  });
              }

              }



          }
      };


    private void publishDeviceMemory(MqttClient client){

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = "200,c8y_AvailableMemory,GB,"+DeviceManagement.getDeviceMemory();
                    client.publish("s/us",message.getBytes(),QOS,false);

                }catch(MqttException e){
                    e.printStackTrace();

                }
            }
        },1,10, TimeUnit.SECONDS);

    }

    private void publishDeviceBattery(MqttClient client){

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = "212,"+DeviceManagement.getBatteryManagement(getApplicationContext());
                    client.publish("s/us",message.getBytes(),QOS,false);

                }catch(MqttException e){
                    e.printStackTrace();

                }
            }
        },1,10, TimeUnit.SECONDS);

    }

    private void publishDeviceTemp(MqttClient client){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = "211,"+DeviceManagement.batteryTemperature(getApplicationContext());
                    client.publish("s/us",message.getBytes(),QOS,false);

                }catch(MqttException e){
                    e.printStackTrace();

                }
            }
        },1,10, TimeUnit.SECONDS);

    }


    private void publishDeviceLocation(MqttClient client){

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = "112,"+latitude+","+longitude+","+"80";
                    String locationVal = "401,";
                    client.publish("s/us", message.getBytes(), QOS, false);// Tracking


                }catch (MqttException e){
                    e.printStackTrace();
                }
            }
        },1,10,TimeUnit.SECONDS);

    }


    public static void publishEvent(String event) {

        try{
            client.publish("s/us", event.getBytes(), QOS, false);// custom event

        }catch (MqttException e){
            e.printStackTrace();
        }



    }
}
