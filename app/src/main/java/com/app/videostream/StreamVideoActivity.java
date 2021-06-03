package com.app.videostream;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videostream.Database.TopicHelper;
import com.app.videostream.Model.Subject;
import com.app.videostream.Model.Topic;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.potyvideo.library.utils.PathUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class StreamVideoActivity extends AppCompatActivity implements ExoPlayer.EventListener{

    public static Topic TOPIC;
    public static Subject SUBJECT;

    boolean isDownloaded = false;

    SimpleExoPlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;

    RelativeLayout exoParentLayout;
    TextView txtTitle, txtDescription,txtDetail;

    Dialog mFullScreenDialog;

    boolean isFull = false;
    boolean isFirstTime=true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_video);

        initializeViews();


        setValueForViews();

        Log.d("TAGER", "Video is dtreaming from URL");
        TopicHelper topicHelper = new TopicHelper(this);
        topicHelper.getWritableDatabase();
        if (topicHelper.getAllIds().contains(TOPIC.getId())) {
            Log.d("TAGER", "Video streaming from path");
            String path = Environment.getExternalStorageDirectory() + "/Android/data/com.app.videostream/topic/" + TOPIC.getId() + ".mp4";
            streamVideoFromPath(path);
        } else {
            streamVideoFromUrl(TOPIC.getVideoUrl());
        }

    }

    private void initializeViews() {
        exoPlayerView = findViewById(R.id.exoPlayerView);
        exoParentLayout = findViewById(R.id.exoParentLayout);
        txtTitle = findViewById(R.id.txtTitle);
        txtDetail=findViewById(R.id.txtDetail);
        txtDescription = findViewById(R.id.txtDescription);

        startAnimation();
    }

    private void startAnimation(){
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.anim_fade_text);
        txtTitle.startAnimation(animation);
        Animation animation1=AnimationUtils.loadAnimation(this,R.anim.anim_fade_text);
        animation1.setStartOffset(300);
        txtDescription.startAnimation(animation1);
        Animation animation2=AnimationUtils.loadAnimation(this,R.anim.anim_fade_text);
        animation2.setStartOffset(500);
        txtDetail.startAnimation(animation2);
    }

    private void setValueForViews() {

        txtTitle.setText(TOPIC.getTitle());
        txtDescription.setText(TOPIC.getDescription());
        if(SUBJECT!=null){
            txtDetail.setText("Subject : "+SUBJECT.getTitle()+"\n" +
                    "Uploaded On : "+TOPIC.getDate());
        }

        RelativeLayout controlView = exoPlayerView.findViewById(R.id.parent);
        FrameLayout mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        ImageView exoResize=controlView.findViewById(R.id.exo_resize);

        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        final String[] resize = {"fit"};
        exoResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resize[0].equals("fill")){
                    exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    resize[0] ="zoom";
                }
                else if(resize[0].equals("zoom")){
                    exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    resize[0] ="fit";
                }
                else if(resize[0].equals("fit")){
                    exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    resize[0] ="fill";
                }

            }
        });
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFull)
                {
                    closeFullscreenDialog();
                    isFull=false;
                }
                else
                {
                    openFullscreenDialog();
                    isFull=true;
                }
            }
        });

    }

    private void streamVideoFromUrl(String url) {
        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            Uri uri = Uri.parse(url);
            MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);

            exoPlayer.setPlayWhenReady(true);


        } catch (Exception e) {
            Log.d("TAGER", "Exception : " + e.toString());
        }
    }

    private void streamVideoFromPath(String path) {
        File filew = null;
        filew = new File(path);
        Uri uri = Uri.fromFile(filew);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(StreamVideoActivity.this), new DefaultLoadControl());

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
        Log.d("TAGER", "onPause called");
        if(exoPlayer!=null)
            exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAGER", "onResume called");
        if(exoPlayer!=null && isFirstTime){
            exoPlayer.setPlayWhenReady(true);
            isFirstTime=false;
        }
        else {
            exoPlayer.setPlayWhenReady(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(exoPlayer!=null)
            exoPlayer.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(StreamVideoActivity.this);
    }
}
