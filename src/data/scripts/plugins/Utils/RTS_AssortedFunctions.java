/****************************************************************************************
 * RTSAssist version 0.1.0
 * Copyright (C) 2025, Raatle

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 ****************************************************************************************/

package data.scripts.plugins.Utils;

import org.lwjgl.util.vector.Vector2f;

public class RTS_AssortedFunctions {

    public static float fastSqrt (float var) { return ((float)fastSqrt((int)var)); }
    public static double fastSqrt (double var) { return ((double)fastSqrt((int)var)); }
    public static int fastSqrt (int var) {
        if (var <= 0)
            return (0);
        return (var < 1000
                        ? linearSearch(var)
                        : heronsMethod(var)
        );
    }

    /* wikipedia : https://en.m.wikipedia.org/wiki/Integer_square_root */
    private static int linearSearch (int var) {
        int L = 0;
        int a = 1;
        int d = 3;
        while (a <= var) {
            a = a + d;
            d = d + 2;
            L = L + 1;
        }
        return (L);
    }

    /* wikipedia : https://en.m.wikipedia.org/wiki/Integer_square_root */
    private static int heronsMethod (int var) {
        int x0 = var / 2;
        int x1 = (x0 + var / x0) / 2;
        while (x1 < x0) {
            x0 = x1;
            x1 = (x0 + var / x0) / 2;
        }
        return (x0);
    }

    public static double getDistanceSquared(Vector2f start, Vector2f end) {
        return (
                (Math.pow(Math.abs(start.getX() - end.getX()), 2)
                        + Math.pow(Math.abs(start.getY() - end.getY()), 2))
        );
    }

    public static int fastGetDistance(Vector2f start, Vector2f end) {
        return ((int)fastSqrt(getDistanceSquared(start, end)));
    }

    public interface LRSTest {
        /**
         * @param test values passed in from lastResortSolver.
         * @return 0 means hit. 1 means testcase is too high. -1 means testcase is too low.
         */
        int test(float test);
    }

    /**
     * Used to solve various problems iteratively. Trys to make LRSTest return 0.
     * If LRSTest returns -1, testCase lowers, 1 will rise and 0 is a match.
     * This can be used to solve square roots for example.
     * @param startingVal initial starting value. Must be higher than the predicted return value.
     * @param tFunc LRSTest instance. see LRSTest for usage.
     * @param iterCount how many iterations should be attempted before giving up.
     * @param accuracy 0 implies integer accuracy. 5 would imply a return value accurate to 0.00001.
     * @return Best attempt at getting LRSTest instance to return 0
     */
    public static float lastResortSolver(float startingVal, LRSTest tFunc, int iterCount, int accuracy) {
        float testValHold = startingVal / 2;
        float deltaTest = startingVal / 4;
        int tFuncReturn;
        float howAccurate = 1f;
        while (accuracy > 0) {
            howAccurate = howAccurate / 10f;
            accuracy--;
        }
        while (iterCount >= 0) {
            if (deltaTest < howAccurate)
                return (testValHold);
            tFuncReturn = tFunc.test(testValHold);
            if (tFuncReturn == 0)
                return (testValHold);
            if (tFuncReturn == 1)
                testValHold = testValHold + deltaTest;
            else
                testValHold = testValHold - deltaTest;
            deltaTest = deltaTest / 2;
            iterCount--;
        }
        return testValHold;
    }
}

























