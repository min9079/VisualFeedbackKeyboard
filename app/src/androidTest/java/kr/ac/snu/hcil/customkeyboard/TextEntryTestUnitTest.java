package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by min90 on 07/02/2017.
 */


@RunWith(AndroidJUnit4.class)
public class TextEntryTestUnitTest {
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private List<String> mPhraseList = null;

    @Before
    public void setUp() {
        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(appContext);
        mPhraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.TWEET, 10, false);
    }

    @Test(expected = IllegalStateException.class)
    public void singleTextEntryTest1() throws Exception {
        assertEquals(mPhraseList.get(0), "the best way to get in contact with you");
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.endTest("the best way to get in contact with you ");
    }

    @Test(expected = IllegalStateException.class)
    public void singleTextEntryTest2() throws Exception {
        assertEquals(mPhraseList.get(0), "the best way to get in contact with you");
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.startTest();
        assertEquals(1.0, textEntryTest.getKSPC(), 0.01);
    }

    @Test(expected = IllegalStateException.class)
    public void singleTextEntryTest3() throws Exception {
        assertEquals(mPhraseList.get(0), "the best way to get in contact with you");
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.startTest();
        assertEquals(0.0, textEntryTest.getErrRate(), 0.01);
    }

    @Test
    public void singleTextEntryTest4() throws Exception {
        String presentedPhrase = "the best way to get in contact with you";
        assertEquals(mPhraseList.get(0), presentedPhrase);
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.startTest();
        TextEntryTest.TouchInfo touchInfo = new TextEntryTest.TouchInfo(TextEntryTest.TouchType.TOUCH);

        for (int i = 0; i < presentedPhrase.length(); i++)
            textEntryTest.updateKeyStroke(touchInfo);
        textEntryTest.endTest(presentedPhrase);
        assertEquals(1.0, textEntryTest.getKSPC(), 0.01);
    }

    @Test
    public void singleTextEntryTest5() throws Exception {
        assertEquals(mPhraseList.get(0), "the best way to get in contact with you");
        TextEntryTest textEntryTest = new TextEntryTest(mPhraseList.get(0));
        textEntryTest.startTest();
        textEntryTest.endTest("the best way to get in contact with you");
        assertEquals(0.0, textEntryTest.getErrRate(), 0.01);
    }

    @Test
    public void singleTextEntryTest6() throws Exception {
        TextEntryTest textEntryTest = new TextEntryTest("the quick brown fox");
        textEntryTest.startTest();
        textEntryTest.endTest("the quixck brwn fox");
        assertEquals(2.0 / 19, textEntryTest.getErrRate(), 0.01);
    }

    @Test
    public void singleTextEntryTest7() throws Exception {
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
    }


}
