<template>
  <div class="chat-input-bar">
    <div class="input-row">
      <div class="tool-toggles">
        <button class="toggle-btn" :class="{ active: deepThink }" @click="deepThink = !deepThink">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" width="15" height="15"><path d="M11 19l-7-7 7-7m8 14l-7-7 7-7"/></svg>
          深度思考
        </button>
        <button class="toggle-btn" :class="{ active: webSearch }" @click="webSearch = !webSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" width="15" height="15"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
          联网搜索
        </button>
      </div>
      <div class="send-row">
        <button class="attach-btn" title="上传文件">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" width="18" height="18"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
        </button>
        <textarea ref="inputEl" v-model="text" class="input-area" placeholder="给 AI 发送消息" :disabled="disabled" rows="1" @keydown.enter.exact.prevent="send" @input="autoResize"></textarea>
        <button v-if="!disabled" class="send-btn" @click="send" :disabled="!text.trim()">
          <svg viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
        <button v-else class="stop-btn" @click="$emit('stop')">
          <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><rect x="6" y="6" width="12" height="12" rx="1"/></svg>
        </button>
      </div>
    </div>
    <div class="input-footer">内容由 AI 生成，仅供参考</div>
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

function send() { if (!text.value.trim() || props.disabled) return; emit('send', text.value.trim()); text.value = ''; nextTick(autoResize) }
function autoResize() { const el = inputEl.value; if (!el) return; el.style.height = 'auto'; el.style.height = Math.min(el.scrollHeight, 150) + 'px' }
</script>

<style scoped>
.chat-input-bar { padding: 0 24px 22px; flex-shrink: 0; max-width: 880px; margin: 0 auto; width: 100%; }

.input-row { background: #fff; border: 1px solid #e2e2ea; border-radius: 16px; overflow: hidden; }
.tool-toggles { display: flex; gap: 6px; padding: 12px 16px 6px; }
.toggle-btn {
  display: flex; align-items: center; gap: 5px; padding: 5px 12px; border: 1px solid #e2e2ea; border-radius: 16px;
  background: #fff; font-size: 12px; color: #8e8e9e; cursor: pointer; transition: all 0.15s; font-weight: 500;
}
.toggle-btn:hover { border-color: #ccceda; color: #5b5d72; }
.toggle-btn.active { background: #f0f2ff; border-color: #b8c4ff; color: #4f6ef7; }

.send-row { display: flex; align-items: flex-end; gap: 8px; padding: 4px 16px 14px; }
.attach-btn { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: none; border: none; color: #b8bacc; cursor: pointer; border-radius: 8px; flex-shrink: 0; }
.attach-btn:hover { color: #8e8e9e; }

.input-area { flex: 1; padding: 8px 0; border: none; font-size: 15px; font-family: inherit; resize: none; outline: none; line-height: 1.55; min-height: 36px; max-height: 150px; background: transparent; color: #1d1d2c; }
.input-area::placeholder { color: #b8bacc; }
.input-area:disabled { color: #b8bacc; }

.send-btn { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: #e2e2ea; color: #fff; border: none; border-radius: 10px; cursor: pointer; flex-shrink: 0; transition: background 0.15s; }
.send-btn:not(:disabled) { background: #4f6ef7; }
.send-btn:not(:disabled):hover { background: #3f5ee8; }
.send-btn:disabled { cursor: not-allowed; }

.stop-btn { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: #fff; color: #5b5d72; border: 1px solid #e2e2ea; border-radius: 10px; cursor: pointer; flex-shrink: 0; }
.stop-btn:hover { background: #f7f8fa; }

.input-footer { text-align: center; font-size: 11px; color: #b8bacc; margin-top: 8px; }

@media (max-width: 768px) { .chat-input-bar { padding: 0 12px 14px; } }

@media (prefers-color-scheme: dark) {
  .input-row { background: #21212e; border-color: #2a2a3d; }
  .input-area { color: #e4e4ed; }
  .toggle-btn { background: #21212e; border-color: #2a2a3d; color: #7a7c90; }
  .toggle-btn:hover { border-color: #4f6ef7; color: #bcbec8; }
  .toggle-btn.active { background: #1a2448; }
  .stop-btn { background: #21212e; color: #bcbec8; border-color: #2a2a3d; }
}
</style>
