package com.example.musicplayer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements PlaylistFragment.OnSongSelected {

    public static final String TAG = "CPTR320";
    public static final String PLAY_LIST_KEY = "playlist";
    public static final String PLAY_LIST_INDEX_KEY = "index-key";
    public final String ARG_POSITION = "position";
    public final String PLAYLIST_TITLE_KEY = "playlisttitlekey";
    public String playlistTitle = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if(savedInstanceState != null){
            playlistTitle = savedInstanceState.getString(PLAYLIST_TITLE_KEY);


        } else{
            Playlist playlist = createPlaylist();
            playlistTitle = playlist.getName();
            PlaylistFragment headlinesFragment = new PlaylistFragment(playlist);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, headlinesFragment).commit();
        }
        TextView title = findViewById(R.id.playlistTitle);
        title.setText(playlistTitle);

    }

    private Playlist createPlaylist(){
        Playlist myList = new Playlist("Malachi's Playlist");
        Song s1 = new Song(R.drawable.shawn3, R.raw.bizarre, "Bizzare");
        Song s2 = new Song(R.drawable.natalia2, R.raw.funkymusic, "Funky");
        Song s3 = new Song(R.drawable.newloris, R.raw.pianomusic, "Me bomba");
        Song s4 = new Song(R.drawable.touchamp, R.raw.drums, "drumma boy");


        myList.addSong(s1);
        myList.addSong(s2);
        myList.addSong(s3);
        myList.addSong(s4);



        return myList;
    }

    @Override
    public void onSongSelected(Playlist playlist, int position) {
        MediaPlayerFragment mpFragment = new MediaPlayerFragment();
        //Pass the playlist and the position to play the fragment
        Bundle args = new Bundle();
        args.putInt(PLAY_LIST_INDEX_KEY, position);
        args.putParcelable(PLAY_LIST_KEY, playlist);
        mpFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mpFragment);
        transaction.addToBackStack("mediaplayer");
        transaction.commit();


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(PLAYLIST_TITLE_KEY, playlistTitle);
        super.onSaveInstanceState(outState);
    }
}