package com.example.gps_wifi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

public class FirstActivity extends AppCompatActivity {

    /* PERMISSIONS STUFF */
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<String>();
    private ArrayList<String> permissions = new ArrayList<String>();

    private final static int ALL_PERMISSIONS_RESULT = 101;

    /* LAYOUT COMPONENTS */
    Button bt;
    TextInputEditText inputText;
    TextInputEditText passText;

    /* COMMUNICATION */
    Thread thread;
    SocketHandlerSR shSR;
    String ip = "188.82.90.18";
    int port = 3001;

    @Override
    protected void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.first_activity);

        /* FIND IDS */
        bt = (Button) findViewById(R.id.button);
        inputText = (TextInputEditText) findViewById(R.id.txtInputEdit);
        passText = (TextInputEditText) findViewById(R.id.txtPassEdit);

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

        /* BUTTON FUNCTION */
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = "" + inputText.getText();
                String pass = "" + passText.getText();



                if(!(user.equals("") || pass.equals(""))){

                    // Sends message to server. In the message is included the username and password. Server validates if it enters in the app or not
                    String msgToSend = "" + user + " " + pass;
                    String msgToRecv = "";

                    shSR = new SocketHandlerSR(ip, port);
                    thread = new Thread(shSR);
                    thread.start();
                    shSR.addItemToSend(msgToSend);

                    while (msgToRecv == "") {
                        msgToRecv = shSR.receiveData();
                    }

                    Log.i("rcv", msgToRecv);
                    Toast.makeText(getApplicationContext(), msgToRecv, Toast.LENGTH_LONG).show();



                    Toast.makeText(getApplicationContext(), "abcdef", Toast.LENGTH_LONG).show();

                    if (msgToRecv.equals("ok") ) {
                        Toast.makeText(getApplicationContext(), "User ok", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                        intent.putExtra("username", user);
                        intent.putExtra("password", pass);
                        startActivity(intent);
                    }
                    else if (msgToRecv.equals("pass") ) {
                        Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_LONG).show();
                    }
                    else if (msgToRecv.equals("created") ){
                        Toast.makeText(getApplicationContext(), "New user", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                        intent.putExtra("username", user);
                        intent.putExtra("password", pass);
                        startActivity(intent);
                    }
                    else if (msgToRecv.equals("error") ) {
                        Toast.makeText(getApplicationContext(), "Some error has occurred", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "abcd 1", Toast.LENGTH_LONG).show();
                    }

                }
                else {
                    if(user.equals("") && pass.equals("")){
                        Toast.makeText(getApplicationContext(), "Enter username and password", Toast.LENGTH_LONG).show();
                    }
                    else if (user.equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter username", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
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

