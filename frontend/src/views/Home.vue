<template>
  <div class="home-page">
    <!-- Search Bar -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索帖子..." clearable @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="categoryId" placeholder="全部分类" clearable style="width: 160px; margin-left: 10px;" @change="search">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-select v-model="sort" style="width: 120px; margin-left: 10px;" @change="search">
        <el-option label="最新" value="latest" />
        <el-option label="最热" value="hot" />
        <el-option label="热门" value="popular" />
      </el-select>
    </div>

    <!-- Post List -->
    <div v-loading="loading" class="post-list">
      <div v-if="posts.length === 0 && !loading" class="empty">暂无帖子</div>
      <div v-for="post in posts" :key="post.id" class="post-card" @click="goPost(post.id)">
        <div class="post-card-header">
          <span v-if="post.pinned" class="pinned-tag">置顶</span>
          <h3 class="post-title">{{ post.title }}</h3>
        </div>
        <p class="post-summary">{{ post.summary }}</p>
        <div class="post-meta">
          <span class="meta-item">
            <el-icon><User /></el-icon> {{ post.author?.nickname || post.author?.username }}
          </span>
          <span v-if="post.category" class="meta-item">
            <el-tag size="small">{{ post.category.name }}</el-tag>
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon> {{ post.viewCount }}
          </span>
          <span class="meta-item">
            <el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}
          </span>
          <span class="meta-item time">{{ formatTime(post.createdAt) }}</span>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePostStore } from '../stores/post'

const router = useRouter()
const postStore = usePostStore()

const loading = ref(false)
const posts = ref([])
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const sort = ref('latest')
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(async () => {
  try {
    const res = await postStore.fetchCategories()
    categories.value = res.data
  } catch (e) { /* ignore */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await postStore.fetchPosts({ page: page.value, size: size.value, keyword: keyword.value, categoryId: categoryId.value, sort: sort.value })
    posts.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ }
  loading.value = false
}

function search() {
  page.value = 1
  fetchData()
}

function goPost(id) {
  router.push(`/post/${id}`)
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.search-bar {
  display: flex;
  margin-bottom: 20px;
  gap: 8px;
}
.search-bar .el-input { flex: 1; min-width: 0; }

.post-list { min-height: 200px; }

.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.post-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.post-card:active { background: #fafafa; }

.pinned-tag {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
  margin-right: 8px;
  flex-shrink: 0;
}
.post-title { font-size: 18px; margin: 0; display: inline; }
.post-summary { color: #666; margin: 8px 0; line-height: 1.6; }
.post-meta { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #999; margin-top: 10px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 4px; white-space: nowrap; }
.time { margin-left: auto; }
.empty { text-align: center; color: #999; padding: 60px 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

@media (max-width: 640px) {
  .search-bar {
    flex-wrap: wrap;
  }
  .search-bar .el-input {
    flex: 1 1 100%;
  }
  .search-bar .el-select {
    flex: 1;
    min-width: 0;
  }
  .post-card { padding: 14px; }
  .post-title { font-size: 16px; }
  .post-summary {
    font-size: 14px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .post-meta { gap: 8px; font-size: 12px; }
  .time { margin-left: 0; width: 100%; }
}
</style>
