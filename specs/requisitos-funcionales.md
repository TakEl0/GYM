# Especificación de Requisitos Funcionales - GYM

## 0. Módulo de Autenticación y Cuenta
- **FR-00**: Creación de cuenta con correo electrónico, contraseña (mínimo 8 caracteres) y nombre completo.
- **FR-01**: Inicio de sesión persistente con restauración automática de la sesión cifrada al reabrir la aplicación (EncryptedSharedPreferences + Android Keystore).
- **FR-02**: Cierre de sesión con limpieza segura de credenciales cifradas locales y de memoria (el campo de contraseña se elimina del estado al salir).
- **FR-03**: Sincronización del perfil del usuario entre el backend remoto (Supabase GoTrue) y la base de datos local Room.
- **FR-04**: Modo de desarrollo sin backend: cuando no hay credenciales Supabase configuradas, la autenticación funciona en memoria mediante repositorios simulados (`*Fake`).

## 1. Módulo de Gestión de Entrenamientos
- **FR-05**: Creación automática y guiada de rutinas de gimnasio a partir de planes nutricionales externos en formato PDF proporcionados por nutricionistas.
- **FR-06**: Configuración y personalización de rutinas de entrenamiento seleccionando equipamiento y maquinaria específica disponible en el gimnasio.
- **FR-07**: Seguimiento en tiempo real del progreso de entrenamiento, registro de pesos, repeticiones, series y hitos alcanzados.
- **FR-08**: Calendario interactivo de entrenamientos con notificaciones y vistas de progreso mensual/anual.

## 2. Módulo de Control Nutricional
- **FR-09**: Lectura y análisis automatizado de dietas externas en PDF mediante el subagente de datos.
- **FR-10**: Registro diario de comidas y comparación con los macronutrientes y calorías establecidos en el plan del nutricionista.
- **FR-11**: Cálculo del resumen nutricional diario (consumido, objetivo, restante y progreso porcentual) según el método Naturvitia.

## 3. Módulo de Peso y Recomposición Corporal
- **FR-12**: Registro de mediciones de peso corporal y porcentaje de grasa con validación de rangos.
- **FR-13**: Historial reactivo de peso con consulta del último registro y evolución temporal.

## 4. Módulo de Comunidad
- **FR-14**: Muro de actividad con publicaciones de la comunidad (compartir entrenamientos, motivar a otros usuarios).
- **FR-15**: Reacciones a publicaciones (LIKE, FIRE, MUSCLE) con lectura pública para usuarios autenticados.
- **FR-16**: Eventos grupales de calendario con creación y consulta por usuario autenticado.

## 5. Calidad, Diseño y Seguridad
- **FR-17**: Validación visual de todas las pantallas en Jetpack Compose mediante el MCP de OpenDesign.
- **FR-18**: Almacenamiento cifrado de credenciales y sesión mediante `EncryptedSharedPreferences` y Android Keystore (AES256_GCM).
- **FR-19**: Ausencia total de secretos en el código: las credenciales de Supabase se inyectan mediante `local.properties` (excluido de git) y `BuildConfig`.
- **FR-20**: Respaldo del sistema desactivado (`android:allowBackup="false"`) para proteger los datos locales de salud.
- **FR-21**: Cobertura completa de pruebas unitarias y de interfaz con ejecución de bucles de verificación `./gradlew test lint`.
- **FR-22**: Políticas Row Level Security (RLS) habilitadas en todas las tablas del esquema remoto de Supabase (ver ADR 0002).
- **FR-23**: Documentación técnica en castellano y trazabilidad de decisiones en `docs/decisions/`.
- **FR-24**: Importación de los documentos PDF del método Naturvitia (Dieta, Entrenamiento y Báscula/InBody) mediante el selector de documentos del sistema, procesando el texto con la librería PDFBox para Android.
- **FR-25**: Registro automático en la aplicación de los datos extraídos de los documentos importados: comidas del plan nutricional, rutinas de entrenamiento y medidas corporales (peso y grasa), persistiéndose en Room y sincronizándose con Supabase.
