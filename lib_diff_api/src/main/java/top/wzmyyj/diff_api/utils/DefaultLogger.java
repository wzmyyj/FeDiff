package top.wzmyyj.diff_api.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created on 2021/01/22.
 * <p>
 * default Logger util. copy from ARouter.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class DefaultLogger implements ILogger {

    private static boolean isShowLog = false;
    private static boolean isShowStackTrace = false;

    private String defaultTag = "ARouter";

    public void showLog(boolean showLog) {
        isShowLog = showLog;
    }

    public void showStackTrace(boolean showStackTrace) {
        isShowStackTrace = showStackTrace;
    }

    public DefaultLogger() {
    }

    public DefaultLogger(String defaultTag) {
        this.defaultTag = defaultTag;
    }

    @Override
    public void debug(String tag, String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.d(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void info(String tag, String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.i(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void warning(String tag, String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.w(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public void error(String tag, String message) {
        if (isShowLog) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.e(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message + getExtInfo(stackTraceElement));
        }
    }

    @Override
    public String getDefaultTag() {
        return defaultTag;
    }

    public static String getExtInfo(StackTraceElement stackTraceElement) {
        if (isShowStackTrace) {
            String separator = " & ";
            StringBuilder sb = new StringBuilder("  [");
            String threadName = Thread.currentThread().getName();
            String fileName = stackTraceElement.getFileName();
            String className = stackTraceElement.getClassName();
            String methodName = stackTraceElement.getMethodName();
            long threadID = Thread.currentThread().getId();
            int lineNumber = stackTraceElement.getLineNumber();

            sb.append("ThreadId=").append(threadID).append(separator);
            sb.append("ThreadName=").append(threadName).append(separator);
            sb.append("FileName=").append(fileName).append(separator);
            sb.append("ClassName=").append(className).append(separator);
            sb.append("MethodName=").append(methodName).append(separator);
            sb.append("LineNumber=").append(lineNumber);
            sb.append(" ] ");
            return sb.toString();
        }

        return "";
    }
}
