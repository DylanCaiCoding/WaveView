package com.dylanc.waveviewdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.dylanc.waveview.WaveView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WaveView mWaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWaveView = findViewById(R.id.wave_view);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                mWaveView.startAnim();
                break;
            case R.id.btn_stop:
                mWaveView.stopAnim();
                break;
        }
    }
}
