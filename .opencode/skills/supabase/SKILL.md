---
name: supabase
description: Habilidad especializada en la integración de Supabase (PostgREST, GoTrue, Auth, RLS y supabase-kt) en la aplicación Android GYM.
---

# Habilidad: Integración con Supabase (GYM)

Esta habilidad guía el desarrollo, consulta y gestión de Supabase en la aplicación GYM:

1. **Cliente Supabase en Android (`supabase-kt`)**:
   - Configuración centralizada en `ClienteSupabase.kt`.
   - Uso de módulos `Gotrue` (Autenticación) y `Postgrest` (Base de datos).
   - Mapeo de DTOs remotos (`DtoPerfilRemoto`, `DtoRegistroPesoRemoto`, `DtoEntrenamientoRemoto`, `DtoComidaRemoto`) utilizando `snake_case` y `@SerialName`.

2. **Esquema de Base de Datos y RLS**:
   - Tablas principales: `perfiles`, `registros_peso`, `entrenamientos`, `comidas`.
   - Row Level Security (RLS) habilitada en todas las tablas para garantizar que cada usuario solo acceda a sus propios datos mediante `auth.uid() = user_id` (o `id` en perfiles).

3. **MCP de Supabase**:
   - Utilizar el servidor MCP de Supabase (`@supabase/mcp-server-supabase`) para interactuar con la base de datos, inspeccionar esquemas y gestionar migraciones de forma automatizada.
   - Idioma oficial de documentación y explicaciones: **castellano**.
