package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Subject;
import com.app.videostream.Model.Topic;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.potyvideo.library.utils.PathUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.ProviderMismatchException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadTopicActivity extends AppCompatActivity {

    public static Subject SUBJECT;
    public static Topic TOPIC;

    Uri thumbnailUri;
    Uri videoUri;
    String videoTitle;
    String videoDescription;
    String uploadDate;
    int totalVideos;
    String videoDuration;

    SimpleExoPlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;

    RelativeLayout exoParentLayout;
    Button btnSelect;
    RelativeLayout btnUpload;
    TextView txtTitle, txtDescription, txtDate, txtUpload, txtDuration;
    ProgressBar uploadProgressBar;
    ImageView imgDisplay;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    UploadTask uploadTask;

    Dialog mFullScreenDialog;

    boolean isCanceled = false;
    boolean isFull = false;
    boolean isActive = true;
    boolean isPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_topic);

        initializeViews();

        initializeExoButtons();

        setCurrentDate();


        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    getVideoFromGallery();
                } else {
                    Toast.makeText(UploadTopicActivity.this, "You cannot change it while uploading", Toast.LENGTH_SHORT).show();
                }

            }
        });

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    showTitleDialog();
                } else {
                    Toast.makeText(UploadTopicActivity.this, "You cannot change it while uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    showDescriptionDialog();
                } else {
                    Toast.makeText(UploadTopicActivity.this, "You cannot change it while uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                    getThumbnailFromGallery();
                } else {
                    Toast.makeText(UploadTopicActivity.this, "You cannot change it while uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoUri != null) {
                    if (thumbnailUri != null) {
                        if (videoTitle != null && videoDescription != null) {
                            if (txtUpload.getText().equals("Upload") || txtUpload.getText().equals("Retry")) {
                                uploadTopicToDatabase();
                            }
                            if (!txtUpload.getText().equals("Upload") && !txtUpload.getText().equals("Please wait...") && !txtUpload.getText().equals("Retry")) {
                                showDeleteDialog();
                            }
                        } else {
                            Toast.makeText(UploadTopicActivity.this, "Please enter video details", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UploadTopicActivity.this, "Please select a thumbnail", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UploadTopicActivity.this, "Please select a video", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            thumbnailUri = result.getUri();
            imgDisplay.setImageURI(thumbnailUri);
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            setVideoIntoExoplayer(videoUri);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(PathUtil.getPath(this, videoUri));
                String dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                videoDuration = convertDuration(dur);
                txtDuration.setText(videoDuration);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertDuration(String duration) {
        duration = duration.substring(0, duration.length() - 3);
        int sec = Integer.parseInt(duration);
        int minute = sec / 60;
        sec = sec % 60;
        String sMin = Integer.toString(minute);
        if (sMin.length() == 1) {
            sMin = "0" + sMin;
        }
        String sSec = Integer.toString(sec);
        if (sSec.length() == 1) {
            sSec = "0" + sSec;
        }

        return sMin + ":" + sSec;

    }

    private void initializeViews() {
        exoPlayerView = findViewById(R.id.exoPlayerView);
        exoParentLayout = findViewById(R.id.exoParentLayout);
        btnSelect = findViewById(R.id.btnSelect);
        txtDate = findViewById(R.id.txtDate);
        btnUpload = findViewById(R.id.btnUpload);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        imgDisplay = findViewById(R.id.imgDisplay);
        txtUpload = findViewById(R.id.txtUpload);
        txtDuration = findViewById(R.id.txtDuration);
        uploadProgressBar = findViewById(R.id.uploadProgressBard);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

    }

    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM, yyyy");
        uploadDate = dateFormat.format(new Date());
        txtDate.setText("Uploaded On  \n" + uploadDate);
    }

    private void getThumbnailFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(UploadTopicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UploadTopicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(120, 140)
                        .start(UploadTopicActivity.this);
            }
        }
    }

    private void getVideoFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(UploadTopicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UploadTopicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, 2);
            }
        }
    }

    private void setVideoIntoExoplayer(Uri dataUri) {
        File file = null;
        try {
            file = new File(PathUtil.getPath(this, dataUri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.fromFile(file);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(UploadTopicActivity.this), new DefaultLoadControl());

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayerView.setPlayer(exoPlayer);
        exoPlayer.prepare(audioSource);
        exoPlayer.setPlayWhenReady(true);
    }

    private void showTitleDialog() {
        final Dialog dialog = new Dialog(UploadTopicActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_add_text);
        dialog.setCancelable(true);

        final TextView txtDialogTitle = dialog.findViewById(R.id.txtDialogTitle);
        final EditText txtDialogDescription = dialog.findViewById(R.id.edtDialogDesciption);
        Button btnDialogUpdate = dialog.findViewById(R.id.btnDialogUpdate);
        Button btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);

        if (videoTitle != null) {
            txtDialogDescription.setText(videoTitle);
        } else {
            txtDialogDescription.setHint("Enter Your Title Here..");
        }

        dialog.show();

        btnDialogUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtDialogDescription.getText() != null) {
                    videoTitle = txtDialogDescription.getText().toString();
                    txtTitle.setText(videoTitle);
                    dialog.dismiss();
                } else {
                    Toast.makeText(UploadTopicActivity.this, "Please enter title", Toast.LENGTH_SHORT).show();
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

    private void showDescriptionDialog() {
        final Dialog dialog = new Dialog(UploadTopicActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_add_text);
        dialog.setCancelable(true);

        final TextView txtDialogTitle = dialog.findViewById(R.id.txtDialogTitle);
        final EditText txtDialogDescription = dialog.findViewById(R.id.edtDialogDesciption);
        Button btnDialogUpdate = dialog.findViewById(R.id.btnDialogUpdate);
        Button btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);

        txtDialogTitle.setText("Set Video Description");

        if (videoDescription != null) {
            txtDialogDescription.setText(videoDescription);
        } else {
            txtDialogDescription.setHint("Enter Your Description Here..");
        }

        dialog.show();

        btnDialogUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtDialogDescription.getText() != null) {
                    videoDescription = txtDialogDescription.getText().toString();
                    txtDescription.setText(videoDescription);
                    dialog.dismiss();
                } else {
                    Toast.makeText(UploadTopicActivity.this, "Please enter Description", Toast.LENGTH_SHORT).show();
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

    private String convertDate(String date) {
        String convertedDate = null;
        DateFormat original = new SimpleDateFormat("d MMM, yyyy");
        DateFormat target = new SimpleDateFormat("d MMM");
        try {
            Date converted = original.parse(date);
            convertedDate = target.format(converted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    private void cancelUploading() {
        if (uploadTask != null && !uploadTask.isComplete()) {
            uploadTask.cancel();
            isCanceled = true;
            txtUpload.setText("Upload");
            uploadProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Upload Canceled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Something went wrong..", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadTopicToDatabase(){
        final String id=generateId();
        uploadProgressBar.setVisibility(View.VISIBLE);


        isCanceled=false;

        firebaseFirestore.collection("Subject").document(SUBJECT.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                totalVideos= (int)documentSnapshot.get("totalVideos");
                totalVideos++;

                uploadTask=storageReference.child("Subject").child(SUBJECT.getTitle()).child(id).child(id).putFile(videoUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        txtUpload.setText("Please wait...");
                        final String videoUrl=getFileUrl(taskSnapshot);
                        storageReference.child("Subject").child(SUBJECT.getTitle()).child(id).child("imgThumbnail").putFile(thumbnailUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String thumbnailUrl=getFileUrl(taskSnapshot);

                                Topic topic=new Topic(id, thumbnailUrl, videoTitle, videoDescription, uploadDate, videoUrl,videoDuration);

                                firebaseFirestore.collection("Subject").document(SUBJECT.getId())
                                        .collection("Topic").document(id).set(topic).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        firebaseFirestore.collection("Subject").document(SUBJECT.getId()).update("totalVideos", totalVideos ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                txtUpload.setText("Uploaded");
                                                uploadProgressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(UploadTopicActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                                if(isActive){
                                                    Intent intent=new Intent(UploadTopicActivity.this,TopicActivity.class);
                                                    startActivity(intent);
                                                    Animatoo.animateFade(UploadTopicActivity.this);
                                                    finish();
                                                }
                                                else {
                                                    isPending=true;
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                uploadProgressBar.setVisibility(View.VISIBLE);
                                                txtUpload.setText("Retry");
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        uploadProgressBar.setVisibility(View.VISIBLE);
                                        txtUpload.setText("Retry");
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                uploadProgressBar.setVisibility(View.VISIBLE);
                                txtUpload.setText("Retry");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadProgressBar.setVisibility(View.VISIBLE);
                        txtUpload.setText("Retry");
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        if(isCanceled) {
                            txtUpload.setText("Upload");
                        }
                        else {
                            showNotification(UploadTopicActivity.this, 11, videoTitle, getProgressMB(taskSnapshot) + "/" + getTotalProgressMB(taskSnapshot) + " MB",getProgress(taskSnapshot));
                            txtUpload.setText("Uploading... " + getProgressMB(taskSnapshot) + "/" + getTotalProgressMB(taskSnapshot) + " MB");
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadProgressBar.setVisibility(View.VISIBLE);
                txtUpload.setText("Retry");
            }
        });


    }

    private int getProgress(UploadTask.TaskSnapshot taskSnapshot) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        return (int) progress;
    }

    private String getProgressMB(UploadTask.TaskSnapshot taskSnapshot) {
        double d = taskSnapshot.getBytesTransferred() / 100000;
        double dq = (int) d;
        dq = dq / 10;
        return Double.toString(dq);
    }

    private String getTotalProgressMB(UploadTask.TaskSnapshot taskSnapshot) {
        double d = taskSnapshot.getTotalByteCount() / 100000;
        double dq = (int) d;
        dq = dq / 10;
        return Double.toString(dq);
    }

    private String generateId() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String id = currentDate + "_" + currentTime;
        return id;
    }

    private String getFileUrl(UploadTask.TaskSnapshot taskSnapshot) {
        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
        while (!uri.isComplete()) ;
        Uri postImgURL = uri.getResult();
        return postImgURL.toString();
    }


    private String increment(String text) {
        int i = Integer.parseInt(text);
        i++;
        String s = Integer.toString(i);

        return s;

    }

    private void showDeleteDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_alert);
        dialog.setCancelable(true);

        final TextView txtDialogTitle = dialog.findViewById(R.id.txtDialogTitle);
        Button btnDialogPositive = dialog.findViewById(R.id.btnDialogPositive);
        Button btnDialogNegative = dialog.findViewById(R.id.btnDialogNegative);

        txtDialogTitle.setText("Cancel Uploading...");
        txtDescription.setText("Are you sure you want to cancel uploading ?");
        btnDialogPositive.setText("Yes");
        btnDialogNegative.setText("No");

        dialog.show();

        btnDialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                dialog.dismiss();
            }
        });

        btnDialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelUploading();
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (uploadTask != null) {
            if (uploadTask.isComplete()) {
                super.onBackPressed();
                Animatoo.animateSlideDown(UploadTopicActivity.this);
            } else {
                Toast.makeText(this, "Please wait until upload finishes", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onBackPressed();
            Animatoo.animateSlideDown(UploadTopicActivity.this);
        }


    }

    void showNotification(Context context, int notificationId, String title, String message, int progress) {
        int progressMax = 100;

        Log.d("TAGER", "Show notification called...");

        if (title.length() > 30) {
            StringBuilder stringBuilder = new StringBuilder(title);
            title = stringBuilder.substring(0, 28) + "...";
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false);

        if (progress == 100) {
            builder.setContentText("Uploaded Successsfully")
                    .setOngoing(false)
                    .setOnlyAlertOnce(false);
            builder.setProgress(0, 0, false);
        }

        if (message.equals("cancel")) {
            builder.setContentText("Upload Canceled")
                    .setOngoing(false);
            builder.setProgress(0, 0, false);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String chennalId = "YOUR_CHENNAL_ID";
            NotificationChannel channel = new NotificationChannel(chennalId, "Chennal human redabal Tital", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(chennalId);
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private void initializeExoButtons() {
        RelativeLayout controlView = exoPlayerView.findViewById(R.id.parent);
        FrameLayout mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        ImageView exoResize = controlView.findViewById(R.id.exo_resize);

        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        final String[] resize = {"fit"};
        exoResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resize[0].equals("fill")) {
                    exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    resize[0] = "zoom";
                } else if (resize[0].equals("zoom")) {
                    exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    resize[0] = "fit";
                } else if (resize[0].equals("fit")) {
                    exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    resize[0] = "fill";
                }

            }
        });
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFull) {
                    closeFullscreenDialog();
                    isFull = false;
                } else {
                    openFullscreenDialog();
                    isFull = true;
                }
            }
        });
    }

    private void openFullscreenDialog() {

        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                closeFullscreenDialog();
                super.onBackPressed();
            }
        };
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((ViewGroup) exoPlayerView.getParent()).removeView(exoPlayerView);
        mFullScreenDialog.addContentView(exoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenDialog.show();
    }


    private void closeFullscreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) exoPlayerView.getParent()).removeView(exoPlayerView);
        ((RelativeLayout) findViewById(R.id.exoParentLayout)).addView(exoPlayerView);
        mFullScreenDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        Log.d("TAGER", "onPause called");
        if (exoPlayer != null)
            exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        Log.d("TAGER", "onResume called");
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
        if (isPending) {
            Intent intent = new Intent(UploadTopicActivity.this, TopicActivity.class);
            startActivity(intent);
            Animatoo.animateFade(UploadTopicActivity.this);
            finish();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null)
            exoPlayer.stop();
    }
}
