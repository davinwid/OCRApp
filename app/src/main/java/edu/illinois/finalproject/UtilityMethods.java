package edu.illinois.finalproject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Davinwid on 12/13/2017.
 */

class UtilityMethods {

    /**
     * Create a file of the captured image
     *
     * @return file object of the image stored in the device
     * @throws IOException
     * source: https://www.youtube.com/watch?v=Nt5GMaFUvog
     */
    static File createImageFile() throws IOException {
        // uses timestamp to generate a unique filename everytime a file is created
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";

        // sets the image directory to be saved at
        File storageDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // create the actual file in the directory and records the image path
        return File.createTempFile(imageFileName, ".jpg", storageDirectory);
    }

    /**
     * Gets the image bitmap and does the orientation offsetting to get the correct orientation
     * @return  bitmap image that has been offset to the correct orientation
     * source: https://stackoverflow.com/questions/20478765/how-to-get-the
     * -correct-orientation-of-the-image-selected-from-the-default-image
     */
    static Bitmap getCorrectOrientedImage(String imageFilePath) {
        ExifInterface exifInterface = null;

        // creates an exifinterface and matrix object whose job is to rotate to the correct orientation
        Matrix matrix = new Matrix();
        try {
            exifInterface = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the orientation
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        // cases of the orientation value and sets the matrix offsetting
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
        }
        // return the rotated bitmap of the original
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Makes up the OCR recognition functionality
     *
     * @param image image to do the OCR with
     * @return OCR result from the image scanning
     */
    static String texRecognition(Bitmap image, Context context) {
        String ocrResult = "";
        // creates an image text recognizer object
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context.getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            // reads the text from the given bitmap image and creates a string result
            Frame frame = new Frame.Builder().setBitmap(image).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append(" ");
            }
            ocrResult = String.valueOf(stringBuilder);
        }
        return ocrResult;
    }

    /**
     * Makes a string of the real path to the file from the context it is called and the given uri
     * @param context   context it is being called
     * @param uri       uri object whole location needs to be found
     * @return String result of the path uri
     */
    static String getRealPathFromURI(Context context, Uri uri) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    static void copyToClipboard(TextView text, Context context) {
        // creates a button that takes in the OCR result text into the clipboard
        String result = text.getText().toString();
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("OCR Result", result);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context.getApplicationContext(), "Text Copied", Toast.LENGTH_SHORT).show();
    }
}
