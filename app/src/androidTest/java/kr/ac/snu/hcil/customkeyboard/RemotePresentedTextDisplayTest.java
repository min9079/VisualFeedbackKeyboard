package kr.ac.snu.hcil.customkeyboard;


import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

/**
 * Created by min90 on 10/07/2017.
 */

public class RemotePresentedTextDisplayTest {
    private static final String TAG = RemotePresentedTextDisplayTest.class.getName();
    private Context appContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void connectTest() throws Exception {
        RemotePresentedTextDisplay remotePresentedTextDisplay = new RemotePresentedTextDisplay(appContext);
        Thread.sleep(200);
        remotePresentedTextDisplay.setText("Hello World");
        Thread.sleep(1000);
    }

}
