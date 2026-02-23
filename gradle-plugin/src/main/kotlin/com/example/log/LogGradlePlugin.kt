package com.example.log

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class LogGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        // @Log アノテーションの依存を自動追加
        target.dependencies.add("implementation",
            target.dependencies.project(mapOf("path" to ":annotations")))
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }

    override fun getCompilerPluginId(): String = "com.example.log"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(groupId = "com.example", artifactId = "compiler-plugin", version = "0.1.0")
}
