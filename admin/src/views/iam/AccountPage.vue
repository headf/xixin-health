<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>账号管理</h2>
      <el-button type="primary" @click="handleCreate">新增账号</el-button>
    </div>

    <el-table :data="allData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="phone" label="登录账号" min-width="120" />
      <el-table-column label="所属分院" width="110">
        <template #default="{ row }">
          {{ branchMap[row.branchId] || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="displayName" label="名称" min-width="80" />
      <el-table-column prop="email" label="邮箱" min-width="100" show-overflow-tooltip />
      <el-table-column label="角色">
        <template #default="{ row }">
          <div style="display:flex;gap:4px;white-space:nowrap">
            <el-tag
              v-for="role in (row.roleCodes || [])"
              :key="role"
              size="small"
              style="flex-shrink:0;border-radius:6px"
            >
              {{ role }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="105" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            @change="handleToggle(row)"
            :loading="togglingId === row.id"
            size="small"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)" style="margin-left:4px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[15, 30, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑账号' : '新增账号'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="登录账号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="所属分院">
          <el-select v-model="form.branchId" placeholder="选择分院" clearable style="width:100%">
            <el-option v-for="b in branches" :key="b.id" :label="b.branchName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!editing">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-select v-model="form.roleCodes" multiple style="width:100%" placeholder="选择角色">
            <el-option
              v-for="r in allRoles"
              :key="r.code"
              :label="r.name"
              :value="r.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAccounts, createAccount, updateAccount, toggleAccount, deleteAccount, getRoles, getBranches } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const allData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const togglingId = ref(null)
const allRoles = ref([])
const branches = ref([])
const branchMap = computed(() => {
  const map = {}
  branches.value.forEach(b => { map[b.id] = b.branchName })
  return map
})
const formRef = ref(null)
const form = ref({
  phone: '',
  displayName: '',
  email: '',
  password: '',
  roleCodes: [],
  branchId: null
})

const formRules = computed(() => ({
  phone: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '账号格式不正确', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' }
  ],
  password: [
    { required: !editing.value, message: '请输入密码', trigger: 'blur' }
  ]
}))

async function load() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (auth.currentBranchId) params.branchId = auth.currentBranchId
    const res = await getAccounts(params)
    if (Array.isArray(res.data)) {
      allData.value = res.data
      total.value = res.data.length
    } else {
      allData.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function loadRoles() {
  try {
    const res = await getRoles()
    allRoles.value = res.data || []
  } catch { ElMessage.error('加载角色失败') }
}

async function loadBranches() {
  try {
    const res = await getBranches()
    branches.value = res.data || []
  } catch { /* ignore */ }
}

function handlePageChange(p) {
  currentPage.value = p
  load()
}

function handleSizeChange(s) {
  pageSize.value = s
  currentPage.value = 1
  load()
}

function handleCreate() {
  editing.value = null
  form.value = { phone: '', displayName: '', email: '', password: '', roleCodes: [], branchId: null }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleEdit(row) {
  editing.value = row
  form.value = {
    phone: row.phone || '',
    displayName: row.displayName || '',
    email: row.email || '',
    password: '',
    roleCodes: row.roleCodes || [],
    branchId: row.branchId || null
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const payload = { ...form.value }
    if (editing.value && !payload.password) {
      delete payload.password
    }
    if (editing.value) {
      await updateAccount(editing.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createAccount(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await load()
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

async function handleToggle(row) {
  togglingId.value = row.id
  try {
    await toggleAccount(row.id, !row.enabled)
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}


async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该账号吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteAccount(row.id)
    ElMessage.success('已删除')
    await load()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => { load(); loadRoles(); loadBranches() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
