# ADR 001: Integración de Equipamiento Específico Fitness Park y Arquitectura Especializada

## Estado
Aprobado y Vigente

## Contexto
La aplicación Android GYM no busca ser un software genérico o básico de seguimiento de entrenamientos, sino una solución de élite diseñada específicamente para usuarios de la red de gimnasios **Fitness Park**. Los centros Fitness Park cuentan con maquinaria de gama alta muy específica (marcas líderes como *Technogym* [Excite, Artis, Skill], *Hammer Strength*, *Panatta*, *Life Fitness*, poleas Kinesis, racks de peso libre y zonas de cross-training). 

Para ofrecer una experiencia inmersiva, precisa y superior, la arquitectura debe modelar y reflejar fielmente este inventario de maquinaria y equipamiento, permitiendo vincular los ejercicios directamente al aparato exacto y registrar el progreso y cargas históricas de forma granular.

---

## Decisiones Arquitectónicas

### 1. Dominio y Modelado Específico (`subagente-dominio`)
Se estructurarán entidades y casos de uso en Clean Architecture orientados al equipamiento real de Fitness Park:
- **`GymMachine`**: Representa la máquina específica (ej. *Press de pecho convergente Hammer Strength*, *Polea alta Technogym*).
- **`EquipmentBrand`**: Catálogo de fabricantes (Hammer Strength, Technogym, Panatta, etc.).
- **`FitnessParkZone`**: Zonas del club (Sala de Fuerza Guiada, Peso Libre, Cardio Avanzado, Zona Functional).
- **Vínculo de Ejercicios**: Cada rutina de entrenamiento y ejercicio individual estará asociada a la máquina o equipamiento exacto disponible en el centro, registrando ajustes (número de placas, posición de asiento, agarres).

### 2. Capa de Datos y Offline-First (`subagente-datos`)
- **Base de Datos Local (Room)**: Precarga robusta con el inventario completo de maquinaria de Fitness Park para garantizar funcionamiento instantáneo sin dependencias de red.
- **Historial de Rendimiento**: Registro detallado de series, repeticiones, peso/placas, RPE (esfuerzo percibido) y notas por cada máquina específica.
- **Procesamiento de PDF**: Extracción de planes nutricionales y de entrenamiento externos mediante **Google Gemini Flash Lite** para alinear las dietas del nutricionista con las rutinas de fuerza.

### 3. Interfaz de Usuario de Alta Gama (`subagente-ui`)
- **Jetpack Compose**: Pantallas inmersivas, fluidas y con diseño visual adaptado a la selección rápida de máquinas por zonas de Fitness Park.
- **Estadísticas Avanzadas**: Gráficas de evolución de cargas específicas por cada aparato.

### 4. Resiliencia y Modelos de IA
Para garantizar que el desarrollo y la ejecución de los agentes nunca se detengan por límites de tasa (`429 Rate Limit`) o fallos externos, se utiliza la siguiente asignación de modelos estables nativos de OpenCode y Google:
- **Orquestador y Dominio**: `opencode/deepseek-v4-flash-free`
- **Analista, UI, Seguridad, Datos y Testing**: `google/gemini-3.5-flash-lite`

---

## Consecuencias
- **Valor Diferencial**: La aplicación ofrecerá una experiencia profesional adaptada al 100% a las instalaciones reales de Fitness Park.
- **Robustez Técnica**: Clean Architecture desacoplada, persistencia local con Room y cero bloqueos en la IA gracias a la resiliencia de modelos nativos.
