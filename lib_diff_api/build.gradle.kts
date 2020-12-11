plugins { id(PluginId.library) }
setupCore()
dependencies {
    api(project(":lib_diff_annotation"))
}

apply { plugin(PluginId.github_maven) }
group = Publish.github_group