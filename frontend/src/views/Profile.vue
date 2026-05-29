<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card style="max-width: 600px;">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="auth.user?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatar" placeholder="头像图片链接" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { usePostStore } from '../stores/post'

const auth = useAuthStore()
const postStore = usePostStore()
const saving = ref(false)

const form = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

onMounted(async () => {
  try {
    await auth.fetchUser()
    form.nickname = auth.user?.nickname || ''
    form.email = auth.user?.email || ''
    form.phone = auth.user?.phone || ''
    form.avatar = auth.user?.avatar || ''
  } catch (e) { /* ignore */ }
})

async function save() {
  saving.value = true
  try {
    await postStore.updateProfile(form)
    await auth.fetchUser()
    ElMessage.success('保存成功')
  } catch (e) { /* ignore */ }
  saving.value = false
}
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
</style>
