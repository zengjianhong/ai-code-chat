<template>
  <div class="chat-input-bar">
    <textarea
      ref="inputEl"
      v-model="text"
      class="input-area"
      placeholder="输入你的编程问题..."
      :disabled="disabled"
      rows="1"
      @keydown.enter.exact.prevent="send"
      @input="autoResize"
    ></textarea>
    <button v-if="!disabled" class="send-btn" @click="send" :disabled="!text.trim()">发送</button>
    <button v-else class="stop-btn" @click="$emit('stop')">停止</button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({ disabled: Boolean })
const emit = defineEmits(['send', 'stop'])

const text = ref('')
const inputEl = ref(null)

function send() {
  if (!text.value.trim() || props.disabled) return
  emit('send', text.value.trim())
  text.value = ''
  nextTick(autoResize)
}

function autoResize() {
  const el = inputEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 150) + 'px'
}
</script>

<style scoped>
.chat-input-bar {
  display: flex; align-items: flex-end; gap: 8px; padding: 16px 24px;
  background: #fff; border-top: 1px solid #e5e7eb; flex-shrink: 0;
}
.input-area {
  flex: 1; padding: 10px 16px; border: 1px solid #d1d5db; border-radius: 12px;
  font-size: 15px; font-family: inherit; resize: none; outline: none; line-height: 1.5;
  min-height: 44px; max-height: 150px; transition: border-color 0.15s;
}
.input-area:focus { border-color: #2563eb; }
.input-area:disabled { background: #f9fafb; }

.send-btn {
  padding: 10px 22px; background: #2563eb; color: #fff; border: none;
  border-radius: 10px; font-size: 15px; cursor: pointer; transition: background 0.2s;
  white-space: nowrap;
}
.send-btn:hover:not(:disabled) { background: #1d4ed8; }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.stop-btn {
  padding: 10px 22px; background: #ef4444; color: #fff; border: none;
  border-radius: 10px; font-size: 15px; cursor: pointer; white-space: nowrap;
}
.stop-btn:hover { background: #dc2626; }

@media (max-width: 768px) {
  .chat-input-bar { padding: 12px; }
}
</style>
