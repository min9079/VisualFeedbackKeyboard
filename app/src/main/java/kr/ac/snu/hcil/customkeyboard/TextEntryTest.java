package kr.ac.snu.hcil.customkeyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Vector;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by min90 on 07/02/2017.
 */

class TextEntryTest {

    enum TouchType {TOUCH, SWIPE_LEFT, SWIPE_RIGHT, SWIPE_UP, SWIPE_DOWN, SPACE, BACK}

    static class TouchInfo {

        TouchType mTouchType;
        float mX;
        float mY;
        String mNearChar;
        String mDecodedChar;
        long mUpdateTime;

        public TouchInfo(TouchType touchType) {
            mTouchType = touchType;
            mX = -1;
            mY = -1;
            mNearChar = "";
            mDecodedChar = "";
            mUpdateTime = 0;
        }

        public TouchInfo(float x, float y) {
            mTouchType = TouchType.TOUCH;
            mX = x;
            mY = y;
            mNearChar = "";
            mDecodedChar = "";
            mUpdateTime = 0;
        }

        public void setTouchType(TouchType touchType) {
            this.mTouchType = touchType;
        }

        public void setNearlestChar(String nearestChar) {
            this.mNearChar = nearestChar;
        }

        public void setDecodedChar(String decodedChar) {
            this.mDecodedChar = decodedChar;
        }

        public void setUpdateTime(long updateTime) {
            this.mUpdateTime = updateTime;
        }

        public long getUpdateTime() {
            return mUpdateTime;
        }
    }


    private class TestInfo {
        String mPresentedPhrase;
        String mTranscribedPhrase;
        Vector<TouchInfo> mTouches;
        long mStartTime;
        long mEndTime;
    }
    private TestInfo mTestInfo = null;

    public enum TestStatus {INIT, STARTED, ENDED}

    private TestStatus mTestStatus = TestStatus.INIT;

    TextEntryTest(String presentedPhrase) {
        mTestInfo = new TestInfo();
        mTestInfo.mPresentedPhrase = presentedPhrase;
        mTestInfo.mTouches = new Vector<TouchInfo>();
    }

    void startTest() {
        mTestInfo.mStartTime = 0;
        mTestStatus = TestStatus.STARTED;
    }

    void endTest(String transcribedPhrase) throws IllegalStateException {
        if (mTestStatus == TestStatus.ENDED) {
            return;
        }

        if(mTestStatus != TestStatus.STARTED)
            throw new IllegalStateException();

        if(mTestInfo.mTouches.size()>0)
            mTestInfo.mEndTime = mTestInfo.mTouches.lastElement().getUpdateTime();
        mTestStatus = TestStatus.ENDED;
        mTestInfo.mTranscribedPhrase = transcribedPhrase;
    }

    void updateKeyStroke(TouchInfo touchInfo) {
        touchInfo.setUpdateTime(System.currentTimeMillis());
        if (mTestInfo.mStartTime < 0.01) {
            mTestInfo.mStartTime = touchInfo.getUpdateTime();
        }
        mTestInfo.mTouches.add(touchInfo);
    }

    float getErrRate() throws IllegalStateException {
        if(mTestStatus != TestStatus.ENDED)
            throw new IllegalStateException();
        return (float) calcMSD(mTestInfo.mPresentedPhrase, mTestInfo.mTranscribedPhrase) / max(mTestInfo.mPresentedPhrase.length(), mTestInfo.mTranscribedPhrase.length());
    }

    public TestStatus getTestStatus() {
        return mTestStatus;
    }

    float getKSPC() throws IllegalStateException {
        if(mTestStatus != TestStatus.ENDED)
            throw new IllegalStateException();
        return (float) mTestInfo.mTouches.size() / (float) mTestInfo.mTranscribedPhrase.length();
    }

    float getWPM() throws IllegalStateException {
        return (float) mTestInfo.mTranscribedPhrase.length() * 1000 * 60 / (5 * (float) (mTestInfo.mEndTime - mTestInfo.mStartTime));
    }

    String getPresentedPhrase() {
        return mTestInfo.mPresentedPhrase;
    }

    String getTranscribedPhrase() {
        return mTestInfo.mTranscribedPhrase;
    }

    String getStartTime() {
        return DateFormat.getDateTimeInstance().format(mTestInfo.mStartTime);
    }

    String getEndTime() {
        return DateFormat.getDateTimeInstance().format(mTestInfo.mEndTime);
    }

    public JSONArray getTouches() throws JSONException {
        JSONArray touchesJsonArray = new JSONArray();

        for (TouchInfo touchInfo : mTestInfo.mTouches) {
            JSONObject touchJson = new JSONObject();
            touchJson.put("Type", touchInfo.mTouchType.toString());
            touchJson.put("X", touchInfo.mX);
            touchJson.put("Y", touchInfo.mY);
            touchJson.put("Nearest", touchInfo.mNearChar);
            touchJson.put("Decoded", touchInfo.mDecodedChar);
            touchJson.put("Time", touchInfo.mUpdateTime);

            touchesJsonArray.put(touchJson);
        }

        return touchesJsonArray;
    }

    private int calcMSD(String string1, String string2) {
        int ret = 0;
        int m = string1.length();
        int n = string2.length();
        int[][] A = new int[m+1][n+1];

        for(int i=0; i <=m; i++) {
            A[i][0] = i;
        }

        for(int j=0; j <=n; j++){
            A[0][j] = j;
        }

        for(int i=1; i <=m; i++) {
            for(int j=1; j <=n; j++) {
                int case1 = A[i-1][j-1] + (string1.charAt(i-1) == string2.charAt(j-1) ? 0 : 1);
                int case2 = A[i-1][j] + 1; // insertion on string2
                int case3 = A[i][j-1] + 1; // deletion on string2
                A[i][j] = min(case1, min(case2,case3));
            }
        }
        return A[m][n];
    }
}
