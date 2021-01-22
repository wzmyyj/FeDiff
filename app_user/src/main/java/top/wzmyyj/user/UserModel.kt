package top.wzmyyj.user

import top.wzmyyj.diff_annotation.SameContent
import top.wzmyyj.diff_annotation.SameItem

/**
 * Created on 2021/01/19.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
open class UserModel {

    @SameItem
    var id:String=""

    @SameContent
    var name:String=""
}