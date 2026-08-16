# ADR 0003: Calendario Personal de Entrenamientos

## Estado
Aprobado / Implementado

## Contexto
El módulo de gestión de entrenamientos (FR-04) exige un calendario interactivo de entrenamientos con vistas de progreso mensual. La tabla `entrenamientos` carecía de una fecha de sesión, por lo que no era posible agrupar ni planificar los entrenos por día.

## Decisión
1. **Añadir la columna `fecha` (bigint, epoch millis) a la tabla `entrenamientos`** de Supabase y a la entidad Room `EntidadEntrenamiento`, al DTO remoto `DtoEntrenamientoRemoto` y al modelo de dominio `Entrenamiento`.
2. **Consultas por rango de fechas**:
   - DAO Room: `observarEntreFechas(userId, inicio, fin)` y `obtenerEnFecha(userId, fecha)`.
   - Puerto `RepositorioEntrenamiento`: `observarEntrenamientosEntre(inicio, fin)` y `obtenerEntrenamientoEnFecha(fecha)`.
3. **Caso de uso**: `ObservarEntrenamientosCalendarioCasoUso`, expuesto desde el `ContenedorDependencias`.
4. **Pantalla Compose**: `PantallaCalendarioEntrenosGYM` con rejilla mensual (semana comenzando en lunes), marcado de días con sesión, navegación entre meses y detalle de las sesiones del día seleccionado.
5. **Navegación**: Nuevo destino `CALENDARIO` en la barra inferior.

## Esquema SQL de Referencia
```sql
ALTER TABLE public.entrenamientos
    ADD COLUMN IF NOT EXISTS fecha bigint NOT NULL DEFAULT 0;
```

## Notas
- La versión de Room se incrementó a `2` (migración destructiva de desarrollo mediante `fallbackToDestructiveMigration`).
- Los entrenamientos con `fecha = 0` se consideran sin fecha asignada y no se muestran en el calendario.
- La conversión de `fecha` a día calendario se realiza con la zona horaria del sistema.