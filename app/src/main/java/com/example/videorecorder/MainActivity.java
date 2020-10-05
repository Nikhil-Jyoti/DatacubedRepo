package com.example.videorecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkReadWritePermission();
    }


    @Override
    protected void onStart() {
        super.onStart();
        final EditText videoName = (EditText) findViewById(R.id.video_Name);
        final Button recordButton = findViewById(R.id.record_Video);
        final Slider videoDuration = findViewById(R.id.video_Duration);


        videoDuration.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                int value1 = (int) value;
                if (value1 >= 60) {
                    return value1 / 60 + ":" + value1 % 60;
                } else {
                    return value1 + "seconds";
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(videoName.getText())) {
                    Toast.makeText(getApplicationContext(), "Please write a video name", Toast.LENGTH_SHORT).show();
                } else {
                    startRecording(videoName.getText().toString(), videoDuration.getValue());
                }
            }
        });


    }

    public void startRecording(String videoName, float videoDuration) {


        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("video_name", videoName);
        intent.putExtra("video_duration", videoDuration);
        startActivity(intent);
    }

    private void checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                    .checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "Need permissions from settings", Toast.LENGTH_SHORT).show();
                } else {
                    String permission[] = new String[]{Manifest.permission
                            .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    ActivityCompat.requestPermissions(this, permission, 0);
                }
            } else {
                loadVideos();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {

            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                loadVideos();
            }
        }

    }


    private void loadVideos() {

        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        ArrayList<String> videos = new ArrayList<>();
        findVideos(movieFile, videos);
        ArrayList<String> listOfVideos = new ArrayList<>();

        for (String video : videos) {
            MediaMetadataRetriever r = new MediaMetadataRetriever();
            r.setDataSource(video);
            String durString = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int duration = Integer.parseInt(durString) / 1000;

            String name = video.substring(video.indexOf("Videos/"), video.indexOf('+'));
            name = name.substring(name.indexOf('/') + 1);

            String date = video.substring(video.indexOf('+') + 1, video.indexOf('-'));
            String time = date.substring(date.indexOf('_') + 1);
            date = date.substring(0, date.indexOf('_'));
            String year = date.substring(0, 4);
            String month = date.substring(4, 6);
            date = date.substring(6);
            String hours = time.substring(0, 2);
            String minutes = time.substring(2, 4);
            time = date + "/" + month + "/" + year + "-" + hours + ":" + minutes;
            listOfVideos.add("Video Name: " + name + " Duration:" + duration + " Time:" + time);

        }

        RecyclerView recyclerView = findViewById(R.id.video_List);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(this, listOfVideos);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) layoutManager).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter.setClickListener(new RecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getApplicationContext(), "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void findVideos(File dir, ArrayList<String> list) {

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) findVideos(file, list);
            else if (file.getAbsolutePath().contains(".mp4"))
                list.add(file.getAbsolutePath());
        }
    }
}
