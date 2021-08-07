package com.example.gps_wifi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    /* DECLARATION OF XML VARIABLES */
    Button btGPS;
    Button btWiFi;

    TextView txtGPS;
    TextView txtWiFi;

    int counterGPS = 0;
    int counterWiFi = 0;

    String username;
    String password;

    /* DECLARATION OF WIFI VARIABLES */
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    ConnectivityManager connectivityManager;

    /* DECLARATION OF GPS VARIABLES */
    LocationTrack locationTrack;

    /* DECLARATION OF SOCKET VARIABLES */
    String ip = "188.82.90.18";
    int port = 3000;
    Thread thread;
    SocketHandler sh;

    /* TIMER STUFF*/
    TimerTask timerTask;
    Timer timer;
    Handler handler;

    /* DATE AND TIME*/
    Date date;
    SimpleDateFormat sdf;
    String dateString;

    private ArrayList<String> wifiList = new ArrayList<>();
    private List<ScanResult> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            username = "" + bundle.get("username");
            password = "" + bundle.get("password");
        }
        else {
            onDestroy();
        }

        /* FIND IDS */
        btGPS = (Button) findViewById(R.id.btGPS);
        btWiFi = (Button) findViewById(R.id.btWiFi);

        txtGPS = (TextView) findViewById(R.id.txtGPS);
        txtWiFi = (TextView) findViewById(R.id.txtWiFi);


        /* INITIALIZES SOCKET AND THREAD STUFF */
        sh = new SocketHandler(ip, port);
        thread = new Thread(sh);
        thread.start();

        /* SET FUNCTIONS TO BUTTONS */
        btGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPS();
            }
        });


        btWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiFi2();
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        /* SIMPLE DATE FORMAT INITIALIZER */
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        /* SET TIMER */
        handler = new Handler();
        timer = new Timer();
        timerTask =  new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        date = new Date();
                        dateString = sdf.format(date);
                        GPS();
                        WiFi2();
                    }
                });
            }
        };
        timer.schedule(timerTask, 0L, 15000L);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // CANCEL TIMER WHEN APP IS DOWN
        timer.cancel();
        timer.purge();
    }

    // FUNCTION THAT SEARCHES FOR WIFI CONNECTION - SSID
    public void WiFi() {
        counterWiFi++;

        String ssid = "";
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // check if is connected to wifi
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                ssid = "WiFi " + username + " " + dateString + " " + wifiInfo.getSSID();
                sh.addItemToSend(ssid);
                txtWiFi.setText(ssid);
            }
        }


    }

    public void WiFi2() {
        counterWiFi++;

        wifiList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        String ssid = "";
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            for (ScanResult scanResult : results) {
                wifiList.add(scanResult.SSID);
            }
            for (String res : wifiList) {
                ssid = "WiFi " + username + " " + dateString + " " + res;
                sh.addItemToSend(ssid);
                txtWiFi.setText(ssid);
            }
        }
    };

    // FUNCTION THAT LOOKS FOR GPS COORDINATES
    public void GPS() {
        counterGPS++;
        String msg = "Clicked in GPS button " + counterGPS;
        msg +=  (counterGPS == 1) ? (" time!") : (" times!");

        locationTrack = new LocationTrack(MainActivity.this);

        if (locationTrack.canGetLocation()) {

            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();
            double altitude = locationTrack.getAltitude();

            if(longitude != 0.0 && latitude != 0.0) {
                String lat = latitude < 0 ? "S " : "N ";
                String lon = longitude < 0 ? "W " : "E ";

                //latitude = Math.abs(latitude);
                //longitude = Math.abs(longitude);
                int lon_grau = (int) Math.floor(longitude);
                int lat_grau = (int) Math.floor(latitude);

                double aux_lon = (longitude - (double) lon_grau) * 60;
                double aux_lat = (latitude - (double) lat_grau) * 60;
                int lon_min = (int) Math.floor(aux_lon);
                int lat_min = (int) Math.floor(aux_lat);

                int lon_dec = (int) Math.floor((aux_lon - lon_min)*10000);
                int lat_dec = (int) Math.floor((aux_lat - lat_min)*10000);

                String toSend = "GPS " + username + " " + dateString + " " + latitude + " " + longitude + " " + altitude;
                sh.addItemToSend(toSend);
                txtGPS.setText(toSend);
            }
            else{
                txtGPS.setText("GPS - empty coordinates");
            }
        } else {
            locationTrack.showSettingsAlert();
        }
    }

}