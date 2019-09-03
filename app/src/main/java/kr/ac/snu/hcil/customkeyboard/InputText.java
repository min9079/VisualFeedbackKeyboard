package kr.ac.snu.hcil.customkeyboard;

import java.util.ArrayList;

import static java.lang.Math.max;

/**
 * Created by min90 on 02/06/2017.
 */

class InputText {
    static class Point {
        public float x;
        public float y;
    }

    static class InputWord {
        public InputWord(String word) {
            mWord = new StringBuilder(word);
            mPoints = new ArrayList<>(15);
        }

        public InputWord() {
            mWord = new StringBuilder();
            mPoints = new ArrayList<>(15);
        }

        public InputWord(InputWord word) {
            mWord = new StringBuilder(word.mWord);
            mPoints = new ArrayList<>(word.mPoints);
        }

        void pushCharacter(char c, float x, float y) {
            mWord.append(c);
            Point point = new Point();
            point.x = x;
            point.y = y;
            mPoints.add(point);
        }

        void popCharacter() {
            if(mWord.length()>0)
                mWord.setLength(mWord.length()-1);

            if(mPoints.size()>0)
                mPoints.remove(mPoints.size() - 1);
        }

        void clear() {
            mWord.setLength(0);
            mPoints.clear();
        }

        int length() {
            return mWord.length();
        }

        StringBuilder mWord = null;
        ArrayList<Point> mPoints = null;
    }

    private ArrayList<InputWord> mInputWords = new ArrayList<InputWord>();

    public void clear() {
        mInputWords.clear();
    }

    public int length() {
        return getString().length();
    }

    public void pushWord(InputWord word)
    {
        mInputWords.add(new InputWord(word));
    }

    public void popWord() {
        if(mInputWords.size() > 0)
            mInputWords.remove(mInputWords.size() - 1);
    }

    public InputWord peekWord() {
        if (mInputWords.size() > 0) {
            return mInputWords.get(mInputWords.size() - 1);
        }
        return new InputWord();
    }

    public String getString() {
        String ret = "";

        if (mInputWords.size() > 0) {
            ret = mInputWords.get(0).mWord.toString();
        }

        for (int i=1; i< mInputWords.size(); i++) {
            ret += " " + mInputWords.get(i).mWord;
        }

        return ret;
    }

    public String getContextString(int length) {
        String ret = "";

        if (length==0)
            return ret;

        if (mInputWords.size() > 0) {

            int index = max(0, mInputWords.size() - length);

            ret = mInputWords.get(index).mWord.toString();

            for (int i = index + 1; i < mInputWords.size(); i++) {
                ret += " " + mInputWords.get(i).mWord;
            }
        }

        return ret;
    }
}
