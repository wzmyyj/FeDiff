package top.wzmyyj.diff_compiler;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

import top.wzmyyj.diff_annotation.SameContent;
import top.wzmyyj.diff_annotation.SameItem;
import top.wzmyyj.diff_annotation.SameType;
import top.wzmyyj.diff_compiler.utils.EmptyUtils;

/**
 * Created on 2021/01/21.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class NodeCollection {

    private final EnvTools tools;
    private final Map<TypeElement, TypeNode> tempElementMap;

    public NodeCollection(EnvTools tools, TempData data) {
        this.tools = tools;
        this.tempElementMap = data.tempElementMap;
    }

    /**
     * 收集注解。
     */
    public void collection(RoundEnvironment roundEnvironment) {
        // 获取所有被 @SameItem, @SameContent, @SameType 注解的 元素集合
        Set<? extends Element> elements1 = roundEnvironment.getElementsAnnotatedWith(SameItem.class);
        Set<? extends Element> elements2 = roundEnvironment.getElementsAnnotatedWith(SameContent.class);
        Set<? extends Element> elements3 = roundEnvironment.getElementsAnnotatedWith(SameType.class);

        tools.note("@SameItem 数量：" + elements1.size());
        tools.note("@SameContent 数量：" + elements2.size());
        tools.note("@SameType 数量：" + elements3.size());
        // 缓存数据
        saveElementSameItem(elements1);
        saveElementSameContent(elements2);
        saveElementSameType(elements3);
    }

    //--------------搜集数据----------------//

    /**
     * 缓存 被 {@link SameItem} 注解的元素。
     *
     * @param elements 被 {@link SameItem} 注解的集合。
     */
    private void saveElementSameItem(Set<? extends Element> elements) {
        if (!EmptyUtils.isNullOrEmpty(elements)) {
            for (Element element : elements) {
                VariableElement ve = (VariableElement) element;
                // 注解在属性的上面，属性节点父节点 是 类节点
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                if (tempElementMap.containsKey(enclosingElement)) {
                    TypeNode node = tempElementMap.get(enclosingElement);
                    if (node.mainSet.contains(ve)) {
                        tools.error("@SameItem, @SameContent, @SameType 不能同时作用在一个属性上！");
                    }
                    node.mainSet.add(ve);
                    node.sameItemList.add(ve);
                } else { // 没有key (类型）
                    TypeNode node = new TypeNode();
                    node.data = enclosingElement;
                    node.mainSet.add(ve);
                    node.sameItemList.add(ve);
                    tempElementMap.put(enclosingElement, node); // 加入缓存
                }
            }
        }
    }

    /**
     * 缓存 被 {@link SameContent} 注解的元素。
     *
     * @param elements 被 {@link SameContent} 注解的集合。
     */
    private void saveElementSameContent(Set<? extends Element> elements) {
        if (!EmptyUtils.isNullOrEmpty(elements)) {
            for (Element element : elements) {
                VariableElement ve = (VariableElement) element;
                // 注解在属性的上面，属性节点父节点 是 类节点
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                if (tempElementMap.containsKey(enclosingElement)) {
                    TypeNode node = tempElementMap.get(enclosingElement);
                    if (node.mainSet.contains(ve)) {
                        tools.error("@SameItem, @SameContent, @SameType 不能同时作用在一个属性上！");
                    }
                    node.mainSet.add(ve);
                    node.sameContentList.add(ve);
                } else { // 没有key (类型）
                    TypeNode node = new TypeNode();
                    node.data = enclosingElement;
                    node.mainSet.add(ve);
                    node.sameContentList.add(ve);
                    tempElementMap.put(enclosingElement, node); // 加入缓存
                }
            }
        }
    }

    /**
     * 缓存 被 {@link SameType} 注解的元素。
     *
     * @param elements 被 {@link SameType} 注解的集合。
     */
    private void saveElementSameType(Set<? extends Element> elements) {
        if (!EmptyUtils.isNullOrEmpty(elements)) {
            for (Element element : elements) {
                VariableElement ve = (VariableElement) element;
                if (ve.asType().getKind() != TypeKind.DECLARED) {
                    tools.error("@SameType 只能作用在声明类型的属性上！");
                }
                // 注解在属性的上面，属性节点父节点 是 类节点
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                if (tempElementMap.containsKey(enclosingElement)) {
                    TypeNode node = tempElementMap.get(enclosingElement);
                    if (node.mainSet.contains(ve)) {
                        tools.error("@SameItem, @SameContent, @SameType 不能同时作用在一个属性上！");
                    }
                    // 类型先为空
                    node.sameTypeMap.put(ve, null);
                } else { // 没有key (类型）
                    TypeNode node = new TypeNode();
                    node.data = enclosingElement;
                    // 类型先为空
                    node.sameTypeMap.put(ve, null);
                    tempElementMap.put(enclosingElement, node); // 加入缓存
                }
            }
        }
    }

}
