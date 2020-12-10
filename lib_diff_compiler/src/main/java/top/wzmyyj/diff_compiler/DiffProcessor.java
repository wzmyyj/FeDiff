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
import java.util.Objects;
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
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_CONTENT;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_ITEM;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_API_PACKAGE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.FACTORY_HELPER_NAME;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.FACTORY_NAME_LAST;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.FACTORY_PACKAGE_LAST;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.MODEL_NAME_LAST;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.MODEL_PACKAGE_LAST;
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

    // 单链表缓存数据和处理继承关系
    static class ElementNode {
        // 当前类型
        public TypeElement data = null;
        // 集合中最近的父类类型，下一个节点
        public ElementNode next = null;
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
    // 所有以叶子子类为起点，最近的父类为下一个节点 的单链表集合
    private final Set<ElementNode> rootNodeSet = new HashSet<>();

    private static class ClassBean {
        public String clzName;
        public String packageName;

        public ClassBean(String packageName, String clzName) {
            this.clzName = clzName;
            this.packageName = packageName;
        }
    }

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
        createAllModelFile();
        createAllFactoryFile();

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
     * 整理类型继承关系，组成多个单链表。
     */
    private void arrangeSuperType() {
        rootNodeSet.addAll(tempElementMap.values());
        Set<TypeElement> typeSet = tempElementMap.keySet();
        for (TypeElement te : typeSet) {
            TypeElement p = te;
            while (p != null) {
                p = getSuperclass(p);
                if (p != null && typeSet.contains(p)) {
                    ElementNode node = tempElementMap.get(p);
                    tempElementMap.get(te).next = node;
                    rootNodeSet.remove(node);
                    break;
                }
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
                for (ElementNode node : rootNodeSet) {
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
    private final Map<ElementNode, ClassBean> tempModelMap = new HashMap<>();
    // 记录生成过的factory文件
    private final Map<ElementNode, ClassBean> tempFactoryMap = new HashMap<>();
    // 记录是否进入过createModelFile方法，防止造成无限递归。
    private final Set<ElementNode> createModelNodeSet = new HashSet<>();
    private TypeElement modelType = null;
    private TypeElement payloadType = null;
    private TypeElement factoryType = null;
    private TypeElement helperType = null;

    /**
     * 创建所有model文件。
     */
    private void createAllModelFile() {
        modelType = elementTool.getTypeElement(TYPE_MODEL_TYPE);
        payloadType = elementTool.getTypeElement(TYPE_PAYLOAD);
        if (modelType == null) {
            error("没找到" + TYPE_MODEL_TYPE);
        }
        if (payloadType == null) {
            error("没找到" + TYPE_PAYLOAD);
        }
        createModelNodeSet.clear();
        // 从每个叶子子类开始
        for (ElementNode node : rootNodeSet) {
            createModelFile(node);
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
            error("小老弟 写的 @SameType 的属性 存在闭环呀！" + node.data.getQualifiedName());
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
            ClassBean cb = tempModelMap.get(entry.getValue());
            if (cb == null) {
                error("一定是哪里出问题了2！");
                return;
            }
            ClassName cn = ClassName.get(cb.packageName, cb.clzName);// 属性穿透类型
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
            methodBuilder3.addStatement("if (!$T.equals(this.$L,$N.$L)) return false",
                    Objects.class, element.getSimpleName(), "m", element.getSimpleName());
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            if (node.sameTypeMap.get(element).sameItemCount == 0) continue;
            methodBuilder3.addStatement("if (!this.$L.isSameItem($N.$L)) return false",
                    element.getSimpleName(), "m", element.getSimpleName());
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
            methodBuilder4.addStatement("if (!$T.equals(this.$L,$N.$L)) return false",
                    Objects.class, element.getSimpleName(), "m", element.getSimpleName());
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            if (node.sameTypeMap.get(element).sameContentCount == 0) continue;
            methodBuilder4.addStatement("if (!this.$L.isSameContent($N.$L)) return false",
                    element.getSimpleName(), "m", element.getSimpleName());
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
                    element.getSimpleName(), "m", element.getSimpleName());
        }
        for (VariableElement element : node.sameTypeMap.keySet()) {
            methodBuilder6.addStatement("this.$L.from($N.$L)",
                    element.getSimpleName(), "m", element.getSimpleName());
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
                    .beginControlFlow("if (!$T.equals(this.$L, $N.$L))",
                            Objects.class, element.getSimpleName(), "m", element.getSimpleName())
                    .addStatement("$N.put($S, $N.$L)", "p", key, "m", element.getSimpleName())
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
                            element.getSimpleName(), "m", element.getSimpleName())
                    .addStatement("$N.put($S, $N.$L)", "p", key, "m", element.getSimpleName())
                    .endControlFlow();
        }
        methodBuilder7.addStatement("return $N", "p");
        methodSpecList.add(methodBuilder7.build());

        // 生成类
        String clzName = node.data.getSimpleName() + MODEL_NAME_LAST;
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(clzName) // 类名
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .addFields(fieldSpecList)
                .addMethods(methodSpecList);// 方法的构建（方法参数 + 方法体）
        if (node.next != null) {
            ClassBean cb = tempModelMap.get(node.next);
            if (cb == null) {
                error("一定是哪里出问题了2！");
                return;
            }
            ClassName cn = ClassName.get(cb.packageName, cb.clzName);// 父类类型
            typeBuilder.superclass(cn);
        } else {
            typeBuilder.addSuperinterface(ClassName.get(modelType)); // 实现IDiffModelType接口
        }

        // 生成类文件
        String packageName = ClassName.get(node.data).packageName() + MODEL_PACKAGE_LAST;
        JavaFile.builder(packageName, typeBuilder.build())
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件

        tempModelMap.put(node, new ClassBean(packageName, clzName));
    }

    /**
     * 创建所有工厂。
     */
    private void createAllFactoryFile() {
        factoryType = elementTool.getTypeElement(TYPE_FACTORY);
        helperType = elementTool.getTypeElement(TYPE_FACTORY_HELPER);
        if (factoryType == null) {
            error("没找到" + TYPE_FACTORY);
        }
        if (helperType == null) {
            error("没找到" + TYPE_FACTORY_HELPER);
        }
        // 从每个叶子子类开始
        for (ElementNode node : rootNodeSet) {
            createFactoryFile(node);
        }
        try {
            writeFactoryHelperFile();
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    /**
     * 创建工厂。
     *
     * @param node 信息
     */
    private void createFactoryFile(ElementNode node) {
        if (node == null) return;
        if (tempFactoryMap.get(node) != null) return;// 生成过了
        // 优先生成父类的工厂文件
        if (node.next != null) {
            createFactoryFile(node.next);
        }
        try {
            writeFactoryFile(node);
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    /**
     * 写代码。factory文件
     *
     * @param node 信息
     */
    private void writeFactoryFile(ElementNode node) throws IOException {
        ClassBean cb = tempModelMap.get(node);
        if (cb == null) {
            error("一定是哪里出问题了3！");
            return;
        }
        ClassName cn = ClassName.get(cb.packageName, cb.clzName);// model类型
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(ClassName.get(modelType)) // 方法返回类型
                .addStatement("return new $T()", cn);

        // 生成类
        String clzName = node.data.getSimpleName() + FACTORY_NAME_LAST;
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(clzName) // 类名
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .addSuperinterface(ClassName.get(factoryType))
                .addMethod(methodBuilder.build());
        // 生成类文件
        String packageName = ClassName.get(node.data).packageName() + FACTORY_PACKAGE_LAST;
        JavaFile.builder(packageName, typeBuilder.build())
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件

        tempFactoryMap.put(node, new ClassBean(packageName, clzName));
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
        Set<ElementNode> set = new HashSet<>();
        // 从每个叶子子类开始
        for (ElementNode node : rootNodeSet) {
            ElementNode next = node;
            while (next != null && !set.contains(next)) {
                ClassName nodeCn = ClassName.get(next.data);
                ClassBean cb = tempFactoryMap.get(next);
                if (cb == null) {
                    error("一定是哪里出问题了4！");
                    return;
                }
                ClassName cn = ClassName.get(cb.packageName, cb.clzName);// model类型
                methodBuilder
                        .beginControlFlow("if ($N instanceof $T)", "o", nodeCn)
                        .addStatement("return new $T()", cn)
                        .endControlFlow();
                set.add(next);
                next = node.next;
            }
        }
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

    private void note(String msg) {
        messager.printMessage(NOTE, msg + "; ");
    }

    private void error(String msg) {
        messager.printMessage(ERROR, msg + "; ");
        throw new RuntimeException("报错了哦！");
    }

}
