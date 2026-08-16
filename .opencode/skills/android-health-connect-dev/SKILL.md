---
name: android-health-connect-dev
description: Habilidad especializada en desarrollo Android nativo (Kotlin, Min SDK 26, Target SDK 34) e integración con Jetpack Health Connect y Supabase para sincronización de peso y actividad física.
---

# Habilidad: Desarrollo e Integración Health Connect (`android_health_connect_dev`)

Asignada a `subagente-datos` y `subagente-dominio`:

1. **Entorno y Dependencias**:
   - IDE: Android Studio / Gradle Kotlin DSL
   - Lenguaje: Kotlin
   - Min SDK: 26, Target SDK: 34
   - Librerías Clave:
     - `androidx.health.connect:connect-client:1.1.0-alpha10`
     - `io.supabase:postgrest-kt`

2. **Capacidades Principales**:
   - Verificación de disponibilidad del cliente Jetpack Health Connect (`HealthConnectClient.getSdkStatus()`).
   - Gestión de permisos dinámicos en tiempo de ejecución (Lectura de Peso `READ_WEIGHT`, Grasa Corporal `READ_BODY_FAT`, Pasos `READ_STEPS` y Ejercicio `READ_EXERCISE`).
   - Consulta de historial de datos con `TimeRangeFilter` dinámico.
   - Mapeo de DTOs para transformar registros de salud (`WeightRecord`, `StepsRecord`, `ExerciseSessionRecord`) a esquemas de base de datos.
   - Lógica de sincronización delta o por rango de fechas hacia la base de datos (Supabase).

3. **Prompt de Instrucción para el Desarrollador Android Senior**:
   - **Manifest & Permisos**: Declarar la actividad `ViewPermissionUsageActivity` en el `AndroidManifest.xml` y añadir permisos de lectura para peso, grasa corporal, pasos y ejercicio.
   - **Cliente Health Connect**: Implementar un `HealthConnectManager` singleton que compruebe la disponibilidad del SDK.
   - **Lectura de Datos**: Métodos independientes para leer `WeightRecord` (ej. báscula Renpho) y registros de actividad aplicando filtros de rango temporal para evitar duplicados.
   - **Mapeo y Persistencia**: Extracción de kilogramos (`record.weight.inKilograms`), marcas de tiempo (`record.time`) y empaquetado en JSON para sincronización backend.
   - **Manejo de Errores**: Bloques `try-catch` estructurados para permisos denegados o SDK no disponible (`Success` / `PermissionDenied` / `SDKUnavailable`).
