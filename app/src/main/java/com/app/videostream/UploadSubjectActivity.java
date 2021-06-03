package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Model.Subject;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadSubjectActivity extends AppCompatActivity {

    boolean isUploading=false;

    String subjectTitle;
    String subjectDescription;
    Uri thumbnailUri;

    TextView txtTitle,txtDescription,txtUpload;
    ImageView imgDisplay;
    RelativeLayout btnUpload;
    ProgressBar uploadProgressBar;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_subject);

        initializeViews();

        imgDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    getThumbnailFromGallery();
                }else {
                    Toast.makeText(UploadSubjectActivity.this, "You cannot change it while Uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    showTitleDialog();
                }else {
                    Toast.makeText(UploadSubjectActivity.this, "You cannot change it while Uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    showDescriptionDialog();
                }else {
                    Toast.makeText(UploadSubjectActivity.this, "You cannot change it while Uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    uploadSubjectToDatabase();
                }
                else {
                    Toast.makeText(UploadSubjectActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            thumbnailUri=result.getUri();
            imgDisplay.setImageURI(thumbnailUri);
        }
    }

    private void initializeViews(){
        btnUpload=findViewById(R.id.btnUpload);
        txtTitle=findViewById(R.id.txtTitle);
        txtDescription=findViewById(R.id.txtDescription);
        imgDisplay=findViewById(R.id.imgDisplay);
        txtUpload=findViewById(R.id.txtUpload);
        uploadProgressBar=findViewById(R.id.uploadProgressBard);

        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
    }

    private void getThumbnailFromGallery() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(UploadSubjectActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UploadSubjectActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(120, 140)
                        .start(UploadSubjectActivity.this);
            }
        }
    }

    private void showTitleDialog(){
        final Dialog dialog = new Dialog(UploadSubjectActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_add_text);
        dialog.setCancelable(true);

        final TextView txtDialogTitle=dialog.findViewById(R.id.txtDialogTitle);
        final EditText txtDialogDescription=dialog.findViewById(R.id.edtDialogDesciption);
        Button btnDialogUpdate=dialog.findViewById(R.id.btnDialogUpdate);
        Button btnDialogCancel=dialog.findViewById(R.id.btnDialogCancel);

        txtDialogTitle.setText("Enter Subject Title");

        if(subjectTitle!=null){
            txtDialogDescription.setText(subjectTitle);
        }
        else {
            txtDialogDescription.setHint("Enter Your Title Here..");
        }

        dialog.show();

        btnDialogUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtDialogDescription.getText()!=null){
                    subjectTitle=txtDialogDescription.getText().toString();
                    txtTitle.setText(subjectTitle);
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(UploadSubjectActivity.this, "Please enter Title", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }

    private void showDescriptionDialog(){
        final Dialog dialog = new Dialog(UploadSubjectActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_add_text);
        dialog.setCancelable(true);

        final TextView txtDialogTitle=dialog.findViewById(R.id.txtDialogTitle);
        final EditText txtDialogDescription=dialog.findViewById(R.id.edtDialogDesciption);
        Button btnDialogUpdate=dialog.findViewById(R.id.btnDialogUpdate);
        Button btnDialogCancel=dialog.findViewById(R.id.btnDialogCancel);

        txtDialogTitle.setText("Set Subject Description");

        if(subjectDescription!=null){
            txtDialogDescription.setText(subjectDescription);
        }
        else {
            txtDialogDescription.setHint("Enter Your Description Here..");
        }

        dialog.show();

        btnDialogUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtDialogDescription.getText()!=null){
                    subjectDescription=txtDialogDescription.getText().toString();
                    txtDescription.setText(subjectDescription);
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(UploadSubjectActivity.this, "Please enter Description", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }

    private void uploadSubjectToDatabase(){
        final String id=generateId();
        uploadProgressBar.setVisibility(View.VISIBLE);


        if(subjectTitle!=null && subjectDescription!=null && thumbnailUri!=null) {
            isUploading=true;
            storageReference.child("Subject").child(subjectTitle).child("imgThumbnail").putFile(thumbnailUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    txtUpload.setText("Please Wait...");
                    String thumbnailUrl = getFileUrl(taskSnapshot);

                    Subject subject = new Subject(id, subjectTitle, subjectDescription, thumbnailUrl,0);

                    firebaseFirestore.collection("Subject").document(id).set(subject).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uploadProgressBar.setVisibility(View.INVISIBLE);
                            isUploading=false;
                            txtUpload.setText("Uploaded");
                            Intent intent=new Intent(UploadSubjectActivity.this,SubjectActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            isUploading=false;
                            Toast.makeText(UploadSubjectActivity.this, "Failed", Toast.LENGTH_LONG).show();
                            uploadProgressBar.setVisibility(View.INVISIBLE);
                            txtUpload.setText("Retry");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadSubjectActivity.this, "Failed", Toast.LENGTH_LONG).show();
                    uploadProgressBar.setVisibility(View.INVISIBLE);
                    txtUpload.setText("Retry");
                    isUploading=false;
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadProgressBar.setVisibility(View.VISIBLE);
                    txtUpload.setText("Uploading "+getProgress(taskSnapshot)+"%");
                }
            });
        }
        else {
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateId(){
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String id=currentDate+"_"+currentTime;
        return id;
    }

    private double getTotalProgress(UploadTask.TaskSnapshot taskSnapshot){
        double d=taskSnapshot.getTotalByteCount()/10000;
        return d;
    }

    private int getProgress(UploadTask.TaskSnapshot taskSnapshot){
        double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
        return (int)progress;
    }

    private String getFileUrl(UploadTask.TaskSnapshot taskSnapshot){
        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
        while(!uri.isComplete());
        Uri postImgURL=uri.getResult();
        return postImgURL.toString();
    }

    @Override
    public void onBackPressed() {
        if(!isUploading) {
            super.onBackPressed();
            Animatoo.animateSlideDown(UploadSubjectActivity.this);
        }
        else {
            Toast.makeText(this, "Please wait until upload finishes", Toast.LENGTH_SHORT).show();
        }
    }
}
