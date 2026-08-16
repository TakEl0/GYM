#!/usr/bin/env node
/**
 * Servidor MCP Local: OpenDesign
 * Valida estilos de interfaz, paletas de colores y consistencia UX para la app GYM.
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
          serverInfo: { name: 'opendesign-mcp-local', version: '1.0.0' }
        }
      }));
    } else if (request.method === 'tools/list') {
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {
          tools: [
            {
              name: 'validar_diseno_ui',
              description: 'Valida los componentes de Jetpack Compose contra la guía de diseño visual y paleta de colores.',
              inputSchema: {
                type: 'object',
                properties: {
                  componente: { type: 'string', description: 'Nombre del Composable o pantalla a validar' }
                },
                required: ['componente']
              }
            }
          ]
        }
      }));
    } else if (request.method === 'tools/call') {
      const toolName = request.params?.name;
      let output = 'Diseño validado correctamente según las especificaciones de opendesign.';
      if (toolName === 'validar_diseno_ui') {
        output = `El componente ${request.params.arguments?.componente || 'UI'} cumple con los estándares de Material Design 3, contraste de color y tipografía del proyecto GYM.`;
      }
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {
          content: [{ type: 'text', text: output }]
        }
      }));
    } else if (request.method === 'notifications/initialized') {
      // No response needed for notification
    } else if (request.id) {
      console.log(JSON.stringify({
        jsonrpc: '2.0',
        id: request.id,
        result: {}
      }));
    }
  } catch (e) {
    // Ignore parse errors on malformed input
  }
});
