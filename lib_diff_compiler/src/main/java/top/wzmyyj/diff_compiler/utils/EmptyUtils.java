package top.wzmyyj.diff_compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created on 2020/12/03.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
public class EmptyUtils {

    public static boolean isNullOrEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNullOrEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
