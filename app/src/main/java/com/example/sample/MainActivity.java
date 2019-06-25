package com.example.sample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Bitmap bitmap;
    private ImageView qrCodeImage,horizontalBar,initImage;
    private TextView resultText;
    private View transparentBG;
    private Button saveImageButton;
    private GifImageView showOnSuccess,showOnFailure;

    public static final String ACTION_BAR_TITLE = "action_bar_title";

    private static int secondsToScan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle( getIntent().getStringExtra( ACTION_BAR_TITLE));
        }

        setContentView(R.layout.activity_main);
        qrCodeImage = (ImageView) findViewById( R.id.main_image);
        initImage = (ImageView) findViewById( R.id.main_image_scene_1);
        resultText = (TextView) findViewById( R.id.main_text_results);
        saveImageButton = (Button) findViewById( R.id.main_save_image_button);
        horizontalBar = (ImageView) findViewById(R.id.bar_scan);
        transparentBG = (View) findViewById(R.id.transparent_background);
        showOnSuccess = (GifImageView) findViewById(R.id.gif_view_on_success);
        showOnFailure = (GifImageView) findViewById(R.id.gif_view_on_failure);
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
                    initImage.setImageBitmap( bitmap);

                    initImage.setVisibility(View.VISIBLE);
                    resultText.setVisibility(View.INVISIBLE);
                    saveImageButton.setVisibility(View.INVISIBLE);
                    qrCodeImage.setVisibility(View.INVISIBLE);

                    Display display = getWindowManager().getDefaultDisplay();
                    secondsToScan = (QRCodeUtility.getSecondsFromImageHeight(initImage,display))*3;

                    QRCodeUtility.startBarScannerAnimation( horizontalBar, transparentBG, initImage, display);
                    final FirebaseVisionBarcode barcode = QRCodeUtility.getQRCodeBarcodeFromBitmap(bitmap);
                    new CountDownTimer(secondsToScan, 1000) {

                        public void onTick(long millisUntilFinished) {

                        }

                        public void onFinish() {
                            if( barcode != null) {
                                bitmap = QRCodeUtility.getQRCodeImageFromBitmap(barcode, bitmap);
                                QRCodeUtility.viewsAfterSuccessfulScan( bitmap, QRCodeUtility.getQrCodeValue(barcode), qrCodeImage,
                                        initImage, resultText, saveImageButton);
                                new CountDownTimer(2380, 1000) {

                                    public void onTick(long millisUntilFinished) {
                                        showOnSuccess.setVisibility(View.VISIBLE);
                                        initImage.setVisibility(View.INVISIBLE);
                                        qrCodeImage.setVisibility(View.INVISIBLE);
                                        resultText.setVisibility(View.INVISIBLE);
                                        saveImageButton.setVisibility( View.INVISIBLE);
                                    }

                                    public void onFinish() {
                                        showOnSuccess.setVisibility(View.INVISIBLE);
                                        initImage.setVisibility(View.INVISIBLE);
                                        qrCodeImage.setVisibility(View.VISIBLE);
                                        resultText.setVisibility(View.VISIBLE);
                                        saveImageButton.setVisibility( View.VISIBLE);
                                    }
                                }.start();

                            }else{
                                QRCodeUtility.viewsAfterNoBarcodeScan( bitmap, QRCodeUtility.NO_QRCODE, qrCodeImage,
                                        initImage, resultText, saveImageButton);
                                new CountDownTimer(2380, 1000) {

                                    public void onTick(long millisUntilFinished) {
                                        showOnFailure.setVisibility(View.VISIBLE);
                                        initImage.setVisibility(View.INVISIBLE);
                                        qrCodeImage.setVisibility(View.INVISIBLE);
                                        resultText.setVisibility(View.INVISIBLE);
                                        saveImageButton.setVisibility( View.INVISIBLE);
                                    }

                                    public void onFinish() {
                                        showOnFailure.setVisibility(View.INVISIBLE);
                                        initImage.setVisibility(View.INVISIBLE);
                                        qrCodeImage.setVisibility(View.VISIBLE);
                                        resultText.setVisibility(View.VISIBLE);
                                        saveImageButton.setVisibility( View.INVISIBLE);
                                    }
                                }.start();
                            }
                        }
                    }.start();
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