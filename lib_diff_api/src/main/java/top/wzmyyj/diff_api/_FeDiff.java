package top.wzmyyj.diff_api;

import android.app.Application;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import top.wzmyyj.diff_api.thread.DefaultPoolExecutor;
import top.wzmyyj.diff_api.utils.ClassUtil;
import top.wzmyyj.diff_api.utils.Constants;
import top.wzmyyj.diff_api.utils.DefaultLogger;
import top.wzmyyj.diff_api.utils.ILogger;

/**
 * Created on 2021/01/22.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
final class _FeDiff {

    private volatile static _FeDiff instance = null;
    public static ILogger logger = new DefaultLogger();
    private Application mContext;
    private boolean debug = false;

    public static _FeDiff getInstance() {
        if (instance == null) {
            synchronized (FeDiff.class) {
                if (instance == null) {
                    instance = new _FeDiff();
                }
            }
        }
        return instance;
    }

    public boolean init(Application application, boolean debug, List<IDiffFactoryHelper> helperList) {
        mContext = application;
        this.debug = debug;
        logger.showLog(debug);
        if (helperList != null) {
            factoryManager.addHelpers(helperList);
        } else {
            startFind();
        }
        logger.info(Constants.TAG, "FeDiff init success!");
        return true;
    }

    public boolean isDebug() {
        return debug;
    }

    private final FactoryManager factoryManager = new FactoryManager();

    public FactoryManager getFactoryManager() {
        return factoryManager;
    }

    private void startFind() {
        DefaultPoolExecutor.getInstance().execute(() -> {
            synchronized (factoryManager) {
                findClass();
            }
        });
    }

    private void findClass() {
        try {
            Set<String> set = ClassUtil.getFileNameByPackageName(mContext,
                    DefaultPoolExecutor.getInstance(), Constants.FACTORY_HELPER_PACKAGE_NAME);
            logger.info(Constants.TAG, "find class set: size= " + set.size() + " " + set);
            List<IDiffFactoryHelper> helperList = new ArrayList<>();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String clz : set) {
                Object o = Class.forName(clz, true, classLoader);
                if (o instanceof IDiffFactoryHelper) {
                    helperList.add((IDiffFactoryHelper) o);
                }
            }
            _FeDiff.getInstance().factoryManager.addHelpers(helperList);
        } catch (PackageManager.NameNotFoundException | IOException
                | InterruptedException | ClassNotFoundException e) {
            logger.error(Constants.TAG, "find class error: " + e.getMessage());
        }
    }

}
