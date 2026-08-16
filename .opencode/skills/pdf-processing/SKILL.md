---
name: pdf-processing
description: Utilizar cuando sea necesario leer, extraer texto/tablas o generar archivos PDF (como planes nutricionales externos de nutricionistas, dietas, informes de progreso y rutinas de entrenamiento) en la app GYM.
---

# Habilidad: Procesamiento de Archivos PDF (Lectura y Escritura)

Esta habilidad proporciona directrices y herramientas para procesar documentos PDF en el flujo de trabajo del proyecto GYM:

1. **Lectura y Extracción de Dietas / Planes de Nutricionistas**:
   - Para extraer texto, tablas y datos nutricionales de archivos PDF provistos por nutricionistas externos, utilizar bibliotecas de análisis de PDF (como Python `pdfplumber`, `pypdf`, o librerías equivalentes en Kotlin/Java según el backend/herramienta de procesamiento).
   - Validar la estructura del plan nutricional extraído para asegurar su correcta conversión en rutinas y metas de control de comidas en la aplicación.

2. **Generación y Escritura de Informes PDF**:
   - Para generar informes de progreso, calendarios de entrenamiento o resúmenes de dieta en formato PDF para el usuario o su nutricionista, utilizar herramientas de generación de PDF (como ReportLab en Python o iText/PdfBox en Android).
   - Asegurar un diseño visual profesional, limpio y coherente con las especificaciones de `opendesign`.

3. **Idioma y Documentación**:
   - Todos los scripts de procesamiento, funciones auxiliares y comentarios de código deben estar redactados estrictamente en **castellano**.
   - Toda extracción de datos debe incluir manejo robusto de errores y registros (logs) claros.
