package kr.ac.snu.hcil.customkeyboard;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by min90 on 21/01/2017.
 */

public class TDDExample {
    @Test
    public void testFibonacci() {
        int cases[][] = {{0, 0}, {1, 1}, {2, 1}, {3, 2}};
        for (int i = 0; i < cases.length; i++) {
            assertEquals(cases[i][1], fib(cases[i][0]));
        }
    }

    int fib(int n) {
        if (n == 0) {
            return 0;
        }
        if (n <= 2) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }
}
