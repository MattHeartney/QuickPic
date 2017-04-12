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
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import static android.R.attr.width;
import static android.media.ExifInterface.*;
import static mh.quickpic.R.attr.height;

/**
 * Name ideas:
 *  - QuickPic
 *  - FlashDrive
 *  - Pic Zip
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int activityCode = 1148;

    private Button selectButton;
    private Button rotateButton;
    private Button saveButton;
    private ImageView imageHolder;

    //TODO break GoogleAPIClient into a manager class
    private GoogleApiClient client;
    private Bitmap imageData;

    ResultCallback<DriveApi.DriveContentsResult> contentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // Handle error
                        return;
                    }

                    DriveContents data = result.getDriveContents();
                    OutputStream out = null;
                    try {
                        out = data.getOutputStream();
                        imageData.compress(Bitmap.CompressFormat.PNG, 100, out);
                    } catch (Exception e) { e.printStackTrace(); }
                    finally {
                        try {
                            out.close();

                            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                    .setMimeType("image/png").build();
                            IntentSender intentSender = Drive.DriveApi
                                    .newCreateFileActivityBuilder()
                                    .setInitialMetadata(metadataChangeSet)
                                    .setInitialDriveContents(data)
                                    .build(client);
                            try {
                                startIntentSenderForResult(intentSender, 1, null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                                // Handle the exception
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        imageHolder = (ImageView)findViewById(R.id.image_holder);

        generateGoogleAPI();

        generateBottomBar();
    }

    private void generateGoogleAPI() {
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,
                        this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();
        client.connect();
    }

    public void onConnectionFailed(ConnectionResult result) {
        //TODO
        Toast.makeText(this, "Stuff broke m8", Toast.LENGTH_SHORT).show();
    }

    /**
     * Assign bottom bar variables and actions
     * //TODO move to an extra Editor object with customizable actions
     */
    private void generateBottomBar() {
        selectButton = (Button)findViewById(R.id.select_image_button);
        rotateButton = (Button)findViewById(R.id.rotate_image_button);
        saveButton   = (Button)findViewById(R.id.save_image_button);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select();
            }
        });
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    /**
     * Open the camera roll and import the selected image on return.
     */
    private void select() {
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
    private void rotate() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        imageData = Bitmap.createBitmap(imageData, 0, 0, imageData.getWidth(), imageData.getHeight(), matrix, true);

        imageHolder.setImageBitmap(imageData);
    }

    /**
     * Save the image to the camera roll
     * //TODO save this online so I can access from my computer directly
     */
    private void save() {
        Drive.DriveApi.newDriveContents(client)
                .setResultCallback(contentsCallback);
        Toast.makeText(this, "SAVE", Toast.LENGTH_SHORT).show();
    }
}
