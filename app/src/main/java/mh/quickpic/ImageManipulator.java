package mh.quickpic;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by matt on 4/20/17.
 */

public class ImageManipulator {
    private Bitmap imageData;

    public void select(Bitmap input) {
        imageData = input;
    }
    /**
     * Rotates the image bitmap by angle degrees
     * TODO rework width / height calcs for non-90 degree rotations
     */
    public Bitmap rotate(int angle) {
        if (imageData != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap output = Bitmap.createBitmap(imageData, 0, 0, imageData.getWidth(), imageData.getHeight(), matrix, true);
            imageData = output;
            return output;
        } return null;
    }

    /**
     * Crop the image bitmap to the specified bounds.
     * TODO: swap inputs if end < start
     */
    public Bitmap crop(int xStart, int yStart, int xEnd, int yEnd) {
        if (imageData != null) {
            Bitmap output = Bitmap.createBitmap(imageData, xStart, yStart, xEnd - xStart, yEnd - yStart);
            imageData = output;
            return output;
        } return null;
    }

    public Bitmap getImageData() {
        return imageData;
    }
}
