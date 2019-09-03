package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by min90 on 07/02/2017.
 */


@RunWith(AndroidJUnit4.class)
public class PresentedTextRepositoryUnitTest {
    private static final String TAG = PresentedTextRepositoryUnitTest.class.getName();
    private Context appContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void getPhraseListForTweet() throws Exception {
        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(appContext);
        List<String> phraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.TWEET, 10, false);

        assertEquals(10, phraseList.size());
        assertEquals("the best way to get in contact with you", phraseList.get(0));
        assertEquals("they obviously learned from a few mistake", phraseList.get(9));
    }

    @Test
    public void getPhraseListForMackenzie() throws Exception {
        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(appContext);
        List<String> phraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.MACKENZIE100, 100, false);

        assertEquals(100, phraseList.size());
        assertEquals("My watch fell in the water.".toLowerCase(), phraseList.get(0));
        assertEquals("Watch out for low flying objects.".toLowerCase(), phraseList.get(9));

        phraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.MACKENZIE2, -1, false);

        assertEquals("my watch fell in the water", phraseList.get(0));
        assertEquals("elections bring out the best", phraseList.get(7));
        assertEquals(500, phraseList.size());
        Log.d(TAG, "size of MACKENZIE2:" + phraseList.size());
    }

    @Test
    public void getPhraseListForEnronMobile() throws Exception {
        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(appContext);
        List<String> phraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.ENRON_MOBILE, 10, false);

        assertEquals(10, phraseList.size());
        assertEquals("are you going to join us for lunch", phraseList.get(0));
        assertEquals("how are you", phraseList.get(3));
    }
}
