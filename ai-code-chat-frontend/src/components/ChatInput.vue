<template>
  <div class="chat-input-bar">
    <div class="input-row">
      <div class="tool-toggles">
        <button class="toggle-btn" :class="{ active: deepThink }" @click="deepThink = !deepThink">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="15" height="15">
            <path d="M11 19l-7-7 7-7m8 14l-7-7 7-7"/>
          </svg>
          深度思考
        </button>
        <button class="toggle-btn" :class="{ active: webSearch }" @click="webSearch = !webSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="15" height="15">
            <circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/>
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
          </svg>
          联网搜索
        </button>
      </div>
      <div class="send-row">
        <button class="attach-btn" title="上传文件">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
            <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
          </svg>
        </button>
        <textarea
          ref="inputEl"
          v-model="text"
          class="input-area"
          placeholder="给 AI 发送消息"
          :disabled="disabled"
          rows="1"
          @keydown.enter.exact.prevent="send"
          @input="autoResize"
        ></textarea>
        <button v-if="!disabled" class="send-btn" @click="send" :disabled="!text.trim()">
          <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
        <button v-else class="stop-btn" @click="$emit('stop')">
          <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
            <rect x="6" y="6" width="12" height="12" rx="1"/>
          </svg>
        </button>
      </div>
    </div>
    <div class="input-footer">AI Chat 也可能会犯错。请核实重要信息。</div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
const props = defineProps({ disabled: Boolean })
const emit = defineEmits(['send', 'stop'])
const text = ref('')
const inputEl = ref(null)
const deepThink = ref(false)
const webSearch = ref(false)

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
.chat-input-bar { padding: 0 24px 20px; flex-shrink: 0; }

.input-row {
  background: #fff; border: 1px solid #e4e4eb; border-radius: 14px; overflow: hidden;
}
.tool-toggles { display: flex; gap: 6px; padding: 10px 14px 6px; }
.toggle-btn {
  display: flex; align-items: center; gap: 5px; padding: 5px 12px;
  border: 1px solid #e4e4eb; border-radius: 16px; background: #fff;
  font-size: 12px; color: #8b8b9e; cursor: pointer; transition: all 0.15s;
}
.toggle-btn:hover { border-color: #c4c4d4; color: #6b6b8a; }
.toggle-btn.active { background: #eef2ff; border-color: #93b5fd; color: #4f8cff; }

.send-row { display: flex; align-items: flex-end; gap: 8px; padding: 4px 14px 12px; }
.attach-btn {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: none; border: none; color: #b0b0c0; cursor: pointer; border-radius: 8px;
  flex-shrink: 0; transition: color 0.15s;
}
.attach-btn:hover { color: #8b8b9e; }

.input-area {
  flex: 1; padding: 8px 0; border: none; font-size: 15px; font-family: inherit;
  resize: none; outline: none; line-height: 1.5; min-height: 36px; max-height: 150px;
  background: transparent; color: #1d1d2c;
}
.input-area::placeholder { color: #b0b0c0; }
.input-area:disabled { color: #b0b0c0; }

.send-btn {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: #e4e4eb; color: #fff; border: none; border-radius: 10px; cursor: pointer;
  flex-shrink: 0; transition: background 0.15s;
}
.send-btn:not(:disabled) { background: #4f8cff; }
.send-btn:not(:disabled):hover { background: #3d7ae8; }
.send-btn:disabled { cursor: not-allowed; }

.stop-btn {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: #fff; color: #6b6b8a; border: 1px solid #e4e4eb; border-radius: 10px;
  cursor: pointer; flex-shrink: 0; transition: background 0.15s;
}
.stop-btn:hover { background: #f5f5f7; }

.input-footer { text-align: center; font-size: 11px; color: #b0b0c0; margin-top: 8px; }

@media (max-width: 768px) { .chat-input-bar { padding: 0 12px 14px; } }

@media (prefers-color-scheme: dark) {
  .input-row { background: #21212e; border-color: #2a2a3d; }
  .input-area { color: #e4e4ed; }
  .toggle-btn { background: #21212e; border-color: #2a2a3d; color: #8b8b9e; }
  .toggle-btn:hover { border-color: #4f8cff; color: #e4e4ed; }
  .toggle-btn.active { background: #1a2744; }
  .stop-btn { background: #21212e; color: #e4e4ed; border-color: #2a2a3d; }
}
</style>
