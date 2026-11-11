plugins {
    id("com.android.library")
}

android {
    namespace = "org.autojs.feature.inspector"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(Modules.CORE_RUNTIME))
    api(project(Modules.TOOLKIT_ACCESSIBILITY))
    api(project(Modules.TOOLKIT_FLOATY))
    implementation(Deps.CORE_KTX)
    implementation(Deps.COROUTINES)
    implementation(Deps.RECYCLER_VIEW)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
