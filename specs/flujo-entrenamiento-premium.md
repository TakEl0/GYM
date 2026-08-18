# Especificación — Entrenamiento en Vivo Premium (Sesión Activa)

> **Objetivo**: convertir la app GYM en una experiencia de entrenamiento de nivel de las
> mejores apps del mercado (Strong, Hevy, Fitbod): registrar series con carga real por
> ejercicio, cronómetro de descanso con avisos, edición en vivo de kg/reps y resumen de
> sesión con estadísticas. Todo sobre la maquinaria REAL de Fitness Park ya mapeada.

## 1. Flujo del usuario (de extremo a extremo)

1. El usuario abre la app → pestaña **Rutinas** → ve sus 5 rutinas Naturvitia por día
   (Día 1..5, cada una con sus ejercicios mapeados a máquinas reales: marca · modelo).
2. Pulsa **"Entrenar"** en la rutina del día → se abre **PantallaEntrenamientoEnVivo**.
3. La pantalla muestra:
   - **Cabecera de sesión**: nombre de la rutina, temporizador total de la sesión
     (cronómetro que corre en vivo) y barra de progreso (series completadas / total).
   - **Lista de ejercicios** en orden, cada tarjeta muestra: nombre del ejercicio,
     máquina real (marca · modelo), series completadas (p. ej. "2/4"), y chips de
     cada serie registrada con su kg y reps (p. ej. "80 kg × 12").
4. Al pulsar **"Registrar serie"** en un ejercicio se abre un **diálogo de serie**:
   - Campo **kg** (pre-rellenado con la *carga sugerida*: el último kg usado en ese
     ejercicio, o la proyección 1RM−10% si no hay historial).
   - Campo **reps** (pre-rellenado con las reps prescritas del plan, p. ej. 12).
   - Botón **"Guardar serie"**.
5. Al guardar la serie:
   - Se persiste `SerieRealizada` (ejercicio, serie N, kg, reps, fecha, sesión).
   - Se actualiza el chip en la tarjeta.
   - **Arranca automáticamente el cronómetro de descanso** con el tiempo prescrito
     del plan (p. ej. 60 s; 30 s en abdomen) mostrado en un círculo de cuenta atrás
     fijado en la parte inferior de la pantalla.
   - Cuando el cronómetro llega a **0**: **aviso sonoro + vibración** y el círculo
     cambia de color (verde → pulsando). El usuario puede **saltar el descanso**
     (botón "Saltar") o **ajustar el tiempo** (+/−15 s) para personalizarlo.
6. El usuario puede **editar o eliminar** cualquier serie registrada (tocar el chip):
   - Editar cambia kg/reps de esa serie (se persiste de nuevo).
   - Eliminar quita la serie (el contador vuelve a N−1/4).
7. Cuando todas las series de un ejercicio están completas, la tarjeta se marca como
   **completada** (check verde) y se puede marcar manualmente si se quiere.
8. Al completar el último ejercicio, o al pulsar **"Finalizar sesión"**:
   - Se persiste la `SesionEntrenamiento` con: nombreRutina, fecha, duración real
     (minutos transcurridos del temporizador), ejercicios completados, serieRealizadas
     totales y completo = true.
   - Se muestra la **Pantalla de Resumen de Sesión**: duración, volumen total
     (Σ kg×reps), ejercicios completados x/y, y el **1RM estimado por ejercicio**
     (fórmula de Epley/Brzycki) con su proyección.

## 2. Modelo de dominio nuevo: SerieRealizada

```kotlin
data class SerieRealizada(
    val id: String,                 // UUID
    val sesionId: String,           // sesión activa a la que pertenece
    val ejercicioId: String,        // ejercicio de la rutina
    val numeroSerie: Int,           // 1..N (orden en la sesión)
    val pesoKg: Double,             // carga real levantada
    val repeticiones: Int,          // reps reales ejecutadas
    val fecha: Long                 // epoch millis
)
```

- Se persiste en Room (tabla `serie_realizada`), offline-first como el resto.
- Un ejercicio puede tener varias `SerieRealizada` en la misma sesión.
- El conjunto de series de una sesión permite: volumen total, progresión por
  ejercicio (evolución de cargas, gráficas futuras) y proyección 1RM.

## 3. Sesión activa en el ViewModel (SesionActivaViewModel)

Estado inmutable (StateFlow) con:

```kotlin
data class EstadoSesionActiva(
    val rutina: Rutina?,                        // rutina que se está ejecutando
    val ejercicios: List<EjercicioConMaquina>,  // ejercicios + máquina real (marca/modelo)
    val series: List<SerieRealizada>,           // series registradas en esta sesión
    val sesionId: String?,                      // id de la sesión (si está en curso)
    val segundosTranscurridos: Long,            // cronómetro total de la sesión
    val descansoRestante: Int,                  // segundos restantes del descanso (0 = inactivo)
    val descansoTotal: Int,                     // duración configurada del descanso actual
    val descansoActivo: Boolean,                // true mientras el cronómetro de descanso corre
    val ejercicioActualId: String?,             // ejercicio sobre el que se registró la última serie
    val finalizada: Boolean,
    val cargando: Boolean,
    val error: String?
)
```

Comportamiento del ViewModel:
- `iniciarSesion(rutinaId)`: carga la rutina con sus bloques y resuelve cada
  ejercicio a su máquina real (repositorio de gimnasio) → construye la lista de
  ejercicios; crea el id de sesión; arranca el ticker del cronómetro total.
- `registrarSerie(ejercicioId, kg, reps)`: persiste la serie (nº = series actuales + 1),
  arranca el descanso con `descansoSegundos` prescrito del bloque.
- `editarSerie(serieId, kg, reps)`: actualiza kg/reps de una serie guardada.
- `eliminarSerie(serieId)`: elimina la serie y renumera las restantes (1..N).
- `saltarDescanso()` / `ajustarDescanso(deltaSegundos)` / `reanudarDescanso()`.
- `finalizarSesion()`: persiste `SesionEntrenamiento` resumida + marca finalizada;
  calcula el resumen (volumen total y 1RM por ejercicio) usando `CalcularUnRMCasoUso`.
- Ticker interno (coroutine cada 1 s) para el cronómetro total y la cuenta atrás del
  descanso; cuando `descansoRestante` llega a 0 → `descansoActivo = false` + flag
  `descansoTerminado = true` para que la UI reproduzca el aviso (sonido + vibración).

## 4. Pantalla de Entrenamiento en Vivo (Jetpack Compose)

`PantallaEntrenamientoEnVivoGYM(rutinaId, contenedor, alVolver)`:
- **Scaffold** con fondo del tema; **botón volver** (si la sesión está en curso, al
  volver se mantiene activa en segundo plano: el ViewModel conserva el estado).
- **Cabecera**: nombre de rutina, cronómetro total `mm:ss`, barra de progreso
  (LinearProgressIndicator) con "Series 8/20" o similar.
- **Lista** (LazyColumn) de `TarjetaEjercicioEnVivo`:
  - Nombre del ejercicio + máquina (marca · modelo, bodySmall AzulSecundario).
  - Chips de series (FlowRow o Row): cada chip muestra "kg×reps" (p. ej. "80×12");
    al pulsar un chip se abre el diálogo de edición.
  - Contador "x/N series" + botón **"Registrar serie"** (deshabilitado si ya está
    completa la serie N del plan... no: siempre permite añadir series extra).
  - Check verde si el ejercicio está completo.
- **Barra inferior de descanso** (fija, solo cuando `descansoActivo`):
  - Círculo de cuenta atrás con `descansoRestante` en segundos (CircularProgressIndicator
    con progress animado) + texto "Descanso 0:47".
  - Botones: **Saltar** (skip), **+15s** / **−15s**, **Pausar/Reanudar**.
  - Cuando termina (descansoRestante = 0): vibración (`LocalHapticFeedback` o
    `Vibrator`), sonido breve (MediaPlayer con un tono generado o ToneGenerator) y
    el círculo parpadea en verde → la UI muestra "¡Siguiente serie!" hasta que el
    usuario la registra o salta.
- **Diálogo de serie** (`DialogoRegistrarSerie`): OutlinedTextField numérico para kg
  y reps, carga sugerida mostrada como texto auxiliar ("Sugerido: 80 kg"), botones
  Guardar / Cancelar. Reutilizable para registrar y para editar (si viene serieId).
- **Diálogo de confirmación** al finalizar la sesión si quedan ejercicios sin
  completar ("Te quedan 2 ejercicios sin completar. ¿Finalizar de todos modos?").

## 5. Pantalla de Resumen de Sesión

`PantallaResumenSesionGYM(resumen, alVolver)` tras finalizar:
- Icono de éxito, "¡Sesión completada!" y duración (mm:ss).
- **Volumen total**: Σ (kg × reps) de todas las series.
- Ejercicios completados x/y con check.
- Lista por ejercicio del **1RM estimado** (Epley: `kg * (1 + reps/30)`, redondeado)
  y la carga usada en la última serie ("Última carga: 80 kg × 12 → 1RM ≈ 112 kg").
- Botones: "Volver a rutinas".

## 6. Casos de uso nuevos (capa de dominio)

En `domain/usecase/entrenamiento/`:
- `IniciarSesionActivaCasoUso`: prepara la sesión (id, ejercicios resueltos a máquina,
  carga sugerida inicial por ejercicio = último kg o 1RM−10%).
- `RegistrarSerieCasoUso`: valida kg>0, reps>0 y persiste la serie.
- `EditarSerieCasoUso` / `EliminarSerieCasoUso`: actualización/borrado + renumeración.
- `FinalizarSesionActivaCasoUso`: persiste `SesionEntrenamiento` resumida.
- `ObservarSeriesSesionCasoUso`: flujo reactivo de las series de la sesión activa.
- `CalcularCargaSugeridaCasoUso`: consulta el último kg de ese ejercicio en sesiones
  anteriores (o usa 1RM proyectado con `CalcularUnRMCasoUso` si no hay historial).
- `CalcularResumenSesionCasoUso`: volumen total + 1RM por ejercicio (Epley/Brzycki).

## 7. Persistencia

- **Nueva tabla** `serie_realizada` (EntidadSerieRealizada): id PK, sesionId, ejercicioId,
  numeroSerie, pesoKg (Double/REAL), repeticiones (Int), fecha (Long), sincronizado.
  Índice por `(sesionId)`.
- `DaoSerieRealizada`: insertar (REPLACE), observarPorSesion(sesionId): Flow,
  eliminar(id), actualizar (kg/reps), ultimoPesoPorEjercicio(ejercicioId): último kg.
- `RepositorioSerieRealizada` (interface) + `RepositorioSerieRealizadaRoom` (offline-first
  con sync a Supabase tabla `series_realizadas`) + `Fake` para tests.
- `SesionEntrenamiento` se amplía **sin romper** la API actual: se añade `serieIds` opcional
  o se mantiene el resumen actual (ejerciciosCompletados, serieRealizadas, duracionMinutos)
  que se rellena al finalizar; NO se cambia la firma de los constructores existentes
  (añadir solo campos con valor por defecto si hace falta).
- `BaseDeDatosGYM` → versión 5, registrar entidad + DAO (fallbackToDestructiveMigration).

## 8. Criterios de aceptación (lo que el usuario debe ver al probar)

1. Al entrar en una rutina y pulsar "Entrenar" se abre la sesión en vivo sin errores.
2. Cada ejercicio muestra su máquina real (Technogym · Artis…, Hammer Strength…).
3. Registrar una serie guarda kg/reps, muestra el chip y arranca el cronómetro de
   descanso con el tiempo del plan (60 s / 30 s abdomen).
4. El cronómetro avisa (sonido + vibración) al llegar a 0 y se puede saltar/ajustar.
5. Editar y eliminar series funciona y renumeran correctamente.
6. El cronómetro total de la sesión corre y el resumen final muestra duración real,
   volumen total y 1RM estimado por ejercicio.
7. La sesión queda en el historial de Sesiones de la semana.
8. No hay carencias visuales: diseño consistente con el tema (AzulPrimario,
   AzulSecundario, CianAcento, SuperficieOscura/Elevada), tipografías del tema.