package mh.quickpic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.width;
import static android.media.ExifInterface.*;
import static android.util.Log.VERBOSE;
import static mh.quickpic.R.attr.height;

/**
 * Name ideas:
 *  - QuickPic
 *  - FlashDrive
 *  - Pic Zip
 */

public class MainActivity extends AppCompatActivity {

    private static final int activityCode = 1148;

    @BindView(R.id.image_holder) protected ImageView imageHolder;

    private Bitmap imageData;

    @Inject DropboxClient dropboxClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ((BaseApplication)getApplication()).getDaggerComponent().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dropboxClient.onResumeConnect();
    }

    /**
     * Open the camera roll and import the selected image on return.
     */
    @OnClick(R.id.select_image_button)
    protected void select() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.setType("image/*");
        startActivityForResult(intent, activityCode);
    }

    /**
     * Recieves the camera roll's result and stores it
     * @param requestCode sorts this
     * @param resultCode
     * @param resultData
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == activityCode && resultData != null) {
            try {
                Uri selectedImage = resultData.getData();
                Glide.with(this)
                        .load(selectedImage)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                imageHolder.setImageBitmap(resource);
                                imageData = resource;
                            }
                        });
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Rotates the image by 90 degrees
     * //TODO think up a way to chose rotation direction
     */
    @OnClick(R.id.rotate_image_button)
    protected void rotate() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        imageData = Bitmap.createBitmap(imageData, 0, 0, imageData.getWidth(), imageData.getHeight(), matrix, true);

        imageHolder.setImageBitmap(imageData);
    }

    /**
     * Save the image to the camera roll
     * //TODO save this online so I can access from my computer directly
     */
    @OnClick(R.id.save_image_button)
    protected void save() {
        dropboxClient.saveBitmap(imageData);
    }
}
