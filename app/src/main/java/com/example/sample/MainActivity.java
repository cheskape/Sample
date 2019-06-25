package com.example.sample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Bitmap bitmap;
    private ImageView mMainImage,horizontalBar;
    private TextView mResultText,transparentBG;
    private Button mSaveImageButton;




    public static final String ACTION_BAR_TITLE = "action_bar_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle( getIntent().getStringExtra( ACTION_BAR_TITLE));
        }

        setContentView(R.layout.activity_main);
        mMainImage = findViewById( R.id.main_image);
        mResultText = findViewById( R.id.main_text_results);
        mSaveImageButton = findViewById( R.id.main_save_image_button);
        horizontalBar = (ImageView) findViewById(R.id.bar_scan);
        transparentBG = (TextView) findViewById(R.id.transparent_background);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId()){
            case R.id.action_gallery:
                QRCodeUtility.checkStoragePermission( this, this, QRCodeUtility.STORAGE_PERMISSION);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case QRCodeUtility.STORAGE_PERMISSION:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    QRCodeUtility.startPickImageActivity( this);
                }else{
                    QRCodeUtility.warnNeedPermission( this, requestCode, QRCodeUtility.REQUEST_FILES_ACCESS);

                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_OK){
            switch ( requestCode){
                case QRCodeUtility.STORAGE_PERMISSION:
                    QRCodeUtility.checkStoragePermission( MainActivity.this, this, QRCodeUtility.STORAGE_PERMISSION);
                    break;
                case QRCodeUtility.PICK_IMAGE:
                    bitmap = QRCodeUtility.getBitmapFromGalleryActivityResult( this, data);
                    FirebaseVisionBarcode barcode = QRCodeUtility.getQRCodeBarcodeFromBitmap(bitmap);
                    if( barcode != null) {
                        bitmap = QRCodeUtility.getQRCodeImageFromBitmap(barcode, bitmap);
                        mMainImage.setImageBitmap( bitmap);

                        mResultText.setText(QRCodeUtility.getQrCodeValue(barcode));
                        mResultText.setVisibility(View.VISIBLE);
                        mSaveImageButton.setVisibility( View.VISIBLE);
                        QRCodeUtility.startBarScannerAnimation(horizontalBar,transparentBG,mMainImage);
                    }else{
                        mMainImage.setImageBitmap( bitmap);
                        mResultText.setText( QRCodeUtility.NO_QRCODE);
                        mResultText.setVisibility(View.VISIBLE);
                        mSaveImageButton.setVisibility( View.INVISIBLE);
                        QRCodeUtility.startBarScannerAnimation(horizontalBar,transparentBG,mMainImage);
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
    }

    public void saveImage( View v){
        QRCodeUtility.saveBitmapToGallery( MainActivity.this, this, "hellomoney", bitmap);
    }
}
