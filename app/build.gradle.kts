/**
 * Configuración del módulo de la aplicación Android GYM.
 * Define dependencias de Jetpack Compose, Clean Architecture, Room y Supabase.
 *
 * Los secretos de Supabase (URL del proyecto y clave anónima) NO se codifican en duro:
 * se inyectan mediante `local.properties` (excluido del control de versiones) y se
 * exponen a la aplicación a través de campos de BuildConfig.
 */
import java.util.Properties

// Lectura segura de secretos de configuración local (local.properties).
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) {
        archivo.inputStream().use { load(it) }
    }
}

// URL del proyecto Supabase (p. ej. https://xxxx.supabase.co).
val supabaseUrl: String = propiedadesLocales.getProperty("SUPABASE_URL") ?: ""
// Clave pública anónima (anon key) del proyecto Supabase.
val supabaseAnonKey: String = propiedadesLocales.getProperty("SUPABASE_ANON_KEY") ?: ""

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Serialización JSON de Kotlin para los modelos de Supabase.
    id("org.jetbrains.kotlin.plugin.serialization")
    // KSP para el compilador de Room.
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.gym.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gym.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Campos de BuildConfig con los secretos de Supabase.
        // Si local.properties no está configurado, quedan vacíos y la app
        // muestra el estado "configuración pendiente" en lugar de fallar.
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")

        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // ---------------------------------------------------------------------
    // Nombre descriptivo del APK
    // ---------------------------------------------------------------------
    // ANOTACIÓN (requisito del usuario): el nombre del APK generado debe ser
    // descriptivo para facilitar su identificación al instalarlo a mano o al
    // distribuirlo entre dispositivos. Se genera con el patrón:
    //   GYM-MMG-<versionName>-<buildType>.apk   (p. ej. GYM-MMG-1.0-debug.apk)
    // Aplicable a todos los build types (debug y release).
    // ---------------------------------------------------------------------
    applicationVariants.all {
        val variante = this
        variante.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { salida ->
                salida.outputFileName = "GYM-MMG-${variante.versionName}-${variante.name}.apk"
            }
    }
}

dependencies {
    // --- Núcleo Android y Compose ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // --- Persistencia local: Room ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // --- Almacenamiento seguro de sesión: EncryptedSharedPreferences ---
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // --- Procesamiento de PDF (Naturvitia / InBody / Rutinas) ---
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // --- Backend Supabase (autenticación persistente + datos de comunidad) ---
    // Versión 2.2.2 compilada con Kotlin 1.9.22, compatible con el proyecto (Kotlin 1.9.23).
    implementation(platform("io.github.jan-tennert.supabase:bom:2.2.2"))
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    // Motor HTTP de Ktor para Android (usado por Supabase).
    implementation("io.ktor:ktor-client-okhttp:2.3.12")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}