package top.wzmyyj.fediff.model

import top.wzmyyj.diff_annotation.SameContent
import top.wzmyyj.user.UserModel

/**
 * Created on 2021/01/19.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
class User2Model :UserModel() {

    @SameContent
    var avatar:String=""
}