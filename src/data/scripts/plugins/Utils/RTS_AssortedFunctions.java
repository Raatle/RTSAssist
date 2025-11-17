/****************************************************************************************
 * RTSAssist version 0.1.5
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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import data.scripts.plugins.RTSAssist;
import org.lwjgl.util.vector.Vector2f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RTS_AssortedFunctions {

    public class vector2fMaxComparator implements Comparator {
        @Override
        public int compare(Object obj1, Object obj2) {
            Vector2f a = (Vector2f)obj1;
            Vector2f b = (Vector2f)obj2;

            return (0);
        }
    }

    public static Comparator Vector2fComparator (Character type) {
        class vectorComparatorX implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {
                Vector2f a = (Vector2f)o1;
                Vector2f b = (Vector2f)o2;
                return (Float.compare(a.getX(), b.getX()));
            }
        }
        class vectorComparatorY implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {
                Vector2f a = (Vector2f)o1;
                Vector2f b = (Vector2f)o2;
                return (Float.compare(a.getY(), b.getY()));
            }
        }
        return (type.equals('x') ? new vectorComparatorX() : type.equals('y') ? new vectorComparatorY() : null);
    }

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
     * If LRSTest returns -1 (testCase is too high), testCase lowers, 1 will rise and 0 is a match.
     * This can be used to solve square roots for example.
     * @param startingVal Initial starting value. Must be higher than the predicted return value.
     *                    Must be positive. Will not return value lower than 0.
     * @param tFunc LRSTest instance. See LRSTest for usage.
     * @param iterCount How many iterations should be attempted before giving up.
     * @param accuracy Return value accuracy. 0 implies integer accuracy. 5 would imply a return
     *                 value accurate to 0.00001. Can save on iterations.
     * @param resolution This field aims to hone in on a singular solution where their are several.
     *                   A positive value implies we start high and descend. A negative implys start
     *                   at 0 an ascend. Steps are calculated as starting value over this value.
     *                   1, 0, -1 implys no resolution.
     * @return Best attempt at getting LRSTest instance to return 0.
     */
    public static float lastResortSolver (float startingVal, LRSTest tFunc, int iterCount, int accuracy, int resolution) {
        float testValHold = startingVal;
        float deltaTest = startingVal / 2;
        int tFuncReturn;
        float howAccurate = 1f;
        while (accuracy > 0) {
            howAccurate = howAccurate / 10f;
            accuracy--;
        }
        if (resolution != 1 && resolution != -1 && resolution != 0) {
            deltaTest = deltaTest / Math.abs(resolution);
            if (resolution > 0) {
                while (tFunc.test(testValHold) < 0 && testValHold > 0)
                    testValHold = testValHold - (startingVal / resolution);
                if (testValHold <= 0)
                    testValHold = startingVal /resolution;
                else
                    testValHold = testValHold + (testValHold / resolution);
            }
            else {
                testValHold = 0;
                while (tFunc.test(testValHold) > 0 && testValHold <= startingVal)
                    testValHold = testValHold + (startingVal / (resolution * -1));
                if (testValHold > startingVal)
                    testValHold = startingVal;
            }
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

    public static Vector2f getCollectiveCentre (List<Vector2f> VecColl) {
        Vector2f min = new Vector2f(1000000f, 1000000f);
        Vector2f max = new Vector2f(-1000000f,-1000000f);
        for (Vector2f item : VecColl) {
            min.setX(Math.min(min.getX(), item.getX()));
            min.setY(Math.min(min.getY(), item.getY()));
            max.setX(Math.max(max.getX(), item.getX()));
            max.setY(Math.max(max.getY(), item.getY()));
        }
        return (new Vector2f(
                (min.getX() + max.getX()) / 2f,
                (min.getY() + max.getY()) / 2f
        ));
    }
}

























