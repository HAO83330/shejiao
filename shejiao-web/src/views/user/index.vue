<template>
  <div class="user-page">
    <div class="user">
      <div class="user-info">
        <div class="avatar">
          <div class="avatar-wrapper">
            <img :src="userInfo.avatar" class="user-image" style="border: 0.0625rem solid var(--border-color)" />
            <div class="img-edit">
              <el-upload
                v-show="uid === currentUid"
                class="upload-demo"
                :action="fileAction"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :headers="uploadHeader"
              >
                <button class="btn-avatr">更换</button>
              </el-upload>
            </div>
          </div>
        </div>
        <div class="info-part">
          <div class="info">
            <div class="basic-info">
              <div class="user-basic">
                <div class="user-nickname">
                  <div class="user-name" v-if="uid === currentUid">
                    <span v-if="!_isEditInfo"> {{ userInfo.username }}</span>
                    <el-input
                      v-else
                      v-model="userInfo.username"
                      style="width: 15rem"
                      maxlength="10"
                      placeholder="请输入你想要的名称吧！❤️"
                      show-word-limit
                      @keyup.enter="confirmUserInfo"
                      type="text"
                    />
                    <el-button
                      :icon="Edit"
                      v-show="!_isEditInfo"
                      @click="_isEditInfo = true"
                      circle
                      size="small"
                      style="margin-left: 0.3125rem"
                    />
                  </div>
                  <div class="user-name" v-else>
                    {{ userInfo.username }}
                  </div>
                </div>
                <div class="user-content">
                  <span class="user-redId">社交平台号：{{ userInfo.hsId }}</span
                  ><span class="user-IP"> IP属地：{{ userInfo.address }}</span>
                </div>
              </div>
            </div>
            <div class="user-desc">
              <div v-if="!_isEditInfo">
                <span v-if="userInfo.description === null">这个人什么都没有写～</span>
                <span v-else>{{ userInfo.description }}</span>
              </div>
              <el-input
                v-else
                v-model="userInfo.description"
                maxlength="250"
                placeholder="请对你自己进行简单的介绍吧！❤️"
                @keyup.enter="confirmUserInfo"
                show-word-limit
                :autosize="true"
                type="textarea"
              />
            </div>
            <div class="user-tags">
              <el-tag
                style="margin-left: 0.3125rem"
                v-for="tag in tagList"
                :key="tag"
                :closable="uid === currentUid"
                :disable-transitions="false"
                @close="handleClose(tag)"
                effect="light"
                type="info"
                round
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="inputVisible"
                ref="InputRef"
                v-model="inputTagValue"
                style="width: 3.125rem; margin-left: 0.3125rem"
                size="small"
                @keyup.enter="handleInputConfirm"
                @blur="handleInputConfirm"
              />
              <el-button
                style="margin-left: 0.3125rem"
                v-else
                class="button-new-tag"
                size="small"
                @click="showInput"
                round
                v-show="uid === currentUid"
              >
                +
              </el-button>
            </div>
            <div class="data-info">
              <div class="user-interactions">
                <div>
                  <span class="count">{{ userInfo.trendCount }}</span
                  ><span class="shows">作品</span>
                </div>
                <div>
                  <span class="count">{{ userInfo.followerCount }}</span
                  ><span class="shows">关注</span>
                </div>
                <div>
                  <span class="count">{{ userInfo.fanCount }}</span
                  ><span class="shows">粉丝</span>
                </div>
              </div>
            </div>
          </div>
          <div class="follow"></div>
        </div>
        <div class="tool-btn" v-show="uid !== currentUid">
          <el-button :icon="ChatLineRound" circle @click="chatShow = true" />
          <el-button type="primary" round v-if="_isFollow" @click="follow(uid, 1)">已关注</el-button>
          <el-button type="primary" round v-else @click="follow(uid, 0)">关注</el-button>
          <el-button class="back-button" round @click="goToMyPage">返回我的主页</el-button>
        </div>
      </div>
    </div>
    <div class="reds-sticky-box user-page-sticky" style="--1ee3a37c: all 0.4s cubic-bezier(0.2, 0, 0.25, 1) 0s">
      <div class="reds-sticky" style="">
        <div class="tertiary center reds-tabs-list" style="padding: 0rem 0.75rem">
          <div
            :class="type == 1 ? 'reds-tab-item active' : 'reds-tab-item'"
            style="padding: 0rem 1rem; margin-right: 0rem; font-size: 1rem"
          >
            <span @click="toPage(1)">笔记</span>
          </div>
          <div
            :class="type == 2 ? 'reds-tab-item active' : 'reds-tab-item'"
            style="padding: 0rem 1rem; margin-right: 0rem; font-size: 1rem"
          >
            <span @click="toPage(2)">点赞</span>
          </div>
          <div
            :class="type == 3 ? 'reds-tab-item active' : 'reds-tab-item'"
            style="padding: 0rem 1rem; margin-right: 0rem; font-size: 1rem"
          >
            <span @click="toPage(3)">收藏</span>
          </div>
          <div
            :class="type == 4 ? 'reds-tab-item active' : 'reds-tab-item'"
            style="padding: 0rem 1rem; margin-right: 0rem; font-size: 1rem"
          >
            <span @click="toPage(4)">关注</span>
          </div>
          <div
            :class="type == 5 ? 'reds-tab-item active' : 'reds-tab-item'"
            style="padding: 0rem 1rem; margin-right: 0rem; font-size: 1rem"
          >
            <span @click="toPage(5)">粉丝</span>
          </div>
          <div class="active-tag" style="width: 4rem; left: 39.1875rem"></div>
        </div>
      </div>
    </div>
    <div class="feeds-tab-container" style="--1ee3a37c: all 0.4s cubic-bezier(0.2, 0, 0.25, 1) 0s">
      <Chat
        v-if="chatShow"
        :acceptUid="uid"
        class="animate__animated animate__zoomIn animate__delay-0.5s"
        @click-chat="chatShow = false"
      >
      </Chat>
      <Note :type="type" v-if="type <= 3"> </Note>
      <div v-else-if="type === 4" class="follow-list">
        <div v-for="user in followList" :key="user.uid" class="follow-item">
          <div class="user-avatar" @click="goToUserPage(user.uid)">
            <img :src="user.avatar" alt="avatar" />
          </div>
          <div class="user-info" @click="goToUserPage(user.uid)">
            <div class="user-name">{{ user.username }}</div>
            <div class="user-hsId">社交平台号：{{ user.hsId }}</div>
          </div>
          <div class="follow-btn">
            <el-button type="primary" v-if="user.isFollow" round @click="follow(user.uid, 1)">已关注</el-button>
            <el-button type="primary" v-else round @click="follow(user.uid, 0)">关注</el-button>
          </div>
        </div>
        <div v-if="followList.length === 0" class="empty-list">
          <span>暂无关注用户</span>
        </div>
      </div>
      <div v-else-if="type === 5" class="fan-list">
        <div v-for="user in fanList" :key="user.uid" class="follow-item">
          <div class="user-avatar" @click="goToUserPage(user.uid)">
            <img :src="user.avatar" alt="avatar" />
          </div>
          <div class="user-info" @click="goToUserPage(user.uid)">
            <div class="user-name">{{ user.username }}</div>
            <div class="user-hsId">社交平台号：{{ user.hsId }}</div>
          </div>
          <div class="follow-btn">
            <el-button type="primary" v-if="user.isFollow" round @click="follow(user.uid, 1)">已关注</el-button>
            <el-button type="primary" v-else round @click="follow(user.uid, 0)">关注</el-button>
          </div>
        </div>
        <div v-if="fanList.length === 0" class="empty-list">
          <span>暂无粉丝</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ChatLineRound, Edit } from "@element-plus/icons-vue";
import { ref, nextTick, onMounted, watch } from "vue";
import { getUserById, updateUser } from "@/api/user";
import Note from "@/components/Note.vue";
import { useUserStore } from "@/store/userStore";
import Chat from "@/components/Chat.vue";
import { followById, isFollow, getFollowList, getFanList } from "@/api/follower";
import { useRoute, useRouter } from "vue-router";
import { ElInput, ElMessage, UploadProps } from "element-plus";
import { baseURL } from "@/constant/constant";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const uploadHeader = ref({
  accessToken: userStore.getToken(),
});
const currentUid = userStore.getUserInfo().id;
const userInfo = ref<any>({});
//const uid = history.state.uid;
const uid = route.query.uid as string || currentUid;
const type = ref(1);
const chatShow = ref(false);
const _isFollow = ref(false);
const _isEditInfo = ref(false);
const tagList = ref<string[]>([]);
const inputVisible = ref(false);
const InputRef = ref<InstanceType<typeof ElInput>>();
const inputTagValue = ref("");
const fileAction = baseURL + "web/oss/save/1";
const followList = ref<any[]>([]);
const fanList = ref<any[]>([]);

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    InputRef.value!.input!.focus();
  });
};

const handleClose = (tag: string) => {
  tagList.value.splice(tagList.value.indexOf(tag), 1);
  commonUpdateUser();
};

const handleInputConfirm = () => {
  if (inputTagValue.value) {
    tagList.value.push(inputTagValue.value);
    commonUpdateUser();
  }
  // _isClosable.value = false;
  inputVisible.value = false;
  inputTagValue.value = "";
};

const commonUpdateUser = () => {
  // 检查标签数量并处理
  if (tagList.value.length > 4) {
    tagList.value.splice(4);
    ElMessage.warning("最多支持4个标签!");
    return;
  }
  // 创建用户DTO对象并赋值
  let userDTO = {
    id: userInfo.value.id,
    avatar: userInfo.value.avatar,
    username: userInfo.value.username,
    description: userInfo.value.description,
    tags: JSON.stringify(tagList.value),
  };
  updateUser(userDTO)
    .then(() => {
      // 更新用户存储信息
      ElMessage.success("修改成功～");
      const user = userStore.getUserInfo();
      user.avatar = userInfo.value.avatar;
      userStore.setUserInfo(user);
    })
    .catch(() => {
      ElMessage.error("更新失败，请稍后再试!");
    });
};

const confirmUserInfo = () => {
  _isEditInfo.value = false;
  commonUpdateUser();
};

const toPage = (val: number) => {
  type.value = val;
  if (val === 4) {
    getFollowListData();
  } else if (val === 5) {
    getFanListData();
  }
};

const getFollowListData = () => {
  getFollowList(1, 100, uid).then((res) => {
    followList.value = res.data.records;
  });
};

const getFanListData = () => {
  getFanList(1, 100, uid).then((res) => {
    fanList.value = res.data.records;
  });
};

const goToUserPage = (userId: string) => {
  router.push({ path: '/user', query: { uid: userId } });
};

const goToMyPage = () => {
  router.push({ path: '/user', query: { uid: currentUid } });
};

const handleAvatarSuccess: UploadProps["onSuccess"] = (response) => {
  userInfo.value.avatar = response.data;
  commonUpdateUser();
};

const follow = (fid: string, type: number) => {
  followById(fid).then(() => {
    _isFollow.value = type == 0;
    // 更新关注列表和粉丝列表中的关注状态
    followList.value.forEach(user => {
      if (user.uid === fid) {
        user.isFollow = type == 0;
      }
    });
    fanList.value.forEach(user => {
      if (user.uid === fid) {
        user.isFollow = type == 0;
      }
    });
  });
};

const initData = () => {
  getUserById(uid).then((res) => {
    userInfo.value = res.data;
    if (res.data.tags != null) {
      tagList.value = JSON.parse(res.data.tags);
    }
  });
  isFollow(uid).then((res) => {
    _isFollow.value = res.data;
  });
};

initData();
</script>

<style lang="less" scoped>
:deep(.el-button:hover) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border-color);
}

:deep(.el-tag) {
  border: 0;
}

.follow-button {
  background-color: #3d8af5;
  border-color: #3d8af5;
  color: #fff;
}

.follow-button:hover {
  background-color: #2d7ae8;
  border-color: #2d7ae8;
}

.back-button {
  margin-left: 0.5rem;
  background-color: #f0f0f0;
  border-color: #f0f0f0;
  color: var(--text-primary);
  
  &:hover {
    background-color: #e0e0e0;
    border-color: #e0e0e0;
  }
}

.user-page {
  background: var(--bg-primary);
  height: 100vh;

  .user {
    padding-top: 4.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;

    .user-info {
      display: flex;
      justify-content: center;
      padding: 3rem 0;

      .avatar {
        .avatar-wrapper {
          text-align: center;
          width: 15.6667rem;
          height: 10.9667rem;

          .user-image {
            border-radius: 50%;
            margin: 0 auto;
            width: 70%;
            height: 100%;
            object-fit: cover;
          }

          .btn-avatr {
            border: 0.0625rem solid var(--border-color);
            width: 2.875rem;
            font-size: 0.75rem;
            height: 1.75rem;
            color: var(--text-primary);
            border-radius: 0.5rem;
          }
          .btn-avatr:hover {
            background-color: var(--bg-secondary);
            color: var(--text-primary);
          }
        }
      }

      .info-part {
        position: relative;
        width: 100%;

        .info {
          @media screen and (min-width: 108rem) {
            width: 33.3333rem;
          }

          margin-left: 2rem;

          .basic-info {
            display: flex;
            align-items: center;

            .user-basic {
              width: 100%;

              .user-nickname {
                width: 100%;
                display: flex;
                align-items: center;
                max-width: calc(100% - 6rem);

                .user-name {
                  font-weight: 600;
                  font-size: 1.5rem;
                  line-height: 120%;
                  color: var(--text-primary);
                }
              }

              .user-content {
                width: 100%;
                font-size: 0.75rem;
                line-height: 120%;
                color: var(--text-tertiary);
                display: flex;
                margin-top: 0.5rem;

                .user-redId {
                  padding-right: 0.75rem;
                }
              }
            }
          }

          .user-desc {
            width: 100%;
            font-size: 0.875rem;
            line-height: 140%;
            color: var(--text-primary);
            margin-top: 1rem;
            white-space: pre-line;
          }

          .user-tags {
            height: 1.5rem;
            margin-top: 1rem;
            display: flex;
            align-items: center;
            font-size: 0.75rem;
            color: var(--text-primary);
            text-align: center;
            font-weight: 400;
            line-height: 120%;

            .tag-item :first-child {
              padding: 0.1875rem 0.375rem;
            }

            .tag-item {
              display: flex;
              align-items: center;
              justify-content: center;
              padding: 0.25rem 0.5rem;
              grid-gap: 0.25rem;
              gap: 0.25rem;
              height: 1.125rem;
              border-radius: 2.5625rem;
              background: var(--bg-tertiary);
              height: 1.5rem;
              line-height: 1.5rem;
              margin-right: 0.375rem;
              color: var(--text-tertiary);
            }
            :hover {
              cursor: pointer; /* 显示小手指针 */
              transform: scale(1.15); /* 鼠标移入时按钮稍微放大 */
            }
          }

          .data-info {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-top: 1.25rem;

            .user-interactions {
              width: 100%;
              display: flex;
              align-items: center;

              .count {
                font-weight: 500;
                font-size: 0.875rem;
                margin-right: 0.25rem;
              }

              .shows {
                color: var(--text-tertiary);
                font-size: 0.875rem;
                line-height: 120%;
              }
            }

            .user-interactions > div {
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: center;
              text-align: center;
              margin-right: 1rem;
            }
          }
        }

        .follow {
          position: absolute;
          margin-left: auto;
          display: block;
          right: 0;
          top: 0;
        }
      }

      .tool-btn {
        position: absolute;
        top: 50%;
        right: 10%;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
    }
  }

  .reds-sticky {
    padding: 1rem 0;
    z-index: 5 !important;
    background: var(--bg-primary);

    .reds-tabs-list {
      @media screen and (min-width: 108rem) {
        width: 90.3333rem;
      }

      display: flex;
      flex-wrap: nowrap;
      position: relative;
      font-size: 1rem;
      justify-content: center;

      .reds-tab-item {
        padding: 0rem 1rem;
        margin-right: 0rem;
        font-size: 1rem;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        height: 2.5rem;
        cursor: pointer;
        color: var(--text-secondary);
        white-space: nowrap;
        transition: transform 0.3s cubic-bezier(0.2, 0, 0.25, 1);
        z-index: 1;
      }
      :hover {
        cursor: pointer; /* 显示小手指针 */
        transform: scale(1.15); /* 鼠标移入时按钮稍微放大 */
      }

      .reds-tab-item.active {
        background-color: var(--bg-tertiary);
        border-radius: 1.25rem;
        font-weight: 600;
        color: var(--text-secondary);
      }
    }
  }

  .feeds-tab-container {
    padding-left: 2rem;
  }

  .follow-list, .fan-list {
    padding: 2rem;
    
    .follow-item {
      display: flex;
      align-items: center;
      padding: 1rem;
      border-bottom: 1px solid var(--border-color);
      
      .user-avatar {
        width: 3rem;
        height: 3rem;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 1rem;
        cursor: pointer;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        &:hover {
          transform: scale(1.05);
          transition: transform 0.3s ease;
        }
      }
      
      .user-info {
        flex: 1;
        cursor: pointer;
        
        &:hover {
          color: #3d8af5;
        }
        
        .user-name {
          font-size: 1rem;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 0.25rem;
        }
        
        .user-hsId {
          font-size: 0.75rem;
          color: var(--text-tertiary);
        }
      }
      
      .follow-btn {
        
        .el-button {
          background-color: #3d8af5;
          border-color: #3d8af5;
          color: #fff;
          
          &:hover {
            background-color: #2d7ae8;
            border-color: #2d7ae8;
          }
        }
      }
    }
    
    .empty-list {
      text-align: center;
      padding: 4rem 0;
      color: var(--text-tertiary);
      font-size: 1rem;
    }
  }
}
</style>
