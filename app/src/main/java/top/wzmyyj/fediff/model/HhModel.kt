package top.wzmyyj.fediff.model

import top.wzmyyj.diff_annotation.SameContent
import top.wzmyyj.diff_annotation.SameItem
import top.wzmyyj.diff_annotation.SameType

/**
 * Created on 2020/12/11.
 *
 * @author feling
 * @version 1.1.0
 * @since 1.1.0
 */
class HhModel {

    @SameItem
    var id: Long = 0

    @SameContent
    var name: String? = null

    @SameItem()
    var isX = false

    @SameContent
    var is_X: Boolean = false

    @SameContent
    var vvv: Boolean? = false

    @SameType
    var yy: YyModel? = null
}