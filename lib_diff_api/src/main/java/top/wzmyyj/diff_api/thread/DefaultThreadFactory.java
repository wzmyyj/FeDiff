package top.wzmyyj.diff_api.thread;

import androidx.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import top.wzmyyj.diff_api.FeDiff;
import top.wzmyyj.diff_api.utils.Constants;

/**
 * Created on 2021/01/22.
 * <p>
 * copy from ARouter.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;

    public DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "ARouter task pool No." + poolNumber.getAndIncrement() + ", thread No.";
    }

    public Thread newThread(@NonNull Runnable runnable) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        FeDiff.logger().info(Constants.TAG, "Thread production, name is [" + threadName + "]");
        Thread thread = new Thread(group, runnable, threadName, 0);
        if (thread.isDaemon()) {   //设为非后台线程
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) { //优先级为normal
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        // 捕获多线程处理中的异常
        thread.setUncaughtExceptionHandler((t, e) ->
                FeDiff.logger().info(Constants.TAG, "Running task appeared exception! Thread ["
                        + t.getName() + "], because [" + e.getMessage() + "]"));
        return thread;
    }
}