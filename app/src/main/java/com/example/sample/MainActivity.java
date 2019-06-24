package com.example.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.IOException;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Bitmap bitmap;
    private ImageView mMainImage;
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainImage = findViewById( R.id.main_image);
        mResultText = findViewById( R.id.main_text_results);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_OK){
            switch ( requestCode){
                case RC_STORAGE_PERMS1:
                    checkStoragePermission( requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath( this, dataUri);

                    if( path == null){
                        bitmap = MyHelper.resizeImage( imageFile, this, dataUri, mMainImage);
                    }else{
                        bitmap = MyHelper.resizeImage( imageFile, path, mMainImage);
                    }
                    if( bitmap != null){

                        FirebaseVisionBarcodeDetectorOptions options =
                                new FirebaseVisionBarcodeDetectorOptions.Builder()
                                        .setBarcodeFormats(
                                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                                FirebaseVisionBarcode.FORMAT_AZTEC)
                                        .build();

                        bitmap = toGrayscale(bitmap);
                        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);//                        try {
//                            image = FirebaseVisionImage.fromFilePath( this, dataUri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                                .getVisionBarcodeDetector( options);
                        mMainImage.setImageBitmap( bitmap);
                        mResultText.setText(R.string.app_name);
                        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                        if( firebaseVisionBarcodes.size() > 0){
                                            mResultText.setText(R.string.found);
                                            for( FirebaseVisionBarcode barcode: firebaseVisionBarcodes){

                                                Point[] corners = barcode.getCornerPoints();

                                                int x1 = corners[0].x < corners[3].x ? corners[0].x : corners[3].x;
                                                int y1 = corners[0].y < corners[1].y? corners[0].y : corners[1].y;
                                                int x2 = corners[1].x > corners[2].x? corners[1].x : corners[2].x;
                                                int y2 = corners[2].y > corners[3].y? corners[2].y : corners[3].y;

                                                mMainImage.setImageBitmap(Bitmap.createBitmap( bitmap, x1, y1, x2-x1 + 2, y2-y1 + 2));
                                            }
                                        }else{
                                            mResultText.setText(R.string.not_found);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });


                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
    }

    public static Bitmap toGrayscale( Bitmap srcImage){

        Bitmap bmpGrayscale = Bitmap.createBitmap( srcImage.getWidth(), srcImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(srcImage, 0, 0, paint);

        return bmpGrayscale;
    }
}
