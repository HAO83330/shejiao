<template>
  <div class="chat-overlay" @click="close">
    <div
      class="container"
      style="transition: background-color 0.4s ease 0s;
  hsla(0,0%,100%,0.98)"
      @click.stop
    >
    <div class="chat-container">
      <header class="chat-header">
        <div class="header-left"></div>
        <div class="header-user">
          <el-avatar :src="acceptUser.avatar" />
          <span style="margin-left: 0.3125rem">{{ acceptUser.username }}</span>
        </div>
        <div class="header-tool">
          <More class="icon-item"></More>
        </div>
      </header>
      <hr color="#f4f4f4" />
      <main class="chat-main">
        <div class="chat-record" ref="ChatRef" @scroll="showScroll()">
          <div v-for="(item, index) in dataList" :key="index">
            <div class="message-my-item" v-if="item.acceptUid === acceptUser.id">
              <Loading v-show="item.isLoading" style="width: 0.8em; height: 0.8em; margin-right: 0.5rem" />
              <div class="message-my-wrapper">
                <div class="message-my-conent" v-if="item.msgType == 1">{{ item.content }}</div>
                <img :src="item.content" class="message-img" v-if="item.msgType == 2" @click="previewImage(item.content)" />
                <div class="message-time">{{ formateTime(item.timestamp) }}</div>
              </div>
              <div class="user-avatar">
                <el-avatar :src="currentUser.avatar" />
              </div>
            </div>

            <div class="message-item" v-else>
              <div class="user-avatar">
                <el-avatar :src="acceptUser.avatar" />
              </div>
              <div class="message-wrapper">
                <div class="message-conent" v-if="item.msgType == 1">{{ item.content }}</div>
                <img :src="item.content" class="message-img" v-if="item.msgType == 2" @click="previewImage(item.content)" />
                <div class="message-time">{{ formateTime(item.timestamp) }}</div>
              </div>
            </div>
          </div>
        </div>
        <hr color="#f4f4f4" />
        <div class="chat-input">
          <div class="input-tool">
            <div class="tool-left">
              <div class="emoji-icon" @click="toggleEmojiPanel">
                <Sunny class="icon-item"></Sunny>
              </div>
              <el-upload :auto-upload="false" :show-file-list="false" :on-change="handleChange">
                <Picture class="icon-item"></Picture>
              </el-upload>
            </div>
            
            <!-- 表情选择面板 -->
            <div v-if="showEmojiPanel" class="emoji-panel">
              <div class="emoji-grid">
                <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="selectEmoji(emoji)">
                  {{ emoji }}
                </span>
              </div>
            </div>
            <div class="tool-history">
              <Clock class="icon-item"></Clock>
            </div>
          </div>

          <!-- <textarea type="textarea" v-model="content" class="input-content" rows="15" @keyup.enter="submit" /> -->
          <div class="input-content">
            <!-- 图片预览区域 -->
            <div v-if="pendingImage" class="image-preview-area">
              <div class="image-preview-wrapper">
                <img :src="pendingImage.url" class="preview-image" />
                <div class="image-actions">
                  <span class="image-name">{{ pendingImage.name }}</span>
                  <el-button type="danger" link size="small" @click="removePendingImage">
                    <Close style="width: 1em; height: 1em;" />
                  </el-button>
                </div>
              </div>
            </div>
            <el-input
              v-model="inputMessage"
              type="textarea"
              :autosize="{ minRows: pendingImage ? 3 : 6, maxRows: 8 }"
              placeholder="请输入消息，支持发送图片哦～"
              resize="none"
              @keyup.enter.prevent="submit"
              class="post-content"
            />
            <div class="input-btn">
              <div></div>
              <el-button type="primary" round @click="submit" :disabled="!inputMessage && !pendingImage">
                {{ pendingImage && inputMessage ? '发送' : (pendingImage ? '发送图片' : '发送') }}
              </el-button>
            </div>
          </div>
        </div>
      </main>
    </div>
    <div class="close-cricle" @click="close">
      <div class="close close-mask-white">
        <Close style="width: 1.2em; height: 1.2em; color: rgba(51, 51, 51, 0.8)" />
      </div>
    </div>

    <el-dialog
      v-if="previewVisible"
      v-model="previewVisible"
      title="图片预览"
      width="80%"
      center
      :show-close="true"
      append-to-body
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :modal="true"
      :lock-scroll="true"
      :z-index="10000"
    >
      <img :src="previewImageUrl" style="width: 100%; height: auto; display: block; margin: 0 auto;" />
      <template #footer>
        <el-button type="primary" @click="downloadImage">下载图片</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { More, Picture, Clock, Close, Loading, Sunny } from "@element-plus/icons-vue";
import { ref, onMounted, watch, nextTick } from "vue";
import { getUserById } from "@/api/user";
import { getAllChatRecord, sendMsg } from "@/api/im";
import { useUserStore } from "@/store/userStore";
import { useImStore } from "@/store/imStore";
import type { UploadProps } from "element-plus";
import { convertImgToBase64 } from "@/utils/util";
import { getRandomString } from "@/utils/util";
import { formateTime } from "@/utils/util";

const imStore = useImStore();
const userStore = useUserStore();
const props = defineProps({
  acceptUid: {
    type: String,
    default: "",
  },
});

const ChatRef = ref();
const currentUser = ref<any>({});
const acceptUser = ref<any>({});
const dataList = ref<any>();
const currentPage = ref(1);
const pageSize = 15;
const messageTotal = ref(0);
const postContent = ref(null);
const previewVisible = ref(false);
const previewImageUrl = ref("");
const inputMessage = ref("");
const showEmojiPanel = ref(false);
const pendingImage = ref<{ url: string; base64: string; name: string } | null>(null);
const emojis = ref([
  "😊", "😂", "😍", "🤔", "😢", "😎", "🤣", "😅",
  "😡", "🤗", "🤩", "😴", "😷", "🤒", "🤓", "😇",
  "😘", "🤙", "👍", "👎", "👌", "✌️", "🤞", "🙏",
  "🎉", "🎂", "🎁", "🎈", "🎊", "🎯", "🎨", "🎵",
  "🔥", "💯", "✨", "🌟", "⭐", "💫", "💥", "💢",
  "💔", "💖", "💗", "💓", "💙", "💚", "💛", "💜",
  "😋", "😏", "😣", "😖", "😫", "😤", "😠", "😑",
  "😶", "😐", "😯", "😦", "😧", "😮", "😲", "😵"
]);

watch(
  () => imStore.message,
  (newVal) => {
    if (newVal.sendUid === acceptUser.value.id) {
      insertMessage(newVal);
    }
  },
  {
    deep: true,
  }
);

const insertMessage = async (message: any) => {
  dataList.value?.push(message);
  await nextTick();
  // 滚动到最底部
  ChatRef.value.lastElementChild.scrollIntoView({
    block: "start",
    behavior: "smooth",
  });
};

const emit = defineEmits(["clickChat"]);

const close = () => {
  emit("clickChat", props.acceptUid);
};

const previewImage = (imageUrl: string) => {
  previewImageUrl.value = imageUrl;
  previewVisible.value = true;
};

const downloadImage = () => {
  const link = document.createElement("a");
  link.href = previewImageUrl.value;
  link.download = `image_${Date.now()}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 切换表情面板显示/隐藏
const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value;
};

// 选择表情
const selectEmoji = (emoji: string) => {
  inputMessage.value += emoji;
  showEmojiPanel.value = false;
};

// 选择图片
const handleChange: UploadProps["onChange"] = (uploadFile) => {
  const imgSrc = URL.createObjectURL(uploadFile.raw!);
  convertImgToBase64(
    uploadFile.raw!,
    (data: any) => {
      // 保存图片到待发送区域
      pendingImage.value = {
        url: imgSrc,
        base64: data,
        name: uploadFile.name || '图片'
      };
    },
    (error: any) => {
      console.log("error", error);
    }
  );
};

// 移除待发送图片
const removePendingImage = () => {
  pendingImage.value = null;
};

const sendMessage = (message: any) => {
  new Promise((res) => {
    insertMessage(message);
    res(message);
  }).then((_message: any) => {
    sendMsg(_message).then(() => {
      const data = dataList.value.filter((item: any) => item.id === _message.id);
      data[0].isLoading = false;
    });
  });
};

const submit = () => {
  let content = inputMessage.value;
  const hasImage = !!pendingImage.value;
  const hasText = content && content.trim() !== "";

  // 如果既没有图片也没有文字，不发送
  if (!hasImage && !hasText) {
    return;
  }

  // 如果有待发送图片，先发送图片
  if (hasImage) {
    const imageMessage = {} as any;
    imageMessage.id = getRandomString(12);
    imageMessage.sendUid = currentUser.value.id;
    imageMessage.acceptUid = acceptUser.value.id;
    imageMessage.content = pendingImage.value!.base64;
    imageMessage.msgType = 2; // 2表示图片消息
    imageMessage.chatType = 0;
    imageMessage.timestamp = Date.now();
    imageMessage.isLoading = true;
    sendMessage(imageMessage);

    // 清空待发送图片
    pendingImage.value = null;
  }

  // 如果有文字内容，再发送文字
  if (hasText) {
    const textMessage = {} as any;
    textMessage.id = getRandomString(12);
    textMessage.sendUid = currentUser.value.id;
    textMessage.acceptUid = acceptUser.value.id;
    textMessage.content = content;
    textMessage.msgType = 1; // 1表示文字消息
    textMessage.chatType = 0;
    textMessage.timestamp = Date.now();
    textMessage.isLoading = true;
    sendMessage(textMessage);

    // 清空输入框
    inputMessage.value = "";
  }
};

const showScroll = () => {
  const topval = ChatRef.value.scrollTop;
  if (topval === 0) {
    loadMoreData();
  }
};

const loadMoreData = () => {
  currentPage.value++;
  getAllChatRecordMethod();
};

const getAllChatRecordMethod = () => {
  getAllChatRecord(currentPage.value, pageSize, props.acceptUid).then((res) => {
    const { records, total } = res.data;
    messageTotal.value = total;
    records.forEach((item: any) => {
      dataList.value?.splice(0, 0, item);
    });
    if (dataList.value.length >= total) {
      ChatRef.value.scrollTop = 0;
    } else {
      ChatRef.value.scrollTop += ChatRef.value.clientHeight;
    }
  });
};

onMounted(async () => {
  currentUser.value = userStore.getUserInfo();
  getUserById(props.acceptUid).then((res) => {
    acceptUser.value = res.data;
  });
  dataList.value = [];
  getAllChatRecord(currentPage.value, pageSize, props.acceptUid).then(async (res) => {
    const { records, total } = res.data;
    messageTotal.value = total;
    records.forEach((item: any) => {
      dataList.value.splice(0, 0, item);
    });
    await nextTick();
    // 滚动到最底部
    ChatRef.value.lastElementChild.scrollIntoView({
      block: "start",
      behavior: "smooth",
    });
  });
});
</script>
<style lang="less" scoped>
.chat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.icon-item {
  width: 1.2em;
  height: 1.2em;
  margin-right: 0.3125rem;
  color: rgba(51, 51, 51, 0.8);
  cursor: pointer;
}

.emoji-icon {
  cursor: pointer;
}

.emoji-panel {
  position: absolute;
  bottom: 100%;
  left: 0.3125rem;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 8px;
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 1.5em;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background-color: #f0f0f0;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px 20px;
}

.container {
  width: 90vw;
  height: 90vh;
  z-index: 20;
  overflow: auto;
  display: flex;

  .chat-container {
    width: 65%;
    margin: auto;
    height: 90%;
    min-width: 50rem;
    transition:
      transform 0.4s ease 0s,
      width 0.4s ease 0s;
    transform: translate(0, 0) scale(1);
    overflow: visible;
    box-shadow:
      0 8px 64px 0 rgba(0, 0, 0, 0.04),
      0 1px 4px 0 rgba(0, 0, 0, 0.02);
    border-radius: 20px;
    background: #f8f8f8;
    transform-origin: left top;
    z-index: 100;

    .chat-header {
      height: 3.75rem;
      width: 100%;
      background: #f8f8f8;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-user {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .chat-main {
      height: 100%;

      .message-img {
        width: 15rem;
        height: 18.75rem;
        object-fit: cover;
        margin: 0 0.3125rem;
        border-radius: 0.5rem;
        cursor: pointer;
        transition: transform 0.2s;
      }

      .message-img:hover {
        transform: scale(1.02);
      }

      .chat-record {
        height: 60%;
        padding: 0 1.25rem;
        overflow-y: scroll;

        .message-item {
          display: flex;
          justify-content: left;
          align-items: center;
          margin: 1.25rem 0;

          .message-wrapper {
            display: flex;
            flex-direction: column;
            align-items: flex-start;

            .message-conent {
              margin-left: 0.3125rem;
              padding: 0.25rem 0.625rem;
              border: 0.0625rem solid #e0e8ff;
              background-color: #fff;
              border-radius: 0.5rem;
              font-size: 1rem;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
              transition: all 0.3s ease;

              &:hover {
                box-shadow: 0 4px 12px rgba(58, 100, 255, 0.15);
              }
            }

            .message-time {
              margin-left: 0.3125rem;
              margin-top: 0.25rem;
              font-size: 0.75rem;
              color: rgba(51, 51, 51, 0.5);
            }
          }
        }

        .message-my-item {
          display: flex;
          justify-content: right;
          align-items: center;
          margin: 1.25rem 0;

          .message-my-wrapper {
            display: flex;
            flex-direction: column;
            align-items: flex-end;

            .message-my-conent {
              margin-right: 0.3125rem;
              padding: 0.25rem 0.625rem;
              color: #fff;
              background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
              border-radius: 0.5rem;
              font-size: 1rem;
              box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
              transition: all 0.3s ease;

              &:hover {
                box-shadow: 0 6px 20px rgba(58, 100, 255, 0.4);
              }
            }

            .message-time {
              margin-right: 0.3125rem;
              margin-top: 0.25rem;
              font-size: 0.75rem;
              color: rgba(51, 51, 51, 0.5);
            }
          }
        }
      }

      .chat-input {
        height: 25%;

        .input-tool {
          display: flex;
          justify-content: space-between;
          height: 1.25rem;
          padding: 0 0.3125rem;
          position: relative;

          .tool-left {
            display: flex;
            justify-content: left;
          }
        }

        .input-content {
          width: 100%;
          height: 90%;
          resize: none;
          border: 0rem;
          outline: none;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          padding: 0 0.625rem;

          .image-preview-area {
            padding: 0.625rem 0;
            border-bottom: 1px solid #f0f0f0;
            margin-bottom: 0.625rem;

            .image-preview-wrapper {
              display: flex;
              align-items: center;
              background: #f8f8f8;
              padding: 0.5rem;
              border-radius: 8px;
              width: fit-content;

              .preview-image {
                width: 3.75rem;
                height: 3.75rem;
                object-fit: cover;
                border-radius: 4px;
                margin-right: 0.625rem;
              }

              .image-actions {
                display: flex;
                align-items: center;
                gap: 0.5rem;

                .image-name {
                  font-size: 0.875rem;
                  color: #666;
                  max-width: 150px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }
              }
            }
          }

          :deep(.el-textarea) {
            margin-bottom: 1.25rem;

            &__inner {
              border-radius: 10px;
              padding: 12px 16px;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
              transition: all 0.3s ease;
              font-size: 14px;
              line-height: 1.6;
              resize: none;
              border: 2px solid #f0f0f0;

              &:hover {
                box-shadow: 0 4px 12px rgba(58, 100, 255, 0.15);
              }

              &:focus {
                box-shadow: 0 0 0 2px rgba(58, 100, 255, 0.2);
                border-color: #3a64ff;
              }
            }
          }

          .input-btn {
            display: flex;
            justify-content: flex-end;
            padding: 0;

            .el-button {
              width: 110px;
              height: 40px;
              font-size: 16px;
              display: inline-block;
              cursor: pointer;
              transition: all 0.3s ease;
              font-weight: 600;
              border-radius: 24px;
              background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
              color: #fff;
              border: none;
              box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);

              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(58, 100, 255, 0.4);
                background: linear-gradient(135deg, #5a84ff 0%, #3a64ff 100%);
              }

              &:active {
                transform: translateY(0);
              }
            }
          }
        }
      }
    }
  }

  .close-cricle {
    right: 4vw;
    top: 1.3vw;
    position: fixed;
    display: flex;
    z-index: 100;
    cursor: pointer;

    .close-mask-white {
      box-shadow:
        0 0.125rem 0.5rem 0 rgba(0, 0, 0, 0.04),
        0 0.0625rem 0.125rem 0 rgba(0, 0, 0, 0.02);
      border: 0.0625rem solid rgba(0, 0, 0, 0.08);
    }

    .close {
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 100%;
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 2.5rem;
      cursor: pointer;
      transition: all 0.3s;
      background-color: #fff;
    }
    :hover {
      cursor: pointer; /* 显示小手指针 */
      transform: scale(1.2); /* 鼠标移入时按钮稍微放大 */
    }
  }
}
</style>
