package top.wzmyyj.diff_compiler;

import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import top.wzmyyj.diff_annotation.SameType;

/**
 * Created on 2021/01/21.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class NodeArrange {

    private final EnvTools tools;
    private final Map<TypeElement, TypeNode> tempElementMap ;
    private final Set<TypeNode> leafNodeSet ;
    private final Set<TypeNode> rootNodeSet ;

    public NodeArrange(EnvTools tools, TempData data) {
        this.tools = tools;
        this.rootNodeSet=data.rootNodeSet;
        this.leafNodeSet=data.leafNodeSet;
        this.tempElementMap=data.tempElementMap;
    }

    /**
     * 整理数据。
     */
    public void arrange() {
        // 整理类型的继承关系
        arrangeSuperType();
        // 修正 @SameType 注解的属性类型
        fixSameTypeFieldType();
    }

    //--------------整理数据----------------//

    /**
     * 整理类型继承关系，组成多个单链表 和 多个树。
     */
    private void arrangeSuperType() {
        leafNodeSet.addAll(tempElementMap.values());
        Set<TypeElement> typeSet = tempElementMap.keySet();
        for (TypeElement te : typeSet) {
            TypeElement p = te;
            TypeNode node = tempElementMap.get(te);
            while (p != null) {
                p = getSuperclass(p);
                if (p != null && typeSet.contains(p)) {
                    TypeNode parent = tempElementMap.get(p);
                    node.next = parent;
                    parent.children.add(node);
                    leafNodeSet.remove(parent);
                    break;
                }
            }
            if (p == null) {
                rootNodeSet.add(node);
            }
        }
    }

    /**
     * 修正 被 {@link SameType} 注解的属性类型，找到最近的类型。
     */
    private void fixSameTypeFieldType() {
        Set<TypeElement> typeSet = tempElementMap.keySet();
        for (TypeElement et : typeSet) {
            Map<VariableElement, TypeNode> map = tempElementMap.get(et).sameTypeMap;
            if (map.isEmpty()) continue;
            for (VariableElement element : map.keySet()) {
                TypeMirror typeMirror = element.asType();
                for (TypeNode node : leafNodeSet) {
                    boolean isFind = false;
                    TypeNode next = node;
                    while (next != null) {
                        TypeMirror tm = next.data.asType();
                        if (tools.getTypes().isSameType(typeMirror, tm) || tools.getTypes().isSubtype(typeMirror, tm)) {
                            map.put(element, next);
                            isFind = true;
                            break;
                        }
                        next = next.next;
                    }
                    if (isFind) break;
                }
                if (map.get(element) == null) {// 做个校验
                    tools.error("@SameType 的属性类型 为无效的类型！" + typeMirror.getClass().getSimpleName() + ":" + element.getSimpleName());
                }
            }
        }
    }

    /**
     * 查找父类（只找用户定义的类）。如果父类 是SDK的类，返回null。
     *
     * @param type TypeElement
     * @return 父类的 TypeElement
     */
    private TypeElement getSuperclass(TypeElement type) {
        if (type.getSuperclass().getKind() == TypeKind.DECLARED) {
            TypeElement superclass = (TypeElement) tools.getTypes().asElement(type.getSuperclass());
            String name = superclass.getQualifiedName().toString();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Skip system classes, this just degrades performance
                return null;
            } else {
                return superclass;
            }
        } else {
            return null;
        }
    }


}
