---
name: supabase-auth
description: Habilidad especializada en autenticación con Supabase GoTrue, gestión de sesiones y almacenamiento seguro con Android Keystore para la app GYM.
---

# Habilidad: Supabase Auth (GYM)

Esta habilidad guía la implementación y gestión de la autenticación en la app GYM utilizando Supabase Auth (`auth-kt` / GoTrue):

1. **Autenticación de Usuarios**:
   - Soporte para inicio de sesión por correo electrónico y contraseña (`supabase.auth.signInWith(Email)`).
   - Registro de nuevos usuarios (`supabase.auth.signUpWith(Email)`).
   - Cierre de sesión (`supabase.auth.signOut()`).

2. **Gestión Segura de Sesiones**:
   - Persistencia de tokens de sesión mediante `EncryptedSharedPreferences` y Android Keystore (`GestorSesionCifrado`).
   - Recuperación automática del estado de sesión al iniciar la aplicación.

3. **Manejo de Errores y Seguridad**:
   - Captura de excepciones específicas de red y autenticación (`RestException`, `AuthRestException`).
   - Prohibición estricta de hardcoding de credenciales o claves API sensibles.
   - Toda la documentación y comentarios deben estar en **castellano**.
