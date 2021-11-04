package com.example.homepage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InfoActivity extends AppCompatActivity {
    private FloatingActionButton infoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

       infoBack= findViewById(R.id.infoMenuBack);
       infoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this , MapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }


}