package com.cs160.rymico.spotshot;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends ActionBarActivity {

    public static final int CAMERA_REQUEST = 1888;
    public static final int SEARCH_REQUEST = 1889;
    public static final int NOTIFY_TWEET = 1890;
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "fnjxYnwAUMZPFAPFtjDy9ABuu";
    private static final String TWITTER_SECRET = "2ssmGPs7cVOFFJpPthRJ9X5pmKdlhP3nkT0MYulczeY2sflT0B";

    private GoogleApiClient google;
    private String node;

    private Bitmap image;
    private Button camera;
    private NotificationManager notmanager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TwitterAuthConfig twitterAuthConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TweetComposer());
        setContentView(R.layout.activity_main);

        notmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setOnSnapshotClicked();
        Intent listener = new Intent(this, MobileWearableListener.class);
        startService(listener);
        //sendMessage(node);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setOnSnapshotClicked() {
        camera = (Button) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST){
                image = (Bitmap) data.getExtras().get("data");
                Uri uriSavedImage = getImageUri(getApplicationContext(), image);
                Intent tweeter = new TweetComposer.Builder(this)
                        .text(" #cs160excited")
                        .image(uriSavedImage)
                        .createIntent();
                startActivityForResult(tweeter, SEARCH_REQUEST);

            }
            else if (requestCode == SEARCH_REQUEST){
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                SearchService searchService = twitterApiClient.getSearchService();
                searchService.tweets("#cs160excited", null, null, null, "recent", 100, null, null, null, true, new Callback<Search>() {
                    @Override
                    public void success(Result<Search> result) {
                        String imageUrl = result.data.tweets.get(0).entities.media.get(0).mediaUrl;
                        Bitmap retrieved = getBitmapFromURL(imageUrl);
                        NotificationCompat.Builder notbuilder = new NotificationCompat.Builder(MainActivity.this)
                                .setContentTitle("Get excited!")
                                .setContentText("Someone else took a SpotShot!")
                                .setLargeIcon(retrieved);
                        notmanager.notify(NOTIFY_TWEET, notbuilder.build());

                    }

                    @Override
                    public void failure(TwitterException e) {

                    }
                });
            }
        }


    }


    // Colin Yeoh's elegant Bitmap to URI converter.
    //https://colinyeoh.wordpress.com/2012/05/18/android-getting-image-uri-from-bitmap/
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    // Stack Overflow answer on how to retrieve a Bitmap from a URL
    // http://stackoverflow.com/questions/8992964/android-load-from-url-to-bitmap

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}
