package top.wzmyyj.fediff.model

import top.wzmyyj.diff_annotation.SameContent

/**
 * Created on 2020/12/11.
 *
 * @author feling
 * @version 1.1.0
 * @since 1.1.0
 */
class UserModel {

    @SameContent
    var name: String? = null

    @SameContent
    var avatar: String? = null
}