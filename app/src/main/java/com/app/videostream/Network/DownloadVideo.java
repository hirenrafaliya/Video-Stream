package com.app.videostream.Network;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Subject;
import com.app.videostream.Model.Topic;
import com.app.videostream.R;
import com.app.videostream.TopicActivity;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.media.app.NotificationCompat;

public class DownloadVideo extends AsyncTask {

    Context context;
    Subject subject;
    Topic topic;
    TextView txtDownload;

    public FileDownloadTask downloadTask;
    boolean isCanceled=false;

    public DownloadVideo(Context context, Subject subject, Topic topic, TextView txtDownload) {
        this.context = context;
        this.subject = subject;
        this.topic = topic;
        this.txtDownload = txtDownload;
    }


    @SuppressLint("WrongThread")
    @Override
    protected Object doInBackground(Object[] objects) {

        txtDownload.setText("Downloading...");

        final int position=Utils.getPostion(topic.getId());


        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Subject")
                .child(subject.getTitle())
                .child(topic.getId());


        downloadTask = storageReference.child(topic.getId()).getFile(getVideoDownloadPath());


        downloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                Log.d("TAGER", "Video downloaded");

                downloadTask = storageReference.child("imgThumbnail").getFile(getImageDownloadPath());

                downloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        txtDownload.setText("Downloaded");
                        txtDownload.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_primary_grey));
                        Utils.list.remove(Utils.getPostion(topic.getId()));
                        Utils.idList.remove(topic.getId());
                        Log.d("TAGER", "Completed Download");
                        TopicHelper topicHelper=new TopicHelper(context);
                        topicHelper.getWritableDatabase();
                        topicHelper.insert(topic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAGER","Image not downloaded");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Utils.showNotification(context,position,topic.getTitle(),getProgressMB(taskSnapshot)+"/"+getTotalProgressMB(taskSnapshot)+" MB", Integer.parseInt(getProgressPercentage(taskSnapshot)));
                if(isCanceled){
                    txtDownload.setText("Download");
                    Utils.showNotification(context,position,topic.getTitle(),"cancel",0);
                }
                else {
                    txtDownload.setText("Downloading.." + getProgressPercentage(taskSnapshot) + "%");
                    Log.d("TAGER", Integer.toString(getProgress(taskSnapshot)) + "/" + getTotalProgressMB(taskSnapshot));
                }
            }
        });


//        storageReference.child(topic.getId()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//
//                Log.d("TAGER", "Video downoaded");
//                File rootPath = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.app.videostream/", "thumbnail");
//                if(!rootPath.exists()) {
//                    rootPath.mkdirs();
//                }
//                final File localFile = new File(rootPath,topic.getId()+".jpg");
//
//                storageReference.child("imgThumbnail").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                        txtDownload.setText("Downloaded");
//
//                        Utils.idList.remove(topic.getId());
//
//                        Log.d("TAGER", "Completed Download");
//                        TopicHelper topicHelper=new TopicHelper(context);
//                        topicHelper.getWritableDatabase();
//                        topicHelper.insert(topic);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("TAGER", "Failed and not getiing thumbani img");
//                    }
//                });
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Log.e("TAGER","File Failed to download" +exception.toString());
//            }
//        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                txtDownload.setText("Downloading.."+getProgressPercentage(taskSnapshot)+"%");
//                Log.d("TAGER", Integer.toString(getProgress(taskSnapshot))+"/"+Integer.toString(getTotalProgress(taskSnapshot)));
//            }
//        });

        return null;
    }

    private String getProgressPercentage(FileDownloadTask.TaskSnapshot taskSnapshot) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        int i = (int) progress;
        return Integer.toString(i);
    }

    private int getProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        double d = taskSnapshot.getBytesTransferred() / 100000;
        return (int) d;
    }

    private String getProgressMB(FileDownloadTask.TaskSnapshot taskSnapshot){
        double d=taskSnapshot.getBytesTransferred() / 100000;
        double dq=(int)d;
        dq=dq/10;
        return Double.toString(dq);
    }

    private String getTotalProgressMB(FileDownloadTask.TaskSnapshot taskSnapshot) {
        double d=taskSnapshot.getTotalByteCount() / 100000;
        double dq=(int)d;
        dq=dq/10;
        return Double.toString(dq);
    }

    public String getId() {
        return topic.getId();
    }

    public void setTextView(TextView textView) {
        this.txtDownload = textView;
    }


    public void cancelDownload(){
        if(!downloadTask.isComplete()){
            downloadTask.cancel();
            isCanceled=true;
            Log.d("TAGER", "Canecl0");
            txtDownload.setText("Download");
            Utils.list.remove(Utils.getPostion(topic.getId()));
            Utils.idList.remove(topic.getId());
        }
    }

    public File getVideoDownloadPath() {
        File root=new File(Environment.getExternalStorageDirectory() + "/Android/data/","com.app.videostream");
        if(!root.exists()){
            root.mkdir();
        }
        File rootPath = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.app.videostream/", "topic");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath, topic.getId() + ".mp4");
        return localFile;
    }

    public File getImageDownloadPath() {
        File rootPath = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.app.videostream/", "thumbnail");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath, topic.getId() + ".jpg");
        return localFile;
    }


    public Topic getTopic() {
        return topic;
    }

    public TextView getTxtDownload() {
        return txtDownload;
    }
}
