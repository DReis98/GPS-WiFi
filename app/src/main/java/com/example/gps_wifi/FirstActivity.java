package com.example.gps_wifi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class FirstActivity extends AppCompatActivity {

    Button bt;
    TextInputEditText inputText;
    TextInputEditText passText;

    String ip_user = "188.82.90.18";
    int port_user = 3001;

    Socket clientSocket;
    PrintWriter out;
    BufferedReader in;


    @Override
    protected void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.first_activity);

        bt = (Button) findViewById(R.id.button);
        inputText = (TextInputEditText) findViewById(R.id.txtInputEdit);
        passText = (TextInputEditText) findViewById(R.id.txtPassEdit);

        /*try {
            clientSocket = new Socket(ip_user, port_user);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = "" + inputText.getText();
                String pass = "" + passText.getText();

                String msgToSend = "" + user + " " + pass;

                /*String resp = "";
                out.println(msgToSend);
                try {
                    resp = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                if(!user.equals("") || !pass.equals("")){
                    Intent intent = new Intent(FirstActivity.this, MainActivity.class);;
                    Toast.makeText(getApplicationContext(), "User: " + user + ", Pass: " + pass, Toast.LENGTH_LONG).show();
                    intent.putExtra("username", user);
                    intent.putExtra("password", pass);
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "Entered if" + /*resp*/, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Enter a user", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
