package com.example.mp3palyerandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mp3palyerandroid.Model.UploadSong;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowSongsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<UploadSong> mUpload;
    FirebaseStorage mStorage;
    DatabaseReference reference;
    MediaPlayer mediaPlayer;

    SongsAdapter songsAdapter;

    ValueEventListener valueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarShowSongs);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUpload = new ArrayList<>();
        songsAdapter= new SongsAdapter(ShowSongsActivity.this,mUpload);
        recyclerView.setAdapter(songsAdapter);
        reference =  FirebaseDatabase.getInstance().getReference().child("songs");

        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUpload.clear();
                for(DataSnapshot dss:dataSnapshot.getChildren()){
                    UploadSong uploadSong =dss.getValue(UploadSong.class);
                    uploadSong.setmKey(dss.getKey());
                    mUpload.add(uploadSong);

                }
                songsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }


        });





    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(valueEventListener);
    }

    public void playSongs(List<UploadSong> arrayListSongs, int adapterPosition) throws IOException {

        UploadSong uploadSong =arrayListSongs.get(adapterPosition);
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        mediaPlayer =new MediaPlayer();
        mediaPlayer.setDataSource(uploadSong.getSongLink());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        mediaPlayer.prepareAsync();

    }
}
