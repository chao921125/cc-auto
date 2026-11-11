plugins {
    id("com.android.library")
}

android {
    namespace = "org.autojs.toolkit.floaty"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(Modules.CORE_API))
    implementation(project(Modules.CORE_RUNTIME))
    implementation(Deps.RHINO)
    implementation(Deps.CORE_KTX)
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.COROUTINES)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
