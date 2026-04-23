package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.PLAY_LIST_INDEX_KEY;
import static com.example.musicplayer.MainActivity.PLAY_LIST_KEY;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerFragment extends Fragment {


    int mCurrentPosition = -1;

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private Timer timer = new Timer();

    private ImageButton playButton, pauseButton, stopButton;
    private SeekBar seekBar;
    private PlayerState currentState = PlayerState.Idle;
    private final String CURR_POSITION_KEY = "currentPosition";
    private final String PLAYER_STATE_KEY = "playerState";
    private final String SONG_INDEX_KEY = "songIndex";
    private final String ARRAY_TAG = "arrayTag";
    private final String SHUFFLE_ARRAY_STATE_KEY = "shuffleArray";

    Random rand = new Random();

    // Parameters supplied by the calling activity
    private int currentIndex = 0;
    private Playlist playList;
    private int[] shuffleArray;

    private enum PlayerState{
        Stopped,
        Prepared,
        Playing,
        Paused,
        Looping,
        Idle
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mediaplayer_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null){
            currentIndex = args.getInt(PLAY_LIST_INDEX_KEY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playList = args.getParcelable(PLAY_LIST_KEY, Playlist.class);
                assert playList != null;
                shuffleArray = new int[playList.size()];
            }
        }
        if (savedInstanceState != null){
            currentState = PlayerState.values()[savedInstanceState.getInt(PLAYER_STATE_KEY)];
            currentIndex = savedInstanceState.getInt(SONG_INDEX_KEY);
            shuffleArray = savedInstanceState.getIntArray(SHUFFLE_ARRAY_STATE_KEY);
            setupControls(view);
            setupMediaPlayer(view);
            setupTimer(view);
            updateCurrentSong(view);

            try{
                mediaPlayer.prepare();
                mediaPlayer.seekTo(savedInstanceState.getInt(CURR_POSITION_KEY));
                if(currentState == PlayerState.Playing){
                    mediaPlayer.start();
                }
            } catch( IOException e){
                throw new RuntimeException(e);
            }

        } else{
            setupControls(view);
            setupMediaPlayer(view);
            updateCurrentSong(view);
            mediaPlayer.prepareAsync();
            setupTimer(view);
        }

        // TODO -- we need to check the saved instance for fragment restarts
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupTimer(View view) {
        final SeekBar seekBar = view.findViewById(R.id.seekBar);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(mediaPlayer != null){

                    try{
                        if(mediaPlayer.isPlaying()){
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    }catch (IllegalStateException e){
                        Log.w(MainActivity.TAG, "Media state is in wrong state!");
                    }
                }
            }
        };
        timer.schedule(task, 50, 100);
    }

    private void setupMediaPlayer(View view) {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(currentState == PlayerState.Playing){
                    mediaPlayer.start();
                } else if(currentState == PlayerState.Idle){
                    currentState = PlayerState.Prepared;
                }
                // TODO -- set max and min of the seekbar
                SeekBar seekBar = view.findViewById(R.id.seekBar);
                seekBar.setMax(mediaPlayer.getDuration());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    seekBar.setMin(0);
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentIndex = ++currentIndex % playList.size();
                updateCurrentSong(view);
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mediaPlayer.start();
            }
        });



    }

    private void updateCurrentSong(View view){
        Song song = playList.getSongs()[shuffleArray[currentIndex]];
        ImageView container = view.findViewById(R.id.imageView);
        Drawable drawable = AppCompatResources.getDrawable(requireContext(),
                song.getPicture());
        container.setImageDrawable(drawable);

        if (currentState == PlayerState.Playing){
            mediaPlayer.reset();
        }

        try (AssetFileDescriptor fd = getResources().openRawResourceFd(song.getData())){
            mediaPlayer.setDataSource(fd);
        }catch (IOException e){
            Toast.makeText(requireContext(), "Error on reading song!", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupControls(View view) {
        ImageButton playButton = view.findViewById(R.id.play_button);
        ImageButton pauseButton = view.findViewById(R.id.pause_button);
        ImageButton stopButton = view.findViewById(R.id.cancel_button);

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        Switch shuffleSwitch = view.findViewById(R.id.shuffleSwitch);


        //Tie callbacks to the button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentState == PlayerState.Prepared
                    || currentState == PlayerState.Paused){
                    mediaPlayer.start();
                    currentState = PlayerState.Playing;
                }
                if (currentState == PlayerState.Idle){
                    Toast.makeText(requireContext(), "Please wait", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentState == PlayerState.Playing){
                    mediaPlayer.pause();
                    currentState = PlayerState.Paused;
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = requireActivity()
                        .getSupportFragmentManager();
                fm.popBackStack();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        shuffleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(ARRAY_TAG, "HELLOOOOOO");
                if(shuffleSwitch.isChecked()){
                    Log.i(ARRAY_TAG, "BYEEEE");
                    int size = playList.size();
                    for(int i = 0; i < size; i++){
                        shuffleArray[i] = i;
                    }
                    for(int i = 0; i < size; i++){
                        int randNum = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                            randNum = rand.nextInt(i, size);
                        }
                        int tempnum = shuffleArray[i];
                        shuffleArray[i] = shuffleArray[randNum];
                        shuffleArray[randNum] = tempnum;
                        if(i == currentIndex){
                            currentIndex = randNum;
                        }
                    }
                    Log.i(ARRAY_TAG, Arrays.toString(shuffleArray));

                }else{
                    Log.i(ARRAY_TAG, "current Index: " + currentIndex + " array value of index " + shuffleArray[currentIndex]);
                    currentIndex = shuffleArray[currentIndex];
                    for(int i = 0; i < shuffleArray.length; i++){
                        shuffleArray[i] = i;

                    }
                    Log.i(ARRAY_TAG, Arrays.toString(shuffleArray));

                }
            }
        });
    }

    @Override
    public void onStop() {
        mCurrentPosition = mediaPlayer.getCurrentPosition();

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PLAYER_STATE_KEY, currentState.ordinal());
        outState.putInt(SONG_INDEX_KEY, currentIndex);
        outState.putInt(CURR_POSITION_KEY, mCurrentPosition);
        outState.putIntArray(SHUFFLE_ARRAY_STATE_KEY, shuffleArray);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        timer.cancel();
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDetach();
    }
}
