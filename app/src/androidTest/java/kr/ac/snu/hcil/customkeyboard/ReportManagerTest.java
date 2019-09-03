package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;


/**
 * Created by min90 on 09/05/2017.
 */

@RunWith(AndroidJUnit4.class)
public class ReportManagerTest {
    private static final String TAG = ReportManagerTest.class.getName();
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private List<String> mPhraseList = null;
    private ReportManager mReportManager = null;

    @Before
    public void setUp() {
        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(appContext);
        mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.TWEET, 10, false);

        mReportManager = ReportManager.getInstance(appContext);
        mReportManager.initialize(true);
    }

    @Test
    public void testSetTestInfo() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        assertEquals(true, mReportManager.setTestInfo(sharedPreferences));
        assertEquals(true, mReportManager.dumpTestInfo());
    }

    @Test
    public void testSetTestResult() {
        // setup TextEntryTest
        assertEquals(mPhraseList.get(0), "the best way to get in contact with you");
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.startTest();
        TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(TextEntryTest.TouchType.TOUCH);
        for (int i = 0; i < mPhraseList.get(0).length(); i++) {
            textEntryTest.updateKeyStroke(touchInfo);
        }
        textEntryTest.endTest("the best way to get in contact with you");
        assertEquals(0.0, textEntryTest.getErrRate(), 0.01);
        assertEquals(1.0, textEntryTest.getKSPC(), 0.01);

        assertEquals(true, mReportManager.setTestResult(textEntryTest));
        assertEquals(true, mReportManager.dumpTestInfo());
    }

    @Test
    public void testAll() {
        // setup TextEntryTest
        assertEquals(mPhraseList.get(0), "the best way to get in contact with you");
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.startTest();
        TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(TextEntryTest.TouchType.TOUCH);
        for (int i = 0; i < mPhraseList.get(0).length(); i++) {
            textEntryTest.updateKeyStroke(touchInfo);
        }
        textEntryTest.endTest("the best way to get in contact with you");
        assertEquals(0.0, textEntryTest.getErrRate(), 0.01);
        assertEquals(1.0, textEntryTest.getKSPC(), 0.01);

        // set experiment info.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        assertEquals(true, mReportManager.setTestInfo(sharedPreferences));

        // set experiment result.
        assertEquals(true, mReportManager.setTestResult(textEntryTest));
        assertEquals(true, mReportManager.dumpTestInfo());
    }
}
