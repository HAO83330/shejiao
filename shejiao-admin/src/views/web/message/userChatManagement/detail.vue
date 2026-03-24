<template>
  <div class="app-container">
    <!-- 显示对话标题 -->
    <el-card class="mb-4">
      <template #header>
        <div class="card-header">
          <span>对话详情</span>
          <el-button type="primary" size="small" @click="goBack">
            返回列表
          </el-button>
        </div>
      </template>
      <div class="card-body">
        <p>发送方：{{ senderName || senderId }} <span style="font-size: 12px; color: #999;">ID: {{ senderId }}</span></p>
        <p>接收方：{{ receiverName || receiverId }} <span style="font-size: 12px; color: #999;">ID: {{ receiverId }}</span></p>
      </div>
    </el-card>
    
    <!-- 聊天记录 -->
    <div class="chat-container">
      <div v-for="message in messageList" :key="message.id" class="chat-message" :class="message.sendUid === senderId ? 'sent' : 'received'">
        <div class="message-content">
          <div class="message-header">
            <span class="sender">
              {{ message.sendUid === senderId ? (senderName || senderId) : (receiverName || receiverId) }}
              <span style="font-size: 10px; color: #999; margin-left: 5px;">
                ID: {{ message.sendUid }}
              </span>
            </span>
            <span class="time">{{ parseTime(message.createTime) }}</span>
          </div>
          <div class="message-body">
            <!-- 显示文字内容 -->
            <div v-if="message.msgType === 1" class="text-content">
              {{ message.content }}
            </div>
            <!-- 显示图片内容 -->
            <div v-else-if="message.msgType === 2" class="image-content">
              <el-image
                :src="message.content"
                fit="cover"
                style="width: 200px; height: 200px"
                preview-src-list="[message.content]"
              />
            </div>
            <!-- 显示其他类型内容 -->
            <div v-else class="other-content">
              {{ message.content }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="UserChatDetail">
import { ref, onMounted, getCurrentInstance } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { parseTime } from "@/utils/ruoyi";
import { getUserChatList } from "@/api/web/chat";
import { getUserById } from "@/api/web/user";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const messageList = ref([]);
const loading = ref(true);
const senderId = ref('');
const receiverId = ref('');
const userMap = ref({}); // 用户信息缓存，key为用户ID，value为用户名
const senderName = ref('');
const receiverName = ref('');

// 监听路由参数变化
onMounted(async () => {
  senderId.value = route.query.senderId;
  receiverId.value = route.query.receiverId;
  await getChatMessages();
  await loadUserInfo();
});

/** 获取聊天消息 */
async function getChatMessages() {
  loading.value = true;
  try {
    const response = await getUserChatList({
      currentPage: 1,
      pageSize: 1000, // 获取足够多的消息
      senderId: senderId.value,
      receiverId: receiverId.value
    });
    
    // 处理后端返回的数据格式
    if (response.data && response.data.records) {
      // 反转数组，使消息按照时间顺序显示（从最早到最新）
      messageList.value = response.data.records.reverse();
    } else if (response.records) {
      // 反转数组，使消息按照时间顺序显示（从最早到最新）
      messageList.value = response.records.reverse();
    } else if (response.rows) {
      // 反转数组，使消息按照时间顺序显示（从最早到最新）
      messageList.value = response.rows.reverse();
    } else {
      messageList.value = [];
    }
    loading.value = false;
  } catch (error) {
    ElMessage.error('获取聊天消息失败');
    loading.value = false;
  }
}

/** 加载用户信息 */
async function loadUserInfo() {
  // 获取发送方和接收方的用户信息
  if (senderId.value) {
    try {
      const response = await getUserById(senderId.value);
      if (response.data && response.data.data) {
        // 处理Result格式的数据
        userMap.value[senderId.value] = response.data.data.username || senderId.value;
        senderName.value = response.data.data.username || senderId.value;
      } else if (response.data) {
        // 处理直接返回用户对象的情况
        userMap.value[senderId.value] = response.data.username || senderId.value;
        senderName.value = response.data.username || senderId.value;
      } else {
        userMap.value[senderId.value] = senderId.value;
        senderName.value = senderId.value;
      }
    } catch (error) {
      // 当用户不存在时，显示用户ID
      userMap.value[senderId.value] = senderId.value;
      senderName.value = senderId.value;
    }
  }
  
  if (receiverId.value) {
    try {
      const response = await getUserById(receiverId.value);
      if (response.data && response.data.data) {
        // 处理Result格式的数据
        userMap.value[receiverId.value] = response.data.data.username || receiverId.value;
        receiverName.value = response.data.data.username || receiverId.value;
      } else if (response.data) {
        // 处理直接返回用户对象的情况
        userMap.value[receiverId.value] = response.data.username || receiverId.value;
        receiverName.value = response.data.username || receiverId.value;
      } else {
        userMap.value[receiverId.value] = receiverId.value;
        receiverName.value = receiverId.value;
      }
    } catch (error) {
      // 当用户不存在时，显示用户ID
      userMap.value[receiverId.value] = receiverId.value;
      receiverName.value = receiverId.value;
    }
  }
}

/** 返回列表页面 */
function goBack() {
  router.push('/message/userChatManagement');
}
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

.chat-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  max-height: 600px;
  overflow-y: auto;
}

.chat-message {
  margin-bottom: 20px;
  display: flex;
}

.chat-message.sent {
  justify-content: flex-end;
}

.chat-message.received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px;
  border-radius: 8px;
}

.chat-message.sent .message-content {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.chat-message.received .message-content {
  background-color: #f0f2f5;
  border: 1px solid #d9d9d9;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.message-body {
  word-break: break-word;
}

.text-content {
  line-height: 1.5;
}

.image-content {
  display: inline-block;
}

.other-content {
  line-height: 1.5;
  color: #999;
}
</style>