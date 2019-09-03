package kr.ac.snu.hcil.customkeyboard;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by min90 on 02/06/2017.
 */

public class InputTextTest {
    @Test
    public void getStringTest() {
        InputText inputText = new InputText();

        inputText.pushWord(new InputText.InputWord("hello"));
        inputText.pushWord(new InputText.InputWord("world"));
        inputText.pushWord(new InputText.InputWord("InputText"));

        assertEquals("hello world InputText", inputText.getString());
    }

    @Test
    public void popStringTest() {
        InputText inputText = new InputText();

        inputText.pushWord(new InputText.InputWord("hello"));
        inputText.pushWord(new InputText.InputWord("world"));
        inputText.popWord();
        assertEquals("hello", inputText.getString());
        inputText.popWord();
        assertEquals("", inputText.getString());
    }

    @Test
    public void lengthTest() {
        InputText inputText = new InputText();

        assertEquals(0, inputText.length());

        inputText.pushWord(new InputText.InputWord("hello"));
        assertEquals(5, inputText.length());

        inputText.pushWord(new InputText.InputWord("world"));
        assertEquals(11, inputText.length());
    }

    @Test
    public void peekTest() {
        InputText inputText = new InputText();
        assertEquals("", inputText.peekWord().mWord.toString());

        inputText.pushWord(new InputText.InputWord("hello"));
        assertEquals("hello", inputText.peekWord().mWord.toString());

        inputText.pushWord(new InputText.InputWord("world"));
        assertEquals("world", inputText.peekWord().mWord.toString());
    }

    @Test
    public void clearTest() {
        InputText inputText = new InputText();

        inputText.pushWord(new InputText.InputWord("hello"));
        inputText.clear();

        assertEquals("", inputText.getString());
    }

    @Test
    public void getContextTest() {
        InputText inputText = new InputText();

        assertEquals("", inputText.getContextString(3));

        inputText.pushWord(new InputText.InputWord("i"));
        assertEquals("i", inputText.getContextString(3));

        inputText.pushWord(new InputText.InputWord("love"));
        assertEquals("i love", inputText.getContextString(3));

        inputText.pushWord(new InputText.InputWord("you"));
        assertEquals("i love you", inputText.getContextString(3));

        inputText.pushWord(new InputText.InputWord("so"));
        assertEquals("love you so", inputText.getContextString(3));
    }
}
