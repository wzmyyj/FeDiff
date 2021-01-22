package top.wzmyyj.diff_api.utils;

/**
 * Created on 2021/01/22.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public interface ILogger {

    boolean isShowLog = false;
    boolean isShowStackTrace = false;
    String defaultTag = Constants.TAG;

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void debug(String tag, String message);

    void info(String tag, String message);

    void warning(String tag, String message);

    void error(String tag, String message);

    String getDefaultTag();
}
