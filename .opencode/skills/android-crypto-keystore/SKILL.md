---
name: android-crypto-keystore
description: Habilidad especializada en criptografía y Android Keystore para el almacenamiento seguro de secretos y tokens mediante EncryptedSharedPreferences.
---

# Habilidad: Criptografía y Android Keystore (GYM)

Directrices de seguridad obligatorias para el `subagente-seguridad`:

1. **Almacenamiento Cifrado (`EncryptedSharedPreferences`)**:
   - Utilizar `EncryptedSharedPreferences` de la librería `androidx.security:security-crypto` para guardar tokens de sesión, credenciales y datos sensibles.
   - Las claves maestras se generan y almacenan de forma segura en el **Android Keystore System** mediante hardware de seguridad (cuando esté disponible).

2. **Prohibición de Credenciales en Duro (Hardcoding)**:
   - Ninguna clave de API, contraseña o secreto debe estar presente en el código fuente.
   - Las configuraciones de entorno deben residir en `local.properties` (excluido en `.gitignore`) o en variables cifradas.

3. **Auditoría Continua**:
   - Inspeccionar regularmente el código para detectar registros (`Log.d`) que puedan filtrar información sensible o PII (Personally Identifiable Information).
   - Documentar cualquier mecanismo de cifrado en la gobernanza del proyecto.
