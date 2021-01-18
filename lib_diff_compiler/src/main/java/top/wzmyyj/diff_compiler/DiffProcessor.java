package top.wzmyyj.diff_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import top.wzmyyj.diff_annotation.SameContent;
import top.wzmyyj.diff_annotation.SameItem;
import top.wzmyyj.diff_annotation.SameType;
import top.wzmyyj.diff_compiler.utils.EmptyUtils;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.BOOLEAN_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_CONTENT;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_ITEM;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_API_PACKAGE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.FACTORY_HELPER_NAME;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.MODEL_NAME_LAST;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.MODEL_PACKAGE_LAST;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_EQUALS_UTIL;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_FACTORY;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_FACTORY_HELPER;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_MODEL_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_PAYLOAD;

/**
 * Created on 2020/12/01.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({DIFF_ANNOTATION_SAME_ITEM, DIFF_ANNOTATION_SAME_CONTENT, DIFF_ANNOTATION_SAME_TYPE})
//// 注解处理器接收的参数
//@SupportedOptions({OPTIONS_MODULE_NAME, OPTIONS_PACKAGE_NAME})
public class DiffProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;
//    // 各个模块传递过来的模块名
//    private String moduleName;
//    // 各个模块传递过来的包名
//    private String packageName;

    // 单链表+树 缓存数据和处理继承关系
    private static class ElementNode {
        // 当前类型
        public TypeElement data = null;
        // 集合中最近的父类类型，下一个节点
        public ElementNode next = null;
        // 最近子类集合
        public List<ElementNode> children = new ArrayList<>();
        // SameItem的判断点总数，包含父类和穿透类型
        public int sameItemCount = 0;
        // SameContent的判断点总数，包含父类和穿透类型
        public int sameContentCount = 0;
        // 主要属性的集合，只包含sameItem和sameContent的
        public final Set<VariableElement> mainSet = new HashSet<>();
        public final List<VariableElement> sameItemList = new ArrayList<>();
        public final List<VariableElement> sameContentList = new ArrayList<>();
        // 穿透属性map，Element为key, 属性类型或最近的父类类型为value
        public final Map<VariableElement, ElementNode> sameTypeMap = new HashMap<>();
    }

    // 所有有节点根据类型缓存
    private final Map<TypeElement, ElementNode> tempElementMap = new HashMap<>();
    // 所有以叶子子类为起点，最近的父类为下一个节点 的单链表 集合
    private final Set<ElementNode> leafNodeSet = new HashSet<>();
    // 所有以根父类为起点，最近的子类集合为下一层 的树 集合
    private final Set<ElementNode> rootNodeSet = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();
//        moduleName = processingEnvironment.getOptions().get(OPTIONS_MODULE_NAME);
//        packageName = processingEnvironment.getOptions().get(OPTIONS_PACKAGE_NAME);
//        note(">>>>>>>>>>>>>>>>>>>>>>" + OPTIONS_MODULE_NAME + "：" + moduleName);
//        note(">>>>>>>>>>>>>>>>>>>>>>" + OPTIONS_PACKAGE_NAME + "：" + packageName);
//        if (moduleName != null && packageName != null) {
//            note("APT 环境搭建完成....");
//        } else {
//            note("APT 环境有问题，请检查 " + OPTIONS_MODULE_NAME + " 与 " + OPTIONS_PACKAGE_NAME + " 为null...");
//        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (EmptyUtils.isNullOrEmpty(set)) return false;
        // 获取所有被 @SameItem, @SameContent, @SameType 注解的 元素集合
        Set<? extends Element> elements1 = roundEnvironment.getElementsAnnotatedWith(SameItem.class);
        Set<? extends Element> elements2 = roundEnvironment.getElementsAnnotatedWith(SameContent.class);
        Set<? extends Element> elements3 = roundEnvironment.getElementsAnnotatedWith(SameType.class);

        note("@SameItem 数量：" + elements1.size());
        note("@SameContent 数量：" + elements2.size());
        note("@SameType 数量：" + elements3.size());
        // 缓存数据
        saveElementSameItem(elements1);
        saveElementSameContent(elements2);
        saveElementSameType(elements3);
        // 已得到所有需要处理的类型
        if (EmptyUtils.isNullOrEmpty(tempElementMap)) return false;
        // 整理类型的继承关系
        arrangeSuperType();
        // 修正 @SameType 注解的属性类型
        fixSameTypeFieldType();
        // 创建文件
        createAllFile();
        return true;
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
                    ElementNode node = tempElementMap.get(enclosingElement);
                    if (node.mainSet.contains(ve)) {
                        error("@SameItem, @SameContent, @SameType 不能同时作用在一个属性上！");
                    }
                    node.mainSet.add(ve);
                    node.sameItemList.add(ve);
                } else { // 没有key (类型）
                    ElementNode node = new ElementNode();
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
                    ElementNode node = tempElementMap.get(enclosingElement);
                    if (node.mainSet.contains(ve)) {
                        error("@SameItem, @SameContent, @SameType 不能同时作用在一个属性上！");
                    }
                    node.mainSet.add(ve);
                    node.sameContentList.add(ve);
                } else { // 没有key (类型）
                    ElementNode node = new ElementNode();
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
                    error("@SameType 只能作用在声明类型的属性上！");
                }
                // 注解在属性的上面，属性节点父节点 是 类节点
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                if (tempElementMap.containsKey(enclosingElement)) {
                    ElementNode node = tempElementMap.get(enclosingElement);
                    if (node.mainSet.contains(ve)) {
                        error("@SameItem, @SameContent, @SameType 不能同时作用在一个属性上！");
                    }
                    // 类型先为空
                    node.sameTypeMap.put(ve, null);
                } else { // 没有key (类型）
                    ElementNode node = new ElementNode();
                    node.data = enclosingElement;
                    // 类型先为空
                    node.sameTypeMap.put(ve, null);
                    tempElementMap.put(enclosingElement, node); // 加入缓存
                }
            }
        }
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
            ElementNode node = tempElementMap.get(te);
            while (p != null) {
                p = getSuperclass(p);
                if (p != null && typeSet.contains(p)) {
                    ElementNode parent = tempElementMap.get(p);
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
            Map<VariableElement, ElementNode> map = tempElementMap.get(et).sameTypeMap;
            if (map.isEmpty()) continue;
            for (VariableElement element : map.keySet()) {
                TypeMirror typeMirror = element.asType();
                for (ElementNode node : leafNodeSet) {
                    boolean isFind = false;
                    ElementNode next = node;
                    while (next != null) {
                        TypeMirror tm = next.data.asType();
                        if (typeTool.isSameType(typeMirror, tm) || typeTool.isSubtype(typeMirror, tm)) {
                            map.put(element, next);
                            isFind = true;
                            break;
                        }
                        next = next.next;
                    }
                    if (isFind) break;
                }
                if (map.get(element) == null) {// 做个校验
                    error("@SameType 的属性类型 为无效的类型！" + typeMirror.getClass().getSimpleName() + ":" + element.getSimpleName());
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
            TypeElement superclass = (TypeElement) typeTool.asElement(type.getSuperclass());
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

    //--------------创建文件----------------//

    // 记录生成过的model文件
    private final Map<ElementNode, ClassName> tempModelMap = new HashMap<>();
    // 记录是否进入过createModelFile方法，防止造成无限递归。
    private final Set<ElementNode> createModelNodeSet = new HashSet<>();
    private TypeElement modelType = null;
    private TypeElement utilType = null;
    private TypeElement payloadType = null;
    private TypeElement factoryType = null;
    private TypeElement helperType = null;

    /**
     * 创建所有model文件。
     */
    private void createAllFile() {
        modelType = elementTool.getTypeElement(TYPE_MODEL_TYPE);
        checkNotNull(modelType, "没找到" + TYPE_MODEL_TYPE);
        payloadType = elementTool.getTypeElement(TYPE_PAYLOAD);
        checkNotNull(payloadType, "没找到" + TYPE_PAYLOAD);
        utilType = elementTool.getTypeElement(TYPE_EQUALS_UTIL);
        checkNotNull(utilType, "没找到" + TYPE_EQUALS_UTIL);
        factoryType = elementTool.getTypeElement(TYPE_FACTORY);
        checkNotNull(factoryType, "没找到" + TYPE_FACTORY);
        helperType = elementTool.getTypeElement(TYPE_FACTORY_HELPER);
        checkNotNull(helperType, "没找到" + TYPE_FACTORY_HELPER);
        createModelNodeSet.clear();
        // 从每个叶子子类开始
        for (ElementNode node : leafNodeSet) {
            createModelFile(node);
        }
        try {
            writeFactoryHelperFile();
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    /**
     * 生成单个model的文件。存在递归调用。
     * 如果 穿透属性 有闭环，可能造成无限递归，因此会检测出来并抛异常。
     *
     * @param node 信息
     */
    private void createModelFile(ElementNode node) {
        if (node == null) return;
        if (tempModelMap.get(node) != null) return;// 生成过了
        if (createModelNodeSet.contains(node)) {
            error("小老弟 写的 @SameType 的属性 存在回环呀！" + node.data.getQualifiedName());
        }
        createModelNodeSet.add(node);
        // 优先生成父类文件
        if (node.next != null) {
            createModelFile(node.next);
            node.sameItemCount += node.next.sameItemCount;
            node.sameContentCount += node.next.sameContentCount;
        }
        // 优先生成穿透属性类型文件
        if (!node.sameTypeMap.isEmpty()) {
            for (ElementNode n : node.sameTypeMap.values()) {
                createModelFile(n);
                node.sameItemCount += n.sameItemCount;
                node.sameContentCount += n.sameContentCount;
            }
        }
        node.sameItemCount += node.sameItemList.size();
        node.sameContentCount += node.sameContentList.size();

        if (node.sameItemCount == 0 && node.sameContentCount == 0) {// 正常情况不可能，仅做个校验
            error("一定是哪里出问题了！");
        }
        note(">>>>>>>>>>>>>>>>>>>>>开始写代码！");
        // 写代码
        try {
            writeModelFile(node);
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    /**
     * 写代码。model文件
     *
     * @param node 信息
     */
    private void writeModelFile(ElementNode node) throws IOException {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<MethodSpec> methodSpecList = new ArrayList<>();
        ClassName nodeCn = ClassName.get(node.data);

        // 基本属性
        for (VariableElement element : node.mainSet) {
            TypeName cn = TypeName.get(element.asType());// 属性类型
            FieldSpec.Builder builder = FieldSpec.builder(cn, element.getSimpleName().toString(), Modifier.PRIVATE);
            fieldSpecList.add(builder.build());
        }

        // 穿透属性
        for (Map.Entry<VariableElement, ElementNode> entry : node.sameTypeMap.entrySet()) {
            ClassName cn = tempModelMap.get(entry.getValue());// 属性穿透类型
            checkNotNull(cn, "一定是哪里出问题了2！");
            FieldSpec.Builder builder = FieldSpec.builder(cn, entry.getKey().getSimpleName().toString(), Modifier.PRIVATE)
                    .initializer("new $T()", cn);
            fieldSpecList.add(builder.build());
        }

        // 方法1：sumSameItemCount
        MethodSpec.Builder methodBuilder1 = MethodSpec.methodBuilder("sameItemCount") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(int.class) // 方法返回类型
                .addStatement("return $L", node.sameItemCount);
        methodSpecList.add(methodBuilder1.build());

        // 方法2：sumSameContentCount
        MethodSpec.Builder methodBuilder2 = MethodSpec.methodBuilder("sameContentCount") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(int.class) // 方法返回类型
                .addStatement("return $L", node.sameContentCount);
        methodSpecList.add(methodBuilder2.build());

        // 方法3：isSameItem
        MethodSpec.Builder methodBuilder3 = MethodSpec.methodBuilder("isSameItem") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(boolean.class) // 方法返回类型
                .addParameter(Object.class, "o")
                .addStatement("$T $N = ($T) $N", nodeCn, "m", nodeCn, "o");
        if (node.next != null && node.next.sameItemCount > 0) {
            methodBuilder3.addStatement("if (!super.isSameContent($N)) return false", "m");
        }
        for (VariableElement element : node.sameItemList) {
            methodBuilder3.addStatement("if ($T.unEquals(this.$L,$N.$L)) return false",
                    ClassName.get(utilType), element.getSimpleName(), "m", spellGetFunction(element));
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            if (node.sameTypeMap.get(element).sameItemCount == 0) continue;
            methodBuilder3.addStatement("if (!this.$L.isSameItem($N.$L)) return false",
                    element.getSimpleName(), "m", spellGetFunction(element));
        }
        methodBuilder3.addStatement("return $L", node.sameItemCount > 0);
        methodSpecList.add(methodBuilder3.build());

        // 方法4：isSameContent
        MethodSpec.Builder methodBuilder4 = MethodSpec.methodBuilder("isSameContent") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(boolean.class) // 方法返回类型
                .addParameter(Object.class, "o")
                .addStatement("$T $N = ($T) $N", nodeCn, "m", nodeCn, "o");
        if (node.next != null && node.next.sameContentCount > 0) {
            methodBuilder4.addStatement("if (!super.isSameContent($N)) return false", "m");
        }
        for (VariableElement element : node.sameContentList) {
            methodBuilder4.addStatement("if ($T.unEquals(this.$L,$N.$L)) return false",
                    ClassName.get(utilType), element.getSimpleName(), "m", spellGetFunction(element));
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            if (node.sameTypeMap.get(element).sameContentCount == 0) continue;
            methodBuilder4.addStatement("if (!this.$L.isSameContent($N.$L)) return false",
                    element.getSimpleName(), "m", spellGetFunction(element));
        }
        methodBuilder4.addStatement("return $L", node.sameContentCount > 0);
        methodSpecList.add(methodBuilder4.build());

        // 方法5：canHandle
        MethodSpec.Builder methodBuilder5 = MethodSpec.methodBuilder("canHandle") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(boolean.class) // 方法返回类型
                .addParameter(Object.class, "o")
                .addStatement("return $N instanceof $T", "o", nodeCn);
        methodSpecList.add(methodBuilder5.build());

        // 方法6：from
        MethodSpec.Builder methodBuilder6 = MethodSpec.methodBuilder("from") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(void.class) // 方法返回类型
                .addParameter(Object.class, "o")
                .addStatement("$T $N = ($T) $N", nodeCn, "m", nodeCn, "o");
        if (node.next != null) {
            methodBuilder6.addStatement("super.from($N)", "m");
        }
        for (VariableElement element : node.mainSet) {
            methodBuilder6.addStatement("this.$L = $N.$L",
                    element.getSimpleName(), "m", spellGetFunction(element));
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            methodBuilder6.addStatement("this.$L.from($N.$L)",
                    element.getSimpleName(), "m", spellGetFunction(element));
        }
        methodSpecList.add(methodBuilder6.build());

        // 方法7：payload
        MethodSpec.Builder methodBuilder7 = MethodSpec.methodBuilder("payload") // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(ClassName.get(payloadType)) // 方法返回类型
                .addParameter(Object.class, "o")
                .addStatement("$T $N = ($T) $N", nodeCn, "m", nodeCn, "o");
        if (node.next != null && node.next.sameContentCount > 0) {
            methodBuilder7
                    .addStatement("$T $N = super.payload($N)", ClassName.get(payloadType), "p", "m");
        } else {
            methodBuilder7.addStatement("$T $N = new $T()",
                    ClassName.get(payloadType), "p", ClassName.get(payloadType));
        }
        Set<String> keySet = new HashSet<>();
        for (VariableElement element : node.sameContentList) {
            SameContent sc = element.getAnnotation(SameContent.class);
            String key = EmptyUtils.isNullOrEmpty(sc.value()) ? element.getSimpleName().toString() : sc.value();
            if (keySet.contains(key)) {
                error("@SameContent 注解上的value有重复：" + node.data.getQualifiedName() + "$" + element.getSimpleName());
            }
            keySet.add(key);
            methodBuilder7
                    .beginControlFlow("if ($T.unEquals(this.$L, $N.$L))",
                            ClassName.get(utilType), element.getSimpleName(), "m", spellGetFunction(element))
                    .addStatement("$N.put($S, $N.$L)", "p", key, "m", spellGetFunction(element))
                    .endControlFlow();
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            if (node.sameTypeMap.get(element).sameContentCount == 0) continue;
            SameType st = element.getAnnotation(SameType.class);
            String key = EmptyUtils.isNullOrEmpty(st.value()) ? element.getSimpleName().toString() : st.value();
            if (keySet.contains(key)) {
                error("@SameType 注解上的value有重复：" + node.data.getQualifiedName() + "$" + element.getSimpleName());
            }
            keySet.add(key);
            methodBuilder7
                    .beginControlFlow("if (!this.$L.isSameContent($N.$L))",
                            element.getSimpleName(), "m", spellGetFunction(element))
                    .addStatement("$N.put($S, $N.$L)", "p", key, "m", spellGetFunction(element))
                    .endControlFlow();
        }
        methodBuilder7.addStatement("return $N", "p");
        methodSpecList.add(methodBuilder7.build());


        String clzName = node.data.getSimpleName() + MODEL_NAME_LAST;
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(ClassName.get(modelType)) // 方法返回类型
                .addStatement("return new $L()", clzName);

        // 工厂内部类
        TypeSpec.Builder factoryTypeBuilder = TypeSpec.classBuilder("Factory") // 类名
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC) // public修饰符
                .addSuperinterface(ClassName.get(factoryType))
                .addMethod(methodBuilder.build());

        // 生成类
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(clzName) // 类名
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .addFields(fieldSpecList) // 字段的构建
                .addMethods(methodSpecList) // 方法的构建（方法参数 + 方法体）
                .addType(factoryTypeBuilder.build()); // 内部类的构建
        if (node.next != null) {
            ClassName cn = tempModelMap.get(node.next);// 父类类型
            checkNotNull(cn, "一定是哪里出问题了2！");
            typeBuilder.superclass(cn);
        } else {
            typeBuilder.addSuperinterface(ClassName.get(modelType)); // 实现IDiffModelType接口
        }

        // 生成类文件
        String packageName = ClassName.get(node.data).packageName() + MODEL_PACKAGE_LAST;
        JavaFile.builder(packageName, typeBuilder.build())
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件

        tempModelMap.put(node, ClassName.get(packageName, clzName));
    }

    /**
     * 写代码。factoryHelper文件
     */
    private void writeFactoryHelperFile() throws IOException {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("createFactory")
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(ClassName.get(factoryType))
                .addParameter(Object.class, "o");// 方法返回类型
        // 从每个根父类开始
        for (ElementNode node : rootNodeSet) writeHelperCode(node, methodBuilder);
        methodBuilder.addStatement("return null");
        // 生成类
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(FACTORY_HELPER_NAME) // 类名
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .addSuperinterface(ClassName.get(helperType))
                .addMethod(methodBuilder.build());
        // 生成类文件
        JavaFile.builder(DIFF_API_PACKAGE, typeBuilder.build())
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }

    /**
     * 从树根节点开始遍历写代码。
     *
     * @param node          node
     * @param methodBuilder methodBuilder
     */
    private void writeHelperCode(ElementNode node, MethodSpec.Builder methodBuilder) {
        ClassName nodeCn = ClassName.get(node.data);
        ClassName cn = tempModelMap.get(node);// model类型
        checkNotNull(cn, "一定是哪里出问题了4！");
        methodBuilder.beginControlFlow("if ($N instanceof $T)", "o", nodeCn);
        for (ElementNode child : node.children) writeHelperCode(child, methodBuilder);
        methodBuilder.addStatement("return new $T.Factory()", cn).endControlFlow();
    }

    /**
     * 获取 字段名 或者 Get 方法名字 + ().
     *
     * @param element VariableElement
     * @return GetFunction
     */
    private String spellGetFunction(VariableElement element) {
        if (element.getModifiers().contains(Modifier.PUBLIC)) {
            return element.getSimpleName().toString();
        } else {
            String name = element.getSimpleName().toString();
            if (element.asType().getKind() == TypeKind.BOOLEAN
                    || element.asType().toString().equalsIgnoreCase(BOOLEAN_TYPE)) {
                byte[] items = name.getBytes();
                if (items.length >= 3) {
                    char c0 = (char) items[0];
                    char c1 = (char) items[1];
                    char c2 = (char) items[2];
                    if (c0 == 'i' && c1 == 's' && (c2 < 'a' || c2 > 'z')) return name + "()";
                }
            }
            return "get" + toUpper(name) + "()";
        }
    }

    private static String toUpper(String str) {
        if (EmptyUtils.isNullOrEmpty(str)) return "X";
        byte[] items = str.getBytes();
        char f = (char) items[0];
        if (f < 'a' || f > 'z') return str;
        items[0] = (byte) (f - 'a' + 'A');
        return new String(items);
    }

    private void checkNotNull(Object o, String msg) {
        if (o == null) error(msg);
    }

    private void note(String msg) {
        messager.printMessage(NOTE, msg + "; ");
    }

    private void error(String msg) {
        messager.printMessage(ERROR, msg + "; ");
        throw new RuntimeException("报错了哦！");
    }

}
