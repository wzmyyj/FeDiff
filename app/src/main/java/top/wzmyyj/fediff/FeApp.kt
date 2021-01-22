package top.wzmyyj.fediff

import android.app.Application
import top.wzmyyj.diff_api.FeDiff

/**
 * Created on 2021/01/22.
 *
 * @author feling
 * @version 1.2.0
 * @since 1.2.0
 */
class FeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FeDiff.init(this, true)
    }
}