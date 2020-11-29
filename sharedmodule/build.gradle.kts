plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlin-android-extensions")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}
apply(plugin = "kotlin-kapt")

repositories {
    gradlePluginPortal()
    google()
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
kotlin {
    version = "1.0"
    android()
//    jvm {
//        compilations {
//            val main by compilations.getting {
//                kotlinOptions {
//                    // Setup the Kotlin compiler options for the 'main' compilation:
//                    jvmTarget = "1.8"
//                }
//
//                compileKotlinTask // get the Kotlin task 'compileKotlinJvm'
//                output // get the main compilation output
//            }
//        }
//    }
    val onPhone = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
    if (onPhone) {
        iosArm64("ios")
    } else {
        iosX64("ios")
    }
    val sql_delight_version = "1.4.3"
    val coroutinesVersion = "1.3.9-native-mt"
    val serializationVersion = "1.0.0-RC"
    val ktorVersion = "1.4.0"
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:runtime:$sql_delight_version")
                implementation("com.squareup.sqldelight:coroutines-extensions:$sql_delight_version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion") {
//                    version {
//                        strictly(coroutinesVersion)
//                    }
                }
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.2.0")
                implementation("com.squareup.sqldelight:android-driver:$sql_delight_version")
                implementation("io.ktor:ktor-client-android:$ktorVersion")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.12")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:native-driver:$sql_delight_version")
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosTest by getting
    }

    cocoapods {
        // Configure fields required by CocoaPods.

        ios.deploymentTarget = "13.5"

        summary = "Shared module between android and iOS"
        homepage = "https://github.com/touchlab/KaMPKit"

        // You can change the name of the produced framework.
        // By default, it is the name of the Gradle project.
        frameworkName = "kmm_framework"

//        pod("RxSwift", "~> 5")
    }
}
android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(17)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "URL", "\"http://data.fixer.io/api/\"")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    val dagger_version = "2.24"
//                configurations["kapt"].dependencies.add(project("com.google.dagger:dagger-compiler:$dagger_version"))
    implementation("com.google.dagger:dagger:$dagger_version")
    "kapt"("com.google.dagger:dagger-compiler:$dagger_version")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

sqldelight {
    database("AppDatabase") {
        packageName = "com.example.sharedmodule.repository.db"
    }
}