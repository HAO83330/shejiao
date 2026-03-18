<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="用户名称" prop="username">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="聊天标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入聊天标题"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
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
      :data="chatList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="聊天标题" align="center" prop="title" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" min-width="120">
        <template #default="scope">
          <el-tooltip content="查看对话内容" placement="top">
            <el-button type="primary" icon="View" size="small" @click="handleView(scope.row)" />
          </el-tooltip>
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

<script setup name="ChatManagement">
import { ref, reactive, onMounted, getCurrentInstance } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import Pagination from "@/components/Pagination/index.vue";
import { parseTime } from "@/utils/ruoyi";
import RightToolbar from "@/components/RightToolbar/index.vue";
import { getList as getChatList, deleteChat, batchDeleteChat, getSessionMessages, exportChatList } from "@/api/system/aiChat";

const { proxy } = getCurrentInstance();
const router = useRouter();

const chatList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dateRange = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    username: undefined,
    title: undefined,
  },
});

const { queryParams } = toRefs(data);

/** 查询聊天列表 */
function getList() {
  loading.value = true;
  getChatList({
    currentPage: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    username: queryParams.value.username,
    title: queryParams.value.title
  }).then(response => {
    // 处理后端返回的数据格式
    if (response.rows) {
      // 如果返回的是rows和total格式
      chatList.value = response.rows;
      total.value = response.total;
    } else if (response.data && response.data.records) {
      // 如果返回的是data.records和data.total格式
      chatList.value = response.data.records;
      total.value = response.data.total;
    } else {
      // 如果返回的是其他格式
      chatList.value = [];
      total.value = 0;
    }
    loading.value = false;
  }).catch(error => {
    ElMessage.error('获取聊天列表失败');
    loading.value = false;
  });
}

/** 查看按钮操作 */
function handleView(row) {
  // 跳转到消息管理页面，并传递会话ID作为参数
  router.push({
    path: '/ai/message-management',
    query: {
      sessionId: row.id,
      title: row.title
    }
  });
}

/** 批量删除按钮操作 */
function handleDelete(row) {
  const chatIds = row.id ? [row.id] : ids.value;
  proxy.$modal
    .confirm('是否确认删除聊天记录编号为"' + chatIds + '"的数据项？')
    .then(function () {
      if (chatIds.length === 1) {
        return deleteChat(chatIds[0]);
      } else {
        return batchDeleteChat(chatIds);
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

/** 导出按钮操作 */
function handleExport() {
  exportChatList({
    username: queryParams.value.username,
    title: queryParams.value.title
  }).then(response => {
    const blob = new Blob([response]);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `chat_${new Date().getTime()}.xlsx`;
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
</style>