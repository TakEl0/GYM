---
name: inbody-tracker
description: Skill de extracción, tabulación y análisis de informes InBody para monitorizar la recomposición corporal y calcular deltas antropométricos.
---

# Habilidad: Seguimiento de Evolución InBody (Naturvitia)

Asignada a `subagente-datos` y `subagente-dominio`:

1. **Extracción Antropométrica de Informes InBody**:
   - **Peso Total**: Control de balance energético general.
   - **Masa Muscular Esquelética (MME)**: Medir ganancia y mantenimiento de masa muscular.
   - **Masa Grasa Corporal**: Evaluar pérdida de grasa neta.
   - **Porcentaje Grasa Corporal (% Grasa)**: Indicador clave de recomposición.
   - **Grasa Visceral**: Nivel de salud metabólica.
   - **Tasa Metabólica Basal (TMB)**: Ajuste de calorías teóricas en la dieta.
   - **Puntuación InBody**: Score global de composición corporal.

2. **Cálculo de Deltas ($\Delta$) y Diagnóstico**:
   - Calcula la variación exacta entre la revisión actual y la anterior ($\Delta$Peso, $\Delta$MME, $\Delta$Grasa).
   - Emite un diagnóstico clínico/deportivo de recomposición corporal (ej. "Pérdida de grasa efectiva manteniendo masa muscular").
