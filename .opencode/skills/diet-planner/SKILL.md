---
name: diet-planner
description: Skill de planificación de menús diarios y semanales basada en las reglas del método Naturvitia, incluyendo reglas de pesaje (cocinado vs crudo) y consolidación de lista de la compra en paquetes de supermercado.
---

# Habilidad: Planificador de Menús y Lista de la Compra (Naturvitia)

Asignada a `subagente-datos` y `subagente-dominio`:

1. **Reglas de Negocio de Pesaje**:
   - **Alimentos pesados cocinados**: Arroz, pasta, carnes, pescados, legumbres (preparados/cocinados).
   - **Alimentos pesados en crudo**: Patata, boniato y gnocchis (se pesan antes de cocinar).

2. **Ingredientes Específicos Naturvitia / Mercadona**:
   - Panecillos 100% integrales finos Mercadona.
   - Carne de kebab de pollo congelada Mercadona.
   - Tortillas de maíz / milho Mercadona.
   - Proteína EvoWhey Protein.
   - Queso Havarti light, AOVE (10ml por toma principal).

3. **Generación de Lista de la Compra**:
   - Agrupa los gramos semanales totales y los escala automáticamente a paquetes o unidades comerciales de supermercado (ej: "2 paquetes de panecillos integrales finos Mercadona", "1 bolsa de 1kg de arroz").
   - Prohibido inventar ingredientes fuera de la pauta del nutricionista.
