package kr.ac.snu.hcil.customkeyboard;

import android.support.v4.app.Fragment;

public class TestActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return CustomKeyboardFragment.newInstance();
    }
}
