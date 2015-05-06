package uname.ws.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.IOException;

@EActivity
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int TAKE_PHOTO = 1;
    private static final String TAG = "MainActivity";

    private ImageView photo;

    @InstanceState File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo = (ImageView) findViewById(R.id.photo);
        findViewById(R.id.screen).setOnClickListener(this);

        Log.d(TAG, "-----------------------------------");
        Log.d(TAG, "onCreate \t\t in activity: " + this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Log.d(TAG, "-----------------------------------");
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: take photo from camera");
        imageFile = Files.newImageFile(this);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @OnActivityResult(TAKE_PHOTO)
    protected void onCameraResult(int result) {
        Log.d(TAG, "onCameraResult \t\t in activity: " + this);
        if (result == Activity.RESULT_OK) {
            processImage(Uri.fromFile(imageFile));
        } else {
            finish();
        }
    }

    @Background
    protected void processImage(Uri imageUri) {
        Log.d(TAG, "processImage \t\t in activity: " + this);
        try {
            Pair<String, File> result = Files.saveCachedImage(this, imageUri);
            File smallImage = Files.saveSmallImage(this, result.second);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            imageArrived(result.second);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @UiThread
    protected void imageArrived(File image) {
        Log.d(TAG, "imageArrived \t\t in activity: " + this);
        // after save
        Toast.makeText(this, "Image processed", Toast.LENGTH_SHORT).show();
        photo.setImageDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeFile(image.getAbsolutePath())));
    }

}
