---
name: nutrition-knowledge
description: Habilidad especializada en nutrición, cálculo de metabolismo (Mifflin-St Jeor / Katch-McArdle), macronutrientes, equivalencias de alimentos y parsing de PDF nutricionales.
---

# Habilidad: Dominio de Nutrición y Metabolismo (GYM)

Asignada a `subagente-datos` y `subagente-dominio`:

1. **Extractor y Parser de PDF Nutricional**:
   - Reglas de extracción para leer planes nutricionales en PDF de nutricionistas.
   - Identificación de estructura por días y tomas (Desayuno, Media Mañana, Comida, Merienda, Cena).
   - Mapeo de texto libre a objetos de datos estructurados (`Meal`, `Ingredient`, `Grammage`, `Macros`).

2. **Calculadora de Metabolismo y Macronutrientes**:
   - **Fórmula Mifflin-St Jeor**:
     - Hombres: BMR = (10 × peso en kg) + (6.25 × altura en cm) - (5 × edad en años) + 5
     - Mujeres: BMR = (10 × peso en kg) + (6.25 × altura en cm) - (5 × edad en años) - 161
   - **Cálculo de TDEE**: BMR × Factor de Actividad (Sedentario 1.2, Ligero 1.375, Moderado 1.55, Fuerte 1.725).
   - **Reparto de Macronutrientes**: Ajuste automático según objetivo (Volumen +15% kcal, Definición -20% kcal, Mantenimiento = TDEE). Proteína (1.6 - 2.2g/kg), Grasas (0.8 - 1.2g/kg), el resto en carbohidratos.

3. **Motor de Equivalencias de Alimentos**:
   - Lógica de sustitución de alimentos manteniendo la isocaloricidad e isomacronutrición (ej. equivalencias de carbohidratos entre arroz cocido, avena y patata cocida).
