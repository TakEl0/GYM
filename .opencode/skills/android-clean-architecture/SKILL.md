---
name: android-clean-architecture
description: Utilizar exclusivamente al desarrollar, refactorizar o estructurar código en Kotlin y Jetpack Compose siguiendo los principios de Arquitectura Limpia (Clean Architecture) en la app GYM.
---

# Habilidad: Arquitectura Limpia en Android (GYM)

Esta habilidad guía la estructura de capas de la aplicación Android GYM:

1. **Capa de Presentación (UI / ViewModels)**:
   - Desarrollada con **Jetpack Compose**.
   - Los componentes de UI observan estados inmutables (`StateFlow`) emitidos por el `ViewModel`.
   - Ninguna lógica de negocio reside en los Composables; toda la lógica se delega a los casos de uso.

2. **Capa de Dominio (Domain / Use Cases)**:
   - Contiene la lógica de negocio pura de la aplicación (creación de rutinas desde planes de nutricionista, cálculo de progreso, selección de maquinaria de gimnasio).
   - Independiente de frameworks Android y bases de datos. Contiene interfaces de repositorios (puertos).

3. **Capa de Datos (Data / Repositories / Sources)**:
   - Implementaciones concretas de los repositorios de dominio.
   - Fuentes de datos locales (**Room**) y remotas (**API / Red**).
   - Mappers para transformar modelos de base de datos o red en modelos de dominio.

4. **Documentación Obligatoria**:
   - Todo el código debe estar documentado en **castellano** usando KDoc (`/** ... */`) para clases, funciones públicas, Composable functions y casos de uso.
