<template>
  <div class="message-wrapper">
    <div v-if="message.thinking" class="thinking-section">
      <div class="thinking-toggle" @click="showThinking = !showThinking">
        <svg class="thinking-chevron" :class="{ open: showThinking }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
        <svg class="thinking-brain" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="15" height="15">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15l-4-4 1.41-1.41L11 14.17l6.59-6.59L19 9l-8 8z"/>
        </svg>
        <span>{{ message.thinkingLabel || '已深度思考' }}</span>
        <span v-if="message.thinkingTime" class="thinking-time">({{ message.thinkingTime }})</span>
      </div>
      <div v-if="showThinking" class="thinking-content">{{ message.thinking }}</div>
    </div>

    <div class="message-row" :class="message.role">
      <div class="message-bubble" :class="[message.role, { error: message.error }]">
        <div
          v-if="message.role === 'assistant' && !message.error"
          class="markdown-body"
          v-html="renderMarkdown(message.content)"
        ></div>
        <template v-else>{{ message.content }}</template>
      </div>
    </div>

    <div v-if="message.sources && message.sources.length > 0" class="sources-section">
      <div class="sources-label">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
        </svg>
        引用来源
      </div>
      <div class="sources-list">
        <div v-for="(src, idx) in message.sources" :key="idx" class="source-item">
          <span class="source-idx">{{ idx + 1 }}</span>
          <span class="source-file">{{ src.source }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Marked } from 'marked'
const marked = new Marked({ breaks: true, gfm: true })
defineProps({ message: Object })
const showThinking = ref(false)
function renderMarkdown(text) {
  let processed = text
    // Ensure headings have a blank line before them
    .replace(/([^\n])\n(#{1,6}\s)/g, '$1\n\n$2')
    // Ensure list items preceded by non-list text have a blank line before the list
    .replace(/([^\n*\-])\n([*\-]\s)/g, '$1\n\n$2')
    // Fix bold text followed by colon on same line as list
    .replace(/(\*\*[^*]+\*\*[：:])\s*\n([*\-])/g, '$1\n\n$2')
  return marked.parse(processed)
}
</script>

<style scoped>
.message-wrapper { margin-bottom: 40px; padding: 0 4px; }

/* Thinking */
.thinking-section { margin-bottom: 12px; }
.thinking-toggle {
  display: inline-flex; align-items: center; gap: 6px; font-size: 13px;
  color: #8b8b9e; cursor: pointer; padding: 4px 0; user-select: none;
}
.thinking-toggle:hover { color: #6b6b8a; }
.thinking-chevron { transition: transform 0.2s; flex-shrink: 0; }
.thinking-chevron.open { transform: rotate(90deg); }
.thinking-brain { flex-shrink: 0; color: #4f8cff; }
.thinking-time { font-size: 12px; color: #b0b0c0; }
.thinking-content {
  margin-top: 10px; padding: 14px 18px; background: #f5f5f7; border-radius: 8px;
  font-size: 13px; color: #6b6b8a; line-height: 1.7; white-space: pre-wrap;
  border-left: 3px solid #d4d4de;
}

/* Message bubbles */
.message-row { display: flex; }
.message-row.user { justify-content: flex-end; }
.message-row.assistant { justify-content: flex-start; }
.message-bubble {
  max-width: 82%; line-height: 1.82; font-size: 15px; word-break: break-word; color: #1d1d2c;
}
.message-bubble.user {
  background: #4f8cff; color: #fff; padding: 10px 16px; border-radius: 14px 14px 4px 14px;
}
.message-bubble.assistant { background: transparent; }
.message-bubble.assistant.error {
  background: #fef2f2; color: #dc2626; padding: 10px 16px; border-radius: 14px;
}

/* Sources */
.sources-section { margin-top: 16px; }
.sources-label {
  display: flex; align-items: center; gap: 6px; font-size: 12px; color: #8b8b9e; margin-bottom: 8px;
}
.sources-list { display: flex; flex-wrap: wrap; gap: 6px; }
.source-item {
  display: flex; align-items: center; gap: 6px; padding: 5px 10px;
  background: #f5f5f7; border-radius: 6px; font-size: 12px; color: #6b6b8a;
}
.source-idx {
  width: 18px; height: 18px; border-radius: 4px; background: #e4e4eb;
  display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 600;
  flex-shrink: 0; color: #6b6b8a;
}
.source-file { max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Markdown — DeepSeek-style structured, clean, highly readable */
.markdown-body :deep(h1) { margin: 28px 0 12px; font-size: 1.5em; font-weight: 700; line-height: 1.35; }
.markdown-body :deep(h2) { margin: 24px 0 10px; font-size: 1.25em; font-weight: 700; line-height: 1.4; }
.markdown-body :deep(h3) { margin: 20px 0 8px; font-size: 1.1em; font-weight: 700; line-height: 1.4; }
.markdown-body :deep(h1):first-child,
.markdown-body :deep(h2):first-child,
.markdown-body :deep(h3):first-child { margin-top: 0; }

.markdown-body :deep(p) { margin: 12px 0; font-size: 15px; line-height: 1.75; }

.markdown-body :deep(ul), .markdown-body :deep(ol) { padding-left: 26px; margin: 10px 0; }
.markdown-body :deep(li) { margin-bottom: 6px; font-size: 15px; line-height: 1.7; }

.markdown-body :deep(strong) { font-weight: 700; color: inherit; }
.markdown-body :deep(em) { font-style: italic; }

.markdown-body :deep(code) {
  background: rgba(0,0,0,0.06); color: #c7254e; padding: 2px 7px; border-radius: 5px;
  font-size: 0.9em; font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-weight: 500;
}

.markdown-body :deep(pre) {
  background: #1a1d28; color: #cdd6f4; padding: 18px 22px; border-radius: 12px;
  overflow-x: auto; margin: 16px 0; border: 1px solid #e4e4eb20;
}
.markdown-body :deep(pre code) {
  background: none; padding: 0; color: inherit; font-size: 0.85em; line-height: 1.75;
  font-weight: 400;
}

/* Tables — clean, card-like */
.markdown-body :deep(table) {
  border-collapse: collapse; width: 100%; margin: 16px 0; font-size: 0.93em;
  border-radius: 10px; overflow: hidden; border: 1px solid rgba(0,0,0,0.08);
}
.markdown-body :deep(th), .markdown-body :deep(td) {
  border-bottom: 1px solid rgba(0,0,0,0.06); padding: 10px 14px; text-align: left;
}
.markdown-body :deep(th) { background: rgba(0,0,0,0.04); font-weight: 700; color: inherit; font-size: 0.92em; }
.markdown-body :deep(tr:last-child td) { border-bottom: none; }

.markdown-body :deep(blockquote) {
  border-left: 3px solid #4f8cff; padding: 6px 0 6px 16px; color: inherit;
  opacity: 0.8; margin: 16px 0; font-size: 14px; background: rgba(0,0,0,0.03); border-radius: 0 8px 8px 0;
}

.markdown-body :deep(a) { color: #4f8cff; text-decoration: none; font-weight: 500; }
.markdown-body :deep(a:hover) { text-decoration: underline; }

.markdown-body :deep(hr) { border: none; border-top: 1px solid rgba(0,0,0,0.06); margin: 24px 0; }

.markdown-body :deep(img) { max-width: 100%; border-radius: 10px; margin: 12px 0; }

/* Inline key-value / bold labels */
.markdown-body :deep(strong) + :deep(br) { display: none; }

@media (prefers-color-scheme: dark) {
  .message-bubble.assistant { color: #e4e4ed; }
  .thinking-content { background: #21212e; border-left-color: #2a2a3d; color: #8b8b9e; }
  .source-item { background: #21212e; }
  .source-idx { background: #2a2a3d; }
  .markdown-body :deep(code) { background: rgba(255,255,255,0.08); color: #f48fb1; }
  .markdown-body :deep(th) { background: rgba(255,255,255,0.04); }
  .markdown-body :deep(td) { border-color: rgba(255,255,255,0.06); }
  .markdown-body :deep(pre) { background: #16161f; }
  .markdown-body :deep(hr) { border-color: rgba(255,255,255,0.08); }
  .markdown-body :deep(blockquote) { background: rgba(255,255,255,0.03); }
}

@media (max-width: 768px) {
  .message-bubble { max-width: 92%; }
}
</style>
