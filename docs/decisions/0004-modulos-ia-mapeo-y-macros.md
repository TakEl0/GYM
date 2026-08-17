# ADR 0004: Módulos de IA — Mapeo de Ejercicios y Macros por Imagen

## Estado
Aprobado (pendiente de implementación de las Edge Functions y del cliente HTTP).

## Contexto
La aplicación GYM procesa datos personales de salud (PDFs del nutricionista Naturvitia y
fotos de comida) y requiere dos capacidades de inteligencia artificial para ofrecer una
experiencia profesional de extremo a extremo:

1. **Mapeo de ejercicios del plan PDF → máquina real del gimnasio**: el plan de
   entrenamiento del nutricionista llega en PDF con nombres propios del método
   ("Femoral tumbado", "Prensa a 45º", "Press militar en multipower", "Curl con
   mancuernas en banco 45º"...). El usuario entrena en **Fitness Park**, cuyo parque de
   maquinaria real (Technogym Artis/Selection, Hammer Strength, gym80, Eleiko) está
   catalogado en `CatalogoMaquinaria` con marca + modelo + ejercicios posibles. La IA
   resuelve ambigüedades que las reglas locales no pueden cerrar con confianza.
2. **Cálculo de macros desde una foto de comida**: el usuario fotografía un plato y la
   app estima kcal y macronutrientes (P/C/G) siguiendo las reglas del método Naturvitia
   (pesaje en cocinado salvo patata/boniato en crudo, 2 L de agua, tomas y alternativas).

### Restricciones no negociables
- **Ninguna clave de IA en el cliente Android** (cumple AGENTS.md §4 y la práctica de
  la app: secretos solo en `local.properties`/`.env` excluidos de Git).
- **Privacidad de los datos de salud**: los datos solo salen del dispositivo hacia la
  Edge Function propia, cifrados en tránsito (HTTPS), sin persistencia en el servidor y
  sin metadatos EXIF ni identidad del usuario.
- **Degradación elegante**: si no hay red, o hay error 429/5xx, la app funciona con
  reglas locales y confirmación manual (sin bloquear el flujo del usuario).

## Decisión

### Arquitectura general
- **Backend**: Edge Functions de Supabase (ya disponible en el proyecto vía
  `supabase`/`supabase-auth` skills y token de gestión). Son el ÚNICO punto que posee la
  clave de Gemini (`GEMINI_API_KEY` como secreto de la función, nunca en el APK).
- **Cliente**: capa `data/remote/` con Ktor/OkHttp (ya presentes en `build.gradle`).
  Interfaz de dominio `ServicioIA` + implementación `ServicioIASupabase` que reenvía el
  JWT de la sesión (Authorization Bearer) como método de autenticación.
- **Dominio**: modelos `ResolucionMapeo`, `MapeoAprendido`, `EstimacionComida` y casos
  de uso `ResolverEjercicioAMaquinaCasoUso`, `EstimarMacrosFotoCasoUso`.
- **Presentación**: pantalla de revisión de coincidencias tras importar la rutina
  (candidatos con confianza, confirmar/editar) y pantalla de foto de comida con
  previsualización y edición de la estimación antes de guardar.

### Módulo A — Mapeo de ejercicios (reglas primero, LLM como respaldo)
Estrategia en cascada, de menor a mayor coste:
1. **Normalización local**: minúsculas, sin acentos, expansión de "45º"→"45",
   "multipower"→"smith", singular/plural.
2. **Reglas y sinónimos** (`CatalogoMaquinaria.ejerciciosPosibles` + `sinonimos`) y
   restricción por familia muscular del ejercicio.
3. **Aprendizaje del usuario** (`MapeoAprendido` persistido): toda corrección manual
   queda guardada y resuelve offline la siguiente vez.
4. **LLM (Gemini)** solo si la confianza < 0,6: devuelve candidatos rankeados para que
   el usuario confirme (nunca se auto-aplica sin confirmación).
- Coste: una petición por importación (~30 ejercicios), dentro de los límites del plan
  gratuito; se cachea por (nombrePdf + ids de catálogo).

### Módulo B — Macros por imagen (Gemini Vision)
- La Edge Function recibe: imagen + tipo de ingesta + objetivo diario. Devuelve la lista
  de alimentos reconocidos con gramos estimados, regla de pesaje Naturvitia
  (COCINADO/CRUDO), y totales de kcal, proteínas, carbohidratos y grasas con confianza.
- Post-proceso en el cliente: redondeo contra el catálogo local de `Alimento` para
  trazabilidad y registro en `IngestaRegistrada` con `origen = FOTO`.

### Contratos de API (JSON)

```
POST /functions/v1/mapear-ejercicio
Authorization: Bearer <jwt>
{
  "ejercicio": "Femoral tumbado",
  "catalogo": [
    {"id":"curl-femoral-tumbado","nombre":"Curl femoral tumbado","marca":"Technogym",
     "modelo":"Artis Lying Leg Curl","grupoMuscular":["FEMORAL"],
     "tipoEquipamiento":"MAQUINA_GUIADA","ejerciciosPosibles":["femoral-tumbado","curl-femoral"]}
  ]
}
→ 200
{
  "candidatos": [
    {"maquinaId":"curl-femoral-tumbado","confianza":0.95,"motivo":"SINONIMO"},
    {"maquinaId":"lying-leg-curl-gym80","confianza":0.7,"motivo":"FAMILIA"}
  ],
  "elegido": "curl-femoral-tumbado"
}
→ 204 SIN_CONTENIDO si no hay coincidencia posible
```

```
POST /functions/v1/estimar-comida
Authorization: Bearer <jwt>
{
  "imagenBase64": "...",
  "tipoIngesta": "COMIDA",
  "objetivoDiarioKcal": 2100
}
→ 200
{
  "alimentos": [
    {"nombre":"arroz integral","gramosEstimados":120,"pesaje":"CRUDO"},
    {"nombre":"pollo a la plancha","gramosEstimados":180,"pesaje":"COCINADO"}
  ],
  "total": {"kcal": 620, "proteinasG": 48, "carbohidratosG": 88, "grasasG": 8},
  "confianza": 0.74
}
```

### Privacidad y seguridad
- La imagen y el nombre del ejercicio viajan SOLO a la Edge Function vía HTTPS; la
  función no persiste nada (stateless) y no registra logs de contenido.
- El cliente no posee `GEMINI_API_KEY` ni `service_role`: solo el JWT del usuario.
- Anonimización: en el mapeo se envían solo los nombres de ejercicio y el catálogo
  (sin datos personales); en la imagen no se adjuntan metadatos EXIF.
- Manejo de errores: 401 → relogin, 429 → espera y reintento con backoff, 5xx/red →
  fallback a reglas locales o registro manual.

## Consecuencias
- Sin secretos de IA en el cliente (cumple FR-19 y AGENTS.md §4).
- Coste mínimo (reglas locales primero + caché + LLM solo ante incertidumbre).
- Privacidad de datos de salud preservada.
- La app sigue siendo funcional sin conexión ni IA (modo degradado).
