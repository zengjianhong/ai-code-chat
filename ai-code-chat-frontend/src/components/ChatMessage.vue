<template>
  <div class="message-row" :class="message.role">
    <div class="message-bubble" :class="[message.role, { error: message.error }]">
      <div
        v-if="message.role === 'assistant' && !message.error"
        class="markdown-body"
        v-html="renderMarkdown(message.content)"
      ></div>
      <template v-else>{{ message.content }}</template>
      <div class="message-time">{{ message.time }}</div>
    </div>
  </div>
</template>

<script setup>
import { Marked } from 'marked'
const marked = new Marked()
defineProps({ message: Object })
function renderMarkdown(text) { return marked.parse(text) }
</script>

<style scoped>
.message-row { display: flex; margin-bottom: 20px; }
.message-row.user { justify-content: flex-end; }
.message-row.assistant { justify-content: flex-start; }

.message-bubble {
  max-width: 75%; padding: 12px 18px; border-radius: 16px; line-height: 1.6;
  font-size: 15px; word-break: break-word;
}
.message-bubble.user { background: #2563eb; color: #fff; border-bottom-right-radius: 4px; }
.message-bubble.assistant { background: #fff; border: 1px solid #e5e7eb; border-bottom-left-radius: 4px; }
.message-bubble.assistant.error { background: #fef2f2; border-color: #fecaca; color: #dc2626; }

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

@media (max-width: 768px) {
  .message-bubble { max-width: 90%; }
}
</style>
