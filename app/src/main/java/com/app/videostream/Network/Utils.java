package com.app.videostream.Network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.app.videostream.R;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Utils {

    public static boolean isAdmin=false;

    public static List<DownloadVideo> list = new ArrayList<>();

    public static List<String> idList = new ArrayList<>();

    public static List<String> notificationList = new ArrayList<>();


    public static void cancelDownload(String id) {
        int pos = getPostion(id);
        list.get(pos).cancelDownload();
    }

    public static int getPostion(String id) {
        int postion = -1;
        for (int i = 0; i < id.length(); i++) {
            if (idList.get(i).equals(id)) {
                postion = i;
                break;
            }
        }
        return postion;
    }


    public static void showNotification(Context context ,int notificationId, String title, String message, int progress) {
        int progressMax = 100;

        Log.d("TAGER", "Show notification called...");

        if(title.length()>30) {
            StringBuilder stringBuilder = new StringBuilder(title);
            title = stringBuilder.substring(0, 28) + "...";
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false);

        if(progress==100){
            builder.setContentText("Downloaded")
            .setOngoing(false)
                    .setOnlyAlertOnce(false);
            builder.setProgress(0, 0, false);
        }

        if(message.equals("cancel")){
            builder.setContentText("Download Canceled")
                    .setOngoing(false);
            builder.setProgress(0, 0, false);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String chennalId = "YOUR_CHENNAL_ID";
            NotificationChannel channel = new NotificationChannel(chennalId,"Chennal human redabal Tital",NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(chennalId);
        }
        notificationManager.notify(notificationId,builder.build());
    }
}
