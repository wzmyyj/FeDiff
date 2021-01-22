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
class MsgModel {

    @SameItem
    var id: Long = 0

    @SameContent
    var content: String? = null

    @SameContent
    var time: Long = 0L

    var valid = false

//    @SameType
//    var user: UserModel? = null
}