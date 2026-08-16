---
name: android-advanced-testing
description: Habilidad especializada en pruebas unitarias con JUnit y MockK, y pruebas de interfaz con Compose UI Tests para la app GYM.
---

# Habilidad: Testing Avanzado en Android (GYM)

Directrices obligatorias para el `subagente-testing`:

1. **Pruebas Unitarias (Domain & Data)**:
   - Utilizar **JUnit 4 / JUnit 5** combinados con **MockK** para mockear dependencias en Casos de Uso (`UseCases`) y Repositorios.
   - Probar exhaustivamente los flujos de negocio (ej. conversión de planes nutricionales a rutinas de entrenamiento).

2. **Pruebas de Interfaz (Jetpack Compose UI Tests)**:
   - Utilizar `createComposeRule()` para validar componentes de interfaz en Jetpack Compose.
   - Verificar estados visuales, interacciones de usuario y accesibilidad en las pantallas principales.

3. **Bucles de Verificación Automáticos**:
   - Ejecutar obligatoriamente `./gradlew test` y `./gradlew lint` antes de validar cualquier hito o funcionalidad importante.
   - Documentar los resultados y cobertura de pruebas.
