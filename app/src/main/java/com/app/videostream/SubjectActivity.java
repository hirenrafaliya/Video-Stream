package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.security.keystore.StrongBoxUnavailableException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Adapter.SubjectListAdapter;
import com.app.videostream.Model.Subject;
import com.app.videostream.Network.Utils;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.admin.v1beta1.Progress;
import com.google.firestore.v1beta1.StructuredQuery;

import java.net.IDN;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.NavigableMap;

public class SubjectActivity extends AppCompatActivity {


    public static String ID;
    public static String NAME;
    public static RelativeLayout PROGRESS_LAYOUT;

    RecyclerView recyclerView;
    TextView txtWelcome;
    List<Subject> subjectList;
    FloatingActionButton btnUpload;
    FirebaseFirestore firebaseFirestore;
    SwipeRefreshLayout recyclerRefreshLayout;
    SubjectListAdapter subjectListAdapter;
    EditText edtSearch;
    CardView imgProfile;
    ImageView imgOption,imgDownload,imgHelp;

    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        initializeViews();

        Log.d("TAGER","is Admin "+Boolean.toString(Utils.isAdmin));

        if(!Utils.isAdmin){
            checkIfUserLoggedIn();
        }

        loadSubjectList();

        recyclerRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSubjectList();
            }
        });


        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterSubjectList(editable.toString());
            }
        });

        imgOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogOption();
            }
        });
        imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SubjectActivity.this,MyDownloadActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(SubjectActivity.this);
            }
        });

        if(Utils.isAdmin){
            btnUpload.setVisibility(View.VISIBLE);
            btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(SubjectActivity.this,UploadSubjectActivity.class);
                    startActivity(intent);
                    Animatoo.animateSlideUp(SubjectActivity.this);
                }
            });
        }

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isAdmin){
                    Intent intent=new Intent(SubjectActivity.this,UserDataActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(SubjectActivity.this,ProfileActivity.class);
                    startActivity(intent);
                }

            }
        });

        imgHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SubjectActivity.this,HelpActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(SubjectActivity.this);
            }
        });



    }

    private void initializeViews(){
        recyclerView=findViewById(R.id.recyclerView);
        edtSearch=findViewById(R.id.edtSearch);
        imgOption=findViewById(R.id.imgOption);
        recyclerRefreshLayout=findViewById(R.id.recyclerRefreshLayout);
        txtWelcome=findViewById(R.id.txtWelcome);
        imgDownload=findViewById(R.id.imgDownload);
        btnUpload=findViewById(R.id.btnUpload);
        imgProfile=findViewById(R.id.imgProfile);
        PROGRESS_LAYOUT=findViewById(R.id.progressLayout);
        imgHelp=findViewById(R.id.imgHelp);
        subjectList=new ArrayList<>();

        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    private void initializeRecyclerView(List<Subject> subjectList){
        subjectListAdapter=new SubjectListAdapter(SubjectActivity.this, subjectList);
        recyclerView.setAdapter(subjectListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadSubjectList(){
        firebaseFirestore.collection("Subject").orderBy("id",Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> snapshotList=queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot snapshot:snapshotList){
                        subjectList.add(snapshot.toObject(Subject.class));
                    }
                    initializeRecyclerView(subjectList);
                }
                else {
                    findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                    TextView txtStatus=findViewById(R.id.txtStatus);
                    txtStatus.setText("No videos available");
                }
            }
        });
    }

    private void refreshSubjectList(){

        firebaseFirestore.collection("Subject").orderBy("id",Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    subjectList.clear();
                    List<DocumentSnapshot> snapshotList=queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot snapshot:snapshotList){
                        subjectList.add(snapshot.toObject(Subject.class));
                    }
                    if(subjectListAdapter!=null){
                        subjectListAdapter.notifyDataSetChanged();
                    }
                    else {
                        initializeRecyclerView(subjectList);
                    }
                    recyclerRefreshLayout.setRefreshing(false);
                }
                else {
                    findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                    TextView txtStatus=findViewById(R.id.txtStatus);
                    txtStatus.setText("No videos available");
                    recyclerRefreshLayout.setRefreshing(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                recyclerRefreshLayout.setRefreshing(false);
                Toast.makeText(SubjectActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSubjectList(String text)
    {
        Log.d("TAGER", "filterSubjectList() called");
        List<Subject> subjectList=new ArrayList<>();

        for(Subject subject:this.subjectList){
            if(subject.getTitle().toLowerCase().contains(text.toLowerCase())){
                Log.d("TAGER", subject.getTitle());
                subjectList.add(subject);
            }
        }
        if(subjectListAdapter!=null){
            subjectListAdapter.filterList(subjectList);
        }
        else {
            initializeRecyclerView(subjectList);
        }

    }

    private void checkIfUserLoggedIn(){
        sharedPreferences=getSharedPreferences("UserInfo",0);
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        boolean val=sharedPreferences.getBoolean("isUserLoggedIn", false);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        if(val){
            Log.d("TAGER", "User already logged in");
            ID=sharedPreferences.getString("userId", " ");
            boolean firstTime=sharedPreferences.getBoolean("firstTime",true);

            if(firstTime){
                progressDialog.show();
                FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
                firebaseFirestore.collection("User").document(ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getString("packageAmount").equals("")){
                            String name=documentSnapshot.getString("name");
                            if(!name.equals("")){
                                editor.putString("userName", name);
                            }
                            Intent intent=new Intent(SubjectActivity.this,PaymentActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            String expire=documentSnapshot.getString("packageExpire");
                            editor.putString("packageExpire", expire);
                            editor.putString("userName", documentSnapshot.getString("name"));
                            txtWelcome.setText("Welcome "+documentSnapshot.getString("name"));
                            editor.apply();
                            editor.commit();
                            calculatePackage(progressDialog);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        checkIfUserLoggedIn();
                    }
                });
            }
            else {
                NAME=sharedPreferences.getString("userName", "User");
                txtWelcome.setText("Welcome "+NAME);
                calculatePackage(progressDialog);
            }

        }else {
            Intent intent=new Intent(SubjectActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void calculatePackage(ProgressDialog progressDialog){
        sharedPreferences=getSharedPreferences("UserInfo",0);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        String expireDate=sharedPreferences.getString("packageExpire", null);
        if(expireDate!=null){
            SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM, yyyy");
            String currentDate=dateFormat.format(new Date());
            try {
                Date current=dateFormat.parse(currentDate);
                Date expire=dateFormat.parse(expireDate);
                int dif=calculateDiffrence(expire, current);
                if(dif>=0){
                    editor.putBoolean("firstTime",false);
                    editor.apply();editor.commit();
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }
                else {
                    editor.putBoolean("firstTime",true);
                    editor.apply();editor.commit();
                    Intent intent=new Intent(SubjectActivity.this,PaymentActivity.class);
                    startActivity(intent);
                    finish();
                    //Add here a flag for plan expired
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else {

        }
    }

    private int calculateDiffrence(Date date1,Date date2){
        long diff=date1.getTime()-date2.getTime();
        int days=(int)(diff/(1000*60*60*24));
        return days;
    }

    private void showNameDialog(){
        final Dialog dialog = new Dialog(SubjectActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_add_user);
        dialog.setCancelable(false);

        final TextView txtDialogTitle=dialog.findViewById(R.id.txtDialogTitle);
        final EditText txtDialogDescription=dialog.findViewById(R.id.edtDialogDesciption);
        Button btnDialogContinue=dialog.findViewById(R.id.btnDialogContinue);
        Button btnDialogCancel=dialog.findViewById(R.id.btnDialogCancel);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        dialog.show();
        btnDialogCancel.setVisibility(View.INVISIBLE);
        btnDialogCancel.setEnabled(false);

        btnDialogContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name=txtDialogDescription.getText().toString();
                if(name.length()>0){
                    progressDialog.show();
                    firebaseFirestore=FirebaseFirestore.getInstance();
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("name", name);
                    firebaseFirestore.collection("User").document(ID).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putString("userName", name);
                            txtWelcome.setText("Welcome "+name);
                            editor.putBoolean("firstTime",false);
                            editor.apply();
                            editor.commit();
                            progressDialog.cancel();
                            progressDialog.dismiss();
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.cancel();
                            progressDialog.dismiss();
                            Toast.makeText(SubjectActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(SubjectActivity.this, "Please Enter valid name", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void logOutUser(){
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        Utils.isAdmin=false;
        SharedPreferences sharedPreferences=getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.remove("isUserLoggedIn");
        editor.remove("firstTime");
        editor.remove("userName");
        editor.remove("userId");
        editor.apply();
        editor.commit();
        Intent intent=new Intent(SubjectActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialogOption(){
        final Dialog dialog = new Dialog(SubjectActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_option);
        dialog.setCancelable(true);


        TextView txtProfile=dialog.findViewById(R.id.txtProfile);
        TextView txtHelp=dialog.findViewById(R.id.txtHelp);
        TextView txtLogout=dialog.findViewById(R.id.txtLogout);


        dialog.show();


        txtHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SubjectActivity.this,HelpActivity.class);
                startActivity(intent);
            }
        });

        txtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isAdmin!=true)
                {
                    Intent intent=new Intent(SubjectActivity.this,ProfileActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(SubjectActivity.this, "Admin can not access this", Toast.LENGTH_SHORT).show();
                }

            }
        });

        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutUser();
            }
        });

    }


}
