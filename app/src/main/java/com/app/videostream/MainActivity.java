package com.app.videostream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CountDownTimer countDownTimer=new CountDownTimer(1500,500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Intent intent=new Intent(MainActivity.this,SubjectActivity.class);
                startActivity(intent);
                Animatoo.animateZoom(MainActivity.this);
                finish();
            }
        }.start();

        Animation animation= AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        findViewById(R.id.imgLogo).startAnimation(animation);

    }
}
