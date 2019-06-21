package com.example.sample;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

public class BaseActivity extends AppCompatActivity {

    public static final int RC_STORAGE_PERMS1 = 101;
    public static final int RC_SELECT_PICTURE = 103;

    public static final String ACTION_BAR_TITLE = "action_bar_title";

    public File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle( getIntent().getStringExtra( ACTION_BAR_TITLE));
        }
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
                checkStoragePermission( RC_STORAGE_PERMS1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch ( requestCode){
            case RC_STORAGE_PERMS1:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    selectPicture();
                }else{
                    MyHelper.needPermission( this, requestCode, R.string.gallery_permission_request);
                }
                break;
        }
    }

    public void checkStoragePermission( int requestCode){
        switch( requestCode){
            case RC_STORAGE_PERMS1:
                int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if( hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED){
                    selectPicture();
                }else{
                    ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                }
                break;
        }
    }

    private void selectPicture(){
        imageFile = MyHelper.createTempFile( imageFile);

        Intent intent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult( intent, RC_SELECT_PICTURE);
    }
}
