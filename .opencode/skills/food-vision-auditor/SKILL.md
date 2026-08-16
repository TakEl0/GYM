---
name: food-vision-auditor
description: Skill de auditoría de platos por imagen y estimación de macronutrientes comparando fotos reales con las pautas del método Naturvitia.
---

# Habilidad: Auditoría de Platos por Imagen y Macros (Naturvitia)

Asignada a `subagente-ui` y `subagente-datos`:

1. **Identificación de Toma**:
   - Detecta automáticamente si la foto del plato corresponde a Desayuno, Almuerzo, Comida, Merienda o Cena.

2. **Chequeo de Plantilla Naturvitia**:
   - Contrasta los elementos visuales contra las 5 opciones válidas del plan (ej. Comida: 200g pechuga pollo + 100g patata en crudo + 10ml AOVE + ensalada/verdura).
   - Confirma la presencia obligatoria de la cucharada de aceite de oliva virgen extra (10ml AOVE) y verdura/fibra.

3. **Detección de Desviaciones**:
   - Alerta si falta el aceite, si hay exceso de carbohidratos simples, o si la fuente proteica no coincide con la pauta asignada.
   - Devuelve: 1) Cumplimiento (Sí/No), 2) Macros estimados (Proteínas / Carbohidratos / Grasas), 3) Ajustes correctivos necesarios.
