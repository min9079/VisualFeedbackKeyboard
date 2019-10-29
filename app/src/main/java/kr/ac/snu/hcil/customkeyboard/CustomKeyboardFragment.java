package kr.ac.snu.hcil.customkeyboard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

//import android.util.TypedValue;
//import android.widget.LinearLayout;


/**
 * Created by min90 on 05/12/2016.
 */
public class CustomKeyboardFragment extends Fragment implements QwertyView.OnQwertyActionListener, View.OnTouchListener {
    private static final String TAG = CustomKeyboardFragment.class.getName();
    private List<String> mPhraseList;
    private int mCurrIdx = 0;
    private int mNumTests = 0;
    TextEntryTest mTextEntryTest = null;
    ReportManager mReportManager = null;
    private TextView mLogTextView;
    private CompositePresentedTextDisplay mPresentedTextDisplay;
    private TranscribedTextView mTranscribedTextView;
    private Button mEnterButton;
    private QwertyView mQwertyView;
    private StringBuilder mLog = new StringBuilder();
    private SharedPreferences mSharedPreferences;
    private DisplayMode mCharDisplayMethod = DisplayMode.DECODED;
    private QwertyInput mQwertyInput;
    private boolean mEnableBackKey;
    private boolean mEnableSuggestionBar;
    //private TextView[] updateTranscribedTextView = new TextView[4];
    //private LinearLayout mSuggestionBar;
    //private TextView mPresentedText;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTranscribedTextView.isSameView(v)) {
            flingDown(); // Same behavior with flingDown.
            return true;
        } else {
            mQwertyInput.setPredictedWord(((TextView)v).getText().toString());
            //Log.i(TAG, "X=" + v.getX() + ", Y=" + v.getY());
            //Log.i(TAG, "X=" + event.getX() + ", Y=" + event.getY());
            //Log.i(TAG, "rawX=" + event.getRawX() + ", Y=" + event.getRawY());
            flingDown();
            return true;
        }
    }

    private class TranscribedTextView {
        private TextView mTranscribedTextView_;
        private boolean mIsShow;
        private String mPredictedPostfix = "";
        private String mText;
        private Handler mHandler;

        public Boolean isSameView(View v) {
            return (v == mTranscribedTextView_);
        }

        TranscribedTextView(TextView transcribedTextView, View.OnTouchListener onTouchListener) {
            mTranscribedTextView_ = transcribedTextView;
            mTranscribedTextView_.setOnTouchListener(onTouchListener);
            mTranscribedTextView_.setMovementMethod(new ScrollingMovementMethod());
            mIsShow = true;
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    mTranscribedTextView.displayTextView();
                    this.sendEmptyMessageDelayed(0, 500);
                }
            };

            mHandler.sendEmptyMessage(1);
        }

        private synchronized void displayTextView() {
            String displayedString = mText;

            if (mIsShow) {
                if (0 == mPredictedPostfix.length()) {
                    displayedString = displayedString + "<font color=\"black\">_</font>";
                } else {
                    displayedString = displayedString + "<font color=\"#d3d3d3\"><b><u>" + mPredictedPostfix.substring(0, 1) + "</u>" + mPredictedPostfix.substring(1) + "</b></font>";
                }
                mIsShow = false;
            } else {
                if (0 == mPredictedPostfix.length()) {
                    displayedString = displayedString + "<font color=\"#FFFFFF\">_</font>";
                } else {
                    displayedString = displayedString + "<font color=\"#d3d3d3\"><b>" + mPredictedPostfix + "</b></font>";
                }
                mIsShow = true;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTranscribedTextView_.setText(Html.fromHtml(displayedString, Html.FROM_HTML_MODE_LEGACY));
            } else {
                mTranscribedTextView_.setText(Html.fromHtml(displayedString));
            }

            mTranscribedTextView_.invalidate();
        }

        private void updateTranscribedTextView(String text, String predictedPostfix) {
            mText = text;
            mPredictedPostfix = predictedPostfix;
            displayTextView();
            int offset = Math.max(0, mTranscribedTextView_.getLineCount() - 2);
            int scroll_amount = offset * mTranscribedTextView_.getLineHeight();
            mTranscribedTextView_.scrollTo(0, scroll_amount);
        }
    }

    public static CustomKeyboardFragment newInstance() {
        return new CustomKeyboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_custom_keyboard, container, false);

        mPresentedTextDisplay = new CompositePresentedTextDisplay();
        mPresentedTextDisplay.add(new LocalPresentedTextDisplay((TextView) v.findViewById(R.id.presented_text)));
//        mPresentedText = (TextView) v.findViewById(R.id.presented_text);
        mPresentedTextDisplay.add(new RemotePresentedTextDisplay(getContext()));

        mTranscribedTextView = new TranscribedTextView((TextView) v.findViewById(R.id.transcribed_text), this);

        mEnterButton = (Button) v.findViewById(R.id.enter_button);
        mEnterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextEntryTest.TestStatus.ENDED == mTextEntryTest.getTestStatus()) {
                    // Already reported, just go on to the next step!
                    gotoNextStep();
                    return;
                } else if (TextEntryTest.TestStatus.STARTED == mTextEntryTest.getTestStatus()) {
                    endCurrentStep(TextEntryTest.TouchType.TOUCH);
                } else {
                    Log.d(TAG, "Invalid Test Status to go on next step.");
                }
            }
        });

        mLogTextView = (TextView) v.findViewById(R.id.sample_text);
        mLogTextView.setMovementMethod(new ScrollingMovementMethod());

        mQwertyView = (QwertyView) v.findViewById(R.id.qwerty_view);
        mQwertyView.setOnQwertyActionListener(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mQwertyInput = new QwertyInput(getContext());

        /*
        mSuggestionBar = (LinearLayout) v.findViewById(R.id.suggestions_layout);
        mSuggestionTextViews[0] = (TextView) v.findViewById(R.id.suggestions1);
        mSuggestionTextViews[1] = (TextView) v.findViewById(R.id.suggestions2);
        mSuggestionTextViews[2] = (TextView) v.findViewById(R.id.suggestions3);
        mSuggestionTextViews[3] = (TextView) v.findViewById(R.id.suggestions4);
        for (TextView suggestionTextView : mSuggestionTextViews) {
            suggestionTextView.setOnTouchListener(this);
        }
        */

        return v;
    }

    @Override
    public void onKeyboardViewInit(String log) {
        mLog.insert(0, log + "\n");
        mLogTextView.setText(mLog);
        Log.d(TAG, log + "\n");

        int dpi = getContext().getResources().getDisplayMetrics().densityDpi; // 420 for Nexus 5x default screen size

        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(getContext());
        String phrases_set = mSharedPreferences.getString("phrases_file_list", "enron_mobile");

        if (phrases_set.compareTo("tweets") == 0)
            mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.TWEET, -1, true);
        else if (phrases_set.compareTo("phrases100") == 0)
            mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.MACKENZIE100, -1, true);
        else if (phrases_set.compareTo("phrases2") == 0)
            mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.MACKENZIE2, -1, true);
        else if (phrases_set.compareTo("enrone_mobie") == 0)
            mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.ENRON_MOBILE, -1, true);
        else if (phrases_set.compareTo("preview") == 0)
            mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.PREVIEW, -1, false);
        else
            mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.ENRON_MOBILE, -1, true);

        String numTests = mSharedPreferences.getString("num_phrases_list", "5");
        if (numTests.compareTo("") != 0) {
            mNumTests = Integer.parseInt(numTests);
        } else {
            mNumTests = 5;
        }

        String charDisplayMethod = mSharedPreferences.getString("method_conf_char_display", "DECODED");
        if (charDisplayMethod.compareTo("") != 0)
            mCharDisplayMethod = DisplayMode.valueOf(charDisplayMethod);
        else
            mCharDisplayMethod = DisplayMode.DECODED;

        mEnableBackKey = mSharedPreferences.getBoolean("method_conf_back_key", true);
        mEnableSuggestionBar = mSharedPreferences.getBoolean("method_conf_suggestion_bar", false);

        /*
        if (mEnableSuggestionBar) {
            for (TextView suggestionTextView : mSuggestionTextViews) {
                suggestionTextView.setVisibility(View.VISIBLE);
            }
            mSuggestionBar.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = mSuggestionBar.getLayoutParams();
            final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
            final int padding_left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            layoutParams.height = height;
            mPresentedText.setPadding(padding_left, 0, 0, 0);
            mPresentedText.setLayoutParams(layoutParams);
        } else {
            for (TextView suggestionTextView : mSuggestionTextViews) {
                suggestionTextView.setVisibility(View.GONE);
            }
            mSuggestionBar.setVisibility(View.GONE);
            ViewGroup.LayoutParams layoutParams = mSuggestionBar.getLayoutParams();
            final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
            final int padding_left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
            layoutParams.height = height;
            mPresentedText.setPadding(padding_left, 0, 0, 0);
            mPresentedText.setLayoutParams(layoutParams);
        }
        */

        mQwertyView.applyPreferences();
        mQwertyInput.initialize(mQwertyView, mQwertyView.getLayout(), dpi);

        initTest();
    }

    @Override
    public void onInput(final float x, final float y) {
        TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(x, y);
        mQwertyInput.processInput(x, y, touchInfo);
        updateTranscribedText();
        updateSuggestions();
        mTextEntryTest.updateKeyStroke(touchInfo);
    }

    private void updateSuggestions() {
        /*
        WordCorrector.PredictEntry[] words = mQwertyInput.getPredictedWords();
        for (int i = 0; i < mSuggestionTextViews.length; i++) {
            if (i < words.length)
                mSuggestionTextViews[i].setText(words[i].mStr);
            else
                mSuggestionTextViews[i].setText("");
        }
        */
        //Paint textPaint = mSuggestionTextView.getPaint();
        //Rect bounds = new Rect();
        //textPaint.getTextBounds(suggestions.toString(), 0, suggestions.length(), bounds);

        //Log.i(TAG, "width=" + bounds.width());
        //Log.i(TAG, "view width=" + mSuggestionTextView.getWidth());
        //mSuggestionTextView.setWidth(bounds.width()+40);
    }

    private enum DisplayMode {
        DECODED,
        STAR,
        GRAY,
        ORIGINAL
    }

    private String makeTranscribedWord(String decodedStr, String originalStr) {

        if (0 == decodedStr.length())
            return "";

        if (DisplayMode.DECODED == mCharDisplayMethod) {
            return decodedStr;
        } else if (DisplayMode.ORIGINAL == mCharDisplayMethod) {
            return originalStr;
        }

        StringBuilder retString = new StringBuilder();

        for (int i = 0; i < decodedStr.length(); i++) {
            if (decodedStr.charAt(i) == originalStr.charAt(i)) {
                retString.append(decodedStr.charAt(i));
            } else {
                if (DisplayMode.STAR == mCharDisplayMethod) {
                    retString.append('*');
                } else if (DisplayMode.GRAY == mCharDisplayMethod) {
                    retString.append("<font color=\"#a3a3a3\">" + decodedStr.charAt(i) + "</font>");
                }
            }
        }

        return retString.toString();
    }

    private void updateTranscribedText() {
        String inputWord;
        String predictedWord = "";
        if (mQwertyInput.isAutoCorrectOn()) {
            inputWord = "<u><b>" + makeTranscribedWord(mQwertyInput.getInputWord(), mQwertyInput.getRawInputWord()) + "</b></u>";
            if (mQwertyInput.getPredictedWord().length() > 0) {
                predictedWord = mQwertyInput.getPredictedWord().substring(mQwertyInput.getInputWord().length());
            }
        } else { // when auto correction is off, due to backkey after word completion, color-coded feedback is given to users to indicate auto-correction is off.
            inputWord = "<font color=\"#FF9900\"><u><b>" + mQwertyInput.getInputWord() + "</b></u></font>";
        }

        if (mQwertyInput.getInputText().length() > 0) {
            mTranscribedTextView.updateTranscribedTextView(mQwertyInput.getInputText() + " " + inputWord, predictedWord);
        } else {
            mTranscribedTextView.updateTranscribedTextView(inputWord, predictedWord);
        }
    }

    @Override
    public void flingLeft() {
        //Log.i(TAG, "flingLeft: " + mQwertyInput.getPredictedWord());
        if (mEnableBackKey) {
            TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(TextEntryTest.TouchType.SWIPE_LEFT);
            mTextEntryTest.updateKeyStroke(touchInfo);
            mQwertyInput.processBackWord();
            updateTranscribedText();
            updateSuggestions();
        }
    }

    @Override
    public void flingRight() {
        // word prediction
        //Log.i(TAG, "flingRight: " + mQwertyInput.getPredictedWord());

        TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(TextEntryTest.TouchType.SWIPE_RIGHT);

        mTextEntryTest.updateKeyStroke(touchInfo);
        mQwertyInput.processSpaceKey();
        updateTranscribedText();
        updateSuggestions();
    }

    @Override
    public void flingDown() {
        // Space key
        //Log.i(TAG, "flingDown: " + mQwertyInput.getPredictedWord());

        TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(TextEntryTest.TouchType.SWIPE_DOWN);

        mTextEntryTest.updateKeyStroke(touchInfo);
        mQwertyInput.applyPrediction();
        updateTranscribedText();
        updateSuggestions();
    }

    @Override
    public void flingUp() {
        /*
        gotoNextStep();
        */
    }

    @Override
    public void onLongPressed() {
        finishTest(true);

        String log = String.format(" Test Block exit(%d/%d).", mCurrIdx, mNumTests);
        Toast toast = Toast.makeText(getContext(), log, Toast.LENGTH_LONG);
        toast.show();

        mPresentedTextDisplay.setText(log);
    }

    private void gotoNextStep() {
        if (isBlockEnded()) {
            String log = String.format(" Test Block Ended(%d).", mNumTests);
            Toast toast = Toast.makeText(getContext(), log, Toast.LENGTH_LONG);
            toast.show();

            mPresentedTextDisplay.setText(log);

            exitActivity();
        }
        initTest();
    }

    private void endCurrentStep(TextEntryTest.TouchType touchType) {
        if (TextEntryTest.TestStatus.STARTED == mTextEntryTest.getTestStatus()) {
            // Let's report the result here.
            TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(touchType);
            mTextEntryTest.updateKeyStroke(touchInfo);
            mQwertyInput.processSpaceKey();
            updateTranscribedText();
            updateSuggestions();
            finishTest(false);
        }
    }

    private boolean isBlockEnded() {
        if (mCurrIdx == mNumTests) return true;
        else return false;
    }

    private boolean initTest() {
        if (isBlockEnded()) {
            return false;
        }

        if (mCurrIdx == 0) {
            boolean isNewLog = mSharedPreferences.getBoolean("others_new_log", false);
            mReportManager = ReportManager.getInstance(getContext());
            mReportManager.initialize(isNewLog);
            mReportManager.setTestInfo(mSharedPreferences);
        }

        mQwertyInput.enableAutoCorrection();
        mQwertyInput.resetInput();
        mPresentedTextDisplay.setText(mPhraseList.get(mCurrIdx));
        mTranscribedTextView.updateTranscribedTextView(mQwertyInput.getInputText(), "");
        mLog.setLength(0);
        mLogTextView.setText(mLog);
        mEnterButton.setText("End");

        mTextEntryTest = new TextEntryTest(mPhraseList.get(mCurrIdx++));


        mTextEntryTest.startTest();
        mQwertyInput.processPrediction(1); // Don't find first character prediction on sentence.

        updateSuggestions();

        return true;
    }

    private void finishTest(final boolean isExitActivity) {
        String log = "finishing test..";
        if (TextEntryTest.TestStatus.STARTED == mTextEntryTest.getTestStatus()) {

            String decodedSentence = mQwertyInput.decodeSentence(); // Sentence based decoding is done here!

            mTextEntryTest.endTest(decodedSentence);
            mTranscribedTextView.updateTranscribedTextView(decodedSentence, "");
            mReportManager.setTestResult(mTextEntryTest);
            mLog.setLength(0);
            mLogTextView.setText(mLog);
            log = String.format("WPM=%.2f, ErrRate=%.2f, KSPC=%.2f", mTextEntryTest.getWPM(), mTextEntryTest.getErrRate(), mTextEntryTest.getKSPC());
            mLog.insert(0, log + "\n");
            mLogTextView.setText(mLog);
            Log.d(TAG, log);
            if (isBlockEnded()) {
                mEnterButton.setText("OK");
            } else {
                mEnterButton.setText("Start");
            }
        }

        mPresentedTextDisplay.setText(" " + mPhraseList.get(mCurrIdx - 1) + "\n(" + log + ")");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(log)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isExitActivity) {
                            exitActivity();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void exitActivity() {
        mReportManager.dumpTestInfo();

        ((Activity) getContext()).onBackPressed();
    }
}
