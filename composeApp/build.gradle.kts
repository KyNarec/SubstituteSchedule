import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
    google()
    // Needed for compose-webview-multiplatform: https://github.com/kevinnzou/compose-webview-multiplatform
    maven("https://jogamp.org/deployment/maven")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            api("io.github.kevinnzou:compose-webview-multiplatform:2.0.3")
            implementation("dev.datlag:kcef:2025.03.23")
            implementation(libs.bundles.ktor)
            implementation(libs.slf4j.api)
            implementation(libs.okio)
            // JSON serialization (replaces Gson)
            implementation(libs.kotlinx.serialization.json)

            // Date/Time (replaces SimpleDateFormat/Date)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            // UUID (replaces java.util.UUID)
            implementation(libs.uuid)

            implementation(libs.navigation.compose)
//            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation(libs.material.icons.extended)

            implementation("eu.anifantakis:ksafe:1.1.1")
            implementation("eu.anifantakis:ksafe-compose:1.1.1") // ← Compose state (optional)

            implementation("androidx.security:security-crypto-ktx:1.1.0")


        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
        }

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)

        }

        afterEvaluate {
            tasks.withType<JavaExec> {
                jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
                jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

                if (System.getProperty("os.name").contains("Mac")) {
                    jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
                    jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
                    jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
                }
            }
        }
    }
}

val appVersion = "2.0.1"

android {
    namespace = "org.substitute.schedule"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.substitute.schedule"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = appVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "SubstituteSchedule_v${appVersion}.apk"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.substitute.schedule.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.substitute.schedule"
            packageVersion = appVersion
        }
    }
}

// Task to generate version.properties (runs during configuration)
val generateVersionProperties = tasks.register("generateVersionProperties") {
    doLast {
        val resourcesDir = file("src/jvmMain/resources")
        resourcesDir.mkdirs()
        val versionFile = file("$resourcesDir/version.properties")
        versionFile.writeText("version=$appVersion")
    }
}

// Run it automatically
generateVersionProperties.get().actions.forEach { it.execute(generateVersionProperties.get()) }
