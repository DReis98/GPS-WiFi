package com.example.gps_wifi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<String>();
    private ArrayList<String> permissions = new ArrayList<String>();

    private final static int ALL_PERMISSIONS_RESULT = 101;

    /* DECLARATION OF XML VARIABLES */
    Button btGPS;
    Button btWiFi;

    TextView txtGPS;
    TextView txtWiFi;

    int counterGPS = 0;
    int counterWiFi = 0;

    String username;

    /* DECLARATION OF WIFI VARIABLES */
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    ConnectivityManager connectivityManager;

    /* DECLARATION OF GPS VARIABLES */
    LocationTrack locationTrack;

    /* DECLARATION OF SOCKET VARIABLES */
    String ip = "188.82.90.18";
    int port_gps = 3000;
    int port_wifi = 3001;
    Thread thread_gps;
    Thread thread_wifi;
    SocketHandler sh_gps;
    SocketHandler sh_wifi;

    /* TIMER STUFF*/
    TimerTask timerTask;
    Timer timer;
    Handler handler;

    /* DATE AND TIME*/
    Date date;
    SimpleDateFormat sdf;
    String dateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            username = "" + bundle.get("username");
        }
        else {
            onDestroy();
        }

        /* FIND IDS */
        btGPS = (Button) findViewById(R.id.btGPS);
        btWiFi = (Button) findViewById(R.id.btWiFi);

        txtGPS = (TextView) findViewById(R.id.txtGPS);
        txtWiFi = (TextView) findViewById(R.id.txtWiFi);

        /* PERMISSIONS */
        permissions.add(INTERNET);
        permissions.add(ACCESS_WIFI_STATE);
        permissions.add(ACCESS_NETWORK_STATE);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(CHANGE_WIFI_STATE);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        /* INITIALIZES SOCKET AND THREAD STUFF */
        sh_gps = new SocketHandler(ip, port_gps);
        sh_wifi = new SocketHandler(ip, port_wifi);
        thread_gps = new Thread(sh_gps);
        thread_wifi = new Thread(sh_wifi);
        thread_gps.start();
        thread_wifi.start();

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
                WiFi();
            }
        });

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
                        WiFi();
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
                ssid = "WiFi - " + username + " - " + dateString + " - " + wifiInfo.getSSID();
                sh_wifi.addItemToSend(ssid);
            }
        }
        else {

            ssid = "" + counterWiFi + " - Not Connected to Wifi";
        }

        txtWiFi.setText(ssid);
    }

    // FUNCTION THAT LOOKS FOR GPS COORDINATES
    public void GPS() {
        counterGPS++;
        String msg = "Clicked in GPS button " + counterGPS;
        msg +=  (counterGPS == 1) ? (" time!") : (" times!");

        locationTrack = new LocationTrack(MainActivity.this);

        if (locationTrack.canGetLocation()) {

            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

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

                String toSend = "Longitude: " + lon + lon_grau + "º " + lon_min + "." + lon_dec + "'\nLatitude: " + lat + lat_grau + "º " + lat_min + "." + lat_dec + "'";
                toSend = "GPS - " + username + " - " + dateString + " - Lat: " + latitude + " - Lon: " + longitude;
                sh_gps.addItemToSend(toSend);
                txtGPS.setText(toSend);
            }
            else{
                sh_gps.addItemToSend("GPS - empty coordinates");
                txtGPS.setText("GPS - empty coordinates");
            }
        } else {
            locationTrack.showSettingsAlert();
        }
    }

    // OTHER FUNCTIONS
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
}