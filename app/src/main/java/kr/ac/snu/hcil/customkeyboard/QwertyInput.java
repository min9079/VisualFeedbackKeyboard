package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by min90 on 30/07/2017.
 */

public class QwertyInput {
    private static final String TAG = QwertyInput.class.getName();
    private WordCorrector mWc = WordCorrector.getInstance();
    private InputText mInputText = new InputText();
    private InputText.InputWord mInputWord = new InputText.InputWord();
    private SharedPreferences mSharedPreferences;
    private QwertyView mQwertyView;
    private boolean mIsAutoCorrectOn = true;
    private String mRawInputWord = new String();
    private String mPredictedWord = new String();
    private WordCorrector.PredictEntry[] mPredictedWords = new WordCorrector.PredictEntry[0];
    private Double mCharPredictThreshold;
    private Double mWordPredictThreshold;
    private Integer mNumberOfChars;
    private boolean mEnableWordPrediction;

    QwertyInput(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isAutoCorrectOn() {
        return mIsAutoCorrectOn;
    }

    public void enableAutoCorrection() {
        mIsAutoCorrectOn = true;
    }

    public void disableAutoCorrection() {
        mIsAutoCorrectOn = false;
    }

    public String getInputText() {
        return mInputText.getString();
    }

    public String getInputWord() {
        return mInputWord.mWord.toString();
    }

    public String getRawInputWord() {
        return mRawInputWord;
    }

    public String getPredictedWord() {
        return mPredictedWord;
    }

    public void setPredictedWord(String predictedWord) {
        mPredictedWord = predictedWord;
    }

    public WordCorrector.PredictEntry[] getPredictedWords() {
        return mPredictedWords;
    }

    public void initialize(QwertyView qwertyView, QwertyLayout layout, int dpi) {
        mQwertyView = qwertyView;
        mWc.cleanup(); // to prevent resource leak.
        mWc.init(new WordCorrector.ConfBuilder(layout, dpi).build());
        mWc.setContext(mInputText.getContextString(3));

        String predictThreshold = mSharedPreferences.getString("method_conf_predict_threshold", "0.15");
        if (predictThreshold.compareTo("") != 0)
            mCharPredictThreshold = Double.parseDouble(predictThreshold);
        else
            mCharPredictThreshold = 0.15;

        predictThreshold = mSharedPreferences.getString("method_conf_word_predict_threshold", "0.30");
        if (predictThreshold.compareTo("") != 0)
            mWordPredictThreshold = Double.parseDouble(predictThreshold);
        else
            mWordPredictThreshold = 0.30;

        String numberOfChars = mSharedPreferences.getString("method_conf_n_of_chars", "5");
        if (numberOfChars.compareTo("") != 0)
            mNumberOfChars = Integer.parseInt(numberOfChars);
        else
            mNumberOfChars = 5;
        mEnableWordPrediction = mSharedPreferences.getBoolean("method_conf_word_prediction", true);
    }

    public void resetInput() {
        mInputText.clear();
        mWc.clearSentence();
        mWc.setContext(mInputText.getContextString(3));

        mInputWord = new InputText.InputWord();
    }

    public void processInput(float x, float y, TextEntryTest.TouchInfo touchInfo) {
        char nearestKey = mQwertyView.getNearestChar(x, y);
        touchInfo.setNearlestChar(String.valueOf(nearestKey));

        if ('\0' == nearestKey || '@' == nearestKey)
            return;

        if (' ' == nearestKey) {
            touchInfo.setTouchType(TextEntryTest.TouchType.SPACE); // treat SPACE input special for future usage.
            processSpaceKey();
            return;
        }

        if ('<' == nearestKey) {
            touchInfo.setTouchType(TextEntryTest.TouchType.BACK); // treat BACK input special for future usage.
            processBackKey();
            return;
        }

        mInputWord.pushCharacter(nearestKey, x, y);

        // consider the x, y offset when pass by x, y positions
        if (mIsAutoCorrectOn) {
            WordCorrector.TouchString touchString = mWc.decodeTouchKey(x - mQwertyView.getLayout().getX(), y - mQwertyView.getLayout().getY());
            mInputWord.mWord.replace(0, mInputWord.mWord.length(), touchString.mStr);
            mRawInputWord = touchString.mOriStr;

            Log.d(TAG, mRawInputWord);
            /*
            if (mInputWord.contains(" ")) {
                Toast.makeText(getContext(), "white space inserted!", Toast.LENGTH_LONG).show();
            }
            */
        } else {
            mRawInputWord = mInputWord.mWord.toString();
        }

        String input = String.format("[OnInput] x=%f, y=%f -> %s\n", x, y, mInputWord.toString());
        Log.d(TAG, input);

        processPrediction(1);

        touchInfo.setDecodedChar(mInputWord.mWord.toString());   // update decoded char(s)

    }

    public void processSpaceKey() {
        if (mInputWord.length() > 0) {
            if (mIsAutoCorrectOn) {
                WordCorrector.PredictEntry[] decodeResult = mWc.decodeWord();
                if (decodeResult.length > 0) {
                    mInputWord.mWord.replace(0, mInputWord.mWord.length(), decodeResult[0].mStr);
                }
            } else {
                mWc.pushWord(mInputWord.mWord.toString());
            }
            mInputText.pushWord(mInputWord);

            mInputWord.clear();
            //mRawInputWord="";
            String log = "[processSpaceKey]context: " + mInputText.getContextString(3) + "\n";
            Log.d(TAG, log);
            mWc.setContext(mInputText.getContextString(3));
            processPrediction(1);
            mIsAutoCorrectOn = true;
        } else {
            // Do not permit consecutive space char.
        }
    }

    public void processBackKey() {
        if (mInputWord.length() > 0) {  // delete just a key
            mInputWord.popCharacter();
            mRawInputWord = mRawInputWord.substring(0, mRawInputWord.length() - 1);
            if (mIsAutoCorrectOn) {
                mInputWord.mWord.replace(0, mInputWord.length(), mWc.backKey());
            }
            String log = "[processBackKey] words: %s\n".format(mInputWord.mWord.toString());
            Log.d(TAG, log);
            processPrediction(1);
        } else {
            if (mIsAutoCorrectOn)
                mIsAutoCorrectOn = false;
            mInputWord = mInputText.peekWord();
            mRawInputWord = mInputWord.mWord.toString(); // TODO: consider to displaying raw input here also.
            mInputText.popWord();
            //Log.d(TAG, "before backWord");
            mWc.backWord();
            //Log.d(TAG, "before setContext");
            mWc.setContext(mInputText.getContextString(3));
            processPrediction(1);
        }
    }

    public void processBackWord() {
        if (mIsAutoCorrectOn && mInputWord.length() == 0) {
            // just turn-off auto-correction and undo word decoding.
            mIsAutoCorrectOn = false;
            mInputWord = new InputText.InputWord(mRawInputWord);
            mInputText.popWord();
            //Log.d(TAG, "before backWord");
            mWc.backWord();
            //Log.d(TAG, "before setContext");
            mWc.setContext(mInputText.getContextString(3));
            processPrediction(1);

            return;
        }

        if (mInputWord.length() > 0) {
            mInputWord.clear();
            mRawInputWord = "";
        } else {
            mInputText.popWord();
            mWc.backWord();
        }
        mWc.setContext(mInputText.getContextString(3));
        mIsAutoCorrectOn = true;
        processPrediction(1);
    }

    public void applyPrediction() {
        if (mPredictedWord.length() > 0) {
            mInputWord.mWord.replace(0, mInputWord.length(), mPredictedWord);
            disableAutoCorrection();
            processSpaceKey();
            mPredictedWord = "";
            mPredictedWords = new WordCorrector.PredictEntry[0];
        } else {
            processSpaceKey();
        }
    }

    public void processPrediction(int scale) {
        float charPredictThreshold = Math.min(0.99f, (float) (scale * mCharPredictThreshold));
        float wordPredictThreshold = Math.min(0.99f, (float) (scale * mWordPredictThreshold));

        WordCorrector.PredictEntry[] predictEntries = new WordCorrector.PredictEntry[0];
        if (mIsAutoCorrectOn && charPredictThreshold < 1.0) {
            predictEntries = mWc.predictChar(26, charPredictThreshold);
        }

        mQwertyView.drawButtons(predictEntries, mNumberOfChars);

        String logString = "";
        for (WordCorrector.PredictEntry e : predictEntries) {
            logString += e.mStr + "(" + e.mProb + ") ";
        }
        logString += '\n';

        // word prediction condition
        // 0. word prediction is on
        // 1. auto correction is on
        // 2. char prediction is successful
        // 3. word has initial char to speed up (aided by char prediction)
        // 4. KSPC saving is at least one. (Modified from two to avoid user frustration)
        if (mEnableWordPrediction && mIsAutoCorrectOn && predictEntries.length > 0 && predictEntries[0].mProb > wordPredictThreshold) {
            if (0 == mInputWord.length())
                predictEntries = mWc.predictWord(1000, (float) -1, predictEntries[0].mStr.charAt(0));
            else
                predictEntries = mWc.predictWord(1000, (float) -1);

            if (predictEntries.length > 0) {
                if (predictEntries[0].mStr.length() > mInputWord.length()) {
                    mPredictedWord = predictEntries[0].mStr;
                }
                mPredictedWords = predictEntries;
            } else {
                mPredictedWord = "";
                mPredictedWords = new WordCorrector.PredictEntry[0];
            }
        } else {
            mPredictedWord = "";
            mPredictedWords = new WordCorrector.PredictEntry[0];
        }

        Log.d(TAG, logString);
    }

    public String decodeSentence() {
        return mWc.decodeSentence()[0].mStr;
    }
}

