<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleCreate">新增角色</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="code" label="编码" min-width="80" />
      <el-table-column prop="name" label="名称" min-width="100" />
      <el-table-column prop="roleType" label="类型" min-width="80" />
      <el-table-column label="内置" width="72" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.systemBuiltIn" type="warning" size="small" style="border-radius:6px">内置</el-tag>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="权限">
        <template #default="{ row }">
          <div style="display:flex;gap:4px;white-space:nowrap">
            <el-tag
              v-for="perm in (row.permissions || [])"
              :key="perm"
              size="small"
              style="flex-shrink:0;border-radius:6px"
            >
              {{ perm }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)" style="margin-left:4px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
      <div style="display:flex;justify-content:center;margin-top:12px">
        <el-pagination
          v-if="data.length > 10"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="data.length"
          :page-sizes="[10,20,50,100]"
          layout="total, sizes, prev, pager, next"
          small
        />
      </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑角色' : '新增角色'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色类型">
          <el-select v-model="form.roleType" style="width:100%" placeholder="选择角色类型">
            <el-option label="系统角色" value="SYSTEM" />
            <el-option label="业务角色" value="BUSINESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限编码">
          <el-select v-model="form.permissionCodes" multiple style="width:100%" placeholder="选择权限">
            <el-option label="全部权限" value="ALL" />
            <el-option label="只读权限" value="READ" />
            <el-option label="写入权限" value="WRITE" />
            <el-option label="管理权限" value="ADMIN" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole } from '@/api'

const data = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => data.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const formRef = ref(null)
const form = ref({
  code: '',
  name: '',
  roleType: '',
  permissionCodes: []
})

const formRules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ]
}

async function load() {
  loading.value = true
  try {
    const res = await getRoles()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleCreate() {
  editing.value = null
  form.value = { code: '', name: '', roleType: '', permissionCodes: [] }
  dialogVisible.value = true
}

function handleEdit(row) {
  editing.value = row
  form.value = {
    code: row.code || '',
    name: row.name || '',
    roleType: row.roleType || '',
    permissionCodes: row.permissions || []
  }
  dialogVisible.value = true
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
    if (editing.value) {
      await updateRole(editing.value.code, form.value)
      ElMessage.success('更新成功')
    } else {
      await createRole(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await load()
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}


async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该角色吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteRole(row.code)
    ElMessage.success('已删除')
    await load()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => load())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
