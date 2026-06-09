import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'

function genId() {
  return crypto.randomUUID?.() || Date.now().toString(36) + Math.random().toString(36).slice(2)
}

export const useAiStore = defineStore('ai', () => {
  const sessionId = ref(localStorage.getItem('aiSessionId') || '')
  const messages = ref([])
  const isOpen = ref(false)
  const isLoading = ref(false)
  const toolStatus = ref('')
  const context = ref(null)

  let abortController = null

  function init() {
    if (!sessionId.value) {
      sessionId.value = genId()
      localStorage.setItem('aiSessionId', sessionId.value)
    }
  }

  async function loadHistory() {
    if (!sessionId.value) return
    try {
      const res = await fetch(`/api/ai/chat/session/${sessionId.value}`)
      const data = await res.json()
      messages.value = data.messages || []
    } catch {
      // ignore
    }
  }

  function toggle() {
    isOpen.value = !isOpen.value
    if (isOpen.value && messages.value.length === 0) {
      loadHistory()
    }
  }

  function setContext(ctx) {
    context.value = ctx || null
  }

  async function send(text) {
    if (!text.trim() || isLoading.value) return
    if (!sessionId.value) init()

    messages.value.push({ role: 'user', content: text })
    messages.value.push({ role: 'assistant', content: '' })
    isLoading.value = true

    await nextTick()

    abortController = new AbortController()

    const body = { session_id: sessionId.value, message: text }
    if (context.value) {
      body.context = context.value
    }

    try {
      const res = await fetch('/api/ai/chat/stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
        signal: abortController.signal,
      })

      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (!line.startsWith('data: ')) continue
          try {
            const data = JSON.parse(line.slice(6))
            if (data.type === 'token' && data.content) {
              const last = messages.value[messages.value.length - 1]
              if (last) last.content += data.content
            } else if (data.type === 'tool_start') {
              const names = { search_posts: '搜索帖子', get_post: '读取帖子', list_posts_by_category: '浏览分类' }
              toolStatus.value = names[data.content] || data.content
            } else if (data.type === 'tool_end') {
              toolStatus.value = ''
            } else if (data.type === 'error') {
              const last = messages.value[messages.value.length - 1]
              if (last && !last.content) last.content = '[错误] ' + data.content
            }
          } catch {
            // ignore malformed JSON
          }
        }
      }
    } catch (err) {
      if (err.name !== 'AbortError') {
        const last = messages.value[messages.value.length - 1]
        if (last && !last.content) last.content = '[网络错误，请重试]'
      }
    } finally {
      isLoading.value = false
      toolStatus.value = ''
      abortController = null
    }
  }

  function stop() {
    if (abortController) {
      abortController.abort()
      abortController = null
      isLoading.value = false
      toolStatus.value = ''
    }
  }

  async function clear() {
    stop()
    messages.value = []
    if (sessionId.value) {
      try {
        await fetch(`/api/ai/chat/session/${sessionId.value}`, { method: 'DELETE' })
      } catch {
        // ignore
      }
    }
  }

  init()

  return { sessionId, messages, isOpen, isLoading, toolStatus, context, toggle, setContext, send, stop, clear }
})
