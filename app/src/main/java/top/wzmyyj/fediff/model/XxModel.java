package top.wzmyyj.fediff.model;

import top.wzmyyj.diff_annotation.SameContent;
import top.wzmyyj.diff_annotation.SameItem;
import top.wzmyyj.diff_annotation.SameType;

/**
 * Created on 2020/12/02.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
public class XxModel {

    @SameItem
    public long id;

    @SameContent
    public String name;

    @SameContent("o1")
    public int count;

    @SameContent("o")
    public boolean valid;

    @SameType()
    public YyModel yy;

}
