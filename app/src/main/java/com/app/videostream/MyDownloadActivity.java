package com.app.videostream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.videostream.Adapter.MyDownloadListAdapter;
import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Topic;
import com.app.videostream.Network.DownloadVideo;
import com.app.videostream.Network.Utils;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.ArrayList;
import java.util.List;

public class MyDownloadActivity extends AppCompatActivity {

    List<Topic> topicList;
    TopicHelper topicHelper;
    RecyclerView recyclerView;
    EditText edtSearch;
    ImageView imgBack;
    SwipeRefreshLayout swipeRefreshLayout;
    public static RelativeLayout progressLayout;

    MyDownloadListAdapter myDownloadListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_download);

        topicList=new ArrayList<>();

        recyclerView=findViewById(R.id.recyclerView);
        swipeRefreshLayout=findViewById(R.id.recyclerRefreshLayout);
        edtSearch=findViewById(R.id.edtSearch);
        imgBack=findViewById(R.id.imgBack);
        progressLayout=findViewById(R.id.progressLayout);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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



        List<DownloadVideo> downloadVideoList= Utils.list;

        for(DownloadVideo downloadVideo:downloadVideoList){
            Log.d("TAGER",downloadVideo.getTopic().getTitle());
            topicList.add(downloadVideo.getTopic());
        }

        topicHelper=new TopicHelper(MyDownloadActivity.this);
        topicHelper.getWritableDatabase();

        List<Topic> list=topicHelper.getAllData();
        for (Topic topic:list){
            topicList.add(topic);
        }

        if(topicList.size()==0){
            findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
            TextView txtStatus=findViewById(R.id.txtStatus);
            txtStatus.setText("No Downloads");
        }
        else {
            myDownloadListAdapter=new MyDownloadListAdapter(MyDownloadActivity.this, topicList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(myDownloadListAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                topicList.clear();
                List<DownloadVideo> downloadVideoList= Utils.list;
                for(DownloadVideo downloadVideo:downloadVideoList){
                    Log.d("TAGER",downloadVideo.getTopic().getTitle());
                    topicList.add(downloadVideo.getTopic());
                }
                List<Topic> list=topicHelper.getAllData();
                for (Topic topic:list){
                    topicList.add(topic);
                }
                myDownloadListAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
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
            myDownloadListAdapter.filterList(topicList);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(MyDownloadActivity.this);
    }
}
