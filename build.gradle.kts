// Configuración raíz del proyecto Android GYM
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    // Plugin de serialización JSON de Kotlin (necesario para Supabase/PostgREST).
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
    // KSP (Kotlin Symbol Processing) para el compilador de Room.
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}