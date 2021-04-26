package top.wzmyyj.diff_api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created on 2020/12/08.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DiffModelHelper {

    private static class Data {
        Object model;
        IDiffModelType diff;
    }

    private final Map<Object, Data> bindMap = new WeakHashMap<>();
    private boolean byObjectsEquals = false;

    /**
     * 没有依据时是否用 {@link Object#equals(Object)} 来判断是否相同。
     *
     * @param use 是否使用
     */
    public synchronized void isSameByObjectsEquals(boolean use) {
        this.byObjectsEquals = use;
    }

    /**
     * 新旧数据内容是否同一行。
     *
     * @param oldModel 旧数据
     * @param newModel 新数据
     * @return 是否同一行
     */
    public synchronized boolean isSameItem(@NonNull Object oldModel, @NonNull Object newModel) {
        IDiffModelType diff = findDiff(oldModel);
        if (diff != null && diff.canHandle(newModel) && diff.sameItemCount() > 0) {
            return diff.isSameItem(newModel);
        }
        return byObjectsEquals && oldModel.equals(newModel);
    }

    /**
     * 新旧数据内容是否相同。
     *
     * @param oldModel 旧数据
     * @param newModel 新数据
     * @return 是否相同
     */
    public synchronized boolean isSameContent(@NonNull Object oldModel, @NonNull Object newModel) {
        IDiffModelType diff = findDiff(oldModel);
        if (diff != null && diff.canHandle(newModel) && diff.sameContentCount() > 0) {
            return diff.isSameContent(newModel);
        }
        return byObjectsEquals && oldModel.equals(newModel);
    }

    /**
     * 获取改变的差异。
     *
     * @param oldModel 旧数据
     * @param newModel 新数据
     * @return 差异
     */
    @Nullable
    public synchronized Payload getPayload(@NonNull Object oldModel, @NonNull Object newModel) {
        IDiffModelType diff = findDiff(oldModel);
        if (diff != null && diff.canHandle(newModel) && diff.sameContentCount() > 0) {
            return diff.payload(newModel);
        }
        return null;
    }

    /**
     * 绑定上新的数据。
     *
     * @param bindObj  绑定的对象
     * @param newModel 绑定的数据
     */
    public synchronized void bindNewData(@NonNull Object bindObj, @NonNull Object newModel) {
        Data data = bindMap.get(bindObj);
        if (data != null && data.diff.canHandle(newModel)) {
            data.diff.from(newModel);
            return;
        }
        IDiffModelType diff = tryCreateDiff(newModel);
        if (diff == null) return;
        diff.from(newModel);
        data = new Data();
        data.model = newModel;
        data.diff = diff;
        bindMap.put(bindObj, data);
    }

    @Nullable
    private IDiffModelType findDiff(@NonNull Object model) {
        for (Data data : bindMap.values()) {
            if (data.model == model) return data.diff;
        }
        return null;
    }

    @Nullable
    private IDiffModelType tryCreateDiff(@NonNull Object model) {
        IDiffModelFactory factory = FeDiff.getInstance().factoryManager().getFactory(model);
        if (factory != null) return factory.create();
        return null;
    }

}
