package com.app.videostream;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class HelpActivity extends AppCompatActivity {

    ImageView imgEmail,imgPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initializeViews();

        imgEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "admin@gmail.com", null));
                startActivity(Intent.createChooser(intent, "Send email..."));
            }
        });

        imgPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TedPermission.with(HelpActivity.this)
                        .setPermissions(Manifest.permission.CALL_PHONE)
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                Intent intent=new Intent(Intent.ACTION_DIAL,Uri.fromParts("tel", "+916546546542", null));
                                startActivity(intent);
                            }

                            @Override
                            public void onPermissionDenied(List<String> deniedPermissions) {

                            }
                        }).check();

            }
        });
    }

    private void initializeViews() {
        imgEmail=findViewById(R.id.imgEmail);
        imgPhone=findViewById(R.id.imgPhone);

        findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Animatoo.animateSlideRight(HelpActivity.this);
    }
}
