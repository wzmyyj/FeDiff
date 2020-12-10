package top.wzmyyj.diff_api;

import androidx.annotation.Keep;

/**
 * Created on 2020/12/09.
 *
 * @author feling
 * @version 1
 * @since 1
 */
public class Main {

    @Keep
    public static void main(String[] args) {
        boolean d = true;
        d &= f1();
        d &= f2();
        System.out.println("d=" + d);
    }

    private static boolean f1() {
        System.out.println("f1");
        return false;
    }

    private static boolean f2() {
        System.out.println("f2");
        return true;
    }
}
