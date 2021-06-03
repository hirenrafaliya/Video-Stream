package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Model.Subject;
import com.app.videostream.Network.Utils;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.Logging;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail,edtPassword;
    RelativeLayout btnLogin;
    TextView txtRegister,txtPhoneNumber,txtLogin,txtForgot;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtEmail.getText().toString().equals("admin@gmail.com") && edtPassword.getText().toString().equals("admin@123")){
                    progressBar.setVisibility(View.VISIBLE);
                    txtLogin.setText("Logging in...");
                    firebaseAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),"admin@123").addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Utils.isAdmin=true;
                            Intent intent=new Intent(LoginActivity.this,SubjectActivity.class);
                            startActivity(intent);
                            finish();
                            Animatoo.animateZoom(LoginActivity.this);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            txtLogin.setText("Login");
                            Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else
                LoginUser();
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(LoginActivity.this);
            }
        });

        txtPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(LoginActivity.this);

            }
        });

        txtForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,ForgotPasswordActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(LoginActivity.this);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimations();
    }

    private void initializeViews(){
        edtEmail=findViewById(R.id.edtEmail);
        edtPassword=findViewById(R.id.edtPassword);
        btnLogin=findViewById(R.id.btnLogin);
        txtRegister=findViewById(R.id.txtRegister);
        txtPhoneNumber=findViewById(R.id.txtPhoneNumber);
        progressBar=findViewById(R.id.progressbar);
        txtLogin=findViewById(R.id.txtLogin);
        firebaseAuth=FirebaseAuth.getInstance();
        txtForgot=findViewById(R.id.txtForgot);

        sharedPreferences=getSharedPreferences("UserInfo", 0);
        editor=sharedPreferences.edit();
    }

    private void startAnimations(){
        Animation animation= AnimationUtils.loadAnimation(this, R.anim.anim_scale_views);
        findViewById(R.id.parentCard).startAnimation(animation);
        animation=AnimationUtils.loadAnimation(this,R.anim.anim_logo_alpha);
        findViewById(R.id.imgLogo).startAnimation(animation);
    }

    private void LoginUser(){
        String email=edtEmail.getText().toString();
        String password=edtPassword.getText().toString();

        if(email.length()>2 && password.length()>2){
            if(password.length()>7){
                txtLogin.setText("Logging in..");
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(firebaseAuth.getCurrentUser().isEmailVerified())
                        {
                            editor.putBoolean("isUserLoggedIn", true);
                            editor.putString("userId", firebaseAuth.getCurrentUser().getUid());
                            editor.apply();
                            editor.commit();
//                            Intent intent=new Intent(LoginActivity.this, SubjectActivity.class);
//                            startActivity(intent);
//                            finish();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            txtLogin.setText("Login");
                            enableViews();
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "Please Verify Your Email", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txtLogin.setText("Login");
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                        firebaseAuth.signOut();
                        Toast.makeText(LoginActivity.this, "Failed to Login", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Password must contain 8 characters", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableViews(){
        edtEmail.setEnabled(false);
        edtPassword.setEnabled(false);
        btnLogin.setEnabled(false);
    }

    private void enableViews(){
        edtEmail.setEnabled(true);
        edtPassword.setEnabled(true);
        btnLogin.setEnabled(true);
    }
}
