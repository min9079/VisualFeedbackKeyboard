package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import static android.view.HapticFeedbackConstants.KEYBOARD_TAP;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static java.lang.Math.abs;
import static kr.ac.snu.hcil.customkeyboard.QwertyLayout.Point;
import static kr.ac.snu.hcil.customkeyboard.QwertyLayout.Position;

/**
 * Created by min90 on 19/12/2016.
 */

public class QwertyView extends View implements GestureDetector.OnGestureListener{
    private static final String TAG = QwertyView.class.getName();
    private static final float mMoveThreshold = 10;
    private OnQwertyActionListener mOnQwertyActionListener = null;
    private GestureDetectorCompat mDetector = null;
    private float mDensity = getContext().getResources().getDisplayMetrics().density;
    private int mDpi = getContext().getResources().getDisplayMetrics().densityDpi;
    private QwertyLayout mQwertyLayout;
    private int mQwertyWidth;
    private Map<Character, Boolean> mCharMap;
    private boolean mIsInitailized = false;
    private Paint mBackgroundPaint;
    private Paint mButtonPaint;
    private SharedPreferences mSharedPreferences;
    private Boolean mIsItalic;
    private Boolean mIsSize;
    private Boolean mIsBold;
    private Boolean mIsColor;
    private boolean mIsRedDot;
    private double mMaxButtonRadius = -1;
    private boolean mScrollFlag = false;

    private float pxToDp(float px) {
        return px / mDensity;
    }
    private float dpToPx(float dp) {
        return dp * mDensity;
    }

    /* for GestureDectector.OnGestureListener */
    @Override
    public boolean onDown(MotionEvent e) {
        //Log.d(TAG, "OnDown: " + e.toString());
        mScrollFlag = false;

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        //Log.d(TAG, "OnShowPress: " + e.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //Log.d(TAG, "OnSingleTapUp: " + e.toString());
        performHapticFeedback(KEYBOARD_TAP);

        if (mOnQwertyActionListener != null) {
            mOnQwertyActionListener.onInput(e.getX(),e.getY());
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Log.d(TAG, "OnScroll: " + e1.toString() + e2.toString() );
        Log.d(TAG, "OnScroll: (" + distanceX + ", " + distanceY + ")");

        if (!mScrollFlag && mOnQwertyActionListener != null && Math.max(abs(distanceX), abs(distanceY)) > mMoveThreshold ) {
            performHapticFeedback(LONG_PRESS);

            if (abs(distanceX) > abs(distanceY)) {
                if (distanceX < 0) {
                    mOnQwertyActionListener.flingRight();
                } else {
                    mOnQwertyActionListener.flingLeft();
                }
            } else {
                if (distanceY < 0) {
                    mOnQwertyActionListener.flingDown();
                } else {
                    mOnQwertyActionListener.flingUp();
                }
            }

            mScrollFlag = true;
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //Log.d(TAG, "OnLongPress: " + e.toString());
        performHapticFeedback(LONG_PRESS);

        if (mOnQwertyActionListener != null) {
            mOnQwertyActionListener.onLongPressed();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //float threshold = dpToPx(20f); // set 20dp as a threshold for swipe operation.
        //float threshold = mQwertyWidth / 160; /* 25.0 on NexusX, while 20.0 seems to be the minimum */
        //Log.d(TAG, "OnFling: " + e1.toString() + e2.toString());

        Log.v(TAG, "\nOnFling: (" + e1.getX() + "," + e1.getY() + ") -> (" + e2.getX() + "," + e2.getY() + "): vX=" + velocityX + ", vY=" + velocityY);

        if (mScrollFlag) {
            mScrollFlag = false;
            return true;
        }

        if (abs(velocityX) > abs(velocityY)) {
            // left or right swipe
            /*
            if (abs(e1.getX() - e2.getX()) < threshold) {
               return true; // ignore subtle movement on fling
            }
            */
            performHapticFeedback(LONG_PRESS);
            if (velocityX > 0) {
                if (mOnQwertyActionListener != null) {
                    mOnQwertyActionListener.flingRight();
                }
            } else {
                if (mOnQwertyActionListener != null) {
                    mOnQwertyActionListener.flingLeft();
                }
            }
        } else {
            /*
            if (abs(e1.getY() - e2.getY()) < threshold) {
                return true; // ignore subtle movement on fling
            }
            */
            performHapticFeedback(LONG_PRESS);
            if (velocityY > 0) {
                if (mOnQwertyActionListener != null) {
                    mOnQwertyActionListener.flingDown();
                }
            } else {
                if (mOnQwertyActionListener != null) {
                    mOnQwertyActionListener.flingUp();
                }
            }
        }

        return true;
    }

    public void applyPreferences() {
        mIsItalic = mSharedPreferences.getBoolean("method_conf_char_italic", false);
        mIsSize = mSharedPreferences.getBoolean("method_conf_char_size", false);
        mIsBold = mSharedPreferences.getBoolean("method_conf_char_bold", true);
        mIsColor = mSharedPreferences.getBoolean("method_conf_char_color", true);
        mIsRedDot = mSharedPreferences.getBoolean("others_red_dot", false);
    }

    /* end of GestureDectector.OnGestureListener */

    interface OnQwertyActionListener {
        void onKeyboardViewInit(String log);

        void onInput(final float x, final float y);

        void flingLeft();

        void flingRight();

        void flingDown();

        void flingUp();

        void onLongPressed();
    }

    public QwertyView(Context context) {
        this(context, null);
    }

    public QwertyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCharMap = new HashMap<>();
        initIsBoldMap();

        mBackgroundPaint = new Paint();
        mButtonPaint = new Paint();
        mButtonPaint.setTextAlign(Paint.Align.CENTER);
        mButtonPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(Color.DKGRAY);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        applyPreferences();
    }

    public void setOnQwertyActionListener(OnQwertyActionListener listener){
        mOnQwertyActionListener = listener;
        mDetector = new GestureDetectorCompat(getContext(), this);
    }

    public QwertyLayout getLayout() {
        return mQwertyLayout;
    }

    public char getNearestChar(float x, float y) {
        //return mQwertyLayout.getChar(new Point(x-mQwertyLayout.getX(), y-mQwertyLayout.getY()), Position.Center, false);
        //return mQwertyLayout.getChar(new Point(x, y), Position.Center, (float) mMaxButtonRadius);
        return WordCorrector.getInstance().getChar((int) x, (int) y);
    }

    private void initIsBoldMap() {
        String line = "qwertyuiopasdfghjklzxcvbnm";
        for(Character c: line.toCharArray()){
            mCharMap.put(c, false);
        }
    }

    public void drawButtons(WordCorrector.PredictEntry[] predictEntries, int numberOfChars) {
        int i = 0;
        for(WordCorrector.PredictEntry e: predictEntries) {
            mCharMap.put(e.mStr.charAt(0), true);
            i++;
            if(i==numberOfChars)
                break;
        }
        invalidate();
    }

    private void drawButton(Canvas canvas, int row, int col, boolean isBold, boolean isItalic, boolean isSize, boolean isColor) {

        if(isSize) {
            mButtonPaint.setTextSize((float) getMeasuredHeight() / 3);
        } else {
            mButtonPaint.setTextSize((float) getMeasuredHeight() / 4);
        }

        if (isBold && isItalic) {
            mButtonPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if(isBold) {
            mButtonPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if(isItalic) {
            mButtonPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            mButtonPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }

        if(isColor){
            mButtonPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_orange_light));
        } else{
            mButtonPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        }

        QwertyLayout.Point pt = mQwertyLayout.getPosition(row,col, Position.Center);
        //canvas.drawText(String.valueOf(mQwertyLayout.getCharFromIndex(row,col)), pt.x, pt.y + mQwertyLayout.getBtnH()/3, mButtonPaint); // adjust font rendering height // for spacebar
        canvas.drawText(String.valueOf(mQwertyLayout.getCharFromIndex(row,col)), pt.x, pt.y + mQwertyLayout.getBtnH()/5, mButtonPaint); // adjust font rendering height
    }

    /*
    private void drawSpacebar(Canvas canvas, int row, int col) {
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        paint.setTextSize((float)getMeasuredHeight()/ 3);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        QwertyLayout.Point pt = mQwertyLayout.getPosition(row, col, Position.Center);
        if(' ' == mQwertyLayout.getCharFromIndex(row,col))
            canvas.drawText(String.valueOf('-'), pt.x, pt.y + mQwertyLayout.getBtnH()/3, paint); // adjust font rendering height
        else
            canvas.drawText(String.valueOf(mQwertyLayout.getCharFromIndex(row, col)), pt.x, pt.y + mQwertyLayout.getBtnH()/3, paint); // adjust font rendering height
    }
    */ // for spacebar

    private void drawRedDot(Canvas canvas, int row, int col) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        QwertyLayout.Point pt2 = mQwertyLayout.getPosition(row,col, Position.Center);
        canvas.drawCircle(pt2.x, pt2.y, 2.0f, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // all resolutions are based on pixels
        if (!mIsInitailized && mOnQwertyActionListener != null) {
            int mheight = getMeasuredHeight();
            float ypad = getPaddingTop() + getPaddingBottom();
            int left = getLeft();
            int top = getTop();
            int width = getWidth();
            int height = getHeight();
            int mwidth = getMeasuredWidth();
            float xpad = getPaddingLeft() + getPaddingRight();
            String logString = "rect: (x, y, w, h, mw, mh) : "  + left + " " + top  + " " + width + " " + height + " " + mwidth + " " + mheight;

            Log.v(TAG, logString);
            Log.v(TAG, "xpad=" + xpad + ", ypad=" +ypad);
            Log.v(TAG, String.format("density=%f, densityDpi=%d",mDensity, mDpi));

            mOnQwertyActionListener.onKeyboardViewInit(logString);
            mIsInitailized = true;

            mMaxButtonRadius = mQwertyLayout.getLength(
                    mQwertyLayout.getPosition('q', QwertyLayout.Position.Center ),
                    mQwertyLayout.getPosition('s', QwertyLayout.Position.Center ));
            mMaxButtonRadius = Math.sqrt(mMaxButtonRadius)/1.7;
            mMaxButtonRadius = Math.pow(mMaxButtonRadius,2);
        }

        canvas.drawPaint(mBackgroundPaint);

        for(int i=0; i<3; i++) {
            String line = mQwertyLayout.getLineFromIndex(i);
            for(int j=0; j<line.length(); j++) {
                Boolean isHighProb = mCharMap.get(mQwertyLayout.getCharFromIndex(i,j));
                drawButton(canvas, i, j, isHighProb && mIsBold, isHighProb && mIsItalic, isHighProb && mIsSize, isHighProb && mIsColor);
                if(mIsRedDot)
                    drawRedDot(canvas, i, j);
            }
        }

        // for spacebar
        /*
        for(int i=0; i<5; i++) {
            drawSpacebar(canvas, 3, i);
            if (mIsRedDot) {
                drawRedDot(canvas, 3, i);
            }
        }
        */

        initIsBoldMap();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        String logString = String.format("onSizeChanged(w,h): (%d, %d) -> (%d, %d)\n", oldw, oldh, w, h);
        Log.v(TAG, logString);

        int hh = getHeight() - getPaddingTop() - getPaddingBottom();
        int mm = getWidth() - getPaddingLeft() - getPaddingRight();

        mQwertyWidth = mm;
        mQwertyLayout = new QwertyLayout(mm, hh, getPaddingLeft(), getPaddingTop());

        mIsInitailized = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mDetector != null) {
            mDetector.onTouchEvent(event);
            return true;
        }

        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                break;
        }
        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);

        return super.onTouchEvent(event);
    }
}
