package com.app.videostream.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Model.Subject;
import com.app.videostream.Model.Topic;
import com.app.videostream.Network.Utils;
import com.app.videostream.R;
import com.app.videostream.SubjectActivity;
import com.app.videostream.TopicActivity;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class SubjectListAdapter extends RecyclerView.Adapter<SubjectListAdapter.ViewHolder> {

    Activity context;
    List<Subject> subjectList;

    public SubjectListAdapter(Activity context,List<Subject> subjectList){
        this.context=context;
        this.subjectList=subjectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_layout, null,false);
        Animation animation= AnimationUtils.loadAnimation(context, R.anim.anim_recycler);
        view.startAnimation(animation);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        SubjectActivity.PROGRESS_LAYOUT.setVisibility(View.INVISIBLE);
        holder.txtTitle.setText(subjectList.get(position).getTitle());
        holder.txtDescription.setText(subjectList.get(position).getDescription());
        holder.txtToatal.setText(subjectList.get(position).getTotalVideos()+" Videos");

        Glide.with(context)
                .load(subjectList.get(position).getDisplayImgUrl())
                .into(holder.imgDisplay);

//        Animation animation= AnimationUtils.loadAnimation(context, R.anim.anim_recycler);
//        holder.cardDisplayParent.startAnimation(animation);

        holder.cardDisplayParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopicActivity.SUBJECT=subjectList.get(position);
                Intent intent=new Intent(context, TopicActivity.class);
                context.startActivity(intent);
                Animatoo.animateSlideLeft(context);
            }
        });

        if(Utils.isAdmin){
            holder.cardDisplayParent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDeleteDialog(subjectList.get(position));
                    return false;
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgDisplay;
        TextView txtTitle,txtDescription,txtToatal;
        CardView cardDisplayParent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgDisplay=itemView.findViewById(R.id.imgDisplay);
            txtTitle=itemView.findViewById(R.id.txtTitle);
            txtDescription=itemView.findViewById(R.id.txtDescription);
            cardDisplayParent=itemView.findViewById(R.id.cardDisplayParent);
            txtToatal=itemView.findViewById(R.id.txtTotal);
        }
    }

    public void filterList(List<Subject> subjectList){
        Log.d("TAGER", "Adapter Called filterList()");
        this.subjectList=subjectList;
        notifyDataSetChanged();
    }

    private void showDeleteDialog(final Subject subject){
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
                deleteSubject(subject,dialog);
            }
        });
    }

    private void deleteSubject(Subject subject, final Dialog dialog){
        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();

        final ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        firebaseFirestore.collection("Subject").document(subject.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                dialog.dismiss();
                progressDialog.cancel();
                progressDialog.dismiss();
                Intent intent=new Intent(context, SubjectActivity.class);
                context.startActivity(intent);
                Animatoo.animateFade(context);
                Activity activity=(Activity)context;
                activity.finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
                progressDialog.dismiss();
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }
}
