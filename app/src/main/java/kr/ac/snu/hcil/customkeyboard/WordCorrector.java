package kr.ac.snu.hcil.customkeyboard;

/**
 * Created by min90 on 30/12/2016.
 */

public class WordCorrector {
    private static final String TAG = WordCorrector.class.getName();
    private static WordCorrector instance;
    private WordCorrector() {}

    static {
        System.loadLibrary("kenlm");
        System.loadLibrary("metaphone");
        System.loadLibrary("mobile_word_corrector");
        System.loadLibrary("mwc");
    }

    public static synchronized WordCorrector getInstance() {
        if(instance == null)
            instance = new WordCorrector();
        return instance;
    }

    /* stand-alone api for testing ndk library */
    public native String correctWord(String input_string);

    /* belows are series of API to decode char based input */
    public native void setContext(String context_string);

    public native void clearSentence();

    public native String decodeKey(char key);

    public native TouchString decodeTouchKey(float x, float y);

    public native PredictEntry[] decodeWord();

    public native PredictEntry[] decodeSentence(); // make sure backWord is called correctly to get the proper result.

    public native String backKey();

    public native int backWord();

    public native void init(Configuration buildConf);

    public native void cleanup();

    public native PredictEntry[] predictChar(int nOfCandidates, float threshold);

    public native PredictEntry[] predictWord(int nOfCandidates, float threshold);

    public native PredictEntry[] predictWord(int nOfCandidates, float threshold, char c);

    public native void pushWord(String mInputWord);

    public native char getChar(int x, int y);

    public static class TouchString {
        public String mStr;
        public String mOriStr;
    }

    public static class PredictEntry {
        public String mStr;
        public float mProb;
    }

    public static class Configuration {

        public String mLMName = "twitter";
        public String mEMName = "all";
        public int mEMOrder = 4;
        public float mCER = (float) 0.02;
        public float mLambda = (float) 1.2;
        public String mKeyboard = "custom";
        public WordCorrector.ConfBuilder.FingerType mFingerType = WordCorrector.ConfBuilder.FingerType.NORMAL;
        public boolean mIsVerbose = false;

        public float mBtnW = 0;
        public float mBtnH = 0;
        public float mLeftMargin[];
        public int mDpi = 0;
    }

    public static class ConfBuilder{
        enum FingerType { NORMAL, THUMB, INDEX }

        Configuration mConf = new Configuration();

        public ConfBuilder(QwertyLayout layout, int dpi) {
            mConf.mBtnW = layout.getBtnW();
            mConf.mBtnH = layout.getBtnH();
            mConf.mLeftMargin = layout.getLeftMargin();
            mConf.mDpi = dpi;
        }

        public ConfBuilder(String keyboard) {
            mConf.mKeyboard = keyboard;
        }

        public ConfBuilder setLMName(String LMName) {
            mConf.mLMName = LMName;
            return this;
        }

        public ConfBuilder setEMName(String EMName) {
            mConf.mEMName = EMName;
            return this;
        }

        public ConfBuilder setEMOrder(int EMOrder) {
            mConf.mEMOrder = EMOrder;
            return this;
        }

        public ConfBuilder setCER(float CER) {
            mConf.mCER = CER;
            return this;
        }

        public ConfBuilder setLAMBDA(float Lamba) {
            mConf.mLambda = Lamba;
            return this;
        }

        public ConfBuilder setFingerType(FingerType fingerType) {
            mConf.mFingerType = fingerType;
            return this;
        }

        public ConfBuilder setIsVerbose(boolean isVerbose) {
            mConf.mIsVerbose = isVerbose;
            return this;
        }

        public Configuration build() {
            return mConf;
        }
    }
}
