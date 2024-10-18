import com.google.protobuf.gradle.id


plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("maven-publish")
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "io.noone.androidcore.bnb"
    compileSdk = 34

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
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}


dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.protobuf:protobuf-java:3.22.3")
    implementation(project(":crypto_core"))
}

fun com.android.build.api.dsl.AndroidSourceSet.proto(action: SourceDirectorySet.() -> Unit) {
    (this as ExtensionAware).extensions.configure("proto", action)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.noone.androidcore"
            artifactId = "crypto_bnb"
            version = "2.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "local_repo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}