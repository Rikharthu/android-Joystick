package com.uberv.joystick;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {

    private JoystickView mJoystickView;
    private TextView mOutput;
    private ProgressBar mProgressBarX;
    private ProgressBar mProgressBarY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mJoystickView = (JoystickView) findViewById(R.id.joystick);
        mJoystickView.setJoystickMovedListener(this);

        mOutput = (TextView) findViewById(R.id.output);
        mProgressBarX = (ProgressBar) findViewById(R.id.x_progress_bar);
        mProgressBarY = (ProgressBar) findViewById(R.id.y_progress_bar);
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent) {
        mOutput.setText(String.format("x:%f y:%f", xPercent, yPercent));
        int x_progress = (int) (xPercent*100)+100;
        int y_progress = (int) (yPercent*100)+100;
        mProgressBarX.setProgress(x_progress);
        mProgressBarY.setProgress(y_progress);
    }
}
