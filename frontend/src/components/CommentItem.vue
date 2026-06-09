<template>
  <div class="comment-item" :class="'depth-' + Math.min(depth, 3)">
    <div class="comment-row">
      <div class="comment-avatar">
        <el-avatar :size="28" :src="comment.user?.avatar" />
      </div>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.user?.nickname || comment.user?.username }}</span>
          <span v-if="comment.replyToUser" class="reply-to">回复 @{{ comment.replyToUser.nickname || comment.replyToUser.username }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button text size="small" @click="showReply = !showReply" v-if="depth < 3">
            <el-icon><ChatDotRound /></el-icon> 回复
          </el-button>
          <el-button text size="small" type="danger" v-if="canDelete" @click="$emit('delete', comment.id)">
            删除
          </el-button>
        </div>
        <div v-if="showReply" class="reply-box">
          <el-input v-model="replyContent" type="textarea" :rows="2" placeholder="写下回复..." />
          <el-button size="small" type="primary" style="margin-top: 6px;" @click="doReply">回复</el-button>
          <el-button size="small" style="margin-top: 6px;" @click="showReply = false">取消</el-button>
        </div>
      </div>
    </div>
    <div v-if="comment.children?.length" class="comment-children">
      <CommentItem
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        :depth="depth + 1"
        :post-id="postId"
        @reply="(data) => $emit('reply', data)"
        @delete="(id) => $emit('delete', id)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const props = defineProps({
  comment: { type: Object, required: true },
  depth: { type: Number, default: 0 },
  postId: { type: Number, required: true }
})

const emit = defineEmits(['reply', 'delete'])
const auth = useAuthStore()
const showReply = ref(false)
const replyContent = ref('')

const canDelete = computed(() => {
  return auth.isAdmin || auth.user?.id === props.comment.user?.id
})

function doReply() {
  if (!replyContent.value.trim()) return
  emit('reply', {
    parentId: props.comment.id,
    replyToUserId: props.comment.user?.id,
    content: replyContent.value
  })
  replyContent.value = ''
  showReply.value = false
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.comment-item {
  margin-bottom: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-row {
  display: flex;
}

.comment-avatar {
  margin-right: 10px;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  font-size: 13px;
  margin-bottom: 4px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.comment-author {
  font-weight: 600;
  color: #333;
}

.reply-to {
  color: #409eff;
  margin-left: 6px;
}

.comment-time {
  color: #999;
  margin-left: 10px;
}

.comment-content {
  line-height: 1.6;
  margin-bottom: 6px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 4px;
}

.reply-box {
  margin-top: 8px;
}

.comment-children {
  margin-top: 4px;
}

/* Depth-based indentation — CSS classes override cleanly in media queries */
.depth-1 { margin-left: 20px; }
.depth-2 { margin-left: 40px; }
.depth-3 { margin-left: 60px; }

@media (max-width: 480px) {
  .depth-1,
  .depth-2,
  .depth-3 {
    margin-left: 0;
  }

  .comment-children {
    padding-left: 12px;
  }

  .comment-header {
    font-size: 12px;
  }
}
</style>
