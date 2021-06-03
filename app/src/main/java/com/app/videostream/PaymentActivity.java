package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Model.Subject;
import com.app.videostream.Model.User;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {


    String packageStarted,packageExpired,packageAmount;
    int packageDays=0;
    String name,email,number;
    String planName="";

    String isFreeUsed="";

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ProgressDialog progressDialog;

    EditText edtName,edtEmail,edtNumber;
    RelativeLayout btnPurchase;
    RelativeLayout layoutFree,layoutBasic,layoutStandard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeViews();

        Checkout.preload(this);


        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading....");
        progressDialog.setCancelable(false);

        getUserData();

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=edtName.getText().toString();
                email=edtEmail.getText().toString();
                number=edtNumber.getText().toString();
                String code=number.substring(0,3);




                if(!name.isEmpty() && !email.isEmpty() && !number.isEmpty()){

                    //check for valid number
                    if (number.length() == 10) {
                        number = "+91" + number;
                    } else if (number.length() == 13 && code.equals("+91")) {
                    } else {
                        Toast.makeText(PaymentActivity.this, "Enter valid Phone Number", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if(packageDays==180 || packageDays==360){

                        SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM, yyyy");
                        packageStarted=dateFormat.format(new Date());
                        try {
                            packageExpired=addDaysToDate(packageStarted, packageDays);
                            Log.d("TAGER", "expireDate : "+packageExpired);

                            startPayment();

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                    else if(packageDays==3){

                        SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM, yyyy");
                        packageStarted=dateFormat.format(new Date());
                        try {
                            packageExpired=addDaysToDate(packageStarted, packageDays);
                            Log.d("TAGER", "expireDate : "+packageExpired);

                            String id=sharedPreferences.getString("userId", firebaseAuth.getCurrentUser().getUid());
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("name", name);
                            hashMap.put("number", number);
                            hashMap.put("email",email);
                            hashMap.put("packageAmount","free");
                            hashMap.put("packageStarted", packageStarted);
                            hashMap.put("packageExpire", packageExpired);
                            hashMap.put("isFreeUsed", "true");

                            progressDialog.show();
                            firebaseFirestore.collection("User").document(id).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(PaymentActivity.this, "Free Package Started", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(PaymentActivity.this,SubjectActivity.class);
                                    startActivity(intent);
                                    finishAffinity();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(PaymentActivity.this, "Please select your package", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(PaymentActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }


            }
        });

        layoutFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBackground();
                packageDays=3;
                packageAmount="0";
                layoutFree.setBackground(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.bg_button_primary_blue));
            }
        });

        layoutBasic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBackground();
                packageDays=180;
                packageAmount="49900";
                planName="Rs. 499 / 6 months";
                layoutBasic.setBackground(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.bg_button_primary_blue));
            }
        });

        layoutStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBackground();
                packageDays=360;
                packageAmount="99900";
                planName="Rs. 999 / 1 Year";
                layoutStandard.setBackground(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.bg_button_primary_blue));
            }
        });














    }

    private void resetBackground(){
        layoutFree.setBackground(ContextCompat.getDrawable(this,R.drawable.bg_button_secondary_black));
        layoutBasic.setBackground(ContextCompat.getDrawable(this,R.drawable.bg_button_secondary_black));
        layoutStandard.setBackground(ContextCompat.getDrawable(this,R.drawable.bg_button_secondary_black));
    }

    public void initializeViews(){
        edtEmail=findViewById(R.id.edtEmail);
        edtName=findViewById(R.id.edtName);
        edtNumber=findViewById(R.id.edtNumber);
        btnPurchase=findViewById(R.id.btnPurchase);
        layoutBasic=findViewById(R.id.layoutBasic);
        layoutFree=findViewById(R.id.layoutFree);
        layoutStandard=findViewById(R.id.layoutStandard);

        layoutFree.setEnabled(false);

        sharedPreferences=getSharedPreferences("UserInfo", 0);
        editor=sharedPreferences.edit();

        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();


        findViewById(R.id.imgHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PaymentActivity.this,HelpActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(PaymentActivity.this);
            }
        });
    }

    private void getUserData(){
        progressDialog.show();
        String id=sharedPreferences.getString("userId", firebaseAuth.getCurrentUser().getUid());
        firebaseFirestore.collection("User").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name=documentSnapshot.getString("name");
                email=documentSnapshot.getString("email");
                number=documentSnapshot.getString("number");
                isFreeUsed=documentSnapshot.getString("isFreeUsed");

                if(!number.equals("")){
                    edtNumber.setText(number);
                    edtNumber.setEnabled(false);
                }
                if(!email.equals("")){
                    edtEmail.setText(email);
                    edtEmail.setEnabled(false);
                }
                if(!name.equals("")){
                    edtName.setText(name);
                }

                if(isFreeUsed.equals("true")){
                    layoutFree.setAlpha((float) 0.6);
                }
                else {
                    layoutFree.setEnabled(true);
                }

                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                //add refresh dialog here
            }
        });
    }

    private String addDaysToDate(String date,int days) throws ParseException {
        SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM, yyyy");
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(dateFormat.parse(date));
        calendar.add(Calendar.DATE, days);
        String dt=dateFormat.format(calendar.getTime());
        return dt;
    }


    public void startPayment() {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = this;

        final Checkout co = new Checkout();

        try {
            JSONObject options = new JSONObject();
            options.put("name", "AD Video Stream");
            options.put("description", planName);
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("amount",packageAmount);

            JSONObject preFill = new JSONObject();
            preFill.put("email", email);
            preFill.put("contact", number);

            options.put("prefill", preFill);

            co.open(activity, options);
        } catch (Exception e) {
            Log.d("TAGER","exception : "+e.toString());
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(final String s) {
        Toast.makeText(this, "Payment Success", Toast.LENGTH_LONG).show();
        Log.d("TAGER", "Payment Success : "+s.toString());

        progressDialog.show();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("packageAmount",packageAmount.substring(0,3));
        hashMap.put("packageStarted", packageStarted);
        hashMap.put("packageExpire", packageExpired);

        String id=sharedPreferences.getString("userId", firebaseAuth.getCurrentUser().getUid());

        firebaseFirestore.collection("User").document(id).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                com.app.videostream.Model.User user =new com.app.videostream.Model.User("",name,email,number,packageAmount,packageStarted,packageExpired,"0",s);
                showReceiptDialog(PaymentActivity.this, user, s);
                Toast.makeText(PaymentActivity.this, "Successfully Purchased", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("TAGER", "Something went wrong"+e.toString());
                com.app.videostream.Model.User user =new com.app.videostream.Model.User("",name,email,number,packageAmount,packageStarted,packageExpired,"1",s);
                showReceiptDialog(PaymentActivity.this, user, s);
                Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                //add here for issue
            }
        });
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed "+s.toString(), Toast.LENGTH_LONG).show();
        Log.d("TAGER", "Payment Failed : "+s);
    }

    private String generateId(){
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String id=currentDate+"_"+currentTime;
        return id;
    }

    private int calculateDiffrence(Date date1,Date date2){
        long diff=date1.getTime()-date2.getTime();
        int days=(int)(diff/(1000*60*60*24));
        return days;
    }

    private void showReceiptDialog(Context context, User user, String paymentId){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_receipt);
        dialog.setCancelable(false);

        TextView txtId,txtName,txtEmail,txtNumber,txtPlan,txtAmount,txtStarted,txtExpire;

        Button btnContinue=dialog.findViewById(R.id.btnDialogContinue);
        txtName=dialog.findViewById(R.id.txtDialogName);
        txtEmail=dialog.findViewById(R.id.txtDialogEmail);
        txtNumber=dialog.findViewById(R.id.txtDialogNumber);
        txtPlan=dialog.findViewById(R.id.txtDialogPlan);
        txtAmount=dialog.findViewById(R.id.txtDialogAmount);
        txtStarted=dialog.findViewById(R.id.txtDialogStarted);
        txtId=dialog.findViewById(R.id.txtDialogId);
        txtExpire=dialog.findViewById(R.id.txtDialogExpire);

        txtName.setText("Name : "+user.getName());
        txtEmail.setText("Email : "+user.getEmail());
        txtNumber.setText("Number : "+user.getNumber());
        String amount=user.getPackageAmount().substring(0,3);
        txtAmount.setText("Amount : "+amount);
        txtId.setText("Payment Id : "+paymentId);

        if (amount.equals("499"))
            txtPlan.setText("Package : Basic");
        else if(amount.equals("999"))
            txtPlan.setText("Package : Standard");

        txtStarted.setText("Started On : "+user.getPackageStarted());
        txtExpire.setText("Expires On : "+user.getPackageExpire());

        dialog.show();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PaymentActivity.this, SubjectActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });




    }

}
