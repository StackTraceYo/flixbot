package org.stacktrace.yo.flixbot.commons;

import java.util.Arrays;

public class ArrayUtil {

    public static <T> T[] concat(T[]... arrays)
    {
        int finalLength = 0;
        for (T[] array : arrays) {
            finalLength += array.length;
        }

        T[] dest = null;
        int destPos = 0;

        for (T[] array : arrays)
        {
            if (dest == null) {
                dest = Arrays.copyOf(array, finalLength);
                destPos = array.length;
            } else {
                System.arraycopy(array, 0, dest, destPos, array.length);
                destPos += array.length;
            }
        }
        return dest;
    }

}
