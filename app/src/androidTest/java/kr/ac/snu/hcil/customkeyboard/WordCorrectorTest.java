package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class WordCorrectorTest {
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private WordCorrector wc = WordCorrector.getInstance();
    private QwertyLayout layout;

    @Test
    public void useAppContext() throws Exception {
        assertEquals("kr.ac.snu.hcil.customkeyboard", appContext.getPackageName());
    }

    @Before
    public void testSetup() throws Exception {
        layout = new QwertyLayout(1070, 253, 10, 10);
        int dpi = appContext.getResources().getDisplayMetrics().densityDpi; // 420 for Nexus 5x default screen size
        wc.init(new WordCorrector.ConfBuilder(layout, dpi)
                .setFingerType(WordCorrector.ConfBuilder.FingerType.NORMAL)
                .setIsVerbose(true)
                .build());
    }

    @Test
    public void setupKeyboardTest() throws Exception {
        wc.setContext("i love you");
        for (char ch : "soo".toCharArray()) {
            wc.decodeKey(ch);
        }
        assertEquals("so", wc.decodeWord()[0].mStr);
    }

    @Test
    public void setContextTest() throws Exception {
        wc.setContext("");
        wc.setContext(null);
        wc.setContext("hi");
        wc.setContext("i love you");
        wc.setContext(" ");
        wc.setContext("find me ");
        wc.setContext("how about long context ok");
        wc.setContext(" and this");
    }

    // decode via charKey
    private String decodeWord(String context, String inWord) {
        wc.setContext(context);
        for (char c : inWord.toCharArray()) {
            wc.decodeKey(c);
        }
        return wc.decodeWord()[0].mStr;
    }

    // decode via Touchkey
    private String decodeWord2(String context, String inWord) {
        wc.setContext(context);
        for (char c : inWord.toCharArray()) {
            QwertyLayout.Point pt = layout.getPosition(c, QwertyLayout.Position.Center);
            wc.decodeTouchKey(pt.x, pt.y + layout.getBtnH() / 3);
        }
        return wc.decodeWord()[0].mStr;
    }

    // check decodeKey is work fine
    // check decodeTouchKey is work fine
    @Test
    public void decodeKeyTest() throws Exception {
        class t_case {
            t_case(String c, String i, String o) {
                context = c;
                inWord = i;
                outWord = o;
            }

            String context;
            String inWord;
            String outWord;
        }
        t_case test_cases[] = {
                new t_case("i love you", "so", "so"),
                new t_case("i love you", "soo", "so"),
                new t_case("i love you", "s", "so"),
        };

        for (int i = 0; i < test_cases.length; i++) {
            assertEquals(test_cases[i].outWord, decodeWord(test_cases[i].context, test_cases[i].inWord));
            assertEquals(test_cases[i].outWord, decodeWord2(test_cases[i].context, test_cases[i].inWord));
        }
    }

    // check edge cases for calling backKey() several times
    @Test
    public void backKeyTest() throws Exception {
        wc.setContext("i love you");
        wc.decodeKey('c');
        assertEquals("", wc.backKey());
        wc.decodeKey('s');
        wc.decodeKey('o');
        wc.decodeKey('o');
        assertEquals("so", wc.backKey());
        assertEquals("s", wc.backKey());
        assertEquals("", wc.backKey());
        wc.backKey(); // what if, when there is no remaining input key.
    }

    // check edge cases for calling consecutive decodeWords (with empty input)
    @Test
    public void decodeWordsTest() throws Exception {
        wc.setContext("i love you");

        for (char c : "soo".toCharArray()) {
            wc.decodeKey(c);
        }
        assertEquals("so", wc.decodeWord()[0].mStr);
        wc.setContext("i love you");
        wc.decodeKey('s');
        assertEquals("so", wc.decodeWord()[0].mStr);
        wc.backKey();
        assertEquals(0, wc.decodeWord().length);
    }

    @Test
    public void predictCharTest() throws Exception {
        wc.setContext("i love you");
        wc.decodeKey('s');
        WordCorrector.PredictEntry[] predictEntries = wc.predictChar(26, 0.05f);

        assertEquals("o", predictEntries[0].mStr);
    }

    @Test
    public void predictWordTest() throws Exception {
        // Make sure follwing: add push lm_corpus /data/local/tmp/external/lm_corpus


        wc.setContext("okay i love you");
        wc.decodeKey('s');

        WordCorrector.PredictEntry[] predictEntries = wc.predictWord(1000, 0.05f);

        assertEquals("so", predictEntries[0].mStr);
    }

    @After
    public void teardownKeyboardTest() throws Exception {
        wc.cleanup();
    }

}
