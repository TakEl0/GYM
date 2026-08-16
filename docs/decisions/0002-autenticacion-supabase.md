# ADR 0002: Autenticación Persistente y Base de Datos con Supabase y Room

## Estado
Aprobado / Implementado

## Contexto
La aplicación Android GYM requiere una capa de persistencia híbrida:
1. **Persistencia local robusta** con **Room** (modo offline-first) para garantizar acceso instantáneo y funcionamiento sin conexión a internet.
2. **Backend en la nube** con **Supabase** (PostgREST y GoTrue) para autenticación persistente y sincronización de datos entre dispositivos y comunidad.
3. **Almacenamiento seguro** de credenciales y tokens mediante Android Keystore (`EncryptedSharedPreferences`).

---

## Esquema SQL de Referencia para Supabase

Ejecuta el siguiente script SQL en el editor SQL de tu proyecto Supabase para crear las tablas y las políticas de seguridad Row Level Security (RLS):

```sql
-- 1. Tabla de Perfiles de Usuario
create table public.perfiles (
    id uuid references auth.users not null primary key,
    email text not null,
    nombre text not null,
    peso_objetivo_kg numeric,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.perfiles enable row level security;

create policy "Usuarios pueden ver su propio perfil"
    on public.perfiles for select
    using (auth.uid() = id);

create policy "Usuarios pueden actualizar su propio perfil"
    on public.perfiles for update
    using (auth.uid() = id);

create policy "Usuarios pueden insertar su propio perfil"
    on public.perfiles for insert
    with check (auth.uid() = id);


-- 2. Tabla de Registros de Peso
create table public.registros_peso (
    id text not null primary key,
    user_id uuid references auth.users not null,
    peso_kg numeric not null,
    grasa_corporal numeric,
    fecha bigint not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.registros_peso enable row level security;

create policy "Usuarios gestionan sus propios registros de peso"
    on public.registros_peso for all
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);


-- 3. Tabla de Entrenamientos
create table public.entrenamientos (
    id text not null primary key,
    user_id uuid references auth.users not null,
    nombre text not null,
    grupo_muscular text[] not null,
    series_totales integer not null,
    ejercicios_realizados integer not null,
    total_ejercicios integer not null,
    duracion_minutos integer not null,
    completo boolean not null,
    fecha bigint not null default 0
);

alter table public.entrenamientos enable row level security;

create policy "Usuarios gestionan sus propios entrenamientos"
    on public.entrenamientos for all
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);


-- 4. Tabla de Comidas y Nutrición
create table public.comidas (
    id text not null primary key,
    user_id uuid references auth.users not null,
    nombre text not null,
    kcal integer not null,
    proteinas_g numeric not null,
    carbohidratos_g numeric not null,
    grasas_g numeric not null,
    tipo_ingesta text not null,
    fecha bigint not null
);

alter table public.comidas enable row level security;

create policy "Usuarios gestionan sus propias comidas"
    on public.comidas for all
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);


-- 5. Tabla de Publicaciones (Comunidad)
create table public.publicaciones (
    id text not null primary key,
    user_id uuid references auth.users not null,
    autor_nombre text not null,
    contenido text not null,
    url_imagen text,
    tipo text not null default 'ENTRENAMIENTO',
    fecha bigint not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.publicaciones enable row level security;

create policy "Lectura pública de publicaciones para autenticados"
    on public.publicaciones for select
    to authenticated
    using (true);

create policy "Usuarios crean sus propias publicaciones"
    on public.publicaciones for insert
    to authenticated
    with check (auth.uid() = user_id);

create policy "Usuarios borran sus propias publicaciones"
    on public.publicaciones for delete
    to authenticated
    using (auth.uid() = user_id);


-- 6. Tabla de Reacciones (Comunidad)
create table public.reacciones (
    id text not null primary key,
    publicacion_id text references public.publicaciones not null,
    user_id uuid references auth.users not null,
    tipo_reaccion text not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.reacciones enable row level security;

create policy "Lectura de reacciones para autenticados"
    on public.reacciones for select
    to authenticated
    using (true);

create policy "Usuarios gestionan sus propias reacciones"
    on public.reacciones for all
    to authenticated
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);


-- 7. Tabla de Eventos de Calendario
create table public.calendario_eventos (
    id text not null primary key,
    user_id uuid references auth.users not null,
    titulo text not null,
    descripcion text,
    fecha_inicio bigint not null,
    fecha_fin bigint not null,
    tipo text not null default 'ENTRENAMIENTO_GRUPAL',
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

alter table public.calendario_eventos enable row level security;

create policy "Usuarios gestionan sus propios eventos de calendario"
    on public.calendario_eventos for all
    to authenticated
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

-- Otorgar privilegios a los roles de la API de Supabase (PostgREST)
grant usage on schema public to anon, authenticated;
grant select, insert, update, delete on all tables in schema public to anon, authenticated;
grant usage on all sequences in schema public to anon, authenticated;
alter default privileges in schema public grant select, insert, update, delete on tables to anon, authenticated;
```

---

## Notas de Compatibilidad con `supabase-kt` v2.2.2
- **Auth (GoTrue)**: módulo de autenticación importado desde `io.github.jan.supabase.gotrue.*`; el plugin se instala mediante `install(Auth)`.
- **SessionManager**: `SessionManager.loadSession()` devuelve `UserSession?` (nullable) en v2.2.2 (no lanza `NoSessionFoundException`, perteneciente a la API v3). La sesión se cifra con `GestorSesionCifrado` (EncryptedSharedPreferences + Android Keystore).
- **PostgREST**: mapeo de columnas en `snake_case` mediante `@SerialName` en los DTOs remotos (`DtoPerfilRemoto`, `DtoRegistroPesoRemoto`, `DtoEntrenamientoRemoto`, `DtoComidaRemoto`, `DtoEventoCalendarioRemoto`).
- **Tabla de eventos de calendario**: el nombre real de la tabla en el proyecto es `public.calendario_eventos` (utilizada por `RepositorioComunidadSupabase`), no `eventos_calendario`. Verificado vía Management API.

---

## Credenciales del Proyecto Activo (Supabase)
- **Proyecto**: `GymApp`
- **Project Ref**: `zuxodsyoesxzyoldahrc`
- **URL**: `https://zuxodsyoesxzyoldahrc.supabase.co`
- **Estado del proyecto**: `ACTIVE_HEALTHY` (PostgreSQL 17.6.1, región `eu-west-1`). Verificado mediante la Management API (`GET /v1/projects/{ref}`).

### Gestión segura de secretos
> **IMPORTANTE**: Los secretos NO se documentan en este repositorio ni se codifican en duro.

- **Ubicación de los secretos**: todos los secretos se almacenan en `.env` (raíz del proyecto, excluido de git por `.gitignore`). La app solo consume `SUPABASE_URL` y `SUPABASE_ANON_KEY`, que se inyectan mediante `local.properties` (también excluido de git) y se exponen a través de `BuildConfig`. El token de gestión (`SUPABASE_ACCESS_TOKEN`), el ref del proyecto, el ID de organización y la contraseña de la base de datos residen únicamente en `.env` y en el gestor de secretos del propietario / CI.
- **`SUPABASE_ANON_KEY`**: clave pública anónima. Nunca debe incluirse la clave `service_role`.
- **Contraseña de Base de Datos (DB Password)**: se almacena únicamente en `.env` y en el gestor de secretos del propietario. No debe aparecer en ningún documento versionado.
- **Verificación de exclusión**: comprobar con `git check-ignore -v local.properties .env` que ambos estén excluidos del control de versiones.
- **Rotación**: si un secreto se filtra, rotarlo desde el panel de Supabase y actualizar `.env` y `local.properties`.

## Decisiones de Implementación Registradas
- **Fallback a Fakes**: si `local.properties` no contiene credenciales válidas, `ClienteSupabase.estaConfigurado` devuelve `false` y el `ContenedorDependencias` inyecta los repositorios simulados (`*Fake`) para permitir desarrollo y previsualización sin backend.
- **Higiene de credenciales en memoria**: el `AutenticacionViewModel` limpia el campo de contraseña inmediatamente tras un login o registro exitoso, y al pasar a `NO_AUTENTICADO`.
- **Backups desactivados**: `android:allowBackup="false"` en el manifiesto para evitar la exportación de datos locales de salud (Room) a respaldos del sistema.
- **API v2 de supabase-kt**: se usan las firmas de la versión 2.2.2 (`signUpWith(Email)`, `signInWith(Email)`, `SessionStatus.LoadingFromStorage`/`NetworkError`, `UserInfo` como tipo de `UserSession.user`). No usar la API v3 (`io.github.jan.supabase.auth.*`).
- **Compilador de Compose 1.5.13**: compatible con Kotlin 1.9.23 (el 1.5.8 requiere Kotlin 1.9.22 y el 1.5.14 requiere 1.9.24).
- **Gradle 8.7**: versión del wrapper generado, compatible con AGP 8.3.0.
- **Importación de documentos PDF (Naturvitia)**: se añade `com.tom-roush:pdfbox-android:2.0.27.0` para extraer texto de la Dieta, el Plan de Entrenamiento y el Informe de Báscula/InBody; el caso de uso `ImportarDocumentosNaturvitiaCasoUso` registra automáticamente comidas, rutinas y medidas corporales en Room y Supabase.