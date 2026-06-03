<template>
  <aside class="sidebar">
    <button class="new-chat-btn" @click="$emit('new')">+ 新建对话</button>
    <div class="conversation-list">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="conv-item"
        :class="{ active: conv.id === activeId }"
        @click="$emit('select', conv.id)"
      >
        <span class="conv-title">{{ conv.title }}</span>
        <button class="conv-delete" @click.stop="$emit('delete', conv.id)" title="删除">&times;</button>
      </div>
    </div>
    <div class="sidebar-footer">
      <span>对话记录</span>
    </div>
  </aside>
</template>

<script setup>
defineProps({
  conversations: { type: Array, default: () => [] },
  activeId: String,
})
defineEmits(['new', 'select', 'delete'])
</script>

<style scoped>
.sidebar {
  width: 260px; background: #1e293b; color: #e2e8f0; display: flex; flex-direction: column;
  flex-shrink: 0;
}
.new-chat-btn {
  margin: 12px; padding: 10px; background: #2563eb; color: #fff; border: none;
  border-radius: 8px; font-size: 14px; cursor: pointer; transition: background 0.2s;
}
.new-chat-btn:hover { background: #1d4ed8; }

.conversation-list { flex: 1; overflow-y: auto; padding: 0 8px; }
.conv-item {
  display: flex; align-items: center; justify-content: space-between; padding: 10px 12px;
  border-radius: 8px; cursor: pointer; margin-bottom: 2px; transition: background 0.15s;
}
.conv-item:hover { background: #334155; }
.conv-item.active { background: #334155; }
.conv-title { font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-delete {
  background: none; border: none; color: #94a3b8; font-size: 18px; cursor: pointer;
  padding: 0 4px; opacity: 0; transition: opacity 0.15s;
}
.conv-item:hover .conv-delete { opacity: 1; }
.conv-delete:hover { color: #f87171; }

.sidebar-footer { padding: 12px; font-size: 11px; color: #64748b; text-align: center; border-top: 1px solid #334155; }

@media (max-width: 768px) {
  .sidebar { width: 200px; }
}
</style>
