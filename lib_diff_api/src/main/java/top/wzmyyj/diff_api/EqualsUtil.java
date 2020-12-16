package top.wzmyyj.diff_api;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Created on 2020/12/16.
 *
 * @author feling
 * @version 1.1.0
 * @since 1.1.0
 */
@SuppressWarnings("unused")
public final class EqualsUtil {

    private static final float FLOAT_ACCURACY = 0.000001f;
    private static final double DOUBLE_ACCURACY = 0.00000001;

    public static boolean unEquals(boolean x, boolean y) {
        return x != y;
    }

    public static boolean unEquals(byte x, byte y) {
        return x != y;
    }

    public static boolean unEquals(short x, short y) {
        return x != y;
    }

    public static boolean unEquals(int x, int y) {
        return x != y;
    }

    public static boolean unEquals(long x, long y) {
        return x != y;
    }

    public static boolean unEquals(char x, char y) {
        return x != y;
    }

    public static boolean unEquals(float x, float y) {
        return Math.abs(x - y) > FLOAT_ACCURACY;
    }

    public static boolean unEquals(double x, double y) {
        return Math.abs(x - y) > DOUBLE_ACCURACY;
    }

    public static boolean unEquals(@Nullable Object x, @Nullable Object y) {
        return !Objects.equals(x, y);
    }
}
