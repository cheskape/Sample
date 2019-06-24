package com.example.sample;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class QRCodeUtility{

    public final static int STORAGE_PERMISSION = 102;
    public final static int PICK_IMAGE = 103;

    public final static String REQUEST_FILES_ACCESS = "This app needs to access files in your device.";

    public final static String NO_QRCODE = "No QR code found";

    private final static String PHOTO_FOLDER = "/Pictures/";
    private final static String SAVED_TO_NOTICE = "Image saved to: ";
    private final static String FILENAME_PREFIX = "hellomoney_qr_code_";

    public static void warnNeedPermission( final Activity activity, final int requestCode, int message){
        AlertDialog.Builder alert = new AlertDialog.Builder( activity);
        alert.setMessage( message);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData( Uri.parse( "package" + activity.getPackageName()));
                activity.startActivityForResult( intent, requestCode);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setCancelable( false);
        alert.show();
    }

    public static void checkStoragePermission( Activity activity, Context context, int requestCode){
        switch( requestCode){
            case STORAGE_PERMISSION:
                int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if( hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED){
                    startPickImageActivity( activity);
                }else{
                    ActivityCompat.requestPermissions( activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                }
                break;
        }
    }

    public static void startPickImageActivity( Activity activity){
        Intent intent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult( intent, PICK_IMAGE);
    }

    public static Bitmap getBitmapFromGalleryActivityResult( Context context, Intent data){
        Bitmap imageBitmap = null;
        Uri dataUri = data.getData();
        Log.d( QRCodeUtility.class.getSimpleName(), "HELP : " + getPath( context, dataUri));

        try{
            imageBitmap = MediaStore.Images.Media.getBitmap( context.getContentResolver(), dataUri);
        }catch ( IOException e){
            e.printStackTrace();
        }

        return imageBitmap;
    }

    public static String getPath(Context context, Uri uri) {
        String path = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }

    public static FirebaseVisionBarcode getQRCodeBarcodeFromBitmap( @NonNull Bitmap bitmap){
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                    .setBarcodeFormats(
                            FirebaseVisionBarcode.FORMAT_QR_CODE,
                            FirebaseVisionBarcode.FORMAT_AZTEC)
                    .build();

        bitmap = toGrayscale( bitmap);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap( bitmap);

        FirebaseVisionBarcodeDetector detector =
                FirebaseVision.getInstance()
                    .getVisionBarcodeDetector( options);

        Task<List<FirebaseVisionBarcode>> task = detector.detectInImage( image);

        while( !task.isComplete());

        if( task.getResult().size() > 0) {
            return task.getResult().get(0);
        }else{
            return null;
        }
    }

    public static Bitmap getQRCodeImageFromBitmap( @NonNull FirebaseVisionBarcode barcode, @NonNull Bitmap bitmap){
        Point[] corner = barcode.getCornerPoints();

        int x1 = corner[0].x < corner[3].x ? corner[0].x : corner[3].x;
        int y1 = corner[0].y < corner[1].y? corner[0].y : corner[1].y;
        int x2 = corner[1].x > corner[2].x? corner[1].x : corner[2].x;
        int y2 = corner[2].y > corner[3].y? corner[2].y : corner[3].y;

        return Bitmap.createBitmap( bitmap, x1, y1, x2-x1 + 2, y2-y1 + 2);
    }

    public static String getQrCodeValue( @NonNull FirebaseVisionBarcode barcode){
        return barcode.getRawValue();
    }

    public static void saveBitmapToGallery( Activity activity, Context context, String directory, Bitmap bitmap){
        String filename = FILENAME_PREFIX + System.currentTimeMillis() + ".png";

        directory = PHOTO_FOLDER + directory;

        File location = new File( Environment.getExternalStorageDirectory() + directory);

        if( !location.exists()){
            File newDirectory = new File( Environment.getExternalStorageDirectory() + directory);
            newDirectory.mkdirs();
        }

        File newImage = new File( location, filename);

        try {
            FileOutputStream out = new FileOutputStream(newImage);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, newImage.getName().toLowerCase(Locale.US));
        values.put("_data", newImage.getAbsolutePath());
        ContentResolver cr = activity.getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast toast = Toast.makeText( context, SAVED_TO_NOTICE + newImage.getAbsolutePath(), Toast.LENGTH_LONG);
        toast.show();
    }

    public static Bitmap toGrayscale( Bitmap origBitmap){
        Bitmap grayscaleBitmap = Bitmap.createBitmap( origBitmap.getWidth(), origBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas( grayscaleBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(0);
        paint.setColorFilter( new ColorMatrixColorFilter(cm));
        canvas.drawBitmap( origBitmap, 0, 0, paint);

        return grayscaleBitmap;
    }
}
