const BASE_URL = 'http://localhost:8080'

export function sendMessage(question, conversationId, { onToken, onDone, onError }) {
  const controller = new AbortController()

  fetch(`${BASE_URL}/api/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, conversationId }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          onDone()
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          const trimmed = line.trim()
          if (trimmed) {
            const content = trimmed.startsWith('data:') ? trimmed.slice(5) : trimmed
            onToken(content)
            await new Promise(r => requestAnimationFrame(r))
          }
        }
      }
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err)
      }
    })

  return controller
}
