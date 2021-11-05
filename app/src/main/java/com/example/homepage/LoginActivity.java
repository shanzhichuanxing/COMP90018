package com.example.homepage;

/**************************************************************************************************************************************************
 * Author: Rishav Kumar, Heramba Panda
 * Date: Jan 30, 2020
 * Title of program/source code: Firebase Demo
 * Code version: v2
 * Type: source code
 * Web address or publisher: https://github.com/rishavk1102/FirebaseDemo/blob/Project/app/src/main/java/com/rishav/firebasedemo/LoginActivity.java
 ***************************************************************************************************************************************************/

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView registerUser;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        registerUser = findViewById(R.id.register_user);

        mAuth = FirebaseAuth.getInstance();

        registerUser.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this , RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)));

        login.setOnClickListener(v -> {
            String txt_email = email.getText().toString();
            String txt_password = password.getText().toString();

            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                Toast.makeText(LoginActivity.this, "Please fill out fields!", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(txt_email , txt_password);
            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
              
                Intent intent = new Intent(LoginActivity.this , MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}
