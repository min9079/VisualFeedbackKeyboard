package kr.ac.snu.hcil.customkeyboard;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class QwertyLayoutTest {
    @Test
    public void getLine1_isCorrect() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);

        assertEquals("qwertyuiop", qwertyLayout.getLineFromIndex(0));
    }

    @Test
    public void getLine2_isCorrect() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);

        assertEquals("asdfghjkl", qwertyLayout.getLineFromIndex(1));
    }

    @Test
    public void getLine3_isCorrect() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);

        assertEquals("zxcvbnm", qwertyLayout.getLineFromIndex(2));
    }

    @Test
    public void getLine4_isCorrect() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);

        assertEquals("", qwertyLayout.getLineFromIndex(3));
    }

    @Test(expected = java.lang.ArrayIndexOutOfBoundsException.class)
    public void getLine_isError1() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);
        qwertyLayout.getLineFromIndex(-1);
    }

    @Test(expected = java.lang.ArrayIndexOutOfBoundsException.class)
    public void getLine_isError2() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);
        qwertyLayout.getLineFromIndex(4);
    }

    @Test
    public void getPosition_isCorrect() throws Exception {
        QwertyLayout qwertyLayout = new QwertyLayout(1920, 480, 0, 0);
        assertEquals(new QwertyLayout.Point(-1, -1), qwertyLayout.getPosition('!', QwertyLayout.Position.LeftBottom));
        assertEquals((0 + 0 * 1920f / 10), qwertyLayout.getPosition('q', QwertyLayout.Position.LeftBottom).x, 0.0f);
        assertEquals((0 + (0 + 1) * (480f / 4)), qwertyLayout.getPosition('q', QwertyLayout.Position.LeftBottom).y, 0.0f);
    }
}
