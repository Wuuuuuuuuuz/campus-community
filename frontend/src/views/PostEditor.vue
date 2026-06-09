<template>
  <div class="post-editor">
    <h2>{{ isEdit ? '编辑帖子' : '发布帖子' }}</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="可选，留空自动生成" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="内容 (支持 Markdown)" prop="content">
        <div class="editor-wrap">
          <el-input v-model="form.content" type="textarea" :rows="15" placeholder="支持 Markdown 语法..." />
          <div class="preview-pane" v-html="previewHtml" v-if="form.content"></div>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { usePostStore } from '../stores/post'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

const form = reactive({
  title: '',
  content: '',
  summary: '',
  categoryId: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const previewHtml = computed(() => {
  if (!form.content) return ''
  return marked(form.content)
})

onMounted(async () => {
  try {
    const res = await postStore.fetchCategories()
    categories.value = res.data
  } catch (e) { /* ignore */ }

  if (isEdit.value) {
    try {
      const res = await postStore.fetchPost(route.params.id)
      const post = res.data
      form.title = post.title
      form.content = post.content
      form.summary = post.summary
      form.categoryId = post.category?.id || null
    } catch (e) {
      ElMessage.error('帖子不存在')
      router.push('/')
    }
  }
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await postStore.updatePost(route.params.id, form)
      ElMessage.success('更新成功')
      router.push(`/post/${route.params.id}`)
    } else {
      const res = await postStore.createPost(form)
      ElMessage.success('发布成功')
      router.push(`/post/${res.data.id}`)
    }
  } catch (e) {
    // error handled by interceptor
  }
  submitting.value = false
}
</script>

<style scoped>
.post-editor { background: #fff; border-radius: 8px; padding: 24px; max-width: 800px; margin: 0 auto; }
.editor-wrap { display: flex; gap: 16px; width: 100%; }
.editor-wrap .el-textarea { flex: 1; min-width: 0; }
.preview-pane { flex: 1; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; overflow-y: auto; max-height: 350px; line-height: 1.6; word-break: break-word; }

@media (max-width: 768px) {
  .post-editor { padding: 16px; border-radius: 0; }
  .editor-wrap { flex-direction: column; }
  .preview-pane { max-height: 250px; }
}
</style>
