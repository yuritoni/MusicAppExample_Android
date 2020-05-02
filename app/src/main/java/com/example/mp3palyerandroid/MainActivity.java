package com.example.mp3palyerandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp3palyerandroid.Model.UploadSong;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.xml.datatype.Duration;

public class MainActivity extends AppCompatActivity {

    AppCompatEditText editText;
    TextView textViewImage;
    ProgressBar progressBar;
    Uri audioUri;
    StorageReference storageReference;
    StorageTask storageTask;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (AppCompatEditText) findViewById(R.id.songTitle);
        textViewImage = (TextView) findViewById(R.id.txtViewSongFileSelected);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
       storageReference = FirebaseStorage.getInstance().getReference().child("songs");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("songs");
    }


    public void openAudioFile(View v) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {
            audioUri = data.getData();
            String filename = getFilesName(audioUri);
            textViewImage.setText(filename);

        }
    }

    private String getFilesName(Uri audioUri) {
        String result = null;
        if (Objects.equals(audioUri.getScheme(), "content")) {
            Cursor cursor = getContentResolver().query(audioUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = audioUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    public void uploadAudioFireBase(View v) {
        if (textViewImage.getText().toString().equals("Not File Selected")) {
            Toast.makeText(getApplicationContext(), "Please select an Image", Toast.LENGTH_SHORT).show();
        } else {
            uploadFile();
        }
    }

    private void uploadFile() {

        if (audioUri != null) {
            String durationTxt;
            Toast.makeText(getApplicationContext(), "Uploading Please wait...", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = this.storageReference.child(System.currentTimeMillis() + "." + getFileExtension(audioUri));
            int durationMillis = findSongDuration(audioUri);
            if (durationMillis == 0) {
                durationTxt = "NA";

            }
            durationTxt = getDurationFromMilli(durationMillis);
            final String finalDurationTxt = durationTxt;
            storageTask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadSong uploadSong = new UploadSong(editText.getText().toString(), finalDurationTxt, audioUri.toString());
                            String uplaodId = databaseReference.push().getKey();
                            databaseReference.child(uplaodId).setValue(uploadSong);
                        }
                    });

                }

            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);

                }
            });

        }

    }

    private String getDurationFromMilli(int durationMillis) {

        Date date = new Date(durationMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simpleDateFormat.format(date);
        return myTime;

    }

    private int findSongDuration(Uri audioUri) {
        int timeInMillisec = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Integer.parseInt(time);
            retriever.release();
            return timeInMillisec;


        } catch (Exception c) {
            c.printStackTrace();
            return 0;
        }
    }

    private String getFileExtension(Uri audioUri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audioUri));

    }

    public void openSongActivity(){
        Intent i = new Intent(MainActivity.this,ShowSongsActivity.class);
        startActivity(i);
    }
}
