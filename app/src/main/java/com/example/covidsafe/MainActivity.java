package com.example.covidsafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


// THIS IS THE HOME PAGE
public class MainActivity extends AppCompatActivity {
    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getStartedButton=findViewById(R.id.startButton);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we go to login activity
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }
}