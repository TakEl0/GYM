---
name: intra-day-rebalancer
description: Módulo de Rebalanceo Intra-día encargado de evaluar desvíos de macros en tiempo real y recalcular las tomas pendientes del día usando alimentos permitidos por el método Naturvitia.
---

# Habilidad: Módulo de Rebalanceo Intra-día (Naturvitia)

Asignada a `subagente-dominio` y `subagente-datos`:

1. **Lógica de Ejecución**:
   - **Registro**: Al recibir la foto o texto de la comida consumida, calcula los macronutrientes totales reales ($P_c, CH_c, G_c$).
   - **Cálculo de Desvío ($\Delta$)**: 
     $$\Delta = \text{Macronutrientes Planificados} - \text{Macronutrientes Consumidos}$$
   - **Filtro de Tolerancia**: Si la diferencia es menor al 5% o $\pm 10\text{g}$ de macronutriente, no se altera el menú para evitar rigidez innecesaria.
   - **Redistribución**: Si el desvío supera la tolerancia, ajusta la siguiente comida (o la reparte entre las restantes) utilizando la tabla de equivalencias de la dieta Naturvitia.

2. **Reglas de Compensación**:
   - **Exceso de Carbohidratos**: Reducción equivalente de carbohidratos en la Merienda o Cena (ej. eliminar el pan integral o reducir la patata de la cena manteniendo intacta la proteína).
   - **Déficit de Proteína**: Añadir una fuente limpia de rápida preparación en la Merienda/Cena (ej. +1 lata de atún al natural, +100g de queso fresco 0%, o un scoop extra de EvoWhey).
   - **Exceso de Grasa**: Eliminación de la cucharada de AOVE (10ml) o de la ración de frutos secos/aguacate programada para la cena.

3. **Ejemplo Práctico**:
   - *Registro (14:30h)*: "Me he comido 280g de patata en lugar de los 100g marcados en la Comida Opción 1."
   - *Desvío*: $+180\text{g}$ de patata en crudo ($\approx +30\text{g}$ Carbohidratos).
   - *Ajuste Automático para la Cena*: Se elimina el boniato de la cena programada, manteniendo la hamburguesa de pavo, verdura y AOVE, cerrando el día cumpliendo el objetivo calórico exacto.
