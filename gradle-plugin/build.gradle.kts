plugins {
    kotlin("jvm") version "2.3.0"
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(17)
}

// java-gradle-plugin が同名のプロパティファイルを自動生成するため重複を除外する
tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("logPlugin") {
            id = "com.example.log"
            implementationClass = "com.example.log.LogGradlePlugin"
        }
    }
}
