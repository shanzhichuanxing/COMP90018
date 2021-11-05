package com.example.homepage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InfoActivity extends AppCompatActivity {
    private FloatingActionButton infoBack;
    //TextView info_link = (TextView) findViewById(R.id.info_link);
    private View info_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

       // info_link.setMovementMethod(LinkMovementMethod.getInstance());
        info_link = findViewById(R.id.info_link);

        info_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openURL = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.healthdirect.gov.au/symptom-checker/tool/basic-details"));
                startActivity(openURL);
            }
        });


       infoBack= findViewById(R.id.infoMenuBack);
       infoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this , MapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }


}