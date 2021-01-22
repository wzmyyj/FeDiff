package top.wzmyyj.diff_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_API_PACKAGE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.FACTORY_HELPER_NAME;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.FACTORY_HELPER_PACKAGE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_FACTORY;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.TYPE_FACTORY_HELPER;

/**
 * Created on 2021/01/20.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class HelperFileWriter {

    private final EnvTools tools;

    private final TypeElement factoryType;
    private final TypeElement helperType;
    private final Set<TypeNode> rootNodeSet;
    private final String moduleName;

    public HelperFileWriter(EnvTools tools, TempData data) {
        this.tools = tools;
        this.rootNodeSet = data.rootNodeSet;
        this.moduleName = data.moduleName;
        Elements elementTool = tools.getElements();
        factoryType = elementTool.getTypeElement(TYPE_FACTORY);
        tools.checkNotNull(factoryType, "没找到" + TYPE_FACTORY);
        helperType = elementTool.getTypeElement(TYPE_FACTORY_HELPER);
        tools.checkNotNull(helperType, "没找到" + TYPE_FACTORY_HELPER);
    }

    /**
     * 开始任务。
     */
    public void start() {
        try {
            writeFactoryHelperFile();
        } catch (IOException e) {
            tools.error(e.getMessage());
        }
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
        for (TypeNode node : rootNodeSet) writeHelperCode(node, methodBuilder);
        methodBuilder.addStatement("return null");
        // 生成类
        String clzName = FACTORY_HELPER_NAME + "_" + moduleName;
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(clzName) // 类名
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .addSuperinterface(ClassName.get(helperType))
                .addMethod(methodBuilder.build());
        // 生成类文件
        JavaFile.builder(FACTORY_HELPER_PACKAGE, typeBuilder.build())
                .build() // JavaFile构建完成
                .writeTo(tools.getFiler()); // 文件生成器开始生成类文件
    }

    /**
     * 从树根节点开始遍历写代码。
     *
     * @param node          node
     * @param methodBuilder methodBuilder
     */
    private void writeHelperCode(TypeNode node, MethodSpec.Builder methodBuilder) {
        ClassName nodeCn = ClassName.get(node.data);
        ClassName cn = node.diffClassName;// model类型
        tools.checkNotNull(cn, "一定是哪里出问题了4！");
        methodBuilder.beginControlFlow("if ($N instanceof $T)", "o", nodeCn);
        for (TypeNode child : node.children) writeHelperCode(child, methodBuilder);
        methodBuilder.addStatement("return new $T.Factory()", cn).endControlFlow();
    }
}
