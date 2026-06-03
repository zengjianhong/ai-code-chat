<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <svg viewBox="0 0 36 36" fill="none" stroke="#4f6ef7" stroke-width="1.2" width="24" height="24">
          <path d="M18 3L3 10.5l15 7.5 15-7.5L18 3z"/><path d="M3 25.5l15 7.5 15-7.5"/><path d="M3 18l15 7.5 15-7.5"/>
        </svg>
        <span>智能编程助手</span>
      </div>
    </div>
    <button class="new-chat-btn" @click="$emit('new')">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
        <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
      </svg>
      开启新对话
    </button>

    <div class="conversation-groups">
      <div v-if="todayConversations.length > 0" class="group-label">今天</div>
      <div v-for="conv in todayConversations" :key="conv.id" class="conv-item" :class="{ active: conv.id === activeId }" @click="$emit('select', conv.id)">
        <svg class="conv-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="conv-title">{{ conv.title }}</span>
        <button class="conv-delete" @click.stop="$emit('delete', conv.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
      <div v-if="olderConversations.length > 0" class="group-label">更早</div>
      <div v-for="conv in olderConversations" :key="conv.id" class="conv-item" :class="{ active: conv.id === activeId }" @click="$emit('select', conv.id)">
        <svg class="conv-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="conv-title">{{ conv.title }}</span>
        <button class="conv-delete" @click.stop="$emit('delete', conv.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="sidebar-footer">
      <div class="sidebar-user">
        <div class="user-avatar">U</div>
        <span>用户</span>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ conversations: { type: Array, default: () => [] }, activeId: String })
defineEmits(['new', 'select', 'delete'])
const todayConversations = computed(() => props.conversations)
const olderConversations = computed(() => [])
</script>

<style scoped>
.sidebar {
  width: 260px; background: #1d1e2a; color: #bcbec8; display: flex; flex-direction: column;
  flex-shrink: 0; height: 100vh; user-select: none;
}
.sidebar-header { padding: 18px 16px 10px; }
.sidebar-logo { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: #fff; letter-spacing: 0.2px; }

.new-chat-btn {
  margin: 4px 12px 20px; padding: 10px 0; background: #282a3a; color: #7b8cff;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 500; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px; transition: background 0.15s;
}
.new-chat-btn:hover { background: #32344a; }

.conversation-groups { flex: 1; overflow-y: auto; padding: 0 8px; }
.group-label { font-size: 12px; color: #5b5d72; padding: 10px 12px 6px; font-weight: 600; }
.conv-item {
  display: flex; align-items: center; gap: 10px; padding: 9px 12px; border-radius: 8px; cursor: pointer;
  margin-bottom: 1px; transition: background 0.15s; font-size: 13px; color: #bcbec8;
}
.conv-item:hover { background: #282a3a; }
.conv-item.active { background: #282a3a; }
.conv-icon-svg { flex-shrink: 0; color: #5b5d72; }
.conv-item.active .conv-icon-svg { color: #7b8cff; }
.conv-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-delete { background: none; border: none; color: #5b5d72; cursor: pointer; padding: 2px; opacity: 0; transition: opacity 0.15s; flex-shrink: 0; display: flex; align-items: center; }
.conv-item:hover .conv-delete { opacity: 1; }
.conv-delete:hover { color: #ef4444; }

.sidebar-footer { padding: 12px 16px; border-top: 1px solid #282a3a; }
.sidebar-user { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #7a7c90; }
.user-avatar { width: 28px; height: 28px; border-radius: 50%; background: #7b8cff; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 13px; }

@media (max-width: 768px) { .sidebar { display: none; } }
</style>
