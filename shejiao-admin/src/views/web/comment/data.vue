<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['web:comment:remove']"
        >
          删除
        </el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button type="warning" plain icon="Close" @click="handleClose">
          关闭
        </el-button>
      </el-col>
    </el-row>

    <el-table
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="id" />
      <el-table-column label="头像" align="center" prop="avatar">
        <template #default="scope">
          <el-avatar :size="60" :src="scope.row.avatar"> </el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="评论人" align="center" prop="username" />
      <el-table-column label="被评论人" align="center" prop="pushUsername" />
      <el-table-column label="评论内容" align="center" prop="content" />
      <el-table-column label="评论等级" align="center" prop="level">
        <template #default="scope">
          <el-tag :type="getTagType(scope.row.level)">
            {{ getLevelText(scope.row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="评论时间"
        align="center"
        prop="createTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        width="80"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['web:comment:remove']"
          >
            删除
          </el-button>
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

<script setup name="Data">
import { treeListComment, delComment } from "@/api/web/comment";

const { proxy } = getCurrentInstance();
const dateRange = ref([]);

const dataList = ref([]);
const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const route = useRoute();

const levelMap = {
  1: "一级评论",
  2: "二级评论",
  3: "三级评论",
  4: "四级评论",
  5: "五级评论",
};

// 根据 level 值返回对应的颜色
const getTagType = (level) => {
  switch (level) {
    case 1:
      return "success"; // 一级评论，绿色
    case 2:
      return "primary"; // 二级评论，蓝色
    case 3:
      return "warning"; // 三级评论，黄色
    case 4:
      return "danger"; // 四级评论，红色
    case 5:
      return "info"; // 五级评论，蓝色（或者可以用其他颜色）
    default:
      return "default"; // 默认颜色
  }
};

// 根据 level 返回对应的文本
const getLevelText = (level) => {
  return levelMap[level] || "其他评论";
};

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    nid: undefined
  },
  rules: {}
});

const { queryParams } = toRefs(data);

/** 查询评论数据列表 */
function getList() {
  loading.value = true;
  queryParams.value.nid = route.params.nid;
  treeListComment(queryParams.value).then(
    (response) => {
      dataList.value = response.rows || [];
      total.value = response.total || 0;
      loading.value = false;
    }
  );
}

/** 返回按钮操作 */
function handleClose() {
  proxy.$tab.closePage();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 删除按钮操作 */
function handleDelete(row) {
  const commentIds = row.id || ids.value;
  proxy.$modal
    .confirm('是否确认删除评论编号为"' + commentIds + '"的数据项？')
    .then(function () {
      return delComment(commentIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

getList();
</script>
