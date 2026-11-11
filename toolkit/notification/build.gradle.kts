plugins {
    id("com.android.library")
}

android {
    namespace = "org.autojs.toolkit.notification"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(Modules.CORE_API))
    implementation(Deps.CORE_KTX)
    implementation(Deps.COROUTINES)
    implementation(Deps.GSON)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
