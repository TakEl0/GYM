---
name: android-coroutines-flow
description: Habilidad especializada en asincronía moderna de Kotlin (Coroutines y Flow) para Clean Architecture y Room en la app GYM.
---

# Habilidad: Flujos Reactivos y Corrutinas en Kotlin (GYM)

Directrices obligatorias para el `subagente-dominio` y `subagente-datos`:

1. **Corrutinas de Kotlin (`suspend` / `Dispatchers`)**:
   - Utilizar funciones `suspend` para operaciones asíncronas de una sola ejecución (llamadas a red, consultas de base de datos de una sola vez).
   - Inyectar `Dispatchers` mediante constructores para garantizar la testabilidad en unit testing (`StandardTestDispatcher`).

2. **Flujos Reactivos (`StateFlow` / `SharedFlow` / `Flow`)**:
   - Exponer estados de UI en los `ViewModels` utilizando `StateFlow` inmutables respaldados por `MutableStateFlow`.
   - Consumir flujos de datos reactivos desde la base de datos local (**Room**) mediante `Flow<T>`, asegurando la actualización en tiempo real del progreso del entrenamiento y la dieta.

3. **Manejo Estructurado de Concurrencia**:
   - Utilizar `viewModelScope` en la capa de presentación y cancelar automáticamente tareas cuando el ciclo de vida del ViewModel termine.
   - Manejar excepciones mediante bloques `try-catch` y `Result<T>` envueltos en los Casos de Uso.
