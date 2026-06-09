<template>
  <div class="categories-page">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showAdd = true">新增分类</el-button>
    </div>

    <el-table :data="categories" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="postCount" label="帖子数" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="editRow(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="editingId ? '编辑分类' : '新增分类'" v-model="showAdd" width="400px" @closed="resetForm">
      <el-form :model="catForm" ref="catFormRef" :rules="catRules">
        <el-form-item label="名称" prop="name">
          <el-input v-model="catForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="catForm.description" maxlength="200" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePostStore } from '../stores/post'

const postStore = usePostStore()

const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const showAdd = ref(false)
const editingId = ref(null)

const catForm = reactive({ name: '', description: '', sortOrder: 0 })
const catFormRef = ref(null)
const catRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await postStore.fetchCategories()
    categories.value = res.data
  } catch (e) { /* ignore */ }
  loading.value = false
}

function editRow(row) {
  editingId.value = row.id
  catForm.name = row.name
  catForm.description = row.description
  catForm.sortOrder = row.sortOrder
  showAdd.value = true
}

function resetForm() {
  editingId.value = null
  catForm.name = ''
  catForm.description = ''
  catForm.sortOrder = 0
}

async function save() {
  const valid = await catFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await postStore.updateCategory(editingId.value, catForm)
      ElMessage.success('更新成功')
    } else {
      await postStore.createCategory(catForm)
      ElMessage.success('创建成功')
    }
    showAdd.value = false
    fetchData()
  } catch (e) { /* ignore */ }
  saving.value = false
}

async function deleteRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
    await postStore.deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.categories-page { background: #fff; border-radius: 8px; padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; gap: 8px; }

@media (max-width: 768px) {
  .categories-page { padding: 12px; border-radius: 0; overflow-x: auto; }
  .page-header { flex-wrap: wrap; }
  .page-header h2 { font-size: 18px; }
}
</style>
