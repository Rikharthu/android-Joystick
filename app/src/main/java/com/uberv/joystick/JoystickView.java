package com.uberv.joystick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    public static final String LOG_TAG = JoystickView.class.getSimpleName();

    public static final float RATIO = 8.0f;

    private DrawThread mDrawThread;
    private float mHeight, mWidth,
            mCenterX, mCenterY,
            mHatRadius, mBaseRadius, mHandleRadius,
            mNewX, mNewY,
            mHandleWidth;
    private JoystickListener mListener;


    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    public void setJoystickMovedListener(JoystickListener listener) {
        mListener = listener;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawThread = new DrawThread(holder);
        mDrawThread.setRunning(true);
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        mNewX = mCenterX;
        mNewY = mCenterY;
        mHatRadius = Math.min(mWidth, mHeight) / 5;
        mBaseRadius = Math.min(mWidth, mHeight) / 3;
        mHandleWidth = Math.min(mWidth, mHeight) / 10;
        mHandleRadius = mHandleWidth * 1.25f;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.equals(this)) {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                float displacement = (float) Math.sqrt(
                        Math.pow(event.getX() - mCenterX, 2) + Math.pow(event.getY() - mCenterY, 2));
                if (displacement < mBaseRadius) {
                    mNewY = event.getY();
                    mNewX = event.getX();
                } else {
                    // out of bounds
                    float ratio = mBaseRadius / displacement;
                    float constrainedX = mCenterX + (event.getX() - mCenterX) * ratio;
                    float constrainedY = mCenterY + (event.getY() - mCenterY) * ratio;
                    mNewX = constrainedX;
                    mNewY = constrainedY;
                }
                if (mListener != null) {
                    mListener.onJoystickMoved((mNewX - mCenterX) / mBaseRadius, (mNewY - mCenterY) / mBaseRadius);
                }
            } else {
                if (mNewX != mCenterX || mNewY != mCenterY) {
                    mNewX = mCenterX;
                    mNewY = mCenterY;
                    // we have to notify one time if released
                    mListener.onJoystickMoved((mNewX - mCenterX) / mBaseRadius, (mNewY - mCenterY) / mBaseRadius);
                }
            }
        }
        return true;
    }

    private class DrawThread extends Thread {
        private boolean mIsRunning = false;
        private SurfaceHolder mSurfaceHolder;
        private float oldX, oldY;

        public DrawThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean isRunning) {
            mIsRunning = isRunning;
        }

        private void drawJoyStick(Canvas canvas) {
            Paint colors = new Paint();
            canvas.drawColor(Color.WHITE);
//            // base
//            colors.setARGB(255, 120, 120, 120);
//            canvas.drawCircle(mCenterX, mCenterY, mBaseRadius, colors);
//            // handle
//            colors.setARGB(255, 80, 80, 80);
//            canvas.drawCircle(mCenterX, mCenterY, mHandleRadius / 2, colors);
//            colors.setStrokeWidth(mHandleWidth);
//            canvas.drawLine(mCenterX, mCenterY, mNewX, mNewY, colors);
//            // hat
//            colors.setARGB(255, 255, 0, 0);
//            canvas.drawCircle(mNewX, mNewY, mHatRadius, colors);

            float hypotenuse = (float) Math.sqrt(Math.pow(mNewX - mCenterX, 2) + Math.pow(mNewY - mCenterY, 2));
            float sin = (mNewY - mCenterY) / hypotenuse;
            float cos = (mNewX - mCenterX) / hypotenuse;

            // Draw the base
            colors.setARGB(255, 100, 100, 100);
            canvas.drawCircle(mCenterX, mCenterY, mBaseRadius, colors);
            // Handle
            for (int i = 1; i < (int) (mBaseRadius / RATIO); i++) {
                colors.setARGB(150 / i, 255, 0, 0); // gradualy decrease the shade of black
                canvas.drawCircle(mNewX - cos * hypotenuse * (RATIO / mBaseRadius) * i,
                        mNewY - sin * hypotenuse * (RATIO / mBaseRadius) * i,
                        i * (mHatRadius * RATIO / mBaseRadius),
                        colors);
            }
            // Hat
            for (int i = 0; i <= (int) (mHatRadius / RATIO); i++) {
                colors.setARGB(255,
                        (int) (i * (255 * RATIO / mHatRadius)),
                        (int) (i * (255 * RATIO / mHatRadius)),
                        255);
                canvas.drawCircle(mNewX, mNewY, mHatRadius - (float) i * (RATIO) / 2, colors);
            }
        }

        @Override
        public void run() {
            Canvas canvas;
            while (mIsRunning) {
                canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        //TODO draw here
                        if (oldX != mNewX || oldY != mNewY) {
                            drawJoyStick(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent);
    }
}
