package com.uberv.joystick;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {

    public static final float VALUE_X_INCREASE_MULTIPLIER = 1;
    public static final float VALUE_Y_INCREASE_MULTIPLIER = 1;
    public static final long VALUES_POLL_DELAY = 20;
    public static final int DEGREES_X_AXIS = 180;
    public static final int DEGREES_Y_AXIS = 100;
    public static final int VALUES_MAX_DIFF_BEFORE_PUBLISH = 1;
    public static final int VALUE_X_MAX = 200;

    private JoystickView mJoystickView;
    private TextView mOutput;
    private ProgressBar mProgressBarX;
    private ProgressBar mProgressBarY;
    private float mValueX = 0;
    private float mValueY = 0;
    private float mOldValueX = 0;
    private float mOldValueY = 0;
    private float mCurrentX, mCurrentY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mJoystickView = (JoystickView) findViewById(R.id.joystick);
        mJoystickView.setJoystickMovedListener(this);

        mOutput = (TextView) findViewById(R.id.output);
        mProgressBarX = (ProgressBar) findViewById(R.id.x_progress_bar);
        mProgressBarY = (ProgressBar) findViewById(R.id.y_progress_bar);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mValueX += VALUE_X_INCREASE_MULTIPLIER * mCurrentX;
                mValueY += VALUE_Y_INCREASE_MULTIPLIER * mCurrentY;
                if (mValueX > 200) {
                    mValueX = 200;
                } else if (mValueX < 0) {
                    mValueX = 0;
                }
                if (mValueY > 200) {
                    mValueY = 200;
                } else if (mValueY < 0) {
                    mValueY = 0;
                }

                handler.postDelayed(this, VALUES_POLL_DELAY);
                float maxDifference = Math.max(Math.abs(mOldValueX - mValueX), Math.abs(mOldValueY - mValueY));
                if (maxDifference >= VALUES_MAX_DIFF_BEFORE_PUBLISH || maxDifference + mValueY > VALUE_X_MAX) {
                    // TODO publish update
                    mProgressBarX.setProgress((int) mValueX);
                    mProgressBarY.setProgress((int) mValueY);

                    mOldValueX = mValueX;
                    mOldValueY = mValueY;
                }
            }
        }, VALUES_POLL_DELAY);
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent) {
        mOutput.setText(String.format("x:%f y:%f", xPercent, yPercent));
//        mProgressBarX.setProgress(x_progress);
//        mProgressBarY.setProgress(y_progress);
        mCurrentX = xPercent;
        mCurrentY = yPercent;
    }
}
