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
          <svg viewBox="0 0 36 36" fill="none" stroke="#4f6ef7" stroke-width="1.2" width="52" height="52">
            <path d="M18 3L3 10.5l15 7.5 15-7.5L18 3z"/><path d="M3 25.5l15 7.5 15-7.5"/><path d="M3 18l15 7.5 15-7.5"/>
          </svg>
        </div>
        <h1 class="welcome-title">我是您的 AI 编程助手</h1>
        <p class="welcome-subtitle">解答编程学习与求职面试问题</p>
        <div class="quick-cards">
          <div class="quick-card" v-for="card in quickCards" :key="card.title" @click="handleQuickCard(card.prompt)">
            <div class="card-icon-wrap">
              <svg class="card-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22">
                <template v-if="card.key === 'java'">
                  <path d="M4 6h16M4 12h16M4 18h12"/>
                </template>
                <template v-else-if="card.key === 'thread'">
                  <circle cx="12" cy="12" r="9"/>
                  <path d="M12 6v6l3.5 2"/>
                </template>
                <template v-else-if="card.key === 'algo'">
                  <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2M9 5v2h6V5M9 14l2 2 4-4"/>
                </template>
                <template v-else>
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6z"/>
                  <path d="M14 2v6h6M10 13V9m-2 4h4"/>
                </template>
              </svg>
            </div>
            <div class="card-text">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-desc">{{ card.desc }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="chat-body" ref="chatBody">
        <ChatMessage v-for="msg in currentMessages" :key="msg.id" :message="msg"/>
        <div v-if="streaming" class="streaming-preview">
          <ChatMessage :message="{ id: 'streaming', role: 'assistant', content: streamingText || '...', thinking: streamingThinking, thinkingLabel: '正在思考' }"/>
        </div>
      </div>

      <ChatInput :disabled="streaming" @send="handleSend" @stop="handleStop"/>
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
  { key: 'java', title: 'Java 学习路线', desc: '了解 Java 开发者完整学习路径', prompt: 'Java 的学习路线是什么？' },
  { key: 'thread', title: '多线程编程', desc: '深入理解 Java 并发与多线程', prompt: 'Java 中如何实现多线程编程？' },
  { key: 'algo', title: '算法面试题', desc: '常见算法与数据结构面试题', prompt: '程序员常见的算法面试题有哪些？' },
  { key: 'resume', title: '简历优化建议', desc: '优化技术简历获得更多面试', prompt: '如何优化技术人员的简历？' },
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
  if (saved) { try { conversations.value = JSON.parse(saved) } catch {} }
  if (conversations.value.length === 0) { newConversation() }
  else { currentConversationId.value = conversations.value[0].id }
})

watch(conversations, (val) => { localStorage.setItem('ai-chat-conversations', JSON.stringify(val)) }, { deep: true })

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
  if (currentConversationId.value === id) { currentConversationId.value = conversations.value[0]?.id || null }
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
  streaming.value = true; streamingText.value = ''; streamingThinking.value = ''; streamingSources.value = []
  abortController = sendMessage(question, currentConversationId.value, {
    onToken(token) { fullContent += token; streamingText.value = fullContent; scrollToBottom() },
    onThinking(data) { streamingThinking.value = typeof data === 'string' ? data : JSON.stringify(data); scrollToBottom() },
    onSources(data) { streamingSources.value = Array.isArray(data) ? data : [data]; scrollToBottom() },
    onDone() {
      conv.messages.push({ id: assistantId, role: 'assistant', content: fullContent, time: new Date().toLocaleTimeString(), thinking: streamingThinking.value || undefined, thinkingLabel: '已深度思考', sources: streamingSources.value.length > 0 ? streamingSources.value : undefined })
      streaming.value = false; streamingText.value = ''; streamingThinking.value = ''; streamingSources.value = []
      abortController = null; scrollToBottom()
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

function scrollToBottom() { nextTick(() => { if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight }) }
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', 'PingFang SC', 'Microsoft YaHei', 'Hiragino Sans GB', sans-serif;
  background: #f7f8fa; color: #1d1d2c;
  -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;
  font-size: 16px; line-height: 1.6;
}

.app-layout { display: flex; height: 100vh; }
.chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; background: #f7f8fa; }

/* Welcome */
.welcome-page { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px 24px; text-align: center; }
.welcome-logo { margin-bottom: 18px; opacity: 0.9; }
.welcome-title { font-size: 22px; font-weight: 700; color: #1d1d2c; margin-bottom: 8px; letter-spacing: -0.2px; }
.welcome-subtitle { font-size: 14px; color: #8e8e9e; margin-bottom: 44px; }

.quick-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; max-width: 640px; width: 100%; }
.quick-card {
  display: flex; align-items: flex-start; gap: 14px; padding: 16px 20px;
  border: 1px solid #e8e8ee; border-radius: 14px; cursor: pointer;
  background: #fff; transition: all 0.15s;
}
.quick-card:hover { border-color: #4f6ef7; background: #fafaff; box-shadow: 0 2px 12px rgba(79,110,247,0.08); }
.card-icon-wrap { flex-shrink: 0; width: 40px; height: 40px; border-radius: 10px; background: #f0f2ff; display: flex; align-items: center; justify-content: center; }
.card-icon-svg { color: #4f6ef7; }
.card-text { flex: 1; min-width: 0; }
.card-title { font-size: 14px; font-weight: 600; color: #1d1d2c; margin-bottom: 4px; }
.card-desc { font-size: 12px; color: #8e8e9e; line-height: 1.45; }

/* Chat */
.chat-body { flex: 1; overflow-y: auto; padding: 36px 40px; scroll-behavior: smooth; max-width: 880px; margin: 0 auto; width: 100%; }
.streaming-preview { margin-bottom: 44px; }

@media (max-width: 768px) {
  .chat-body { padding: 12px 16px; }
  .quick-cards { grid-template-columns: 1fr; }
  .welcome-page { padding: 24px 16px; }
}

@media (prefers-color-scheme: dark) {
  body { background: #1a1a2c; color: #e4e4ed; }
  .chat-main { background: #1a1a2c; }
  .welcome-title { color: #e4e4ed; }
  .quick-card { background: #21212e; border-color: #2a2a3d; }
  .quick-card:hover { background: #28283d; border-color: #4f6ef7; box-shadow: none; }
  .card-title { color: #e4e4ed; }
  .card-icon-wrap { background: #282842; }
}
</style>
