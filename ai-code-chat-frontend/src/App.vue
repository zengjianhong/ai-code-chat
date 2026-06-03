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
      <div v-if="currentMessages.length === 0 && !streaming" class="welcome-page">
        <div class="welcome-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="#4f8cff" stroke-width="1.5" width="48" height="48">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1 class="welcome-title">我是您的 AI 编程助手</h1>
        <p class="welcome-subtitle">解答编程学习与求职面试问题</p>
        <div class="quick-cards">
          <div class="quick-card" v-for="card in quickCards" :key="card.title" @click="handleQuickCard(card.prompt)">
            <div class="card-icon">{{ card.icon }}</div>
            <div class="card-title">{{ card.title }}</div>
            <div class="card-desc">{{ card.desc }}</div>
          </div>
        </div>
      </div>

      <div v-else class="chat-body" ref="chatBody">
        <ChatMessage
          v-for="msg in currentMessages"
          :key="msg.id"
          :message="msg"
        />
        <div v-if="streaming" class="streaming-preview">
          <ChatMessage
            :message="{ id: 'streaming', role: 'assistant', content: streamingText || '...', thinking: streamingThinking, thinkingLabel: '正在思考' }"
          />
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
import ChatSidebar from './components/ChatSidebar.vue'
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import { sendMessage } from './api/chat.js'

const quickCards = [
  { title: 'Java 学习路线', desc: '了解 Java 开发者完整学习路径', icon: '\u{1F4DA}', prompt: 'Java 的学习路线是什么？' },
  { title: '多线程编程', desc: '深入理解 Java 并发与多线程', icon: '\u{1F9E9}', prompt: 'Java 中如何实现多线程编程？' },
  { title: '算法面试题', desc: '常见算法与数据结构面试题', icon: '\u{1F4DD}', prompt: '程序员常见的算法面试题有哪些？' },
  { title: '简历优化建议', desc: '优化技术简历获得更多面试', icon: '\u{1F4CB}', prompt: '如何优化技术人员的简历？' },
]

const conversations = ref([])
const currentConversationId = ref(null)
const streaming = ref(false)
const streamingText = ref('')
const streamingThinking = ref('')
const streamingSources = ref([])
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
function switchConversation(id) { currentConversationId.value = id }
function deleteConversation(id) {
  const idx = conversations.value.findIndex(c => c.id === id)
  if (idx === -1) return
  conversations.value.splice(idx, 1)
  if (currentConversationId.value === id) {
    currentConversationId.value = conversations.value[0]?.id || null
  }
  if (conversations.value.length === 0) newConversation()
}
function handleQuickCard(prompt) { handleSend(prompt) }

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
  streamingThinking.value = ''
  streamingSources.value = []

  abortController = sendMessage(question, currentConversationId.value, {
    onToken(token) {
      fullContent += token
      streamingText.value = fullContent
      scrollToBottom()
    },
    onThinking(data) {
      streamingThinking.value = typeof data === 'string' ? data : JSON.stringify(data)
      scrollToBottom()
    },
    onSources(data) {
      streamingSources.value = Array.isArray(data) ? data : [data]
      scrollToBottom()
    },
    onDone() {
      conv.messages.push({
        id: assistantId, role: 'assistant', content: fullContent,
        time: new Date().toLocaleTimeString(),
        thinking: streamingThinking.value || undefined,
        thinkingLabel: '已深度思考',
        sources: streamingSources.value.length > 0 ? streamingSources.value : undefined,
      })
      streaming.value = false; streamingText.value = ''; streamingThinking.value = ''; streamingSources.value = []
      abortController = null
      scrollToBottom()
    },
    onError(err) {
      conv.messages.push({ id: assistantId, role: 'assistant', content: `请求失败: ${err.message}`, time: new Date().toLocaleTimeString(), error: true })
      streaming.value = false; streamingText.value = ''; streamingThinking.value = ''; streamingSources.value = []
      abortController = null
    },
  })
}

function handleStop() {
  if (abortController) {
    abortController.abort()
    const conv = conversations.value.find(c => c.id === currentConversationId.value)
    if (conv && streamingText.value) {
      conv.messages.push({ id: Date.now(), role: 'assistant', content: streamingText.value, time: new Date().toLocaleTimeString(), thinking: streamingThinking.value || undefined, sources: streamingSources.value.length > 0 ? streamingSources.value : undefined })
    }
    streaming.value = false; streamingText.value = ''; streamingThinking.value = ''; streamingSources.value = []
    abortController = null
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f7; color: #1d1d2c; }

.app-layout { display: flex; height: 100vh; }

.chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; background: #f5f5f7; }

.welcome-page {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 40px 24px; text-align: center;
}
.welcome-logo { margin-bottom: 20px; }
.welcome-title { font-size: 22px; font-weight: 600; color: #1d1d2c; margin-bottom: 8px; }
.welcome-subtitle { font-size: 14px; color: #8b8b9e; margin-bottom: 40px; }

.quick-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; max-width: 620px; width: 100%; }
.quick-card {
  padding: 16px 20px; border: 1px solid #e4e4eb; border-radius: 12px; cursor: pointer;
  text-align: left; transition: all 0.15s; background: #fff;
}
.quick-card:hover { border-color: #4f8cff; background: #f8f9ff; }
.card-icon { font-size: 22px; margin-bottom: 8px; }
.card-title { font-size: 14px; font-weight: 600; color: #1d1d2c; margin-bottom: 4px; }
.card-desc { font-size: 12px; color: #8b8b9e; }

.chat-body { flex: 1; overflow-y: auto; padding: 32px 40px; scroll-behavior: smooth; max-width: 860px; margin: 0 auto; width: 100%; }

.streaming-preview { margin-bottom: 40px; }

@media (max-width: 768px) {
  .chat-body { padding: 12px 16px; }
  .quick-cards { grid-template-columns: 1fr; }
  .welcome-page { padding: 24px 16px; }
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  body { background: #1a1a2c; color: #e4e4ed; }
  .chat-main { background: #1a1a2c; }
  .welcome-title { color: #e4e4ed; }
  .quick-card { background: #21212e; border-color: #2a2a3d; }
  .quick-card:hover { background: #28283d; border-color: #4f8cff; }
  .card-title { color: #e4e4ed; }
}
</style>
