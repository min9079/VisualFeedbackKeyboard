package kr.ac.snu.hcil.customkeyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by min90 on 10/07/2017.
 */

public class CompositePresentedTextDisplay implements PresentedTextDisplay {
    private List<PresentedTextDisplay> mPresentedTextDisplays = new ArrayList<PresentedTextDisplay>();

    @Override
    public void setText(String presentedText) {
        for (PresentedTextDisplay presentedTextDisplay : mPresentedTextDisplays) {
            presentedTextDisplay.setText(presentedText);
        }
    }

    public void add(PresentedTextDisplay presentedTextDisplay) {
        mPresentedTextDisplays.add(presentedTextDisplay);
    }

    public void remove(PresentedTextDisplay presentedTextDisplay) {
        mPresentedTextDisplays.remove(presentedTextDisplay);
    }
}
