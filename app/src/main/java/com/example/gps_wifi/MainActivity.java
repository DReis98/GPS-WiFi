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

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                WiFi();
            }
        });

        handler = new Handler();

        timer = new Timer();
        timerTask =  new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
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
        timer.cancel();
        timer.purge();
    }

    public void WiFi() {
        counterWiFi++;

        String ssid = "";
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // check if is connected to wifi
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                ssid = "" + counterWiFi + " - " + wifiInfo.getSSID();
            }
        }
        else {

            ssid = "" + counterWiFi + " - Not Connected to Wifi";
        }

        sh.addItemToSend("WiFi - " + ssid);
        txtWiFi.setText(ssid);
    }

    public void GPS() {
        counterGPS++;
        String msg = "Clicked in GPS button " + counterGPS;
        msg +=  (counterGPS == 1) ? (" time!") : (" times!");

        locationTrack = new LocationTrack(MainActivity.this);

        if (locationTrack.canGetLocation()) {

            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

            String lat = latitude < 0 ? "S " : "N ";
            String lon = longitude < 0 ? "W " : "E ";

            latitude = Math.abs(latitude);
            longitude = Math.abs(longitude);
            int lon_grau = (int) Math.floor(longitude);
            int lat_grau = (int) Math.floor(latitude);

            double aux_lon = (longitude - (double) lon_grau) * 60;
            double aux_lat = (latitude - (double) lat_grau) * 60;
            int lon_min = (int) Math.floor(aux_lon);
            int lat_min = (int) Math.floor(aux_lat);

            int lon_dec = (int) Math.floor((aux_lon - lon_min)*10000);
            int lat_dec = (int) Math.floor((aux_lat - lat_min)*10000);

            Toast.makeText(getApplicationContext(), msg + " - inside", Toast.LENGTH_SHORT).show();
            String toSend = "Longitude: " + lon + lon_grau + "º " + lon_min + "." + lon_dec + "'\nLatitude: " + lat + lat_grau + "º " + lat_min + "." + lat_dec + "'";
            sh.addItemToSend("GPS - " + toSend);
            txtGPS.setText(toSend);
        } else {
            locationTrack.showSettingsAlert();
        }
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

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