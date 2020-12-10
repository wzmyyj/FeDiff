# FeDiff
:fallen_leaf: There is no leaves are alike in the world, but model can.


A Model Diff Tools.


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
app.build.gradle.kts
```
 implementation("com.github.wzmyyj.FeDiff:lib_diff_api:1.0.1")
 // or kotlin use: kapt("com.github.wzmyyj.FeDiff:lib_diff_compiler:1.0.1") 
 annotationProcessor("com.github.wzmyyj.FeDiff:lib_diff_compiler:1.0.1")
```
#### How Use
For example write a `Diffmodelcallback`
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
override fun onBindViewHolder(
        holder: BindingViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        callback.bindNewData(holder, getItem(position))
        // or callback.bindNewData(holder.itemView, getItem(position))
    }
```
`callback` is a `Diffmodelcallback`.

#### Other
see [wzmyyj/FeAdapter](https://github.com/wzmyyj/FeAdapter)
