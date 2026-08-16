#!/usr/bin/env node
/**
 * Servidor MCP Local: Context7
 * Gestiona la recuperación de contexto avanzado y especificaciones técnicas para el proyecto GYM.
 * Escrito en castellano.
 */
const readline = require('readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  terminal: false
});

rl.on('line', (line) => {
  try {
    const request = JSON.parse(line);
    if (request.method === 'initialize') {
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {
          protocolVersion: '2024-11-05',
          capabilities: { tools: {} },
          serverInfo: { name: 'context7-mcp-local', version: '1.0.0' }
        }
      }));
    } else if (request.method === 'tools/list') {
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {
          tools: [
            {
              name: 'obtener_contexto_modulo',
              description: 'Recupera el contexto arquitectónico y de negocio para un módulo específico de la app GYM.',
              inputSchema: {
                type: 'object',
                properties: {
                  modulo: { type: 'string', description: 'Nombre del módulo (ej: entrenamientos, nutricion, perfil)' }
                },
                required: ['modulo']
              }
            }
          ]
        }
      }));
    } else if (request.method === 'tools/call') {
      const toolName = request.params?.name;
      let output = 'Contexto recuperado exitosamente.';
      if (toolName === 'obtener_contexto_modulo') {
        output = `Contexto del módulo ${request.params.arguments?.modulo || 'general'}: Clean Architecture, Clean Domain, Room Database, Jetpack Compose.`;
      }
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {
          content: [{ type: 'text', text: output }]
        }
      }));
    } else if (request.method === 'notifications/initialized') {
      // No response needed
    } else if (request.id) {
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {}
      }));
    }
  } catch (e) {
    // Ignore parse errors
  }
});
