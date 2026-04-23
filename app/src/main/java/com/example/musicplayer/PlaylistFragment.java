package com.example.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import static com.example.musicplayer.MainActivity.PLAY_LIST_KEY;
import static com.example.musicplayer.MainActivity.TAG;

import java.util.Objects;

public class PlaylistFragment extends ListFragment {

    public interface OnSongSelected{
        void onSongSelected(Playlist playlist, int position);
    }

    private OnSongSelected mlistener;
    private Playlist playlist;

    public PlaylistFragment(Playlist playlist){
        this.playlist = playlist;
    }

    public PlaylistFragment() {
        // to keep the framework happy
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try{
            mlistener = (OnSongSelected) context;
        } catch (ClassCastException e){
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        super.onAttach(context);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        int layout = android.R.layout.simple_list_item_activated_1;
        if (savedInstanceState != null){
            playlist = savedInstanceState.getParcelable(PLAY_LIST_KEY, Playlist.class);
        }
        setListAdapter(new ArrayAdapter<String>(requireActivity(),
                layout, playlist.getTitles()));
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v,
                                int position, long id) {
        // daisy chain the call
        mlistener.onSongSelected(playlist, position);
        // set the selected item highlighted
        getListView().setItemChecked(position, true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(PLAY_LIST_KEY, playlist);
        super.onSaveInstanceState(outState);
    }
}
