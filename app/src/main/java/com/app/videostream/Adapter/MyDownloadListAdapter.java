package com.app.videostream.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Topic;
import com.app.videostream.MyDownloadActivity;
import com.app.videostream.Network.Utils;
import com.app.videostream.R;
import com.app.videostream.StreamVideoActivity;
import com.app.videostream.TopicActivity;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MyDownloadListAdapter extends RecyclerView.Adapter<MyDownloadListAdapter.ViewHolder> {

    Activity context;
    List<Topic> topicList;

    public MyDownloadListAdapter(Activity context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_layout, null,false);
        Animation animation= AnimationUtils.loadAnimation(context, R.anim.anim_recycler);
        view.startAnimation(animation);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        MyDownloadActivity.progressLayout.setVisibility(View.INVISIBLE);
        holder.txtTitle.setText(topicList.get(position).getTitle());
        holder.txtDescription.setText(topicList.get(position).getDescription());


//        Animation animation=AnimationUtils.loadAnimation(context,R.anim.anim_recycler);
//        holder.cardDisplayParent.startAnimation(animation);
        holder.cardDisplayParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.txtDownload.getText().equals("Downloaded")){
                    StreamVideoActivity.SUBJECT=null;
                    StreamVideoActivity.TOPIC=topicList.get(position);
                    Intent intent=new Intent(context, StreamVideoActivity.class);
                    intent.putExtra("isDownloded", true);
                    context.startActivity(intent);
                    Animatoo.animateSlideLeft(context);
                }

            }
        });

        holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.txtDownload.getText().toString().equals("Downloaded")){
                    showDeleteDialog(topicList.get(position));
                }
                else {
                    showCancelDialog(topicList.get(position));
                }

            }
        });

        loadImageFormPath(holder.imgDisplay, topicList.get(position));

        if(Utils.idList.contains(topicList.get(position).getId())){
            int pos=Utils.getPostion(topicList.get(position).getId());
            Utils.list.get(pos).setTextView(holder.txtDownload);
        }
        else {

        }


    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtTitle,txtDescription;
        ImageView imgDisplay;
        CardView cardDisplayParent;
        RelativeLayout deleteLayout;
        TextView txtDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle=itemView.findViewById(R.id.txtTitle);
            txtDescription=itemView.findViewById(R.id.txtDescription);
            imgDisplay=itemView.findViewById(R.id.imgDisplay);
            cardDisplayParent=itemView.findViewById(R.id.cardDisplayParent);
            deleteLayout=itemView.findViewById(R.id.deleteLayout);
            txtDownload=itemView.findViewById(R.id.txtDownload);
        }
    }

    public void filterList(List<Topic> topicList){
        this.topicList=topicList;
        notifyDataSetChanged();
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
                File file=new File(Environment.getExternalStorageDirectory()+"/Android/data/com.app.videostream/topic/"+topic.getId()+".mp4");
                boolean isDeleted=file.delete();

                if(isDeleted){
                    TopicHelper topicHelper=new TopicHelper(context);
                    topicHelper.getWritableDatabase();
                    topicHelper.delete(topic);
                    Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    dialog.dismiss();
                    Intent intent=new Intent(context,MyDownloadActivity.class);
                    context.startActivity(intent);
                    Animatoo.animateFade(context);
                    context.finish();
                }
                else {
                    dialog.cancel();
                    dialog.dismiss();
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadImageFormPath(ImageView imageView,Topic topic){
        File file=new File(Environment.getExternalStorageDirectory()+"/Android/data/com.app.videostream/thumbnail/"+topic.getId()+".jpg");
        if(file.exists()){
            Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
            Log.d("TAGER", "Image got from path");

        }
    }

    private void showCancelDialog(final Topic topic) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_alert);
        dialog.setCancelable(true);

        final TextView txtDialogTitle = dialog.findViewById(R.id.txtDialogTitle);
        TextView txtDialogDescription = dialog.findViewById(R.id.txtDialogDescription);
        Button btnDialogPositive = dialog.findViewById(R.id.btnDialogPositive);
        Button btnDialogNegative = dialog.findViewById(R.id.btnDialogNegative);

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
                Intent intent=new Intent(context,MyDownloadActivity.class);
                context.startActivity(intent);
                Animatoo.animateFade(context);
                context.finish();
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
}
