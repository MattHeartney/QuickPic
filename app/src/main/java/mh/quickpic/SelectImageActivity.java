package mh.quickpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Allows the user to select which photo to upload to Dropbox.
 * Also contains simple editing tools.
 */

public class SelectImageActivity extends AppCompatActivity {

    private static final int activityCode = 1148;

    @BindView(R.id.image_holder) protected ImageView imageHolder;

    @Inject DropboxClient dropboxClient;
    @Inject ImageManipulator imageManipulator;

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
     * @param requestCode ensure the result came from this activity
     * @param resultCode unused
     * @param resultData contains the URI for the requested image
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
                                imageManipulator.select(resource);
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
        imageHolder.setImageBitmap(imageManipulator.rotate(90));
    }

    @OnClick(R.id.crop_image_button)
    protected void crop() {
        imageHolder.setImageBitmap(imageManipulator.crop(
                (int)(imageHolder.getWidth()*.1),
                (int)(imageHolder.getHeight()*.1),
                (int)(imageHolder.getWidth()*.9),
                (int)(imageHolder.getHeight()*.9)));
    }

    /**
     * Save the image to the camera roll
     */
    @OnClick(R.id.save_image_button)
    protected void save() {
        dropboxClient.saveBitmap(imageManipulator.getImageData());
    }
}
