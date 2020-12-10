package top.wzmyyj.diff_compiler.utils;

/**
 * Created on 2020/12/01.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ProcessorConfig {

    // 需要处理的注解
    String DIFF_ANNOTATION_SAME_ITEM = "top.wzmyyj.diff_annotation.SameItem";
    String DIFF_ANNOTATION_SAME_CONTENT = "top.wzmyyj.diff_annotation.SameContent";
    String DIFF_ANNOTATION_SAME_TYPE = "top.wzmyyj.diff_annotation.SameType";

    // 接收参数的TAG标记
//    String OPTIONS_MODULE_NAME = "moduleNameForDiffAPT"; // 目的是接收 每个module名称
//    String OPTIONS_PACKAGE_NAME = "packageNameForDiffAPT"; // 目的是接收 包名（APT 存放的包名）

    String DIFF_API_PACKAGE = "top.wzmyyj.diff_api";

    String MODEL_NAME_LAST = "$$Diff$$Model";
    String FACTORY_NAME_LAST = "$$Diff$$Factory";
    String FACTORY_HELPER_NAME = "Diff$$Create$$FactoryHelperImpl";

    String MODEL_PACKAGE_LAST = ".diff_model";
    String FACTORY_PACKAGE_LAST = ".diff_factory";


    String TYPE_MODEL_TYPE = DIFF_API_PACKAGE + ".IDiffModelType";
    String TYPE_PAYLOAD = DIFF_API_PACKAGE + ".Payload";
    String TYPE_FACTORY = DIFF_API_PACKAGE + ".IDiffModelFactory";
    String TYPE_FACTORY_HELPER = DIFF_API_PACKAGE + ".IDiffCreateFactoryHelper";

}
