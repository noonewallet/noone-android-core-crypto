import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.protobuf") version "0.8.19"
}

android {
    namespace = "io.noone.androidcore.bnb"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    sourceSets["main"].proto {
        srcDir("src/main/proto")
    }
}

kapt {
    generateStubs = true
}

protobuf {

    protobuf.protoc {
        artifact = "com.google.protobuf:protoc:3.21.7"
    }
    protobuf.generateProtoTasks {
        all().forEach {
            it.builtins {
                id("java") { option("lite") }
            }
        }
    }
}


dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.google.protobuf:protobuf-java:3.21.7")
    implementation(project(":crypto_core"))
}

fun com.android.build.api.dsl.AndroidSourceSet.proto(action: SourceDirectorySet.() -> Unit) {
    (this as? ExtensionAware)
        ?.extensions
        ?.getByName("proto")
        ?.let { it as? SourceDirectorySet }
        ?.apply(action)
}