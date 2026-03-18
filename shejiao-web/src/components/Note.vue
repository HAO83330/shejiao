<template>
  <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
    <Waterfall
      :list="noteList"
      :width="options.width"
      :gutter="options.gutter"
      :hasAroundGutter="options.hasAroundGutter"
      :animation-effect="options.animationEffect"
      :animation-duration="options.animationDuration"
      :animation-delay="options.animationDelay"
      :breakpoints="options.breakpoints"
      style="min-width: 740px"
    >
      <template #item="{ item }">
        <el-skeleton style="width: 240px" :loading="!item.isLoading" animated>
          <template #template>
            <el-image
              :src="item.noteCover"
              :style="{
                width: '240px',
                maxHeight: '300px',
                height: item.noteCoverHeight + 'px',
                borderRadius: '8px',
              }"
              @load="handleLoad(item)"
            >
            </el-image>

            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 100%" />
              <div style="display: flex; align-items: center; margin-top: 2px; height: 16px">
                <el-skeleton style="--el-skeleton-circle-size: 20px">
                  <template #template>
                    <el-skeleton-item variant="circle" />
                  </template>
                </el-skeleton>
                <el-skeleton-item variant="text" style="margin-left: 10px" />
              </div>
            </div>
          </template>

          <template #default>
            <div class="card" style="max-width: 240px">
              <div class="image-container" @click="toMain(item.id)">
                <el-image
                  :src="item.noteCover"
                  :style="{
                    width: '240px',
                    maxHeight: '300px',
                    height: item.noteCoverHeight + 'px',
                    borderRadius: '8px',
                  }"
                  fit="cover"
                >
                </el-image>
                <div v-if="item.auditStatus === '0'" class="overlay">审核中</div>
                <div v-if="item.auditStatus === '2'" class="overlay not-passed" @click.stop="showRejectReason(item)">未通过⚠️</div>
              </div>
              <div class="footer">
                <a class="title">
                  <span>{{ item.title }}</span>
                </a>
                <div class="author-wrapper">
                  <a class="author">
                    <img class="author-avatar" :src="item.avatar" />
                    <span class="name">{{ item.username }}</span>
                  </a>
                  <span class="like-wrapper like-active" @click="likeOrCollection(item, 1)">
                    <i
                      class="iconfont icon-follow-fill"
                      :style="{ width: '1em', height: '1em', color: item.isLike ? 'red' : 'rgba(51, 51, 51, 0.6)' }"
                      v-if="item.isLike"
                    >
                    </i>
                    <i class="iconfont icon-follow" style="width: '1em', height: '1em', color: 'rgba(51, 51, 51, 0.6)'" v-else></i>
                    <span class="count">{{ item.likeCount }}</span>
                  </span>
                </div>
              </div>
              <div class="top-tag-area" v-show="type === 1 && item.pinned === '1'">
                <div class="top-wrapper">置顶</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </template>
    </Waterfall>
  </div>

  <Main
    v-show="mainShow"
    :nid="nid"
    :nowTime="new Date()"
    class="animate__animated animate__zoomIn animate__delay-0.5s"
    @click-main="close"
  ></Main>

  <!-- 驳回理由对话框 -->
  <el-dialog
    v-model="rejectDialogVisible"
    title="驳回理由"
    width="500px"
    append-to-body
  >
    <div class="reject-reason-content">
      <el-alert
        title="该笔记未通过审核"
        type="error"
        :closable="false"
        show-icon
      />
      <el-divider />
      <div class="reason-text">
        <el-text type="info" v-if="currentRejectReason">
          {{ currentRejectReason }}
        </el-text>
        <el-text type="info" v-else>
          未描述驳回理由
        </el-text>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="rejectDialogVisible = false">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ref, onMounted, watch } from "vue";
import { getTrendByUser } from "@/api/user";
import { likeOrCollectionByDTO } from "@/api/likeOrCollection";
import type { LikeOrCollectionDTO } from "@/type/likeOrCollection";
import Main from "@/views/main/main.vue";
import { options } from "@/constant/constant";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { ElMessage } from "element-plus";
const route = useRoute();

const props = defineProps({
  type: {
    type: Number,
    default: 1,
  },
});

watch(
  () => [props.type],
  ([newType]) => {
    currentPage.value = 1;
    noteList.value = [] as Array<any>;
    getNoteList(newType);
  }
);

const noteList = ref<Array<any>>([]);
const noteTotal = ref(0);
const uid = route.query.uid as string;
const currentPage = ref(1);
const pageSize = 10;
const nid = ref("");
const mainShow = ref(false);
const userStore = useUserStore();
const currentUid = userStore.getUserInfo().id;
const rejectDialogVisible = ref(false);
const currentRejectReason = ref("");

const handleLoad = (item: any) => {
  item.isLoading = true;
};

const close = (nid?: string, likeOrComment?: any) => {
  mainShow.value = false;
  if (nid && likeOrComment) {
    // 更新对应笔记的点赞状态和数量
    const noteIndex = noteList.value.findIndex(item => item.id === nid);
    if (noteIndex !== -1) {
      noteList.value[noteIndex].isLike = likeOrComment.isLike;
      // 根据点赞状态更新点赞数量
      if (likeOrComment.isLike) {
        noteList.value[noteIndex].likeCount += 1;
      } else {
        noteList.value[noteIndex].likeCount -= 1;
      }
    }
  }
};

const toMain = (noteId: string) => {
  // router.push({ name: "main", state: { nid: nid } });
  nid.value = noteId;
  mainShow.value = true;
};

const setData = (res: any) => {
  const { records, total } = res.data;
  noteTotal.value = total;
  // 过滤掉不是当前用户且状态"审核中"或"未通过"的记录
  // 但允许当前用户查看自己的审核中笔记
  const filteredRecords = records.filter((item: any) => {
    return item.uid === currentUid || (item.auditStatus !== "0" && item.auditStatus !== "2");
  });
  noteList.value.push(...filteredRecords);
};

const getNoteList = (type: number) => {
  getTrendByUser(currentPage.value, pageSize, uid, type).then((res) => {
    setData(res);
  });
};

const loadMoreData = () => {
  currentPage.value += 1;
  getNoteList(props.type);
};

const noLoginNotice = () => {
  if (!userStore.isLogin()) {
    ElMessage.warning("用户未登录");
    return false;
  }
  return true;
};

const likeOrCollection = (item: any, type: number) => {
  const _login = noLoginNotice();
  if (!_login) {
    return;
  }
  const likeOrCollectionDTO = {} as LikeOrCollectionDTO;
  likeOrCollectionDTO.likeOrCollectionId = item.id;
  likeOrCollectionDTO.publishUid = item.uid;
  likeOrCollectionDTO.type = type == 1 ? 1 : 3;
  
  // 切换当前状态
  const currentStatus = type == 1 ? item.isLike : item.isCollection;
  const newStatus = !currentStatus;
  const changeVal = newStatus ? 1 : -1;
  
  likeOrCollectionByDTO(likeOrCollectionDTO).then(() => {
    if (type == 1) {
      item.isLike = newStatus;
      item.likeCount += changeVal;
    } else {
      item.isCollection = newStatus;
      item.collectionCount += changeVal;
    }
  });
};

const showRejectReason = (item: any) => {
  currentRejectReason.value = item.rejectReason || "";
  rejectDialogVisible.value = true;
};

const initData = () => {
  getNoteList(1);
};

onMounted(() => {
  initData();
});
</script>

<style lang="less" scoped>
.image-container {
  position: relative;
  display: inline-block;
}
.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5); /* 半透明背景 */
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 20px;
}
.overlay.not-passed {
  color: red; /* 设置未通过状态的字体颜色为红色 */
  cursor: pointer; /* 显示可点击状态 */
}
.reject-reason-content {
  padding: 10px 0;
  
  .reason-text {
    margin-top: 15px;
    font-size: 14px;
    line-height: 1.6;
    max-height: 300px;
    overflow-y: auto;
  }
}
.feeds-container {
  position: relative;
  transition: width 0.5s;
  margin: 0 auto;

  .noteImg {
    width: 240px;
    max-height: 300px;
    object-fit: cover;
    border-radius: 8px;
  }

  .card {
    position: relative;

    .top-tag-area {
      position: absolute;
      left: 12px;
      top: 12px;
      z-index: 4;

      .top-wrapper {
        background: #409EFF;
        border-radius: 999px;
        font-weight: 500;
        color: #fff;
        line-height: 120%;
        font-size: 12px;
        padding: 5px 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .footer {
    padding: 12px;

    .title {
      margin-bottom: 8px;
      word-break: break-all;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
      font-weight: 500;
      font-size: 14px;
      line-height: 140%;
      color: #333;
    }

    .author-wrapper {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 20px;
      color: rgba(51, 51, 51, 0.8);
      font-size: 12px;
      transition: color 1s;

      .author {
        display: flex;
        align-items: center;
        color: inherit;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 12px;

        .author-avatar {
          margin-right: 6px;
          width: 20px;
          height: 20px;
          border-radius: 20px;
          border: 1px solid rgba(0, 0, 0, 0.08);
          flex-shrink: 0;
          object-fit: cover;
        }

        .name {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .like-wrapper {
        position: relative;
        cursor: pointer;
        display: flex;
        align-items: center;

        .count {
          margin-left: 2px;
        }
      }
    }
  }
}
</style>
