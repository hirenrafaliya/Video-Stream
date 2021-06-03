package com.app.videostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.DeadSystemException;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Adapter.TopicListAdapter;
import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Subject;
import com.app.videostream.Model.Topic;
import com.app.videostream.Network.Utils;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.EnumValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopicActivity extends AppCompatActivity {

    public static Subject SUBJECT;
    public static RelativeLayout PROGRESS_LAYOUT;

    RecyclerView recyclerView;
    List<Topic> topicList;
    FirebaseFirestore firebaseFirestore;
    EditText edtSearch;
    ImageView imgBack;
    TextView txtSubject;
    FloatingActionButton btnUpload;
    SwipeRefreshLayout recyclerRefreshLayout;
    TopicListAdapter topicListAdapter;

    List<String> downloadedList;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        initializeViews();

        loadTopicList();

        getDownloadedVideoList();

        recyclerRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTopicList();
            }
        });

        recyclerRefreshLayout.refreshDrawableState();

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

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(Utils.isAdmin){
            btnUpload.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(true);
            btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToUploadTopic();
                }
            });
        }
    }

    private void initializeViews(){
        recyclerView=findViewById(R.id.recyclerView);
        edtSearch=findViewById(R.id.edtSearch);
        recyclerRefreshLayout=findViewById(R.id.recyclerRefreshLayout);
        txtSubject=findViewById(R.id.txtSubject);
        imgBack=findViewById(R.id.imgBack);
        PROGRESS_LAYOUT=findViewById(R.id.progressLayout);
        btnUpload=findViewById(R.id.btnUpload);
        topicList=new ArrayList<>();
        downloadedList=new ArrayList<>();
        firebaseFirestore=FirebaseFirestore.getInstance();

        txtSubject.setText(SUBJECT.getTitle());
    }

    private void initializeRecyclerView(List<Topic> topicList){
        recyclerView.setHasFixedSize(true);
        topicListAdapter=new TopicListAdapter(TopicActivity.this, topicList,SUBJECT,downloadedList);
        recyclerView.setAdapter(topicListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadTopicList(){
        firebaseFirestore.collection("Subject").document(SUBJECT.getId())
                .collection("Topic").orderBy("id",Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> snapshotList=queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot snapshot:snapshotList){
                        topicList.add(snapshot.toObject(Topic.class));
                    }
                    initializeRecyclerView(topicList);
                }
                else {
                    findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                    TextView txtStatus=findViewById(R.id.txtStatus);
                    txtStatus.setText("No videos available");
                }
            }
        });
    }

    private void refreshTopicList(){

        Log.d("TAGER", "Refreshing...");

        firebaseFirestore.collection("Subject").document(SUBJECT.getId())
                .collection("Topic").orderBy("id", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    topicList.clear();
                    List<DocumentSnapshot> snapshotList=queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot snapshot:snapshotList){
                        topicList.add(snapshot.toObject(Topic.class));
                    }
                    getDownloadedVideoList();
                    if(topicListAdapter!=null) {
                        topicListAdapter.notifyDataSetChanged();
                    }
                    else {
                        initializeRecyclerView(topicList);
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
                Toast.makeText(TopicActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSubjectList(String text)
    {
        if(topicList!=null){
            List<Topic> topicList=new ArrayList<>();

            for(Topic topic:this.topicList){
                if(topic.getTitle().toLowerCase().contains(text.toLowerCase())){
                    topicList.add(topic);
                }
            }
            topicListAdapter.filterList(topicList);
        }

    }

    private void getDownloadedVideos(){
        File rootPath = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.app.videostream/topic");
        searchVideo(rootPath);
    }

    public void getDownloadedVideoList(){
        TopicHelper topicHelper=new TopicHelper(this);
        topicHelper.getWritableDatabase();
        List<Topic> tp=topicHelper.getAllData();
        downloadedList.clear();
        for(Topic topic:tp){
            Log.d("TAGER", "Downloaded video list . add()"+topic.getId());
            downloadedList.add(topic.getId()+".mp4");
        }
    }

    public void searchVideo(File dir) {
        downloadedList.clear();
        String pattern = ".mp4";
        //Get the listfile of that flder
        final File listFile[] = dir.listFiles();

        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                final int x = i;
                if (listFile[i].isDirectory()) {
                } else {
                    if (listFile[i].getName().endsWith(pattern)) {
                        // Do what ever u want, add the path of the video to the list
                        downloadedList.add(listFile[i].getName());
                    }
                }
            }
        }
    }

    public void searchVideoRefresh(File dir) {
        downloadedList.clear();
        String pattern = ".mp4";
        //Get the listfile of that flder
        final File listFile[] = dir.listFiles();

        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                final int x = i;
                if (listFile[i].isDirectory()) {
                } else {
                    if (listFile[i].getName().endsWith(pattern)) {
                        // Do what ever u want, add the path of the video to the list
                        downloadedList.add(listFile[i].getName());
                    }
                }
            }
        }
        if(topicListAdapter!=null) {
            topicListAdapter.notifyDataSetChanged();
        }
        else {
            initializeRecyclerView(topicList);
        }

    }

    private void navigateToUploadTopic(){
        UploadTopicActivity.SUBJECT=SUBJECT;
        Intent intent=new Intent(TopicActivity.this,UploadTopicActivity.class);
        startActivity(intent);
        Animatoo.animateSlideUp(TopicActivity.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(TopicActivity.this);
    }
}
