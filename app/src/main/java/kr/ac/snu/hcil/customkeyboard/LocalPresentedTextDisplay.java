package kr.ac.snu.hcil.customkeyboard;

import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class LocalPresentedTextDisplay implements PresentedTextDisplay{

    private final TextView mTextView;

    LocalPresentedTextDisplay(TextView textView) {
        mTextView = textView;
        mTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void setText(String presentedText) {
        mTextView.scrollTo(0,0);
        mTextView.setText(presentedText);
    }
}
