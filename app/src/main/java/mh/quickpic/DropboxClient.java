package mh.quickpic;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.inject.Inject;

/**
 * Created by matt on 4/14/17.
 */

public class DropboxClient {

    private Context context;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    public DropboxClient(Context context) {
        this.context = context;
        AppKeyPair appKeys = new AppKeyPair(
                context.getString(R.string.dropbox_api_key),
                context.getString(R.string.dropbox_api_secret));
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<>(session);
        mDBApi.getSession().startOAuth2Authentication(context);
    }

    public void onResumeConnect() {
        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    public void saveBitmap(Bitmap bitmap) {
        try {
            new SaveImageTask().execute(bitmap);
        } catch (Exception e) {e.printStackTrace();}
    }

    class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
        protected Void doInBackground(Bitmap... urls) {
            try {
                File file = new File(context.getCacheDir(), "test.png");
                file.createNewFile();

                //Convert bitmap to byte array
                Bitmap bitmap = urls[0];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
                FileInputStream inputStream = new FileInputStream(file);
                DropboxAPI.Entry response = mDBApi.putFile("/magnum-opus.png", inputStream,
                        file.length(), null, null);
                Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        protected void onPostExecute(Void result) {
            Toast.makeText(context, "SAVE", Toast.LENGTH_SHORT).show();
        }
    }
}
