package top.wzmyyj.diff_api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2020/12/02.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
public class Payload {

    private static final String TAG = Payload.class.getSimpleName();

    private final Map<String, Object> map = new HashMap<>();

    public void put(@NonNull String key, Object value) {
        map.put(key, value);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public int size() {
        return map.size();
    }

    public boolean isChange(@NonNull String key) {
        return map.containsKey(key);
    }

    @Nullable
    public Object get(@NonNull String key) {
        return map.get(key);
    }

    public boolean getBoolean(@NonNull String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object o = map.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Boolean) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", defaultValue, e);
            return defaultValue;
        }
    }

    public int getInt(@NonNull String key) {
        return getInt(key, 0);
    }

    public int getInt(@NonNull String key, int defaultValue) {
        Object o = map.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", defaultValue, e);
            return defaultValue;
        }
    }

    public long getLong(@NonNull String key) {
        return getLong(key, 0L);
    }

    public long getLong(@NonNull String key, long defaultValue) {
        Object o = map.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Long) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", defaultValue, e);
            return defaultValue;
        }
    }

    public float getFloat(@NonNull String key) {
        return getFloat(key, 0F);
    }

    public float getFloat(@NonNull String key, float defaultValue) {
        Object o = map.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", defaultValue, e);
            return defaultValue;
        }
    }

    public double getDouble(@NonNull String key) {
        return getDouble(key, 0D);
    }

    public double getDouble(@NonNull String key, double defaultValue) {
        Object o = map.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", defaultValue, e);
            return defaultValue;
        }
    }

    @Nullable
    public String getString(@NonNull String key) {
        return getString(key, null);
    }

    @Nullable
    public String getString(@NonNull String key, String defaultValue) {
        Object o = map.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", defaultValue, e);
            return defaultValue;
        }
    }

    @Nullable
    public Payload getPayload(@NonNull String key) {
        Object o = map.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Payload) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Payload", null, e);
            return null;
        }
    }

    @Nullable
    public <T> T getType(@NonNull String key) {
        return getType(key, null);
    }

    @Nullable
    public <T> T getType(@NonNull String key, @Nullable Class<T> clazz) {
        Object o = map.get(key);
        if (o == null) {
            return null;
        }
        try {
            //noinspection unchecked
            return (T) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, clazz == null ? "T" : clazz.getSimpleName(), null, e);
            return null;
        }
    }

    private void typeWarning(String key, Object value, String className,
                             Object defaultValue, ClassCastException e) {
        String sb = "Key " +
                key +
                " expected " +
                className +
                " but value was a " +
                value.getClass().getName() +
                ".  The default value " +
                defaultValue +
                " was returned.";
        Log.w(TAG, sb);
        Log.w(TAG, "Attempt to cast generated internal exception:", e);
    }
}
