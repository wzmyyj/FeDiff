package top.wzmyyj.diff_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import top.wzmyyj.diff_annotation.SameContent;
import top.wzmyyj.diff_annotation.SameType;
import top.wzmyyj.diff_compiler.utils.EmptyUtils;

import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.BOOLEAN_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.MODEL_NAME_PRE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.MODEL_PACKAGE_LAST;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_EQUALS_UTIL;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_FACTORY;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_MODEL_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_PAYLOAD;

/**
 * Created on 2021/01/20.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class ModelFileWriter {

    private final EnvTools tools;

    private final TypeElement modelType;
    private final TypeElement utilType;
    private final TypeElement payloadType;
    private final TypeElement factoryType;
    private final Set<TypeNode> leafNodeSet;

    public ModelFileWriter(EnvTools tools, TempData data) {
        this.tools = tools;
        this.leafNodeSet = data.leafNodeSet;
        Elements elementTool = tools.getElements();
        modelType = elementTool.getTypeElement(TYPE_MODEL_TYPE);
        tools.checkNotNull(modelType, "没找到" + TYPE_MODEL_TYPE);
        payloadType = elementTool.getTypeElement(TYPE_PAYLOAD);
        tools.checkNotNull(payloadType, "没找到" + TYPE_PAYLOAD);
        utilType = elementTool.getTypeElement(TYPE_EQUALS_UTIL);
        tools.checkNotNull(utilType, "没找到" + TYPE_EQUALS_UTIL);
        factoryType = elementTool.getTypeElement(TYPE_FACTORY);
        tools.checkNotNull(factoryType, "没找到" + TYPE_FACTORY);
    }

    // 记录是否进入过createModelFile方法，防止造成无限递归。
    private final Set<TypeNode> createModelNodeSet = new HashSet<>();

    /**
     * 开始任务。
     */
    public void start() {
        // 创建model 从每个叶子子类开始
        for (TypeNode node : leafNodeSet) {
            createModelFile(node);
        }
    }

    /**
     * 生成单个model的文件。存在递归调用。
     * 如果 穿透属性 有闭环，可能造成无限递归，因此会检测出来并抛异常。
     *
     * @param node 信息
     */
    private void createModelFile(TypeNode node) {
        if (node == null) return;
        if (node.diffClassName != null) return;// 生成过了
        if (createModelNodeSet.contains(node)) {
            tools.error("小老弟 写的 @SameType 的属性 存在回环呀！" + node.data.getQualifiedName());
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
            for (TypeNode n : node.sameTypeMap.values()) {
                createModelFile(n);
                node.sameItemCount += n.sameItemCount;
                node.sameContentCount += n.sameContentCount;
            }
        }
        node.sameItemCount += node.sameItemList.size();
        node.sameContentCount += node.sameContentList.size();

        if (node.sameItemCount == 0 && node.sameContentCount == 0) {// 正常情况不可能，仅做个校验
            tools.error("一定是哪里出问题了！");
        }
        tools.note(">>>>>>>>>>>>>>>>>>>>>开始写代码！");
        // 写代码
        try {
            writeModelFile(node);
        } catch (Exception e) {
            tools.error(e.getMessage());
        }
    }

    /**
     * 写代码。model文件
     *
     * @param node 信息
     */
    private void writeModelFile(TypeNode node) throws IOException {
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
        for (Map.Entry<VariableElement, TypeNode> entry : node.sameTypeMap.entrySet()) {
            ClassName cn = entry.getValue().diffClassName;// 属性穿透类型
            tools.checkNotNull(cn, "一定是哪里出问题了2！");
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
                tools.error("@SameContent 注解上的value有重复：" + node.data.getQualifiedName() + "$" + element.getSimpleName());
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
                tools.error("@SameType 注解上的value有重复：" + node.data.getQualifiedName() + "$" + element.getSimpleName());
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


        String clzName = MODEL_NAME_PRE + node.data.getSimpleName();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(ClassName.get(modelType)) // 方法返回类型
                .addStatement("return new $L()", clzName);

        // 工厂内部类
        TypeSpec.Builder factoryTypeBuilder = TypeSpec.classBuilder("Factory") // 类名
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC) // public修饰符
                .addSuperinterface(ClassName.get(factoryType))
//                .addField(fieldBuilder.build())
                .addMethod(methodBuilder.build());

        // 生成类
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(clzName) // 类名
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .addFields(fieldSpecList) // 字段的构建
                .addMethods(methodSpecList) // 方法的构建（方法参数 + 方法体）
                .addType(factoryTypeBuilder.build()); // 内部类的构建
        if (node.next != null) {
            ClassName cn = node.next.diffClassName;// 父类类型
            tools.checkNotNull(cn, "一定是哪里出问题了2！");
            typeBuilder.superclass(cn);
        } else {
            typeBuilder.addSuperinterface(ClassName.get(modelType)); // 实现IDiffModelType接口
        }

        // 生成类文件
        String packageName = ClassName.get(node.data).packageName() + MODEL_PACKAGE_LAST;
        JavaFile.builder(packageName, typeBuilder.build())
                .build() // JavaFile构建完成
                .writeTo(tools.getFiler()); // 文件生成器开始生成类文件
        // 记录生成的文件
        node.diffClassName = ClassName.get(packageName, clzName);
    }

    /**
     * 获取 字段名 或者 Get 方法名字 + ().
     *
     * @param element VariableElement
     * @return GetFunction
     */
    private static String spellGetFunction(VariableElement element) {
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
}
