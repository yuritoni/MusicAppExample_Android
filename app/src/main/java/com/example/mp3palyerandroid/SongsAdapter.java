package com.example.mp3palyerandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp3palyerandroid.Model.UploadSong;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.zip.Inflater;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongAdapterViewHolder> {


    private AdapterView.OnItemClickListener onItemSelectedListener;
    Context context;
    List<UploadSong> arrayListSongs;

    public SongsAdapter(Context context, List<UploadSong> arrayListSongs) {
        this.context = context;
        this.arrayListSongs = arrayListSongs;
    }


    @NonNull
    @Override
    public SongAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.songs_item, parent, false);
        return new SongAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapterViewHolder holder, int position) {

        UploadSong song = arrayListSongs.get(position);
        holder.title.setText(song.getSongTitle());
        holder.duration.setText(song.getSongDuration());
    }


    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }

    public class SongAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, duration;

        public SongAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.song_title);
            duration = (TextView) itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                ((ShowSongsActivity)context).playSongs(arrayListSongs,getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
