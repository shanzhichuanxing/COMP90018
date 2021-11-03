package com.example.covidsafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// THIS IS THE LOGIN PAGE

public class LoginActivity extends AppCompatActivity {
  private Button loginButton;
  private Button createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton=findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we go to login activity
                //startActivity(new Intent(LoginActivity.this,MainActivity.class));
            }
        });

        createAccount=findViewById(R.id.create_account_button);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we go to login activity
                //startActivity(new Intent(LoginActivity.this,MainActivity.class));
            }
        });



    }


}