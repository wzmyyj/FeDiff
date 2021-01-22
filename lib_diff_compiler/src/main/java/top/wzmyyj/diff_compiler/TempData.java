package top.wzmyyj.diff_compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * Created on 2021/01/21.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class TempData {

    public TempData(String moduleName) {
        this.moduleName = moduleName;
    }
    // 各个模块传递过来的module名
    public final String moduleName;

    // 所有有节点根据类型缓存
    public final Map<TypeElement, TypeNode> tempElementMap = new HashMap<>();
    // 所有以叶子子类为起点，最近的父类为下一个节点 的单链表 集合
    public final Set<TypeNode> leafNodeSet = new HashSet<>();
    // 所有以根父类为起点，最近的子类集合为下一层 的树 集合
    public final Set<TypeNode> rootNodeSet = new HashSet<>();

}
