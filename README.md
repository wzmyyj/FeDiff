# FeDiff
:fallen_leaf: There is no leaves are alike in the world, but model can.


A Model Diff Tools.

Blog: [https://www.jianshu.com/p/93bf18bc46b3](https://www.jianshu.com/p/93bf18bc46b3)

#### Dependencies 
Project.build.gradle.kts
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }// add this line.
    }
}
```
app.build.gradle.kts：lastVersion = 1.2.0-beta1
```
 implementation("com.github.wzmyyj.FeDiff:lib_diff_api:lastVersion")
 // or kotlin use kapt
 annotationProcessor("com.github.wzmyyj.FeDiff:lib_diff_compiler:lastVersion")
```
#### How Use
model
```
// The attributes of the annotation tag will be used for comparison.
open class XxModel {
    @SameItem
    var id: Long = 0

    @SameContent
    var name: String? = null
   // can defining aliases, o1 is key of payload.
    @SameContent("o1")
    var count = 0
    // can defining aliases
    @SameContent("valid1")
    var valid = false

    // support attribute penetration
    @SameType
    var yy: YyModel? = null
}
// when it compare XxModel, it also compare XxModel.yy (YyModel)
class YyModel {

    @SameItem
    var id: Long = 0

    @SameContent
    var title: String? = null

    // no comparison
    var zz = false
}
// support extends
class ZzModel : XxModel() {

    @SameContent
    var zzz = false
}
```
Use `DiffUtil`. For example write a `Diffmodelcallback`
```
class DiffModelCallback<M : IVhModelType> : DiffUtil.ItemCallback<M>() {

    private val helper = DiffModelHelper()

    fun getHelper(): DiffModelHelper = helper

    fun bindNewData(bindObj: Any, newModel: M) {
        helper.bindNewData(bindObj, newModel)
    }

    override fun areItemsTheSame(oldItem: M, newItem: M): Boolean {
        return helper.isSameItem(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: M, newItem: M): Boolean {
        return helper.isSameContent(oldItem, newItem)
    }

    override fun getChangePayload(oldItem: M, newItem: M): Any? {
        return helper.getPayload(oldItem, newItem)
    }
}
```
Use  in `RecyclerView.Adapter`. Please use `androidx.recyclerview.widget.ListAdapter` which integrates `DiffUtil`.
```
override fun onBindViewHolder(holder: BindingViewHolder, position: Int, payloads: MutableList<Any>) {
        val payload = payloads.firstOrNull() as? Payload
        if (payload != null && payload.isEmpty.not()) {
            // do local refresh according to payload.
            val newAttr = payload.getString("key", "xxx")
            holder.itemView.tv.text = newAttr
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
        // after onBindViewHolder.
        callback.bindNewData(holder, getItem(position))
        // or callback.bindNewData(holder.itemView, getItem(position))
    }
```
`callback` is a `Diffmodelcallback`.

#### Other
see [wzmyyj/FeAdapter](https://github.com/wzmyyj/FeAdapter)

