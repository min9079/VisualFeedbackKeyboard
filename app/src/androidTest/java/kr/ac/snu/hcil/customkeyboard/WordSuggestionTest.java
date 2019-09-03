package kr.ac.snu.hcil.customkeyboard;

/**
 * Created by min90 on 01/01/2018.
 */

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)

public class WordSuggestionTest {
    private static final String TAG = WordSuggestionTest.class.getName();
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private WordCorrector wc = WordCorrector.getInstance();
    private QwertyLayout layout;
    private List<String> phraseList;

    private int m_ngram = 4;
    private int m_nOfCandidates = 1000;

    private int m_totalWords = 0;
    private int m_totalNoHit = 0;
    private int m_totalChars = 0;
    private int m_totalKeystrokes = 0;

    private double m_startTime;
    private double m_finishTime;

    @Before
    public void testSetup() throws Exception {
        m_ngram = 4; // default value
        m_totalWords = 0;
        m_totalNoHit = 0;
        m_totalChars = 0;
        m_totalKeystrokes = 0;

        layout = new QwertyLayout(1070, 253, 10, 10);
        int dpi = appContext.getResources().getDisplayMetrics().densityDpi; // 420 for Nexus 5x default screen size
        wc.init(new WordCorrector.ConfBuilder(layout, dpi)
                .setFingerType(WordCorrector.ConfBuilder.FingerType.NORMAL)
                .setIsVerbose(true)
                .build());
        PresentedTextRepository presentedTextRepository = PresentedTextRepository.getInstance(appContext);
        phraseList = presentedTextRepository.getPhraseList(PresentedTextRepository.PhraseSetType.MACKENZIE2, -1, false);

        m_startTime = System.nanoTime();
    }

    @Test
    public void checkPhraseListForMackenzie2() throws Exception {
        assertEquals("my watch fell in the water", phraseList.get(0));
        assertEquals("elections bring out the best", phraseList.get(7));
        assertEquals(500, phraseList.size());
        Log.d(TAG, "size of MACKENZIE2:" + phraseList.size());
    }

    private int isHit(String targetWord, WordCorrector.PredictEntry[] predictedWords, int k) {
        for (int i = 0; i < k && i < predictedWords.length; i++) {
            //Log.d(TAG, i + predictedWords[i].mStr);
            if (predictedWords[i].mStr.compareTo(targetWord) == 0) {
                return i;
            }
        }
        return k;
    }

    private void simulateSentence(String sentence, int k) {
        String[] words = sentence.split("\\s");
        int wordIndex = 0;
        char[] charArray = sentence.toCharArray();
        InputText contextWords = new InputText();

        String currentWord = "";

        wc.setContext(contextWords.getContextString(m_ngram - 1));
        WordCorrector.PredictEntry[] predictEntries = new WordCorrector.PredictEntry[0];
        int hitIndex = k;

        for (int i = 0; i < charArray.length; i++) {
            m_totalChars++;
            if (charArray[i] != ' ') // process each key input
            {
                if (hitIndex == k) {
                    predictEntries = wc.predictWord(m_nOfCandidates, -1);
                    hitIndex = isHit(words[wordIndex], predictEntries, k);
                }
                if (hitIndex < k) {
                    //Log.d(TAG, "predicted at " + hitIndex + ":" + predictEntries[hitIndex].mStr);
                } else {
                    m_totalKeystrokes++; // for select a key
                    wc.decodeKey(charArray[i]);
                }
                currentWord += charArray[i];

            } else { // process a word
                contextWords.pushWord(new InputText.InputWord(currentWord));
                wc.setContext(contextWords.getContextString(m_ngram - 1));
                wordIndex++;
                m_totalWords++;
                if (hitIndex == k) { // word prediction failure.
                    m_totalNoHit++;
                    m_totalKeystrokes++; // for select space key
                } else {
                    m_totalKeystrokes++; // for select suggestion box
                }
                currentWord = "";
                hitIndex = k;
                //Log.d(TAG, "context:" + contextWords.getString());
            }
            //Log.d(TAG, "currentWord:" + currentWord);
        }

        m_totalWords++;
        if (hitIndex == k) { // word prediction failure.
            m_totalNoHit++;
        } else {
            m_totalKeystrokes++; // for select suggestion box
        }
        //contextWords.pushWord(new InputText.InputWord(currentWord)); // process last word in a sentence
        //Log.d(TAG, "context:" + contextWords.getString());
    }

    private boolean simulateSentence(int k) throws Exception {
        Log.d(TAG, "k=" + k + ", ngram=" + m_ngram);
        for (int strIndex = 0; strIndex < phraseList.size(); strIndex++) {
            String sentence = phraseList.get(strIndex);
            simulateSentence(sentence, k);
            if (strIndex % 10 == 0) {
                Log.d(TAG, "strIndex=" + strIndex);
            }
        }
        return true;
    }

    @Test
    public void testOneSentence() throws Exception {
        m_ngram = 4;

        String sentence = phraseList.get(0);
        simulateSentence(sentence, 4);
    }

    @Test
    public void simulateSentence4K4GRAM() throws Exception {
        m_ngram = 4;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence3K4GRAM() throws Exception {
        m_ngram = 4;

        assertEquals(true, simulateSentence(3));
    }

    @Test
    public void simulateSentence2K4GRAM() throws Exception {
        m_ngram = 4;

        assertEquals(true, simulateSentence(2));
    }

    @Test
    public void simulateSentence1K4GRAM() throws Exception {
        m_ngram = 4;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence4K3GRAM() throws Exception {
        m_ngram = 3;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence3K3GRAM() throws Exception {
        m_ngram = 3;

        assertEquals(true, simulateSentence(3));
    }

    @Test
    public void simulateSentence2K3GRAM() throws Exception {
        m_ngram = 3;

        assertEquals(true, simulateSentence(2));
    }

    @Test
    public void simulateSentence1K3GRAM() throws Exception {
        m_ngram = 3;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence4K2GRAM() throws Exception {
        m_ngram = 2;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence3K2GRAM() throws Exception {
        m_ngram = 2;

        assertEquals(true, simulateSentence(3));
    }

    @Test
    public void simulateSentence2K2GRAM() throws Exception {
        m_ngram = 2;

        assertEquals(true, simulateSentence(2));
    }

    @Test
    public void simulateSentence1K2GRAM() throws Exception {
        m_ngram = 2;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence4K1GRAM() throws Exception {
        m_ngram = 1;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence3K1GRAM() throws Exception {
        m_ngram = 1;

        assertEquals(true, simulateSentence(3));
    }

    @Test
    public void simulateSentence2K1GRAM() throws Exception {
        m_ngram = 1;

        assertEquals(true, simulateSentence(2));
    }

    @Test
    public void simulateSentence1K1GRAM() throws Exception {
        m_ngram = 1;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence1K4GRAM10() throws Exception {
        m_nOfCandidates = 10;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence1K4GRAM100() throws Exception {
        m_nOfCandidates = 100;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence1K4GRAM1000() throws Exception {
        m_nOfCandidates = 1000;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence1K4GRAM10000() throws Exception {
        m_nOfCandidates = 10000;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence1K4GRAM100000() throws Exception {
        m_nOfCandidates = 100000;

        assertEquals(true, simulateSentence(1));
    }

    @Test
    public void simulateSentence4K4GRAM10() throws Exception {
        m_nOfCandidates = 10;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence4K4GRAM100() throws Exception {
        m_nOfCandidates = 100;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence4K4GRAM1000() throws Exception {
        m_nOfCandidates = 1000;

        assertEquals(true, simulateSentence(4));
    }

    @Test
    public void simulateSentence4K4GRAM10000() throws Exception {
        m_nOfCandidates = 10000;

        assertEquals(true, simulateSentence(4));
    }

    @After
    public void teardownKeyboardTest() throws Exception {
        m_finishTime = System.nanoTime();
        wc.cleanup();
        Log.d(TAG, "Total chars: " + m_totalChars);
        Log.d(TAG, "Total keystrokes: " + m_totalKeystrokes);
        Log.d(TAG, "Total words: " + m_totalWords);
        Log.d(TAG, "Total no hit: " + m_totalNoHit);
        Log.d(TAG, "Total duration (ms): " + (m_finishTime - m_startTime)/1000000 );
    }

}
