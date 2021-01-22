package top.wzmyyj.diff_compiler;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created on 2021/01/20.
 * <p>
 * 单链表+树 缓存数据和处理继承关系。
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class TypeNode {
    // 当前类型
    public TypeElement data = null;
    // 集合中最近的父类类型，下一个节点
    public TypeNode next = null;
    // 最近子类集合
    public List<TypeNode> children = new ArrayList<>();
    // SameItem的判断点总数，包含父类和穿透类型
    public int sameItemCount = 0;
    // SameContent的判断点总数，包含父类和穿透类型
    public int sameContentCount = 0;
    // 主要属性的集合，只包含sameItem和sameContent的
    public final Set<VariableElement> mainSet = new HashSet<>();
    public final List<VariableElement> sameItemList = new ArrayList<>();
    public final List<VariableElement> sameContentList = new ArrayList<>();
    // 穿透属性map，Element为key, 属性类型或最近的父类类型为value
    public final Map<VariableElement, TypeNode> sameTypeMap = new HashMap<>();
    // 生成的副本类文件信息
    public ClassName diffClassName = null;
}
