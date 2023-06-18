package com.example.widgets;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    private AppWidgetTarget appWidgetTarget;
    public static final String WIDGET_IDS_KEY ="mywidgetproviderwidgetids";
    public static final String WIDGET_DATA_KEY ="mywidgetproviderwidgetdata";

    DatabaseReference reference;
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;
    String msg;
    String image;
    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int[] appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn, pendingIntent);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        views.setOnClickPendingIntent(R.id.appwidget_image, pendingIntent);

        if (shp == null) {
            shp = context.getSharedPreferences("myPreferences", MODE_PRIVATE);
        }
        String userName = shp.getString("name", "");
        String display;

        if (userName.equals("her")){
            display="his";
        }else{
            display = "her";
        }

        reference = FirebaseDatabase.getInstance().getReference("Users").child(display);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msg = "" + dataSnapshot.child("Message").getValue();
                image = ""+ dataSnapshot.child("Image").getValue();

                views.setTextViewText(R.id.appwidget_text, msg);
                appWidgetTarget = new AppWidgetTarget(context, R.id.appwidget_image, views, appWidgetId) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                    }
                };
                RequestOptions options = new RequestOptions().
                        override(500, 500).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_foreground).transform(new CenterCrop(), new RoundedCorners(55));

                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(image)
                        .centerCrop()
                        .apply(options)
                        .into(appWidgetTarget);

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
                Intent updateIntent = new Intent();
                updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                updateIntent.putExtra(NewAppWidget.WIDGET_IDS_KEY, appWidgetId);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                remoteViews.setOnClickPendingIntent(R.id.btn, pendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.appwidget_image, pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId, views);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
            updateAppWidget(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            if (intent.hasExtra(WIDGET_DATA_KEY)) {
                Object data = intent.getExtras().getParcelable(WIDGET_DATA_KEY);
                this.updateAppWidget(context, AppWidgetManager.getInstance(context), ids);
            } else {
                this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            }
        } else super.onReceive(context, intent);
    }

    public static void updateMyWidgets(Context context, Parcelable data) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(context,NewAppWidget.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(NewAppWidget.WIDGET_IDS_KEY, ids);
        updateIntent.putExtra(NewAppWidget.WIDGET_DATA_KEY, data);
        context.sendBroadcast(updateIntent);
    }
}