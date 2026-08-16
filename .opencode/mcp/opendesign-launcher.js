/**
 * @file opendesign-launcher.js
 * @brief Lanzador del MCP de Open Design con descubrimiento dinámico del puerto del daemon.
 *
 * El MCP oficial `open-design-mcp` (npm) requiere la variable de entorno
 * `OD_DAEMON_URL` apuntando al daemon de Open Design en ejecución. El problema es
 * que la aplicación de escritorio de Open Design asigna un puerto efímero en cada
 * arranque (no fijo), por lo que una URL "quemada" en opencode.json se rompe en
 * cuanto la app se reinicia.
 *
 * Este lanzador resuelve ese problema:
 *   1. Localiza los procesos de la aplicación "Open Design" en ejecución.
 *   2. Obtiene los puertos TCP en los que escuchan.
 *   3. Prueba el endpoint `/api/health` en cada puerto hasta encontrar el daemon real.
 *   4. Lanza `open-design-mcp` con `OD_DAEMON_URL` apuntando al puerto correcto,
 *      heredando stdin/stdout para el protocolo MCP.
 *
 * De este modo, el MCP de Open Design funciona de forma robusta sin necesidad de
 * editar la configuración cuando el daemon cambia de puerto.
 */
"use strict";

const { spawn } = require("node:child_process");
const { once } = require("node:events");
const http = require("node:http");
const path = require("node:path");
const os = require("node:os");
const fs = require("node:fs");

const DEFAULT_URL = "http://127.0.0.1:7456";
const NOMBRE_PROCESO_APP = "Open Design";

const PROVIDER_BYOK = {
  baseUrl: "https://openrouter.ai/api/v1",
  modelo: "google/gemini-3.5-flash-lite",
};

/**
 * @brief Obtiene la clave API de OpenRouter desde el fichero de autenticación de opencode.
 * @returns {string|undefined} Clave API de OpenRouter, o undefined si no existe.
 */
function obtenerClaveOpenRouter() {
  const candidatos = [
    path.join(os.homedir(), ".local", "share", "opencode", "auth.json"),
    path.join(os.homedir(), ".config", "opencode", "auth.json"),
  ];
  for (const ruta of candidatos) {
    try {
      const datos = JSON.parse(fs.readFileSync(ruta, "utf8"));
      const openrouter = datos && datos.openrouter;
      if (openrouter && openrouter.key) {
        return openrouter.key;
      }
    } catch {
      // Se ignora y se prueba la siguiente ruta.
    }
  }
  return undefined;
}

/**
 * @brief Ejecuta un comando del sistema y devuelve su salida estándar.
 * @param {string} comando - Comando a ejecutar.
 * @returns {Promise<string>} Salida estándar del comando.
 */
async function ejecutarComando(comando) {
  const { execFile } = require("node:child_process");
  return new Promise((resolver, rechazar) => {
    execFile("powershell.exe", ["-NoProfile", "-Command", comando], { encoding: "utf8", timeout: 10000 }, (error, stdout) => {
      if (error) {
        rechazar(error);
      } else {
        resolver(stdout);
      }
    });
  });
}

/**
 * @brief Descubre los puertos TCP en los que escuchan los procesos "Open Design".
 * @returns {Promise<number[]>} Lista de puertos candidatos.
 */
async function descubrirPuertosCandidatos() {
  try {
    const salida = await ejecutarComando(
      `Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | ` +
      `Where-Object { $_.OwningProcess -in (Get-Process -Name '${NOMBRE_PROCESO_APP}' -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Id) } | ` +
      `Select-Object -ExpandProperty LocalPort`
    );
    const puertos = salida
      .split(/\r?\n/)
      .map((linea) => linea.trim())
      .filter((linea) => /^\d+$/.test(linea))
      .map(Number);
    return [...new Set(puertos)];
  } catch {
    return [];
  }
}

/**
 * @brief Comprueba si un puerto aloja el daemon de Open Design.
 * @param {number} puerto - Puerto a comprobar.
 * @returns {Promise<boolean>} True si responde `/api/health` correctamente.
 */
function esDaemonOpenDesign(puerto) {
  return new Promise((resolver) => {
    const opciones = {
      hostname: "127.0.0.1",
      port: puerto,
      path: "/api/health",
      method: "GET",
      timeout: 3000,
    };
    const peticion = http.get(opciones, (respuesta) => {
      let cuerpo = "";
      respuesta.on("data", (trozo) => (cuerpo += trozo));
      respuesta.on("end", () => {
        try {
          const datos = JSON.parse(cuerpo);
          resolver(Boolean(datos && datos.ok));
        } catch {
          resolver(false);
        }
      });
    });
    peticion.on("timeout", () => {
      peticion.destroy();
      resolver(false);
    });
    peticion.on("error", () => resolver(false));
  });
}

/**
 * @brief Descubre la URL del daemon de Open Design en ejecución.
 * @returns {Promise<string>} URL del daemon (o la predeterminada si no se localiza).
 */
async function descubrirUrlDaemon() {
  const urlPrevia = process.env.OD_DAEMON_URL;
  if (urlPrevia) {
    return urlPrevia;
  }

  const puertos = await descubrirPuertosCandidatos();
  for (const puerto of puertos) {
    if (await esDaemonOpenDesign(puerto)) {
      return `http://127.0.0.1:${puerto}`;
    }
  }

  return DEFAULT_URL;
}

/**
 * @brief Función principal: descubre la URL y lanza el MCP de Open Design.
 */
async function principal() {
  const urlDaemon = await descubrirUrlDaemon();
  const claveOpenRouter = obtenerClaveOpenRouter();
  const args = process.argv.slice(2);

  const hijo = spawn(
    process.platform === "win32" ? "npx.cmd" : "npx",
    ["-y", "open-design-mcp", ...args],
    {
      stdio: "inherit",
      shell: process.platform === "win32",
      env: {
        ...process.env,
        OD_DAEMON_URL: urlDaemon,
        BYOK_BASE_URL: PROVIDER_BYOK.baseUrl,
        BYOK_MODEL: PROVIDER_BYOK.modelo,
        ...(claveOpenRouter ? { BYOK_API_KEY: claveOpenRouter } : {}),
      },
    }
  );

  hijo.on("error", (error) => {
    process.stderr.write(`[opendesign-launcher] Error al lanzar el MCP: ${error.message}\n`);
    process.exit(1);
  });

  const [codigo] = await once(hijo, "exit");
  process.exit(codigo);
}

principal().catch((error) => {
  process.stderr.write(`[opendesign-launcher] Error fatal: ${error.message}\n`);
  process.exit(1);
});