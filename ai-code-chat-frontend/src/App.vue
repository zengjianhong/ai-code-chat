<template>
  <div class="app-layout">
    <ChatSidebar
      :conversations="conversations"
      :active-id="currentConversationId"
      @select="switchConversation"
      @new="newConversation"
      @delete="deleteConversation"
    />
    <main class="chat-main">
      <header class="chat-header">
        <h2>AI 编程小助手</h2>
        <span class="chat-subtitle">解答编程学习与求职面试问题</span>
      </header>
      <div class="chat-body" ref="chatBody">
        <div v-if="currentMessages.length === 0" class="chat-empty">
          <div class="empty-icon">&#x1F4AC;</div>
          <p>开始提问，AI 助手将实时回复</p>
        </div>
        <ChatMessage
          v-for="msg in currentMessages"
          :key="msg.id"
          :message="msg"
        />
        <div v-if="streaming" class="message-row assistant">
          <div class="message-bubble assistant">
            <div class="markdown-body" v-html="renderMarkdown(streamingText || '...')"></div>
          </div>
        </div>
      </div>
      <ChatInput
        :disabled="streaming"
        @send="handleSend"
        @stop="handleStop"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { Marked } from 'marked'
import ChatSidebar from './components/ChatSidebar.vue'
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import { sendMessage } from './api/chat.js'

const marked = new Marked()

const conversations = ref([])
const currentConversationId = ref(null)
const streaming = ref(false)
const streamingText = ref('')
const chatBody = ref(null)
let abortController = null

const currentMessages = computed(() => {
  const conv = conversations.value.find(c => c.id === currentConversationId.value)
  return conv ? conv.messages : []
})

onMounted(() => {
  const saved = localStorage.getItem('ai-chat-conversations')
  if (saved) {
    try { conversations.value = JSON.parse(saved) } catch {}
  }
  if (conversations.value.length === 0) {
    newConversation()
  } else {
    currentConversationId.value = conversations.value[0].id
  }
})

watch(conversations, (val) => {
  localStorage.setItem('ai-chat-conversations', JSON.stringify(val))
}, { deep: true })

function newConversation() {
  const id = Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
  conversations.value.unshift({ id, title: '新对话', messages: [] })
  currentConversationId.value = id
}

function switchConversation(id) {
  currentConversationId.value = id
}

function deleteConversation(id) {
  const idx = conversations.value.findIndex(c => c.id === id)
  if (idx === -1) return
  conversations.value.splice(idx, 1)
  if (currentConversationId.value === id) {
    currentConversationId.value = conversations.value[0]?.id || null
  }
  if (conversations.value.length === 0) newConversation()
}

function handleSend(question) {
  const conv = conversations.value.find(c => c.id === currentConversationId.value)
  if (!conv) return

  const userMsg = { id: Date.now(), role: 'user', content: question, time: new Date().toLocaleTimeString() }
  conv.messages.push(userMsg)
  if (conv.title === '新对话' || conv.messages.length <= 2) {
    conv.title = question.slice(0, 30) + (question.length > 30 ? '...' : '')
  }

  const assistantId = Date.now() + 1
  let fullContent = ''
  streaming.value = true
  streamingText.value = ''

  abortController = sendMessage(question, currentConversationId.value, {
    onToken(token) {
      fullContent += token
      streamingText.value = fullContent
      scrollToBottom()
    },
    onDone() {
      conv.messages.push({ id: assistantId, role: 'assistant', content: fullContent, time: new Date().toLocaleTimeString() })
      streaming.value = false
      streamingText.value = ''
      abortController = null
      scrollToBottom()
    },
    onError(err) {
      conv.messages.push({ id: assistantId, role: 'assistant', content: `请求失败: ${err.message}`, time: new Date().toLocaleTimeString(), error: true })
      streaming.value = false
      streamingText.value = ''
      abortController = null
    },
  })
}

function handleStop() {
  if (abortController) {
    abortController.abort()
    const conv = conversations.value.find(c => c.id === currentConversationId.value)
    if (conv && streamingText.value) {
      conv.messages.push({ id: Date.now(), role: 'assistant', content: streamingText.value, time: new Date().toLocaleTimeString() })
    }
    streaming.value = false
    streamingText.value = ''
    abortController = null
  }
}

function renderMarkdown(text) {
  return marked.parse(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f0f2f5; color: #333; }

.app-layout { display: flex; height: 100vh; }

.chat-main {
  flex: 1; display: flex; flex-direction: column; min-width: 0;
}
.chat-header {
  padding: 16px 24px; background: #fff; border-bottom: 1px solid #e5e7eb;
  display: flex; align-items: baseline; gap: 12px; flex-shrink: 0;
}
.chat-header h2 { font-size: 18px; font-weight: 600; }
.chat-subtitle { font-size: 13px; color: #9ca3af; }

.chat-body {
  flex: 1; overflow-y: auto; padding: 24px;
  scroll-behavior: smooth;
}

.chat-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 100%; color: #9ca3af;
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.chat-empty p { font-size: 15px; }

.message-row { display: flex; margin-bottom: 20px; }
.message-row.user { justify-content: flex-end; }
.message-row.assistant { justify-content: flex-start; }

.message-bubble {
  max-width: 75%; padding: 12px 18px; border-radius: 16px; line-height: 1.6;
  font-size: 15px; word-break: break-word;
}
.message-bubble.user { background: #2563eb; color: #fff; border-bottom-right-radius: 4px; }
.message-bubble.assistant { background: #fff; border: 1px solid #e5e7eb; border-bottom-left-radius: 4px; }

.message-time { font-size: 11px; margin-top: 4px; opacity: 0.7; }
.message-row.user .message-time { text-align: right; }
.message-row.assistant .message-time { text-align: left; }

.markdown-body h1, .markdown-body h2, .markdown-body h3 { margin: 8px 0 4px; }
.markdown-body p { margin: 4px 0; }
.markdown-body ul, .markdown-body ol { padding-left: 20px; margin: 4px 0; }
.markdown-body code { background: #f3f4f6; padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
.markdown-body pre { background: #1e293b; color: #e2e8f0; padding: 14px; border-radius: 8px; overflow-x: auto; margin: 8px 0; }
.markdown-body pre code { background: none; padding: 0; color: inherit; }
.markdown-body table { border-collapse: collapse; width: 100%; margin: 8px 0; }
.markdown-body th, .markdown-body td { border: 1px solid #d1d5db; padding: 6px 12px; text-align: left; }
.markdown-body th { background: #f3f4f6; }
.markdown-body blockquote { border-left: 3px solid #2563eb; padding-left: 12px; color: #6b7280; margin: 8px 0; }
.markdown-body a { color: #2563eb; }

.message-bubble.assistant.error { background: #fef2f2; border-color: #fecaca; color: #dc2626; }

@media (max-width: 768px) {
  .chat-body { padding: 12px; }
  .message-bubble { max-width: 90%; }
}
</style>
