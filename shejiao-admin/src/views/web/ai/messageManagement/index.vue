<template>
  <div class="app-container">
    <!-- 显示当前对话标题 -->
    <el-card class="mb-4">
      <template #header>
        <div class="card-header">
          <span v-if="chatTitle">当前对话：{{ chatTitle }}</span>
          <span v-else>所有对话消息</span>
          <div>
            <el-button v-if="chatTitle" type="primary" size="small" @click="viewAllMessages" style="margin-right: 8px;">
              查看所有对话
            </el-button>
            <el-button type="primary" size="small" @click="goBack">
              返回聊天列表
            </el-button>
          </div>
        </div>
      </template>
      <div class="card-body" v-if="chatTitle">
        <p>对话ID：{{ sessionId }}</p>
      </div>
    </el-card>
    
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="对话角色" prop="role">
        <el-select
          v-model="queryParams.role"
          placeholder="请选择对话角色"
          clearable
          style="width: 200px"
          @change="handleQuery"
        >
          <el-option label="用户" value="user" />
          <el-option label="助手" value="assistant" />
        </el-select>
      </el-form-item>
      <el-form-item label="消息状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择消息状态"
          clearable
          style="width: 200px"
          @change="handleQuery"
        >
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>
      </el-form-item>

      <el-form-item label="创建时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          查询
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">重置 </el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >
          批量删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Download"
          @click="handleExport"
        >
          导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList">
      </right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="messageList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="对话角色" align="center" prop="role">
        <template #default="scope">
          <el-tag v-if="scope.row.role === 'assistant'" type="info">ai助手</el-tag>
          <el-tag v-else type="warning">用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消息内容" align="center" prop="content">
        <template #default="scope">
          <el-popover
            placement="top"
            :width="800"
            trigger="hover"
          >
            <template #reference>
              <span class="content-ellipsis">{{ scope.row.content.length > 100 ? scope.row.content.substring(0, 100) + '...' : scope.row.content }}</span>
            </template>
            <div class="popover-content">
              {{ scope.row.content }}
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'success'" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模型版本" align="center" prop="model" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" min-width="80">
        <template #default="scope">
          <el-tooltip content="删除" placement="top">
            <el-button type="danger" icon="Delete" size="small" @click="handleDelete(scope.row)" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="MessageManagement">
import { ref, reactive, onMounted, getCurrentInstance, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import Pagination from "@/components/Pagination/index.vue";
import { parseTime } from "@/utils/ruoyi";
import RightToolbar from "@/components/RightToolbar/index.vue";
import { getMessageList, deleteMessage, batchDeleteMessage, exportMessageList } from "@/api/system/aiChat";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const messageList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dateRange = ref([]);
const sessionId = ref('');
const chatTitle = ref('');

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    role: undefined,
    status: undefined,
  },
});

const { queryParams } = toRefs(data);

// 监听路由参数变化
watch(() => route.query, (newQuery) => {
  if (newQuery.sessionId) {
    sessionId.value = newQuery.sessionId;
    chatTitle.value = newQuery.title || '';
    // 当有会话ID参数时，自动查询该会话的消息
    getList();
  }
}, { immediate: true });

/** 查询消息列表 */
function getList() {
  loading.value = true;
  getMessageList({
    currentPage: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    sessionId: sessionId.value,
    role: queryParams.value.role
  }).then(response => {
    // 处理后端返回的数据格式
    if (response.data && response.data.records) {
      // 如果返回的是data.records和data.total格式
      messageList.value = response.data.records;
      total.value = response.data.total;
    } else if (response.records) {
      // 如果返回的是records和total格式
      messageList.value = response.records;
      total.value = response.total;
    } else {
      // 如果返回的是其他格式
      messageList.value = [];
      total.value = 0;
    }
    loading.value = false;
  }).catch(error => {
    ElMessage.error('获取消息列表失败');
    loading.value = false;
  });
}

/** 批量删除按钮操作 */
function handleDelete(row) {
  const messageIds = row.id ? [row.id] : ids.value;
  proxy.$modal
    .confirm('是否确认删除消息编号为"' + messageIds + '"的数据项？')
    .then(function () {
      if (messageIds.length === 1) {
        return deleteMessage(messageIds[0]);
      } else {
        return batchDeleteMessage(messageIds);
      }
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 查看所有对话消息 */
function viewAllMessages() {
  // 清除会话ID和标题，重新获取所有消息
  sessionId.value = '';
  chatTitle.value = '';
  // 重新获取消息列表
  getList();
}

/** 返回聊天列表页面 */
function goBack() {
  router.push('/ai/chat-management');
}

/** 导出按钮操作 */
function handleExport() {
  exportMessageList({
    sessionId: sessionId.value,
    role: queryParams.value.role,
    status: queryParams.value.status
  }).then(response => {
    const blob = new Blob([response]);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `message_${new Date().getTime()}.xlsx`;
    document.body.appendChild(link);
    link.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
  }).catch(error => {
    ElMessage.error('导出失败');
  });
}

onMounted(() => {
  getList();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-body {
  padding: 10px 0;
}

.mb-4 {
  margin-bottom: 16px;
}

.content-ellipsis {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.popover-content {
  font-size: 15px;
  word-break: break-word;
  line-height: 1.4;
}

:deep(.el-popover) {
  border: 1px solid #000 !important;
  background-color: #fff !important;
  color: #000 !important;
  padding: 8px 12px !important;
  border-radius: 4px !important;
}
</style>