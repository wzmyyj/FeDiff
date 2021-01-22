package top.wzmyyj.diff_api;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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
    private final FactoryManager factoryManager = new FactoryManager();

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

    public FactoryManager getFactoryManager() {
        return factoryManager;
    }


    //-----------------------private method----------------------------//

    private void startFind() {
        Set<String> set = readSetFromSp();
        logger.info(Constants.TAG, "sp fhp set: " + set);
        if (set != null && !set.isEmpty()) {
            factoryManager.addHelpers(loadClass(set));
            return;
        }
        DefaultPoolExecutor.getInstance().execute(() -> {
            synchronized (factoryManager) {
                scanDexFile();
            }
        });
    }

    private void scanDexFile() {
        try {
            Set<String> set = ClassUtil.getFileNameByPackageName(mContext,
                    DefaultPoolExecutor.getInstance(), Constants.FACTORY_HELPER_PACKAGE_NAME);
            logger.info(Constants.TAG, "scan dex set: " + set);
            writeSetInSp(set);
            factoryManager.addHelpers(loadClass(set));
        } catch (PackageManager.NameNotFoundException | IOException | InterruptedException e) {
            logger.error(Constants.TAG, "scan dex error: " + e.getMessage());
        }
    }

    private List<IDiffFactoryHelper> loadClass(Set<String> set) {
        List<IDiffFactoryHelper> helperList = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String clz : set) {
            try {
                Object o = Class.forName(clz, true, classLoader).newInstance();
                if (o instanceof IDiffFactoryHelper) {
                    helperList.add((IDiffFactoryHelper) o);
                }
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                logger.error(Constants.TAG, "load class error: " + e.getMessage());
            }
        }
        logger.info(Constants.TAG, "helperList: size= " + helperList.size());
        return helperList;
    }

    private synchronized Set<String> readSetFromSp() {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.SP_NAME,
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return sp.getStringSet(Constants.SP_FHP_KEY + getVersionCode(), null);
    }

    private synchronized void writeSetInSp(Set<String> set) {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.SP_NAME,
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        sp.edit().putStringSet(Constants.SP_FHP_KEY + getVersionCode(), set).apply();
    }

    private int getVersionCode() {
        try {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            logger.error(Constants.TAG, "getVersionCode error:  " + e.getMessage());
        }
        return -1;
    }
}
