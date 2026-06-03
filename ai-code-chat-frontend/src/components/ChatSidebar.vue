<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="24" height="24">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
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
      <div
        v-for="conv in todayConversations"
        :key="conv.id"
        class="conv-item"
        :class="{ active: conv.id === activeId }"
        @click="$emit('select', conv.id)"
      >
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
      <div
        v-for="conv in olderConversations"
        :key="conv.id"
        class="conv-item"
        :class="{ active: conv.id === activeId }"
        @click="$emit('select', conv.id)"
      >
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
const props = defineProps({
  conversations: { type: Array, default: () => [] },
  activeId: String,
})
defineEmits(['new', 'select', 'delete'])
const todayConversations = computed(() => props.conversations)
const olderConversations = computed(() => [])
</script>

<style scoped>
.sidebar {
  width: 260px; background: #21212e; color: #c9cdd4; display: flex; flex-direction: column;
  flex-shrink: 0; height: 100vh; user-select: none;
}
.sidebar-header { padding: 18px 16px 12px; }
.sidebar-logo { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #fff; }
.sidebar-logo svg { color: #4f8cff; }

.new-chat-btn {
  margin: 0 12px 20px; padding: 10px 0; background: #2a2a3d; color: #4f8cff;
  border: none; border-radius: 10px; font-size: 14px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  transition: background 0.15s;
}
.new-chat-btn:hover { background: #32324a; }

.conversation-groups { flex: 1; overflow-y: auto; padding: 0 8px; }
.group-label { font-size: 12px; color: #6b6b8a; padding: 8px 12px 6px; font-weight: 500; }
.conv-item {
  display: flex; align-items: center; gap: 10px; padding: 9px 12px;
  border-radius: 8px; cursor: pointer; margin-bottom: 1px; transition: background 0.15s;
  font-size: 13px; color: #c9cdd4;
}
.conv-item:hover { background: #2a2a3d; }
.conv-item.active { background: #2a2a3d; }
.conv-icon-svg { flex-shrink: 0; color: #6b6b8a; }
.conv-item.active .conv-icon-svg { color: #4f8cff; }
.conv-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-delete {
  background: none; border: none; color: #6b6b8a; cursor: pointer;
  padding: 2px; opacity: 0; transition: opacity 0.15s; flex-shrink: 0;
  display: flex; align-items: center;
}
.conv-item:hover .conv-delete { opacity: 1; }
.conv-delete:hover { color: #ef4444; }

.sidebar-footer { padding: 12px 16px; border-top: 1px solid #2a2a3d; }
.sidebar-user { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #8b8b9e; }
.user-avatar {
  width: 28px; height: 28px; border-radius: 50%; background: #4f8cff;
  display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 13px;
}

@media (max-width: 768px) { .sidebar { display: none; } }
</style>
