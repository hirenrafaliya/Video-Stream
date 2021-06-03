package com.app.videostream.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.videostream.Model.User;
import com.app.videostream.PaymentActivity;
import com.app.videostream.R;
import com.app.videostream.SubjectActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    Activity context;
    List<User> userList;

    public UserListAdapter(Activity context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_layout, null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        if(userList.get(position).getName()!="")
        holder.txtName.setText(userList.get(position).getName());

        if(userList.get(position).getNumber()!="")
        holder.txtNumber.setText(userList.get(position).getNumber());

        if(userList.get(position).getEmail()!="")
        holder.txtEmail.setText(userList.get(position).getEmail());

        String plan=userList.get(position).getPackageAmount();
        if(plan.equals("free"))
            holder.txtPlan.setText("Free Package");
        else if(plan.equals("499"))
            holder.txtPlan.setText("Basic Package");
        else if(plan.equals("999"))
            holder.txtPlan.setText("Standard Package");
        else if(plan.equals(""))
            holder.txtPlan.setText("Package : Not Available");

        int dif=getDiffrence(userList.get(position).getPackageExpire());

        if(dif>=0){
            holder.txtStatus.setText(Integer.toString(dif)+" Days Left");
            holder.txtStatus.setTextColor(Color.GREEN);
        }
        else if(userList.get(position).getPackageExpire().equals("")) {
            holder.txtStatus.setText("Status : Not Available");
        }
        else {
            holder.txtStatus.setText("Package Expired");
            holder.txtStatus.setTextColor(Color.RED);
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserDialog(context, userList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtName,txtEmail,txtNumber,txtPlan,txtStatus;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtEmail=itemView.findViewById(R.id.txtEmail);
            txtName=itemView.findViewById(R.id.txtName);
            txtNumber=itemView.findViewById(R.id.txtNumber);
            parentLayout=itemView.findViewById(R.id.parentLayout);
            txtPlan=itemView.findViewById(R.id.txtPlan);
            txtStatus=itemView.findViewById(R.id.txtStatus);
        }
    }

    private int getDiffrence(String date){
        SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM, yyyy");
        String currentDate=dateFormat.format(new Date());
        int days=-1;
        try {
            Date current=dateFormat.parse(currentDate);
            Date expire=dateFormat.parse(date);

            long diff=expire.getTime()-current.getTime();
            days=(int)(diff/(1000*60*60*24));
        }
        catch (ParseException p){

        }
        return days;
    }

    private void showUserDialog(Context context,User user){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimaryBlack));
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.diaog_user_data);
        dialog.setCancelable(true);

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

        if(!user.getName().equals(""))
            txtName.setText("Name : "+user.getName());

        if(!user.getEmail().equals(""))
            txtEmail.setText("Email : "+user.getEmail());

        if(!user.getNumber().equals(""))
        txtNumber.setText("Number : "+user.getNumber());

        String amount=user.getPackageAmount();
        if(!amount.equals(""))
            txtAmount.setText("Amount : "+amount);

        if(!user.getPaymentId().equals(""))
            txtId.setText("Payment Id : "+user.getPaymentId());

        if (amount.equals("499"))
            txtPlan.setText("Package : Basic");
        else if(amount.equals("999"))
            txtPlan.setText("Package : Standard");
        else
            txtPlan.setText("Package : Free");

        if(!user.getPackageStarted().equals(""))
            txtStarted.setText("Started On : "+user.getPackageStarted());

        if(!user.getPackageExpire().equals(""))
            txtExpire.setText("Expires On : "+user.getPackageExpire());

        dialog.show();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dialog.dismiss();
            }
        });
    }

     public void filterList(List<User> userList){
        this.userList=userList;
        notifyDataSetChanged();
     }
}
