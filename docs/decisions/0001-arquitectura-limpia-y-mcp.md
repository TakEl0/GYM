# ADR 0001: Adopción de Arquitectura Limpia e Integración MCP en GYM

- **Estado**: Aprobado
- **Fecha**: 16 de Agosto de 2026
- **Contexto**: Se requiere desarrollar una aplicación Android profesional y escalable para la gestión de entrenamientos y control nutricional a partir de dietas externas de nutricionistas.
- **Decisión**:
  1. Adoptar **Clean Architecture** estructurada en tres capas (Presentación con Jetpack Compose, Dominio con Casos de Uso puros, y Datos con Room, API y procesamiento PDF).
  2. Integrar servidores MCP (`opendesign`, `context7`, `github`) para validación de diseño visual, recuperación de documentación oficial y sincronización con GitHub.
  3. Establecer una jerarquía de subagentes especializados (`subagente-ui`, `subagente-dominio`, `subagente-datos`, `subagente-seguridad`, `subagente-testing`) operando con modelos gratuitos optimizados (`DeepSeek`, `Llama 3.3`, `Qwen 2.5`, `Mistral Small`, `Gemini Flash`).
- **Consecuencias**:
  - Máxima modularidad, testeabilidad y mantenibilidad del código.
  - Documentación exhaustiva y estricta en **castellano** en todo el proyecto.
  - Seguridad reforzada mediante Android Keystore y cero credenciales en duro.
