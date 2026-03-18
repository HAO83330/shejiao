<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入标题"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="作者" prop="uid">
        <el-input
          v-model="queryParams.uid"
          placeholder="请输入作者"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="对话状态"
          @change="handleQuery"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in chat_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
          搜索
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">重置 </el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['web:aiChat:add']"
        >
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['web:aiChat:edit']"
        >
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['web:aiChat:remove']"
        >
          删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['web:aiChat:export']"
        >
          导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" icon="refresh" @click="refreshAiChat">
          ES重置
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList">
      </right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="aiChatList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="id" />
      <el-table-column label="标题" align="center" prop="title" />
      <el-table-column label="作者" align="center" prop="author" />
      <el-table-column label="对话内容" align="center" prop="content" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="chat_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="浏览次数" align="center" prop="viewCount" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="更新时间"
        align="center"
        prop="updateTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column
        label="操作"
        align="center"
        fixed="right"
        min-width="200"
      >
        <template #default="scope">
          <el-tooltip content="编辑" placement="top">
            <el-button
              type="primary"
              icon="Edit"
              size="small"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['web:aiChat:edit']"
            />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
              type="danger"
              icon="Delete"
              size="small"
              @click="handleDelete(scope.row)"
              v-hasPermi="['web:aiChat:remove']"
            />
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

    <!-- 添加或修改AI对话对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="aiChatRef" :model="form" :rules="rules" label-width="80px">
        <el-col :span="13">
          <el-form-item label="对话标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入对话标题" />
          </el-form-item>
        </el-col>
        <el-form-item label="对话内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入对话内容"
          />
        </el-form-item>
        <el-form-item label="对话状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in chat_status"
              :key="dict.value"
              :label="dict.value"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiChat">
import {
  listAiChat,
  addAiChat,
  delAiChat,
  getAiChat,
  updateAiChat,
  refreshAiChatDate,
} from "@/api/web/aiChat";
import { ElMessageBox, ElMessage } from "element-plus";

const { proxy } = getCurrentInstance();
const { chat_status } = proxy.useDict("chat_status");

const route = useRoute();
const aiChatList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    status: undefined,
  },
  rules: {
    title: [{ required: true, message: "对话标题不能为空", trigger: "blur" }],
    content: [{ required: true, message: "对话内容不能为空", trigger: "blur" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

/** ES重置 */
function refreshAiChat() {
  ElMessageBox.confirm("是否确认重置ES数据？", "提示", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      refreshAiChatDate().then((response) => {
        ElMessage.success("重置成功");
        getList();
      });
    })
    .catch(() => {
      ElMessage.info("已取消重置");
    });
}

/** 查询AI对话列表 */
function getList() {
  loading.value = true;
  listAiChat(proxy.addDateRange(queryParams.value, dateRange.value)).then(
    (response) => {
      aiChatList.value = response.rows;
      total.value = response.total;
      loading.value = false;
    }
  );
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    title: undefined,
    content: undefined,
    status: "0",
  };
  proxy.resetForm("aiChatRef");
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加AI对话";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const aiChatId = row.id || ids.value;
  getAiChat(aiChatId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "修改AI对话";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["aiChatRef"].validate((valid) => {
    if (valid) {
      if (form.value.id != undefined) {
        updateAiChat(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addAiChat(form.value).then((response) => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const aiChatIds = row.id || ids.value;
  proxy.$modal
    .confirm('是否确认删除AI对话编号为"' + aiChatIds + '"的数据项？')
    .then(function () {
      return delAiChat(aiChatIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    "system/post/export",
    {
      ...queryParams.value,
    },
    `post_${new Date().getTime()}.xlsx`
  );
}

onMounted(() => {
  getList();
});
</script>

<style scoped>
</style>
