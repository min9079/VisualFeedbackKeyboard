package kr.ac.snu.hcil.customkeyboard;

/**
 * Created by min90 on 26/12/2016.
 */

public class QwertyLayout {
    private static final String TAG = QwertyLayout.class.getName();
//    private static final float SPACEBAR_ADJUST = 0f; // for spacebar

    public static class Point {
        public Point(float x, float y)
        {
            this.x = x;
            this.y = y;
        }
        public float x;
        public float y;

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Point)) {
                return false;
            }

            Point that = (Point) other;

            return (this.x == that.x) && (this.y == that.y);
        }
    }

    public enum Position {
        LeftBottom,
        Center
    }

    public float getBtnW() {
        return mBtnW;
    }

    public float getBtnH() {
        return mBtnH;
    }

    public float[] getLeftMargin() {
        return mLeftMargin;
    }

    public float getX()
    {
        return mX;
    }

    public float getY()
    {
        return mY;
    }

    private float mWidth = 0;
    private float mHeight = 0;
    private float mBtnW = 0;
    private float mBtnH = 0;
    private float mX = 0;
    private float mY = 0;
    private float mLeftMargin[] = {0,0,0,0};
    //private final String mQwertySequece[] = {"qwertyuiop", "asdfghjkl", "zxcvbnm", "@   <"}; // for spacebar
    private final String mQwertySequece[] = {"qwertyuiop", "asdfghjkl", "zxcvbnm", ""};

    public QwertyLayout(float width, float height, float x, float y) {
        mWidth = width;
        mHeight = height;
        mX = x;
        mY = y;
        mBtnW = mWidth / mQwertySequece[0].length();
        //mBtnH = mHeight / (mQwertySequece.length + SPACEBAR_ADJUST); // for spacebar
        mBtnH = mHeight / (mQwertySequece.length);

        mLeftMargin[0] = 0;
        mLeftMargin[1] += mLeftMargin[0] + mBtnW/2;
        mLeftMargin[2] += mLeftMargin[1] + mBtnW;
        //mLeftMargin[3] += mLeftMargin[2] + mBtnW; // for spacebar

        //Log.d(TAG,String.format("mWidth=%f, mHight=%f, mX=%f, mY=%f, mBtnW=%f, mBtnH=%f, mLeftMargin[0]=%f, mLeftMargin[1]=%f, mLeftMargin[2]=%f\n", mWidth, mHeight, mX, mY, mBtnW, mBtnH, mLeftMargin[0], mLeftMargin[1],mLeftMargin[2]));
    }

    public String getLineFromIndex(int row)
    {
        assert(row>=0 && row < mQwertySequece.length);
        return mQwertySequece[row];
    }

    public char getCharFromIndex(int row, int col) {
        assert(row>=0 && row < mQwertySequece.length);
        assert (col >= 0 && col < mQwertySequece[row].length());
        return mQwertySequece[row].charAt(col);
    }

    public Point getPosition(int lineIndex, int charIndex, Position pos) {
        float x = mX + mLeftMargin[lineIndex];
        float y = mY;

        //float lineIndex_ = lineIndex == mQwertySequece.length-1 ? lineIndex + SPACEBAR_ADJUST : lineIndex; // for spacebar
        float lineIndex_ = lineIndex;

        switch(pos){
            case LeftBottom:
                x += ((float)charIndex)*mBtnW;
                y += ((float)lineIndex_ + 1)*mBtnH;
                break;
            case Center:
                x += ((float)charIndex + 0.5)*mBtnW;
                y += ((float)lineIndex_ + 0.5)*mBtnH;
                break;
        }
        return new Point(x, y);
    }

    public Point getPosition(char c, Position pos){
        int lineIndex = 0;
        int charIndex = 0;


        for (lineIndex=0; lineIndex< mQwertySequece.length; lineIndex++) {
            charIndex = mQwertySequece[lineIndex].indexOf(c);
            if(charIndex != -1)
                break;
        }

        if (lineIndex == mQwertySequece.length) {
            return new Point(-1,-1);    // Not Found Error!
        }

        return getPosition(lineIndex, charIndex, pos);
    }

    public double getLength(Point p1, Point p2) {
        return Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2);
    }

    public char getChar(Point p, Position position, float threshold) {
        int lineIndex = 0;
        int charIndex = 0;
        float shortestLength = 1000000.0f;
        char retChar = '\0';

        for(lineIndex=0; lineIndex < mQwertySequece.length; lineIndex++) {
            for(charIndex=0; charIndex < mQwertySequece[lineIndex].length(); charIndex++) {
                float newLength = (float) getLength(p, getPosition(lineIndex,charIndex, position));
                if(newLength < shortestLength){
                    shortestLength = newLength;
                    retChar = mQwertySequece[lineIndex].charAt(charIndex);
                }
            }
        }
        if(threshold >=0 && threshold > shortestLength)
            return retChar;
        else
            return '\0';
    }
}
