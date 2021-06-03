package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    EditText edtName,edtEmail,edtNumber;
    TextView txtStarted,txtExpire,txtPackageName,txtDetails,txtLeft;

    FirebaseFirestore firebaseFirestore;
    ProgressDialog progressDialog;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();

        getUserData();

    }

    private void getUserData() {
        SharedPreferences sharedPreferences=getSharedPreferences("UserInfo", 0);
        String id=sharedPreferences.getString("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        progressDialog.show();
        firebaseFirestore.collection("User").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                progressDialog.dismiss();
                user=documentSnapshot.toObject(User.class);
                setValueToViews(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        edtEmail=findViewById(R.id.edtEmail);
        edtName=findViewById(R.id.edtName);
        edtNumber=findViewById(R.id.edtNumber);
        txtExpire=findViewById(R.id.txtExpire);
        txtStarted=findViewById(R.id.txtStarted);
        txtPackageName=findViewById(R.id.packageName);
        txtDetails=findViewById(R.id.txtDetail);
        txtLeft=findViewById(R.id.txtLeft);

        firebaseFirestore=FirebaseFirestore.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setValueToViews(User user){
        edtEmail.setText(user.getEmail());
        edtNumber.setText(user.getNumber());
        edtName.setText(user.getName());

        if(user.getPackageAmount().equals("free")){
            txtPackageName.setText("Free");
            txtDetails.setText("3 Days Full Access");
        }
        else if(user.getPackageAmount().equals("499")){
            txtPackageName.setText("Basic");
            txtDetails.setText("6 Months Full Access");
        }
        else if(user.getPackageAmount().equals("999")){
            txtPackageName.setText("Standard");
            txtDetails.setText("1 Year Full Access");
        }

        String startDate=user.getPackageStarted();
        String expireDate=user.getPackageExpire();
        txtStarted.setText("Started On : "+startDate);
        txtExpire.setText("Expires On : "+expireDate);

        SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM, yyyy");
        String currentDate=dateFormat.format(new Date());

        try {
            Date expire = dateFormat.parse(expireDate);
            Date current=dateFormat.parse(currentDate);
            txtLeft.setText(calculateDiffrence(expire, current)+" Days Left");
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private String calculateDiffrence(Date date1,Date date2){
        long diff=date1.getTime()-date2.getTime();
        int days=(int)(diff/(1000*60*60*24));
        return Integer.toString(days);
    }
}
