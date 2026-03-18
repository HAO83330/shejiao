<template>
  <div class="feeds-page" :class="{ 'dark-mode': isDark }" :style="{
    backgroundColor: isDark ? '#1a1a1a' : '#ffffff'
  }">
    <div class="channel-container" :style="{
      backgroundColor: isDark ? '#1a1a1a' : '#ffffff',
      borderBottom: `1px solid ${isDark ? '#3d3d3d' : '#e0e0e0'}`
    }">
      <div class="scroll-container channel-scroll-container" :style="{
        backgroundColor: isDark ? '#1a1a1a' : '#ffffff'
      }">
        <div class="content-container" :style="{
          color: isDark ? '#ffffff' : '#333333'
        }">
          <div :class="categoryClass == '0' ? 'channel active' : 'channel'" @click="getNoteList" :style="{
            color: categoryClass == '0' ? (isDark ? '#ffffff' : '#333333') : (isDark ? '#cccccc' : '#666666'),
            backgroundColor: categoryClass == '0' ? (isDark ? '#2d2d2d' : '#f0f0f0') : 'transparent'
          }">推荐</div>
          <div
            :class="categoryClass == item.id ? 'channel active' : 'channel'"
            v-for="item in categoryList"
            :key="item.id"
            @click="getNoteListByCategory(item.id)"
            :style="{
              color: categoryClass == item.id ? (isDark ? '#ffffff' : '#333333') : (isDark ? '#cccccc' : '#666666'),
              backgroundColor: categoryClass == item.id ? (isDark ? '#2d2d2d' : '#f0f0f0') : 'transparent'
            }"
          >
            {{ item.title }}
          </div>
        </div>
      </div>
    </div>
    <div class="loading-container"></div>
    <div class="feeds-container" v-infinite-scroll="loadMoreData" :infinite-scroll-distance="50">
      <div class="feeds-loading-top animate__animated animate__zoomIn animate__delay-0.5s" v-show="topLoading">
        <Loading style="width: 1.2em; height: 1.2em"></Loading>
      </div>
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
              ></el-image>
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
                <el-image
                  :src="item.noteCover"
                  :style="{
                    width: '240px',
                    maxHeight: '300px',
                    height: item.noteCoverHeight + 'px',
                    borderRadius: '8px',
                  }"
                  fit="cover"
                  @click="toMain(item.id)"
                ></el-image>
                <div class="footer">
                  <a class="title" :style="{
                    color: isDark ? '#ffffff' : '#333333'
                  }">
                    <span>{{ item.title }}</span>
                  </a>
                  <div class="author-wrapper" :style="{
                    color: isDark ? '#cccccc' : '#666666'
                  }">
                    <a class="author" :style="{
                      color: isDark ? '#cccccc' : '#666666'
                    }">
                      <img class="author-avatar" :src="item.avatar" :style="{
                        border: `1px solid ${isDark ? '#3d3d3d' : '#e0e0e0'}`
                      }" />
                      <span class="name" :style="{
                        color: isDark ? '#cccccc' : '#666666'
                      }">{{ item.username }}</span>
                    </a>
                    <span class="like-wrapper like-active" @click="likeOrCollection(item, 1)" :style="{
                      color: isDark ? '#cccccc' : '#666666'
                    }">
                    <i
                      class="iconfont icon-follow-fill"
                      :style="{ width: '1em', height: '1em', color: 'red' }"
                      v-if="item.isLike"
                    >
                    </i>
                    <i class="iconfont icon-follow" :style="{ width: '1em', height: '1em', color: isDark ? '#cccccc' : 'rgba(51, 51, 51, 0.6)' }" v-else></i>
                    <span class="count" :style="{
                      color: isDark ? '#cccccc' : '#666666'
                    }">{{ item.likeCount }}</span>
                  </span>
                  </div>
                </div>
              </div>
            </template>
          </el-skeleton>
        </template>
      </Waterfall>

      <div class="feeds-loading" v-show="!isEnd">
        <Loading style="width: 1.2em; height: 1.2em"></Loading>
      </div>
      <div class="feeds-end" v-show="isEnd">······ 已经到底了 ·····</div>
    </div>
    <FloatingBtn @click-refresh="refresh"></FloatingBtn>
    <Main
      v-show="mainShow"
      :nid="nid"
      :nowTime="new Date()"
      class="animate__animated animate__zoomIn animate__delay-0.5s"
      @click-main="close"
    ></Main>
  </div>
</template>

<script lang="ts" setup>
import { Waterfall } from "vue-waterfall-plugin-next";
import "vue-waterfall-plugin-next/dist/style.css";
import { ref, watch } from "vue";
import { getRecommendNote, getNoteByDTO, addRecord, getRecommendNoteBatch } from "@/api/search";
import { getCategoryTreeData } from "@/api/category";
import { likeOrCollectionByDTO } from "@/api/likeOrCollection";
import type { NoteDTO, NoteSearch } from "@/type/note";
import type { Category } from "@/type/category";
import type { LikeOrCollectionDTO } from "@/type/likeOrCollection";
import Main from "@/views/main/main.vue";
import FloatingBtn from "@/components/FloatingBtn.vue";
import { options } from "@/constant/constant";
import { useSearchStore } from "@/store/searchStore";
import { useUserStore } from "@/store/userStore";
import { useThemeStore } from "@/store/themeStore";
import Loading from "@/components/Loading.vue";
import { refreshTab } from "@/utils/util";
import { ElMessage } from "element-plus";
const searchStore = useSearchStore();
const userStore = useUserStore();
const themeStore = useThemeStore();
const topLoading = ref(false);
const noteList = ref<Array<NoteSearch>>([]);
const categoryList = ref<Array<Category>>([]);
const currentPage = ref(1);
const pageSize = 20;
const noteTotal = ref(0);
const categoryClass = ref("0");
const mainShow = ref(false);
const nid = ref("");
const isEnd = ref(false);
const isDark = ref(false);
const noteDTO = ref<NoteDTO>({
  keyword: "",
  type: 0,
  cid: "",
  cpid: "",
});

// 推荐数据缓存
const recommendCache = ref<Array<NoteSearch>>([]);
const cacheIndex = ref(0);
const BATCH_SIZE = 100; // 每次批量获取100条数据

const noLoginNotice = () => {
  if (!userStore.isLogin()) {
    ElMessage.warning("用户未登录");
    return false;
  }
  return true;
};

watch(
  () => [searchStore.seed],
  () => {
    noteDTO.value.keyword = searchStore.keyWord;
    noteDTO.value.cpid = "";
    categoryClass.value = "0";
    getNoteListByKeyword();
    addRecord(searchStore.keyWord);
  }
);

watch(
  () => themeStore.isDark,
  (newVal) => {
    isDark.value = newVal;
  }
);

const toMain = (noteId: string) => {
  // router.push({ name: "main", state: { nid: nid } });
  nid.value = noteId;
  mainShow.value = true;
};

const close = (nid: string, val: any) => {
  // 从详情页返回时，重新请求该笔记的最新数据
  if (nid) {
    import('@/api/note').then(({ getNoteById }) => {
      getNoteById(nid).then((res: any) => {
        const index = noteList.value.findIndex((item) => item.id === nid);
        if (index !== -1) {
          // 更新笔记的点赞数量和状态
          noteList.value[index].likeCount = res.data.likeCount;
          noteList.value[index].isLike = res.data.isLike;
          noteList.value[index].commentCount = res.data.commentCount;
          noteList.value[index].isCollection = res.data.isCollection;
          noteList.value[index].collectionCount = res.data.collectionCount;
        }
      });
    });
  }
  mainShow.value = false;
};

const handleLoad = (item: any) => {
  item.isLoading = true;
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
  
  // 先进行乐观更新，提升用户体验
  const oldIsLike = item.isLike;
  const oldIsCollection = item.isCollection;
  const oldLikeCount = item.likeCount;
  const oldCollectionCount = item.collectionCount;
  
  if (type == 1) {
    item.isLike = !item.isLike;
    item.likeCount += item.isLike ? 1 : -1;
  } else {
    item.isCollection = !item.isCollection;
    item.collectionCount += item.isCollection ? 1 : -1;
  }
  
  likeOrCollectionByDTO(likeOrCollectionDTO).then(() => {
    // 点赞成功后，重新请求该笔记的最新数据，确保数据准确性
    import('@/api/note').then(({ getNoteById }) => {
      getNoteById(item.id).then((res: any) => {
        const index = noteList.value.findIndex((note) => note.id === item.id);
        if (index !== -1) {
          // 更新为后端返回的最新数据
          noteList.value[index].likeCount = res.data.likeCount;
          noteList.value[index].isLike = res.data.isLike;
          noteList.value[index].collectionCount = res.data.collectionCount;
          noteList.value[index].isCollection = res.data.isCollection;
        }
      }).catch(() => {
        // 请求失败，回滚到原来的状态
        if (type == 1) {
          item.isLike = oldIsLike;
          item.likeCount = oldLikeCount;
        } else {
          item.isCollection = oldIsCollection;
          item.collectionCount = oldCollectionCount;
        }
      });
    });
  }).catch(() => {
    // 点赞失败，回滚到原来的状态
    if (type == 1) {
      item.isLike = oldIsLike;
      item.likeCount = oldLikeCount;
    } else {
      item.isCollection = oldIsCollection;
      item.collectionCount = oldCollectionCount;
    }
  });
};

const refresh = async () => {
  // 使用回调函数优化代码
  refreshTab(async () => {
    topLoading.value = true;
    console.log("刷新推荐数据");
    
    // 清空缓存和列表
    recommendCache.value = [];
    cacheIndex.value = 0;
    noteList.value = [];
    currentPage.value = 1;
    isEnd.value = false;
    
    // 如果是推荐页面，批量获取100条数据
    if (noteDTO.value.cpid === "" && noteDTO.value.keyword === "") {
      try {
        const res: any = await getRecommendNoteBatch();
        recommendCache.value = res.data || [];
        cacheIndex.value = 0;
        
        // 如果没有数据，标记为结束
        if (recommendCache.value.length === 0) {
          isEnd.value = true;
          topLoading.value = false;
          return;
        }
        
        // 从缓存中取出前20条
        const newNotes = recommendCache.value.slice(cacheIndex.value, cacheIndex.value + pageSize);
        cacheIndex.value += newNotes.length;
        noteList.value.push(...newNotes);
      } catch (error) {
        console.error("批量获取推荐数据失败:", error);
        isEnd.value = true;
      }
    } else {
      // 非推荐页面，使用原来的分页逻辑
      getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
        setData(res);
      });
    }
    
    topLoading.value = false;
  });
};

const loadMoreData = async () => {
  if (noteDTO.value.cpid !== "" || noteDTO.value.keyword !== "") {
    // 非推荐页面，使用原来的分页逻辑
    if (noteList.value.length >= noteTotal.value) {
      isEnd.value = true;
      return;
    }
    currentPage.value += 1;
    getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
      setData(res);
    });
    return;
  }

  // 推荐页面，使用缓存机制
  if (recommendCache.value.length === 0) {
    // 缓存为空，批量获取100条数据
    topLoading.value = true;
    try {
      const res: any = await getRecommendNoteBatch();
      recommendCache.value = res.data || [];
      cacheIndex.value = 0;
      topLoading.value = false;
      
      // 如果没有数据，标记为结束
      if (recommendCache.value.length === 0) {
        isEnd.value = true;
        return;
      }
      
      // 从缓存中取出前20条
      const newNotes = recommendCache.value.slice(cacheIndex.value, cacheIndex.value + pageSize);
      cacheIndex.value += newNotes.length;
      noteList.value.push(...newNotes);
    } catch (error) {
      console.error("批量获取推荐数据失败:", error);
      topLoading.value = false;
      isEnd.value = true;
    }
  } else {
    // 缓存不为空，从缓存中取出数据
    const remainingCache = recommendCache.value.slice(cacheIndex.value);
    if (remainingCache.length === 0) {
      // 缓存用完了，重新批量获取新的100条数据
      topLoading.value = true;
      try {
        const res: any = await getRecommendNoteBatch();
        recommendCache.value = res.data || [];
        cacheIndex.value = 0;
        topLoading.value = false;
        
        // 如果没有新数据，标记为结束
        if (recommendCache.value.length === 0) {
          isEnd.value = true;
          return;
        }
        
        // 从新缓存中取出前20条
        const newNotes = recommendCache.value.slice(cacheIndex.value, cacheIndex.value + pageSize);
        cacheIndex.value += newNotes.length;
        noteList.value.push(...newNotes);
      } catch (error) {
        console.error("批量获取推荐数据失败:", error);
        topLoading.value = false;
        isEnd.value = true;
      }
    } else {
      // 从缓存中取出数据
      const newNotes = remainingCache.slice(0, pageSize);
      cacheIndex.value += newNotes.length;
      noteList.value.push(...newNotes);
    }
  }
};

const setData = (res: any) => {
  const { records, total } = res.data;
  noteTotal.value = total;
  if (records.length === 0) {
    isEnd.value = true;
  } else {
    noteList.value.push(...records);
  }
};

const getNoteList = async () => {
  categoryClass.value = "0";
  noteDTO.value.cpid = "";
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  
  // 清空缓存
  recommendCache.value = [];
  cacheIndex.value = 0;
  
  // 批量获取100条推荐数据
  topLoading.value = true;
  try {
    const res: any = await getRecommendNoteBatch();
    recommendCache.value = res.data || [];
    cacheIndex.value = 0;
    topLoading.value = false;
    
    // 如果没有数据，标记为结束
    if (recommendCache.value.length === 0) {
      isEnd.value = true;
      return;
    }
    
    // 从缓存中取出前20条
    const newNotes = recommendCache.value.slice(cacheIndex.value, cacheIndex.value + pageSize);
    cacheIndex.value += newNotes.length;
    noteList.value.push(...newNotes);
  } catch (error) {
    console.error("批量获取推荐数据失败:", error);
    topLoading.value = false;
    isEnd.value = true;
  }
};

const getNoteListByCategory = (id: string) => {
  categoryClass.value = id;
  noteDTO.value.cpid = id;
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

const getNoteListByKeyword = () => {
  noteList.value = [] as Array<any>;
  currentPage.value = 1;
  isEnd.value = false;
  getNoteByDTO(currentPage.value, pageSize, noteDTO.value).then((res) => {
    setData(res);
  });
};

const getCategoryData = () => {
  getCategoryTreeData().then((res: any) => {
    categoryList.value = res.data;
  });
};

const initData = () => {
  getCategoryData();
  getNoteList();
  themeStore.initTheme();
  isDark.value = themeStore.isDark;
};

initData();
</script>

<style lang="less" scoped>
.dark-mode {
  transition: background-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .channel-scroll-container {
  background-color: var(--bg-primary) !important;
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .content-container {
  color: var(--text-secondary);
  transition: color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .active {
  background: var(--bg-hover) !important;
  color: var(--text-primary) !important;
  box-shadow: 0 2px 8px var(--shadow-color);
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1), color 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .channel {
  color: var(--text-secondary);
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .channel:hover {
  color: var(--text-primary);
  background-color: var(--bg-tertiary);
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1), background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .footer .title {
  color: var(--text-primary);
  transition: color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .author-wrapper {
  color: var(--text-secondary);
  transition: color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .author {
  color: var(--text-secondary);
  transition: color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .author-avatar {
  border-color: var(--border-color);
  transition: border-color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .back-top {
  background: var(--bg-secondary) !important;
  border-color: var(--border-color) !important;
  color: var(--text-secondary) !important;
  box-shadow: 0 2px 8px var(--shadow-color);
  transition: background-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), color 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .back-top:hover {
  background: var(--bg-tertiary) !important;
  color: var(--text-primary) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.dark-mode .reload {
  background: var(--bg-secondary) !important;
  border-color: var(--border-color) !important;
  box-shadow: 0 2px 8px var(--shadow-color) !important;
  color: var(--text-secondary) !important;
  transition: background-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), color 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .reload:hover {
  background: var(--bg-tertiary) !important;
  color: var(--text-primary) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.dark-mode .feeds-end {
  color: var(--text-tertiary);
  transition: color 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.feeds-page {
    flex: 1;
    padding: 0 24px;
    padding-top: 72px;
    height: 100vh;

    .channel-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      user-select: none;
      -webkit-user-select: none;

      .channel-scroll-container {
        backdrop-filter: blur(20px);
        background-color: transparent;
        width: calc(100vw - 24px);

        position: relative;
        overflow: hidden;
        display: flex;
        user-select: none;
        -webkit-user-select: none;
        align-items: center;
        font-size: 16px;
        color: var(--text-secondary);
        height: 40px;
        white-space: nowrap;
        height: 72px;

        .content-container::-webkit-scrollbar {
          display: none;
        }

        .content-container {
          display: flex;
          overflow-x: scroll;
          overflow-y: hidden;
          white-space: nowrap;
          color: var(--text-secondary);

          .active {
            font-weight: 600;
            background: var(--bg-tertiary);
            border-radius: 999px;
            color: var(--text-primary);
          }

          .channel {
            height: 40px;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 0 16px;
            cursor: pointer;
            -webkit-user-select: none;
            user-select: none;
          }
          :hover {
            cursor: pointer; /* 显示小手指针 */
            transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
          }
        }
      }
    }

    .feeds-container {
      position: relative;
      transition: width 0.5s;
      margin: 0 auto;

      .feeds-loading {
        margin: 3vh;
        text-align: center;
      }

      .feeds-loading-top {
        text-align: center;
        line-height: 6vh;
        height: 6vh;
      }

      .noteImg {
        width: 240px;
        max-height: 300px;
        object-fit: cover;
        border-radius: 8px;
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
          color: var(--text-primary);
        }

        .author-wrapper {
          display: flex;
          align-items: center;
          justify-content: space-between;
          height: 20px;
          color: var(--text-secondary);
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
              border: 1px solid var(--border-color);
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

    .floating-btn-sets {
      position: fixed;
      display: flex;
      flex-direction: column;
      width: 40px;
      grid-gap: 8px;
      gap: 8px;
      right: 24px;
      bottom: 24px;

      .back-top {
        width: 40px;
        height: 40px;
        background: var(--bg-secondary);
        border: 1px solid var(--border-color);
        border-radius: 100px;
        color: var(--text-secondary);
        display: flex;
        align-items: center;
        justify-content: center;
        // transition: background 0.2s;
        cursor: pointer;
      }

      .reload {
        width: 40px;
        height: 40px;
        background: var(--bg-secondary);
        border: 1px solid var(--border-color);
        box-shadow:
          0 2px 8px 0 var(--shadow-color),
          0 1px 2px 0 rgba(0, 0, 0, 0.02);
        border-radius: 100px;
        color: var(--text-secondary);
        display: flex;
        align-items: center;
        justify-content: center;
        //transition: background 0.2s;
        cursor: pointer;
      }
    }
    .feeds-end {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 50px;
      color: var(--text-tertiary);
      font-size: 16px;
      margin-top: 20px;
      border-radius: 10px;
    }
  }
</style>
