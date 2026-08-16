# AGENTS.md - Proyecto de Aplicación Android para Gimnasio (GYM)

Este archivo proporciona instrucciones clave, reglas de gobernanza, directrices arquitectónicas y estructuras de orquestación de agentes para todas las sesiones de OpenCode que trabajen en la aplicación Android GYM.

---

## Idioma y Documentación
- **Idioma Oficial**: Todo el contenido de la documentación, comentarios en el código, registros (logs), mensajes y comunicaciones con el usuario deben estar **íntegramente en castellano**.
- **Documentación del Código**: Todo el código de la aplicación debe estar documentado de forma exhaustiva y profesional (KDoc para clases, funciones y componentes principales), explicando claramente el propósito, funcionamiento y lógica de negocio en cada módulo.

---

## 1. Visión del Proyecto y Características Principales
- **Plataforma**: Aplicación móvil nativa para Android (Kotlin, Jetpack Compose, Arquitectura Limpia / Clean Architecture).
- **Dominio Principal**:
  - **Gestión de Entrenamientos**: Creación de rutinas a partir de planes nutricionales externos, configuración de rutinas personalizadas, selección de entrenamientos mediante maquinaria del gimnasio, seguimiento de progreso, hitos, calendarios interactivos.
  - **Control Nutricional**: Registro de comidas y seguimiento de dietas alineadas con los planes del nutricionista.
- **Calidad y Seguridad**: Arquitectura robusta, segura y lista para producción, con manejo de errores avanzado y gestión segura de secretos.

---

## 2. Orquestación de Agentes y Gobernanza

### 2.1 Jerarquía y Roles de Agentes
1. **Agente Orquestador**:
   - Supervisa el progreso del proyecto, desglosa las peticiones del usuario en unidades de trabajo discretas y asigna tareas a subagentes especializados utilizando modelos de lenguaje gratuitos adecuados (ej. Gemini Flash).
2. **Agente Analista / Auditor**:
   - Agente de revisión especializado responsable de auditar la calidad del código, el cumplimiento de la arquitectura, la cobertura de pruebas y de delegar subtareas específicas de revisión de código y seguridad utilizando modelos gratuitos.
3. **Subagentes Especializados**:
   - Agentes de ejecución específicos por dominio (UI/Compose, Dominio/Casos de Uso, Datos/Room/API, integración MCP).

### 2.2 Gobernanza y Documentación
- **Registros de Decisiones (`docs/decisions/`)**: Documentar decisiones arquitectónicas (ADRs), contratos de API y diseños de esquemas para que cualquier agente en cualquier dispositivo pueda retomar el trabajo instantáneamente.
- **Sincronización de Especificaciones (`specs/`)**: Mantener las especificaciones funcionales y técnicas actualizadas junto con la implementación.

---

## 3. MCPs, Habilidades y Herramientas

### 3.1 Integraciones y MCPs Requeridos
- **MCP `opendesign`**: Conectado vía MCP para validar el diseño de pantallas, estilos de interfaz, paletas de colores y consistencia de UX frente a las especificaciones de diseño.
- **Habilidades Adicionales**: Instalar y mantener habilidades de desarrollo Android profesional (buenas prácticas en Kotlin/Compose, patrones de arquitectura limpia, almacenamiento seguro).

---

## 4. Seguridad y Gestión de Secretos
- **Credenciales y Claves de API**:
   - Nunca codificar claves o secretos en duro (hardcoding).
   - Almacenar los secretos específicos del entorno de forma segura mediante configuraciones cifradas locales (`.env` cifrado / `local.properties` excluidos del control de versiones).
   - Documentar detalladamente las estructuras de secretos y los procedimientos de configuración en la documentación de gobernanza protegida.

---

## 5. Flujo de Trabajo y Control de Versiones
- **Commits Locales**: Realizar commits locales frecuentes y atómicos con mensajes claros en castellano (`git commit -m "feat: ..."`), permitiendo retrocesos seguros y un historial granular.
- **Sincronización con GitHub**: Enviar los cambios verificados al repositorio remoto de GitHub regularmente.
- **Bucles de Verificación**:
   - Ejecutar comprobaciones de linter, pruebas unitarias y compilación (`./gradlew test`, `./gradlew lint`) antes de finalizar cualquier funcionalidad importante.
