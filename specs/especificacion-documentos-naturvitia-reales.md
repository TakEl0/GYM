# Especificación de Documentos Naturvitia Reales

> **Fechas de referencia**: Dieta y báscula 13/01/2026 · Entrenamiento 14/01/2026
> **Paciente**: Manu Miralles Granados
> **Estado**: Extraído y verificado desde los PDFs de `Documentacion/`

---

## 1. Informe InBody / Báscula (`naturvitia-bascula-13_01_2026 18_46.pdf`)

| Medición | Valor | Referencia / Nota |
|---|---|---|
| Fecha/Hora | 13/01/2026 18:46 | ID 13706 |
| Puntuación InBody | **91/100** | Muy buena |
| Tasa Metabólica Basal (TMB) | **2037 kcal** | Mifflin-St Jeor |
| Peso | **89,6 kg** | |
| Masa Muscular Esquelética (MME) | **44,1 kg** | |
| Masa Grasa | **12,4 kg** | |
| % Grasa Corporal | **13,8%** | Referencia 10–20 |
| IMC | **25,4 kg/m²** | Referencia 18,5–25,0 |
| Grasa Visceral | **4,6 kg (119,3%)** | **ALTO** |
| Agua Corporal Total | **56,5 L** | |
| MME por segmentos | Brazos izq/der 4,4 / 4,6 kg; Tronco 33,3 kg; Piernas 11,8 / 11,9 kg | |

**Implicación funcional**: la app debe permitir guardar este informe y **calcular los deltas** en el siguiente control (evolución de peso, % grasa, MME).

---

## 2. Dieta (`naturvitia-dieta-13_01_2026.pdf`)

Estructura de 5 tomas con **alternativas intercambiables** ("puedes cambiar por"). Reglas de pesaje: **cocinado** salvo patata/boniato/gnocchis en **crudo**.

| Toma | Comida base | Alternativa |
|---|---|---|
| **DESAYUNO** (6:15, antes de entrenar) | Crepe: 2 huevos + 150 ml claras + (25 g crema de arroz o 35 g harina avena) + 2 onzas chocolate negro 85% + 1 fruta | Batido: 40 g EvoWhey + (4 tortitas arroz/maíz o 25 g crema arroz o 35 g harina avena) + 15 g frutos secos + 1 fruta |
| **ALMUERZO** | 1 panecillo integral fino (Mercadona) + (100 g fiambre pollo/pavo o 70 g jamón serrano) + 40 g queso havarti light + vegetales | Panecillo + 2 huevos + (50 g fiambre o 35 g jamón) + 20 g havarti; o batido 40 g EvoWhey + (25 g crema arroz / 35 g harina avena / crunchy / 30 g corn flakes) |
| **COMIDA** | 1er plato opcional: vegetales. 2º: (140 g legumbres / 2 tortillas maíz / 25 g arroz o pasta secos / 100 g patata) + 10 ml AOVE + **proteína (5 opciones)** | — |
| **MERIENDA** | Batido 40 g EvoWhey + 15 g frutos secos | 250 g queso fresco batido 0% + 15 g EvoWhey + 15 g frutos secos |
| **CENA** | 1er plato opcional: vegetales. 2º: **4 opciones** de proteína (pavo, tortilla+huevos, jamón serrano+huevos, pescado) | — |

**Proteínas de la COMIDA (opciones)**: 200 g muslo/contramuslo pollo · 2 latas atún/caballa + 1 huevo · 200 g carne picada pollo/pavo · 200 g pechuga pollo · 150 g kebab pollo + 1 huevo.

**Suplementos**: MultiMega 1 cápsula en desayuno · SuperOmega 3: 2 cápsulas en cena · EvoWhey Protein 40 g (2-3 veces/día).

**Reglas generales**: 2 L agua/día · bebidas carbonatadas solo ZERO (1-2 máx) · 1 comida libre semanal moderada (fin de semana) · salsas 0,0% y especias permitidas · sal rosa del Himalaya.

---

## 3. Plan de entrenamiento (`naturvitia-entrenamiento-14_01_2026.pdf`)

**Método**: 5 días de gym. **4 series por ejercicio**. Descanso 60 s (30 s en abdomen). **TUT 1-1** (subir 1 s, bajar 1 s). **Carga**: peso máximo que permita completar la última repetición con técnica correcta (fallo técnico). Calentamiento: 2-3 series de 15-20 reps con peso bajo. Al finalizar: 20-30 min cardio a 110-130 ppm.

| Día | Grupo | Ejercicios (S × R × descanso) |
|---|---|---|
| **Día 1** | Pierna | Femoral tumbado (4×12·60) · Prensa 45º (4×12·60) · Adductor (4×12·60) · Patada de glúteo en máquina (4×12·60) · Extensiones (4×12·60) · Hip thrust en banco (4×12·60) |
| **Día 2** | Pecho + Bíceps | Press horizontal en máquina (4×12·60) · Cruces en polea (4×12·60) · Press vertical en máquina peso libre (4×12·60) · Peck deck (4×12·60) · Curl mancuernas banco 45º (4×12·60) · Curl con barra (4×12·60) |
| **Día 3** | Espalda + Abdomen | Peso muerto espalda con barra (4×12·60) · Dominadas en máquina asistida (4×12·60) · Jalones al pecho agarre "V" (4×12·60) · Remo en polea baja (4×12·60) · Elevación piernas en paralelas (4×10·30) · Rueda abdominal (4×10·30) |
| **Día 4** | Hombro + Tríceps | Elevaciones posteriores mancuerna (4×12·60) · Elevaciones laterales mancuerna (4×12·60) · Press militar en multipower (4×12·60) · Deltoide posterior en máquina (4×12·60) · Extensiones en polea (4×12·60) · Press francés con barra (4×12·60) |
| **Día 5** | Pecho + Espalda + Lumbar | Press banca inclinado en multipower (4×12·60) · Aperturas en máquina (4×12·60) · Remo hammer (4×12·60) · Jalones en máquina (4×12·60) · Hiperextensiones (4×12·60) |

**Formato del PDF por ejercicio**: `S R V T` (Series, Repeticiones, Velocidad/TUT, Tiempo de descanso). Ej. `4 12 1 y 1 60` = 4 series, 12 reps, TUT 1-1, 60 s descanso.

**Equipamiento requerido por el plan** (para el catálogo de máquinas): máquina de femoral, prensa 45º, adductor, patada de glúteo, extensión de cuádriceps, hip thrust, press horizontal guiado, cruces en polea, press vertical/multipower, peck deck, banco 45º + mancuernas, barra, peso muerto, dominadas asistidas, jalón "V", remo en polea, paralelas, rueda abdominal, mancuernas, deltoide posterior en máquina, extensión en polea, press francés, aperturas en máquina, remo hammer, jalones en máquina, hiperextensiones.

---

## 4. Implicaciones para la app

1. **Parser de entrenamiento**: el actual NO extrae este formato real (S R V T). Debe detectar el día, cada ejercicio con sus series/reps/TUT/descanso y la técnica general.
2. **Parser de dieta**: debe extraer tomas con ingredientes + alternativas y las reglas de pesaje (cocinado/crudo).
3. **Parser InBody**: debe guardar los valores reales (peso, MME, masa grasa, % grasa, IMC, grasa visceral, TMB, puntuación).
4. **Mapeo ejercicios→máquinas**: cada ejercicio del plan debe resolverse contra el catálogo de maquinaria del gimnasio real (p. ej. "Femoral tumbado" → máquina de curl femoral tumbado del gimnasio).
5. **Seguimiento de evolución**: registrar series/reps/peso realizados por ejercicio para la progresión de carga (el plan dice "peso máximo... de forma que te sea imposible hacer una sola repetición más").
