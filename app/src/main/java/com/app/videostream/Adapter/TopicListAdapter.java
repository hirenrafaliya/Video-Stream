package com.app.videostream.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Subject;
import com.app.videostream.Model.Topic;
import com.app.videostream.Network.DownloadVideo;
import com.app.videostream.Network.Utils;
import com.app.videostream.R;
import com.app.videostream.StreamVideoActivity;
import com.app.videostream.TopicActivity;
import com.app.videostream.UploadTopicActivity;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TopicListAdapter extends RecyclerView.Adapter<TopicListAdapter.ViewHolder> {

    Activity context;
    List<Topic> topicList;
    Subject subject;
    List<String> downloadedList;

    DownloadVideo downloadVideo;

    public TopicListAdapter(Activity context, List<Topic> topicList, Subject subject, List<String> downloadedList) {
        this.context = context;
        this.topicList = topicList;
        this.subject=subject;
        this.downloadedList=downloadedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_layout, null,false);
        Animation animation= AnimationUtils.loadAnimation(context, R.anim.anim_recycler);
        view.startAnimation(animation);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        TopicActivity.PROGRESS_LAYOUT.setVisibility(View.INVISIBLE);
        Log.d("TAGER", "OnBindViewHolder Called");
        holder.txtTitle.setText(topicList.get(position).getTitle());
        holder.txtDescription.setText(topicList.get(position).getDescription());
        holder.txtDate.setText("Uploaded On : "+topicList.get(position).getDate());
        holder.txtDuration.setText(topicList.get(position).getDuration());

        Glide.with(context)
                .load(topicList.get(position).getThumbnailUrl())
                .into(holder.imgDisplay);

//        Animation animation= AnimationUtils.loadAnimation(context, R.anim.anim_recycler);
//        holder.cardDisplayParent.startAnimation(animation);

        if(Utils.idList.contains(topicList.get(position).getId())){
            int pos=Utils.getPostion(topicList.get(position).getId());
            Utils.list.get(pos).setTextView(holder.txtDownload);
        }



        holder.downloadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(holder.txtDownload.getText().equals("Downloaded")){
                    showDeleteDialog(topicList.get(position),holder.txtDownload);
                }
                else {

                    if (Utils.idList.contains(topicList.get(position).getId())) {
                        showCancelDialog(topicList.get(position));
                    } else {
                        TedPermission.with(context)
                                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                                .setPermissionListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        holder.txtDownload.setText("Downloading");
                                        downloadVideo = new DownloadVideo(context, subject, topicList.get(position), holder.txtDownload);
                                        Utils.list.add(downloadVideo);
                                        Utils.idList.add(topicList.get(position).getId());
                                        downloadVideo.execute();
                                    }

                                    @Override
                                    public void onPermissionDenied(List<String> deniedPermissions) {

                                    }
                                }).check();

                    }
                }
            }
        });

        Log.d("TAGER", topicList.get(position).getId()+".mp4");

        if(Utils.isAdmin){
            holder.cardDisplayParent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDeleteDialog(topicList.get(position));
                    return false;
                }
            });
        }


        if(downloadedList.contains(topicList.get(position).getId()+".mp4")){
            loadImageFormPath(holder.imgDisplay,topicList.get(position));
            Log.d("TAGER", topicList.get(position).getId()+"matched");
            holder.txtDownload.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_primary_grey));
            holder.txtDownload.setText("Downloaded");
            holder.cardDisplayParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StreamVideoActivity.SUBJECT= TopicActivity.SUBJECT;
                    StreamVideoActivity.TOPIC=topicList.get(position);
                    Intent intent=new Intent(context, StreamVideoActivity.class);
                    intent.putExtra("isDownloded", true);
                    context.startActivity(intent);
                    Animatoo.animateSlideLeft(context);
                }
            });
        }
        else {
            holder.cardDisplayParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StreamVideoActivity.SUBJECT= TopicActivity.SUBJECT;
                    StreamVideoActivity.TOPIC=topicList.get(position);
                    Intent intent=new Intent(context, StreamVideoActivity.class);
                    intent.putExtra("isDownloded", false);
                    context.startActivity(intent);
                    Animatoo.animateSlideLeft(context);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtTitle,txtDescription,txtDownload,txtDuration;
        ImageView imgDisplay;
        TextView txtDate;
        CardView cardDisplayParent;
        RelativeLayout downloadLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDate=itemView.findViewById(R.id.txtDate);
            txtTitle=itemView.findViewById(R.id.txtTitle);
            txtDescription=itemView.findViewById(R.id.txtDescription);
            imgDisplay=itemView.findViewById(R.id.imgDisplay);
            cardDisplayParent=itemView.findViewById(R.id.cardDisplayParent);
            downloadLayout=itemView.findViewById(R.id.downloadLayout);
            txtDuration=itemView.findViewById(R.id.txtDuration);
            txtDownload=itemView.findViewById(R.id.txtDownload);
        }
    }

    public void filterList(List<Topic> topicList){
        this.topicList=topicList;
        notifyDataSetChanged();
    }

    private void loadImageFormPath(ImageView imageView,Topic topic){
        File file=new File(Environment.getExternalStorageDirectory()+"/Android/data/com.app.videostream/thumbnail/"+topic.getId()+".jpg");
        if(file.exists()){
            Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
            Log.d("TAGER", "Image got from path");

        }
    }

    private void showCancelDialog(final Topic topic){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_alert);
        dialog.setCancelable(true);

        final TextView txtDialogTitle=dialog.findViewById(R.id.txtDialogTitle);
        TextView txtDialogDescription=dialog.findViewById(R.id.txtDialogDescription);
        Button btnDialogPositive=dialog.findViewById(R.id.btnDialogPositive);
        Button btnDialogNegative=dialog.findViewById(R.id.btnDialogNegative);

        txtDialogTitle.setText("Cancel Download");
        txtDialogDescription.setText("Are you sure you want to cancel ?");
        btnDialogPositive.setText("Yes");
        btnDialogNegative.setText("No");

        dialog.show();

        btnDialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Utils.cancelDownload(topic.getId());
                    dialog.cancel();
                    dialog.dismiss();
            }
        });

        btnDialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                dialog.dismiss();
            }
        });

    }

    private void showDeleteDialog(final Topic topic, final TextView txtDownload){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_alert);
        dialog.setCancelable(true);

        final TextView txtDialogTitle=dialog.findViewById(R.id.txtDialogTitle);
        Button btnDialogPositive=dialog.findViewById(R.id.btnDialogPositive);
        Button btnDialogNegative=dialog.findViewById(R.id.btnDialogNegative);

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
                File file=new File(Environment.getExternalStorageDirectory()+"/Android/data/com.app.videostream/topic/"+topic.getId()+".mp4");
                boolean isDeleted=file.delete();

                if(isDeleted){
                    txtDownload.setText("Download");
                    txtDownload.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_primary_blue));
                    TopicHelper topicHelper=new TopicHelper(context);
                    topicHelper.getWritableDatabase();
                    topicHelper.delete(topic);
                    Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    dialog.dismiss();
                }
                else {
                    dialog.cancel();
                    dialog.dismiss();
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteDialog(final Topic topic){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_alert);
        dialog.setCancelable(true);

        final TextView txtDialogTitle=dialog.findViewById(R.id.txtDialogTitle);
        Button btnDialogPositive=dialog.findViewById(R.id.btnDialogPositive);
        Button btnDialogNegative=dialog.findViewById(R.id.btnDialogNegative);

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
                deleteTopic(topic,dialog);
            }
        });
    }

    private void deleteTopic(final Topic topic, final Dialog dialog){
        final StorageReference storageReference= FirebaseStorage.getInstance().getReference();
        final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();

        final ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        firebaseFirestore.collection("Subject")
                .document(subject.getId())
                .collection("Topic")
                .document(topic.getId())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                storageReference.child("Subject").child(subject.getTitle()).child(topic.getId()).child(topic.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> hashMap=new HashMap<>();
                        int count=getItemCount()-1;
                        hashMap.put("totalVideos", Integer.toString(count));
                        firebaseFirestore.collection("Subject").document(subject.getId()).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"Successfully Deleted", Toast.LENGTH_SHORT).show();
                                dialog.cancel();dialog.dismiss();
                                progressDialog.dismiss();
                                Intent intent=new Intent(context,TopicActivity.class);
                                context.startActivity(intent);
                                Animatoo.animateFade(context);
                                Activity activity=(Activity)context;
                                activity.finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(context,"Successfully Deleted", Toast.LENGTH_SHORT).show();
                                dialog.cancel();dialog.dismiss();
                                Intent intent=new Intent(context,TopicActivity.class);
                                context.startActivity(intent);
                                Animatoo.animateFade(context);
                                Activity activity=(Activity)context;
                                activity.finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Successfully Deleted", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent=new Intent(context,TopicActivity.class);
                        context.startActivity(intent);
                        Animatoo.animateFade(context);
                        Activity activity=(Activity)context;
                        activity.finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
