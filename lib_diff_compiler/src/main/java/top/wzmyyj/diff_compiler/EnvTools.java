package top.wzmyyj.diff_compiler;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * Created on 2021/01/20.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
public class EnvTools {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private final Elements elementTool;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private final Types typeTool;
    // Message用来打印 日志相关信息
    private final Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private final Filer filer;

    public EnvTools(Elements elements, Types types, Messager messager, Filer filer) {
        this.elementTool = elements;
        this.typeTool = types;
        this.messager = messager;
        this.filer = filer;
    }

    public Elements getElements() {
        return elementTool;
    }

    public Types getTypes() {
        return typeTool;
    }

    public Filer getFiler() {
        return filer;
    }

    public void checkNotNull(Object o, String msg) {
        if (o == null) error(msg);
    }

    public void note(String msg) {
        messager.printMessage(NOTE, msg + "; ");
    }

    public void error(String msg) {
        messager.printMessage(ERROR, msg + "; ");
        throw new RuntimeException("报错了哦！");
    }
}
