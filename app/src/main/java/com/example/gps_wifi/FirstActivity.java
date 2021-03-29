package com.example.gps_wifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class FirstActivity extends AppCompatActivity {

    Button bt;
    TextInputEditText inputText;


    @Override
    protected void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.first_activity);

        bt = (Button) findViewById(R.id.button);
        inputText = (TextInputEditText) findViewById(R.id.txtInputEdit);



        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = "" + inputText.getText();

                if(!user.equals("")){
                    Intent intent = new Intent(FirstActivity.this, MainActivity.class);;
                    Toast.makeText(getApplicationContext(), "User: " + user, Toast.LENGTH_LONG).show();
                    intent.putExtra("username", user);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Enter a valid user", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
