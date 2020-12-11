package top.wzmyyj.fediff.model;

import top.wzmyyj.diff_annotation.SameContent;
import top.wzmyyj.diff_annotation.SameItem;

/**
 * Created on 2020/12/02.
 *
 * @author feling
 * @version 1
 * @since 1
 */
public class YyModel {

    @SameItem
    public long id;

    @SameContent
    public String title;
//    //这里不能这样用哦，否则就跟XxModel产生回环了。
//    @SameType()
//    public XxModel xx;
}
