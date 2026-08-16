---
name: android-security
description: Utilizar exclusivamente al gestionar claves de API, secretos, credenciales o almacenamiento seguro en la aplicación Android GYM para garantizar la máxima seguridad y cumplimiento de normativas.
---

# Habilidad: Seguridad y Gestión de Secretos (GYM)

Esta habilidad establece las directrices estrictas de seguridad para la aplicación GYM:

1. **Gestión de Secretos y Credenciales**:
   - **Prohibido el Hardcoding**: Ninguna clave de API, contraseña o secreto debe estar escrito directamente en el código fuente.
   - **Almacenamiento Seguro**: Utilizar `EncryptedSharedPreferences` o KeyStore de Android para almacenar tokens de usuario o credenciales sensibles de forma local.
   - **Configuración de Entorno**: Las claves de API y configuraciones sensibles deben residir en archivos locales excluidos del control de versiones (ej. `local.properties`, `.env` cifrados).

2. **Auditoría y Documentación**:
   - Cualquier adición de configuración sensible debe estar documentada en el registro de decisiones y gobernanza protegida.
   - Los mensajes de error y registros de depuración (logs) nunca deben exponer información de identificación personal (PII) ni secretos.
   - Todo el contenido y documentación relacionada debe estar en **castellano**.
