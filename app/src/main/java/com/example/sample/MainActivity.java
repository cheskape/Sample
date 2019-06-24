package com.example.sample;


import com.example.sample.QRCodeUtility;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.FileNotFoundException;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_OK){
            switch ( requestCode){
                case RC_STORAGE_PERMS1:
                    checkStoragePermission( requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    bitmap = QRCodeUtility.getBitmapFromGalleryResult( this, data);
                    FirebaseVisionBarcode barcode = QRCodeUtility.getQRCodeBarcodeFromBitmap( bitmap);
                    if( barcode != null) {
                        mMainImage.setImageBitmap(QRCodeUtility.getQRCodeImageFromBitmap(barcode, bitmap));
                        mResultText.setText(QRCodeUtility.getQrCodeValue(barcode));
                    }else{
                        mMainImage.setImageBitmap( bitmap);
                        mResultText.setText( R.string.not_found);
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
