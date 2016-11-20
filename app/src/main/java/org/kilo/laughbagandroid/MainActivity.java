package org.kilo.laughbagandroid;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String filepath = Environment.getExternalStorageDirectory().getPath();
    private final String localLaughsFolder = "LaughBag/Local";
    public static ArrayList<String> laughNames;

    private File localLaughsPath;

    private MediaRecorder recorder = null;
    private final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private int currentFormat = 0;
    private int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP};

    private ArrayList<MediaPlayer> mediaPlayers;

    private LinearLayout linearLayoutNeededForScrollView;

    public final int REQUEST_CODE = 1;
    public final int REQUEST_CODE_DELETE = 2;
    private static String currentLaughNameForRename;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_main);
        mediaPlayers = new ArrayList<>();
        laughNames = new ArrayList<>();
        linearLayoutNeededForScrollView = (LinearLayout) findViewById(R.id.linearLayout);

        try {
            updateLocalLaughs();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        ImageButton recordButton = (ImageButton) findViewById(R.id.imageButton);
        localLaughsPath = new File(filepath, localLaughsFolder);


        if (recordButton != null) {
            recordButton.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startRecording();
                            break;
                        case MotionEvent.ACTION_UP:
                            stopRecording();
                            try {
                                Thread.sleep(100);
                                updateLocalLaughs();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                    return false;
                }
            });
        }
    }


    private void stopLaughsPlaying() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            mediaPlayer.release();
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            recorder.release();
            recorder.stop();
            stopLaughsPlaying();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    // LOADING LAUGHS BLOCK

    private void updateLocalLaughs() {
        linearLayoutNeededForScrollView.removeAllViews();
        ArrayList<String> localLaughs = getLocalLaughs(new File(filepath, localLaughsFolder));


        for (final String laughFile : localLaughs) {

            // Taking the name of the laugh without extension and full path

            String[] laughFileArraySlash = laughFile.split("/");
            int lastIndex = laughFileArraySlash.length - 1;
            String[] laughFileArrayDot = laughFileArraySlash[lastIndex].split("\\.");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < (laughFileArrayDot.length - 1); i++) {
                stringBuilder.append(laughFileArrayDot[i]);
            }
            final String laughName = stringBuilder.toString();

            laughNames.add(laughName);

            // end of taking the name

            final LinearLayout linearLayoutForLaughName = new LinearLayout(this);

            TextView laughNameTextView = new TextView(this);
            laughNameTextView.setText(laughName);
            laughNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            laughNameTextView.setTextColor(Color.parseColor("#FFFFFF"));


            linearLayoutForLaughName.addView(laughNameTextView);



            final LinearLayout linearLayout = new LinearLayout(this);

            final Button playPauseButton = new Button(this);
            LinearLayout.LayoutParams layoutParamsPlayPauseButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            layoutParamsPlayPauseButton.weight = 0.4f;



            playPauseButton.setText(R.string.play);
            playPauseButton.setBackgroundResource(R.drawable.unpressed_play_button);
            final MediaPlayer mediaPlayer = new MediaPlayer();
            playPauseButton.setOnClickListener(new View.OnClickListener() {

                boolean isBtnClicked = true;

                @Override
                public void onClick(View v) {
                    if (isBtnClicked) {
                        mediaPlayers.add(mediaPlayer);
                        try {
                            playPauseButton.setBackgroundResource(R.drawable.pressed_play_button);
                            playPauseButton.setText(R.string.pause);
                            mediaPlayer.setDataSource(laughFile);
                            mediaPlayer.setLooping(true);
                            mediaPlayer.prepare();
                            mediaPlayer.start();

                        } catch (IOException | IllegalStateException e) {
                            e.printStackTrace();
                        }

                        isBtnClicked = false;
                    } else {
                        playPauseButton.setBackgroundResource(R.drawable.unpressed_play_button);
                        playPauseButton.setText(R.string.play);
                        try {
                            mediaPlayer.reset();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        isBtnClicked = true;
                    }
                }
            });

            final Button deleteButton = new Button(this);
            LinearLayout.LayoutParams layoutParamsDeleteButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            layoutParamsDeleteButton.weight = 0.6f;
            deleteButton.setText("X");

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.reset();
                    Intent intent = new Intent(getBaseContext(), PopUpActivity.class);
                    intent.putExtra("laughPath", laughFile);
                    startActivityForResult(intent, REQUEST_CODE_DELETE);
                }
            });

            final Button renameButton = new Button(this);
            LinearLayout.LayoutParams layoutParamsRenameButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            layoutParamsRenameButton.weight = 0.45f;
            renameButton.setText(R.string.rename);
            renameButton.setTag(laughFile);

            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLaughNameForRename = laughFile;
                    rename();
                }
            });


            linearLayout.addView(playPauseButton, layoutParamsPlayPauseButton);
            linearLayout.addView(renameButton, layoutParamsRenameButton);
            linearLayout.addView(deleteButton, layoutParamsDeleteButton);

            if (linearLayoutNeededForScrollView != null) {
                linearLayoutNeededForScrollView.addView(linearLayoutForLaughName);
                linearLayoutNeededForScrollView.addView(linearLayout);
            }

        }

    }


    // END OF THE UPLOAD BLOCK


    // FILES BLOCK

    private ArrayList<String> getLocalLaughs(final File folder) throws NullPointerException {
        String localLaughsFolder = filepath + "/LaughBag/Local/";
        ArrayList<String> fileNames = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getLocalLaughs(fileEntry);
            } else {
                fileNames.add(localLaughsFolder + fileEntry.getName());
            }
        }
        return fileNames;
    }


    private String getFilename() {
        if (!localLaughsPath.exists()) {
            localLaughsPath.mkdirs();
        }

        return (localLaughsPath.getAbsolutePath() + "/" + "R" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    // END OF THE FILES BLOCKr


    // RECORDING BLOCK

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        String firstLaughFileName = getFilename();
        recorder.setOutputFile(firstLaughFileName);
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (null != recorder) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            recorder.reset();
            recorder.release();

            recorder = null;

        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    // END OF THE RECORDING BLOCK


    // RENAMING BLOCK

    private void rename() {
        stopLaughsPlaying();
        Intent intent = new Intent(this, NamingLaughActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case REQUEST_CODE: {
                    File fileFrom = new File(currentLaughNameForRename);
                    File fileTo = new File(localLaughsPath.getAbsolutePath() + "/" + data.getStringExtra("name") + file_exts[currentFormat]);
                    boolean letsDoThis = fileFrom.renameTo(fileTo);
                    updateLocalLaughs();
                }
                case REQUEST_CODE_DELETE: {
                    updateLocalLaughs();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


}