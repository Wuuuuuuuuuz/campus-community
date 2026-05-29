<template>
  <div class="post-detail" v-loading="loading">
    <div v-if="post">
      <div class="post-header">
        <span v-if="post.pinned" class="pinned-tag">置顶</span>
        <h1>{{ post.title }}</h1>
        <div class="post-info">
          <span>{{ post.author?.nickname || post.author?.username }}</span>
          <el-tag v-if="post.category" size="small">{{ post.category.name }}</el-tag>
          <span>{{ formatTime(post.createdAt) }}</span>
          <span>{{ post.viewCount }} 阅读</span>
          <span>{{ post.commentCount }} 评论</span>
        </div>
      </div>

      <div class="post-body" v-html="renderedContent"></div>

      <div class="post-actions" v-if="canEdit">
        <el-button type="primary" size="small" @click="editPost">编辑</el-button>
        <el-button type="danger" size="small" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <!-- Comments -->
    <div class="comments-section">
      <h3>评论 ({{ post?.commentCount || 0 }})</h3>

      <div class="comment-input" v-if="auth.isLoggedIn">
        <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论..." />
        <el-button type="primary" style="margin-top: 10px;" @click="submitComment">发表评论</el-button>
      </div>
      <div v-else class="comment-login-hint">
        <router-link to="/login">登录</router-link> 后参与评论
      </div>

      <div class="comment-list" v-if="comments.length > 0">
        <CommentItem
          v-for="item in comments"
          :key="item.id"
          :comment="item"
          :post-id="post?.id"
          @reply="handleReply"
          @delete="handleDeleteComment"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePostStore } from '../stores/post'
import { useAuthStore } from '../stores/auth'
import { marked } from 'marked'
import CommentItem from '../components/CommentItem.vue'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()
const auth = useAuthStore()

const loading = ref(false)
const post = ref(null)
const comments = ref([])
const commentContent = ref('')

const canEdit = computed(() => {
  if (!post.value || !auth.user) return false
  return auth.user.id === post.value.author?.id || auth.isAdmin
})

onMounted(async () => {
  loading.value = true
  try {
    const [postRes, commentRes] = await Promise.all([
      postStore.fetchPost(route.params.id),
      postStore.fetchComments(route.params.id)
    ])
    post.value = postRes.data
    comments.value = commentRes.data
  } catch (e) { /* ignore */ }
  loading.value = false
})

const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  return marked(post.value.content)
})

async function submitComment() {
  if (!commentContent.value.trim()) return
  try {
    await postStore.createComment(post.value.id, { content: commentContent.value })
    ElMessage.success('评论成功')
    commentContent.value = ''
    const res = await postStore.fetchComments(post.value.id)
    comments.value = res.data
    post.value.commentCount++
  } catch (e) { /* ignore */ }
}

async function handleReply({ parentId, replyToUserId, content }) {
  try {
    await postStore.createComment(post.value.id, { content, parentId, replyToUserId })
    ElMessage.success('回复成功')
    const res = await postStore.fetchComments(post.value.id)
    comments.value = res.data
    post.value.commentCount++
  } catch (e) { /* ignore */ }
}

async function handleDeleteComment(id) {
  try {
    await postStore.deleteComment(id)
    ElMessage.success('评论已删除')
    const res = await postStore.fetchComments(post.value.id)
    comments.value = res.data
  } catch (e) { /* ignore */ }
}

function editPost() {
  router.push(`/post/${post.value.id}/edit`)
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除这篇帖子吗？', '提示', { type: 'warning' })
    await postStore.deletePost(post.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (e) { /* ignore */ }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.post-detail { background: #fff; border-radius: 8px; padding: 24px; }
.post-header { border-bottom: 1px solid #eee; padding-bottom: 16px; margin-bottom: 16px; }
.post-header h1 { font-size: 24px; margin: 8px 0; }
.post-info { display: flex; gap: 12px; font-size: 13px; color: #999; align-items: center; }
.post-body { line-height: 1.8; padding: 16px 0; }
.post-actions { margin: 16px 0; display: flex; gap: 10px; }
.comments-section { margin-top: 32px; border-top: 1px solid #eee; padding-top: 20px; }
.comments-section h3 { margin-bottom: 16px; }
.comment-input { margin-bottom: 20px; }
.comment-login-hint { text-align: center; color: #999; padding: 20px 0; }
.pinned-tag { background: #f56c6c; color: #fff; font-size: 12px; padding: 2px 6px; border-radius: 3px; }
</style>
