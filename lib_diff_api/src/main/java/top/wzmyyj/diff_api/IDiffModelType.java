package top.wzmyyj.diff_api;

/**
 * Created on 2020/12/02.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IDiffModelType {

    int sameItemCount();

    int sameContentCount();

    boolean isSameItem(Object o);

    boolean isSameContent(Object o);

    boolean canHandle(Object o);

    void from(Object o);

    Payload payload(Object o);
}
