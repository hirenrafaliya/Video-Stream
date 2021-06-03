package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Adapter.UserListAdapter;
import com.app.videostream.Model.Topic;
import com.app.videostream.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class UserDataActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView txtStatus;
    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;
    EditText edtSearch;
    ImageView imgFilter;

    List<User> userList;
    List<User> tempList;

    UserListAdapter userListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        initializeViews();

        loadUserData();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterUserList(editable.toString());
            }
        });

        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });


    }

    private void initializeViews(){
        recyclerView=findViewById(R.id.recyclerView);
        progressBar=findViewById(R.id.progressbar);
        txtStatus=findViewById(R.id.txtStatus);
        edtSearch=findViewById(R.id.edtSearch);
        imgFilter=findViewById(R.id.imgFilter);

        firebaseFirestore=FirebaseFirestore.getInstance();
        userList=new ArrayList<>();


        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadUserData(){
        firebaseFirestore.collection("User").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    progressBar.setVisibility(View.INVISIBLE);
                    txtStatus.setVisibility(View.INVISIBLE);
                    List<DocumentSnapshot> snapshotList=queryDocumentSnapshots.getDocuments();

                    for(DocumentSnapshot documentSnapshot:snapshotList){
                        userList.add(documentSnapshot.toObject(User.class));
                    }

                    initializeRecyclerView();
                }
                else {
                    progressBar.setVisibility(View.INVISIBLE);
                    txtStatus.setText("No User Available");
                    //add something here
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                txtStatus.setText("Something went wrong");
                Toast.makeText(UserDataActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeRecyclerView(){
        userListAdapter=new UserListAdapter(UserDataActivity.this, userList);
        recyclerView.setAdapter(userListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void filterUserList(String text){
        if(tempList!=null){

            List<User> userList=new ArrayList<>();

            for(User user :this.tempList){
                if(user.getName().toLowerCase().contains(text.toLowerCase())){
                    userList.add(user);
                }
            }
            userListAdapter.filterList(userList);
        }

    }

    private void showFilterDialog(){
        final Dialog dialog = new Dialog(UserDataActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            dialog.getWindow().setBackgroundDrawable(colorDrawable);
        }
        dialog.setContentView(R.layout.dialog_filter);
        dialog.setCancelable(true);

        TextView txtAll,txtFree,txtBasic,txtStandard;

        txtAll=dialog.findViewById(R.id.txtAll);
        txtBasic=dialog.findViewById(R.id.txtBasic);
        txtStandard=dialog.findViewById(R.id.txtStandard);
        txtFree=dialog.findViewById(R.id.txtFree);

        dialog.show();
        txtAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                filterList("all");
            }
        });
        txtFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                filterList("free");
            }
        });

        txtBasic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                filterList("499");
            }
        });

        txtStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                filterList("999");
            }
        });
    }

    private void filterList(String text){
        if(userList!=null){

            List<User> userList=new ArrayList<>();

            if(text.equals("all")){
                userList=this.userList;
            }
            else {
                for (User user : this.userList) {
                    if (user.getPackageAmount().equals(text)) {
                        userList.add(user);
                    }
                }
            }
            tempList=userList;
            userListAdapter.filterList(userList);

        }
    }
}
