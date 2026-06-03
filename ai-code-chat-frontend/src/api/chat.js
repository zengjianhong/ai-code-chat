const BASE_URL = 'http://localhost:8080'

export function sendMessage(question, conversationId, callbacks) {
  const controller = new AbortController()
  const { onToken, onThinking, onSources, onDone, onError } = callbacks

  fetch(`${BASE_URL}/api/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, conversationId }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let currentEvent = 'message'

      while (true) {
        const { done, value } = await reader.read()
        if (done) { onDone(); break }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim()
            continue
          }
          const trimmed = line.trim()
          if (!trimmed) continue

          const content = trimmed.startsWith('data:') ? trimmed.slice(5) : trimmed

          if (currentEvent === 'sources') {
            try { onSources(JSON.parse(content)) } catch { onSources(content) }
            currentEvent = 'message'
          } else if (currentEvent === 'thinking') {
            try { onThinking(JSON.parse(content)) } catch { onThinking(content) }
            currentEvent = 'message'
          } else {
            onToken(content)
            currentEvent = 'message'
            await new Promise(r => requestAnimationFrame(r))
          }
        }
      }
    })
    .catch((err) => {
      if (err.name !== 'AbortError') onError(err)
    })

  return controller
}
