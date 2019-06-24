package com.example.sample;

import android.content.Intent;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;

import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.util.List;

public class QRCodeUtility{

    public final static int PICK_IMAGE = 103;

    public static Intent getPickImageIntent(){
        return (new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
    }

    public static Bitmap getBitmapFromGalleryResult( Context context, Intent data){
        Bitmap imageBitmap = null;
        Uri dataUri = data.getData();

        try{
            imageBitmap = MediaStore.Images.Media.getBitmap( context.getContentResolver(), dataUri);
        }catch ( IOException e){
            e.printStackTrace();
        }

        return imageBitmap;
    }

    public static FirebaseVisionBarcode getQRCodeBarcodeFromBitmap( @NonNull Bitmap bitmap){
        FirebaseVisionBarcode qrcode;
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

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage( image);

        while( !result.isComplete());

        if( result.getResult().size() > 0) {
            return result.getResult().get(0);
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

        return Bitmap.createBitmap( bitmap, x1, y1, x2-x1, y2-y1);
    }

    public static String getQrCodeValue( @NonNull FirebaseVisionBarcode barcode){
        return barcode.getRawValue();
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
