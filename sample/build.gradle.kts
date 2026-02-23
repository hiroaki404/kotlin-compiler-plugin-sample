plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    id("com.example.log")
}

dependencies {
    implementation(project(":utils"))
    // :annotations は LogGradlePlugin.apply() が自動追加するため不要
}

application {
    mainClass = "org.example.app.AppKt"
}
