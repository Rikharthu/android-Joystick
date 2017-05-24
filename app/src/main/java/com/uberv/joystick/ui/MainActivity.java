package com.uberv.joystick.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uberv.joystick.Position;
import com.uberv.joystick.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final float VALUE_X_INCREASE_MULTIPLIER = 1;
    public static final float VALUE_Y_INCREASE_MULTIPLIER = 1;
    public static final long VALUES_POLL_DELAY = 20;
    public static final int DEGREES_X_AXIS = 180;
    public static final int DEGREES_Y_AXIS = 100;
    public static final int VALUES_MAX_DIFF_BEFORE_PUBLISH = 1;
    public static final int VALUE_X_MAX = 180;

    // Joystick
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


        Observable<Position> degrees = Observable.create(new ObservableOnSubscribe<Position>() {
            @Override
            public void subscribe(ObservableEmitter<Position> emitter) throws Exception {
                while (!emitter.isDisposed()) {
                    if (!(mCurrentX == 0 && mCurrentY == 0)) {
                        Thread.sleep(VALUES_POLL_DELAY);
                        mValueX += VALUE_X_INCREASE_MULTIPLIER * mCurrentX;

                        mValueY += VALUE_Y_INCREASE_MULTIPLIER * mCurrentY;

                        if (mValueX > 180) {
                            mValueX = 180;
                        } else if (mValueX < 0) {
                            mValueX = 0;
                        }
                        if (mValueY > 180) {
                            mValueY = 180;
                        } else if (mValueY < 0) {
                            mValueY = 0;
                        }

                        mProgressBarX.setProgress((int) mValueX);
                        mProgressBarY.setProgress((int) mValueY);

                        emitter.onNext(new Position((int) mValueX, (int) mValueY));

                        mOldValueX = mValueX;
                        mOldValueY = mValueY;

                    }
                }
            }
        });
        degrees.sample(200, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.computation()).subscribe(new Consumer<Position>() {
            @Override
            public void accept(Position position) throws Exception {
                // TODO publish to BLE
                Log.d(LOG_TAG, String.format("H:%d V:%d", position.getHorizontal(), position.getVertical()));
            }
        });
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
