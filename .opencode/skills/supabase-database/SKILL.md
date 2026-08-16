---
name: supabase-database
description: Habilidad especializada en Supabase Postgrest, consultas, DTOs con snake_case y políticas de seguridad Row Level Security (RLS) para la app GYM.
---

# Habilidad: Supabase Database y Postgrest (GYM)

Esta habilidad guía la interacción con la base de datos PostgreSQL de Supabase en la app GYM:

1. **Mapeo y Consultas Postgrest (`postgrest-kt`)**:
   - Uso de `supabase.from("tabla").select()`, `insert()`, `update()`, `delete()`.
   - Mapeo de columnas con `snake_case` mediante `@SerialName` en DTOs remotos (`DtoPerfilRemoto`, `DtoRegistroPesoRemoto`, `DtoEntrenamientoRemoto`, `DtoComidaRemoto`).

2. **Políticas Row Level Security (RLS)**:
   - Asegurar que todas las tablas tengan RLS habilitado.
   - Filtrado y validación por `auth.uid() = user_id` para garantizar el aislamiento de datos por usuario.

3. **Arquitectura Offline-First**:
   - Sincronización entre la base de datos local (Room) y Supabase en la nube a través de repositorios limpios (`RepositorioPesoSupabase`, `RepositorioEntrenamientoSupabase`, `RepositorioComidaSupabase`).
   - Todo el contenido y documentación deben estar en **castellano**.
