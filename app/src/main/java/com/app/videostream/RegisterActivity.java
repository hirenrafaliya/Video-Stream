package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Model.User;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail,edtPassword;
    RelativeLayout btnRegister;
    ProgressBar progressBar;
    TextView txtLogin,txtRegister;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();

        startAnimations();

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    private void registerUser(){
        final String email=edtEmail.getText().toString();
        String password=edtPassword.getText().toString();

        if(email!=null && password!=null){
            if(password.length()>7){
                progressBar.setVisibility(View.VISIBLE);
                disableViews();
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                String id=firebaseAuth.getCurrentUser().getUid();
                                User user=new User(id,"",email,"","","","","","");
                                firebaseFirestore.collection("User").document(id).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        enableViews();
                                        Toast.makeText(RegisterActivity.this, "We have sent a Verification mail on "+email, Toast.LENGTH_LONG).show();
                                        firebaseAuth.signOut();
                                        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        enableViews();
                                        firebaseAuth.signOut();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                enableViews();
                                firebaseAuth.signOut();
                                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        enableViews();
                        Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else { Toast.makeText(this, "Password must contain 9 characters", Toast.LENGTH_SHORT).show(); }
        }
        else { Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show(); }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(RegisterActivity.this);
    }

    private void initializeViews() {
        edtEmail=findViewById(R.id.edtEmail);
        edtPassword=findViewById(R.id.edtPassword);
        btnRegister=findViewById(R.id.btnRegister);
        txtLogin=findViewById(R.id.txtLogin);
        progressBar=findViewById(R.id.progressbar);
        txtRegister=findViewById(R.id.txtRegister);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    private void startAnimations(){
        Animation animation= AnimationUtils.loadAnimation(this, R.anim.anim_scale_views);
        findViewById(R.id.parentCard).startAnimation(animation);
        animation=AnimationUtils.loadAnimation(this,R.anim.anim_logo_alpha);
        findViewById(R.id.imgLogo).startAnimation(animation);
    }

    private void disableViews(){
        edtEmail.setEnabled(false);
        edtPassword.setEnabled(false);
        btnRegister.setEnabled(false);
    }
    private void enableViews(){
        edtEmail.setEnabled(true);
        edtPassword.setEnabled(true);
        btnRegister.setEnabled(true);
    }

}
