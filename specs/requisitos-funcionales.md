# Especificación de Requisitos Funcionales - GYM

## 1. Módulo de Gestión de Entrenamientos
- **FR-01**: Creación automática y guiada de rutinas de gimnasio a partir de planes nutricionales externos en formato PDF proporcionados por nutricionistas.
- **FR-02**: Configuración y personalización de rutinas de entrenamiento seleccionando equipamiento y maquinaria específica disponible en el gimnasio.
- **FR-03**: Seguimiento en tiempo real del progreso de entrenamiento, registro de pesos, repeticiones, series y hitos alcanzados.
- **FR-04**: Calendario interactivo de entrenamientos con notificaciones y vistas de progreso mensual/anual.

## 2. Módulo de Control Nutricional
- **FR-05**: Lectura y análisis automatizado de dietas externas en PDF mediante el subagente de datos.
- **FR-06**: Registro diario de comidas y comparación con los macronutrientes y calorías establecidos en el plan del nutricionista.

## 3. Calidad, Diseño y Seguridad
- **FR-07**: Validación visual de todas las pantallas en Jetpack Compose mediante el MCP de OpenDesign.
- **FR-08**: Almacenamiento cifrado de credenciales de usuario mediante `EncryptedSharedPreferences` y Android Keystore.
- **FR-09**: Cobertura completa de pruebas unitarias y de interfaz con ejecución de bucles de verificación `./gradlew test lint`.
- **FR-10**: Documentación técnica en castellano y trazabilidad de decisiones en `docs/decisions/`.
