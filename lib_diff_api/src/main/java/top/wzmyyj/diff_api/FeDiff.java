package top.wzmyyj.diff_api;

import android.app.Application;

import java.util.List;

import top.wzmyyj.diff_api.utils.ILogger;

/**
 * Created on 2021/01/21.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public final class FeDiff {

    private FeDiff() {
    }

    private volatile static FeDiff instance = null;
    private volatile static boolean hasInit = false;

    public static FeDiff getInstance() {
        if (!hasInit) {
            throw new RuntimeException("FeDiff::Init::Invoke init(context) first!");
        }
        if (instance == null) {
            synchronized (FeDiff.class) {
                if (instance == null) {
                    instance = new FeDiff();
                }
            }
        }
        return instance;
    }

    public static void init(Application app) {
        init(app, false);
    }

    public static void init(Application app, boolean debug) {
        init(app, debug, null);
    }

    public synchronized static void init(Application app, boolean debug, List<IDiffFactoryHelper> helperList) {
        if (hasInit) return;
        hasInit = _FeDiff.getInstance().init(app, debug, helperList);
    }

    public static void setLogger(ILogger logger) {
        _FeDiff.logger = logger;
    }

    public static ILogger logger() {
        return _FeDiff.logger;
    }

    public boolean isDebug() {
        return _FeDiff.getInstance().isDebug();
    }

    public FactoryManager factoryManager() {
        return _FeDiff.getInstance().getFactoryManager();
    }

}
