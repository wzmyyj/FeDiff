package top.wzmyyj.diff_api;

import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2020/12/08.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.0.0
 */
public final class FactoryManager {

    private static final String DIFF_API_PACKAGE = "top.wzmyyj.diff_api";
    private static final String FACTORY_HELPER_NAME = "Diff$$FactoryHelperImpl";

    private static final int MAX_FACTORY_CACHE_SIZE = 50;

    // 由于model种类不确定，可能很多，IDiffModelFactory时间复杂度不确定，因此做缓存工厂
    private final LruCache<Class<?>, IDiffModelFactory> cache = new LruCache<>(MAX_FACTORY_CACHE_SIZE);

    // 正真创建工厂的对象
    private final DiffFactoryHelperWrapper mWrapper = new DiffFactoryHelperWrapper();

    synchronized void addHelpers(List<IDiffFactoryHelper> helperList) {
        mWrapper.helpers.addAll(helperList);
    }

    /**
     * 获取工厂。
     *
     * @param model model对象
     * @return 工厂
     */
    @Nullable
    public synchronized IDiffModelFactory getFactory(@NonNull Object model) {
        Class<?> clazz = model.getClass();
        IDiffModelFactory cacheFactory = cache.get(clazz);
        if (cacheFactory != null) {
            return cacheFactory;
        }
        IDiffModelFactory factory = mWrapper.createFactory(model);
        cache.put(clazz, factory);
        return factory;
    }

    private static final class DiffFactoryHelperWrapper implements IDiffFactoryHelper {

        private final List<IDiffFactoryHelper> helpers = new ArrayList<>();

        @Override
        public IDiffModelFactory createFactory(Object o) {
            for (IDiffFactoryHelper helper : helpers) {
                IDiffModelFactory factory = helper.createFactory(o);
                if (factory != null) return factory;
            }
            return null;
        }
    }

}
