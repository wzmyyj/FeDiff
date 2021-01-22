package top.wzmyyj.diff_compiler;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import top.wzmyyj.diff_compiler.utils.EmptyUtils;

import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_CONTENT;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_ITEM;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_ANNOTATION_SAME_TYPE;
import static top.wzmyyj.diff_compiler.utils.ProcessorConfig.DIFF_MODULE_NAME;

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
@SupportedOptions({DIFF_MODULE_NAME})
public class DiffProcessor extends AbstractProcessor {

    // 工具
    private EnvTools tools = null;
    // 数据
    private TempData data = null;
    // 收集
    private NodeCollection nodeCollection = null;
    // 整理
    private NodeArrange nodeArrange = null;
    // 写 文件
    private ModelFileWriter modelFileWriter = null;
    private HelperFileWriter helperFileWriter = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Elements elements = processingEnvironment.getElementUtils();
        Types types = processingEnvironment.getTypeUtils();
        Messager messager = processingEnvironment.getMessager();
        Filer filer = processingEnvironment.getFiler();
        tools = new EnvTools(elements, types, messager, filer);
        String moduleName = processingEnvironment.getOptions().get(DIFF_MODULE_NAME);
        if (!EmptyUtils.isNullOrEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            tools.note("APT 环境搭建完成...." + DIFF_MODULE_NAME + "：" + moduleName);
        } else {
            moduleName = "app";
            tools.note("APT 环境未配置，默认使用 " + DIFF_MODULE_NAME + "=app");
        }
        data = new TempData(moduleName);
        nodeCollection = new NodeCollection(tools, data);
        nodeArrange = new NodeArrange(tools, data);
        modelFileWriter = new ModelFileWriter(tools, data);
        helperFileWriter = new HelperFileWriter(tools, data);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (EmptyUtils.isNullOrEmpty(set)) return false;
        // 收集注解
        nodeCollection.collection(roundEnvironment);
        // 已得到所有需要处理的类型
        if (data.tempElementMap.isEmpty()) return false;
        // 整理关系
        nodeArrange.arrange();
        // 创建model
        modelFileWriter.start();
        // 创建helper
        helperFileWriter.start();
        tools.note("APT 处理完成：" + data.moduleName);
        return true;
    }

}
