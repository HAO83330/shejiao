<template>
  <div class="ai-chat-container" :class="{ 'dark-mode': isDark }">
    <div class="ai-chat-header">
      <div class="header-title">
        <el-avatar :size="32" class="ai-avatar" src="/派大星.jpg" />
        <span class="title-text">AI助手</span>
      </div>
      <div class="header-actions">
        <el-button type="text" @click="clearChat" :class="{ 'dark-text': isDark }">
          <el-icon><Delete /></el-icon>
        </el-button>
        <el-button type="text" @click="close" :class="{ 'dark-text': isDark }">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="ai-chat-messages" ref="messagesRef">
      <div v-for="(msg, index) in messages" :key="index" 
           :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-avatar">
            <el-avatar v-if="msg.role === 'ai'" :size="36" class="ai-avatar" src="/派大星.jpg" />
            <el-avatar v-else :size="36" :src="userInfo?.avatar" />
          </div>
        <div class="message-content">
          <div class="message-bubble" :class="{ 
            'user-bubble': msg.role === 'user', 
            'ai-bubble': msg.role === 'ai',
            'dark-bubble': isDark 
          }">
            <div v-if="msg.role === 'ai' && msg.isStreaming" class="streaming-content">
              <span v-html="renderMarkdown(msg.content)"></span>
              <span class="cursor"></span>
            </div>
            <div v-else-if="msg.role === 'ai'" v-html="renderMarkdown(msg.content)"></div>
            <span v-else>{{ msg.content }}</span>
            <el-button 
              type="text" 
              size="small" 
              class="delete-btn" 
              @click.stop="deleteMessage(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>
      
      <div v-if="isLoading && !isStreaming" class="message-item ai-message">
        <div class="message-avatar">
          <el-avatar :size="36" class="ai-avatar" src="@/assets/images/message.png" />
        </div>
        <div class="message-content">
          <div class="message-bubble ai-bubble typing-indicator" :class="{ 'dark-bubble': isDark }">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="ai-chat-input" :class="{ 'dark-input': isDark }">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          maxlength="250"
          show-word-limit
          :autosize="{ minRows: 6, maxRows: 8 }"
          placeholder="填写更全面的描述信息，让更多的人看到你吧❤️"
          resize="none"
          @keyup.enter.prevent="sendMessage"
          :disabled="isLoading"
          :class="{ 'dark-textarea': isDark }"
        />
        <div class="input-actions">
          <el-button 
            type="primary" 
            @click="sendMessage" 
            :loading="isLoading"
            :disabled="!inputMessage.trim()"
            class="send-btn"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
      <div class="input-tips">
        <el-icon><InfoFilled /></el-icon>
        <span>AI生成的内容仅供参考</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { Close, Delete, Promotion, InfoFilled } from '@element-plus/icons-vue';
import { useUserStore } from '@/store/userStore';
import { useThemeStore } from '@/store/themeStore';
import { ElMessage } from 'element-plus';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);

const userStore = useUserStore();
const themeStore = useThemeStore();
const userInfo = ref(userStore.getUserInfo());
const isDark = ref(themeStore.isDark);

const messages = ref<Array<{
  id?: string;
  role: 'user' | 'ai';
  content: string;
  timestamp: number;
  isStreaming?: boolean;
}>>([
  {
    role: 'ai',
    content: '你好！我是AI助手，有什么可以帮助你的吗？',
    timestamp: Date.now()
  }
]);

const inputMessage = ref('');
const isLoading = ref(false);
const isStreaming = ref(false);
const messagesRef = ref<HTMLDivElement>();
let eventSource: EventSource | null = null;

// 简单的Markdown渲染
const renderMarkdown = (text: string) => {
  if (!text) return '';
  return text
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>');
};

const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  });
};

const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const sendMessage = async () => {
  const message = inputMessage.value.trim();
  if (!message || isLoading.value) return;

  // 添加用户消息
  const userMessageIndex = messages.value.push({
    role: 'user',
    content: message,
    timestamp: Date.now()
  }) - 1;

  inputMessage.value = '';
  isLoading.value = true;
  isStreaming.value = true;
  
  await scrollToBottom();

  // 添加AI消息占位
  const aiMessageIndex = messages.value.push({
    role: 'ai',
    content: '',
    timestamp: Date.now(),
    isStreaming: true
  }) - 1;

  // 创建SSE连接
  const encodedMessage = encodeURIComponent(message);
  eventSource = new EventSource(`/ai/chat/stream?message=${encodedMessage}`);

  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      isStreaming.value = false;
      isLoading.value = false;
      messages.value[aiMessageIndex].isStreaming = false;
      eventSource?.close();
      eventSource = null;
    } else {
      messages.value[aiMessageIndex].content += event.data;
      scrollToBottom();
    }
  };

  eventSource.onerror = (error) => {
    console.error('SSE错误:', error);
    isStreaming.value = false;
    isLoading.value = false;
    messages.value[aiMessageIndex].isStreaming = false;
    
    if (!messages.value[aiMessageIndex].content) {
      messages.value[aiMessageIndex].content = '抱歉，服务暂时不可用，请稍后重试。';
    }
    
    eventSource?.close();
    eventSource = null;
    ElMessage.error('连接异常，请稍后重试');
  };

  eventSource.addEventListener('error', (event: any) => {
    console.error('SSE错误事件:', event);
  });
};

const clearChat = () => {
  messages.value = [{
    role: 'ai',
    content: '对话已清空，有什么可以帮助你的吗？',
    timestamp: Date.now()
  }];
};

const deleteMessage = async (index: number) => {
  const message = messages.value[index];
  
  // 从前端数组中删除消息
  messages.value.splice(index, 1);
  
  // 调用后端API删除消息（暂时不依赖messageId，后续可以优化）
  try {
    // 这里可以根据实际情况调整API调用
    // 目前我们先实现前端删除功能，后续可以添加后端删除逻辑
    console.log('删除消息:', message);
  } catch (error) {
    console.error('删除消息异常:', error);
    ElMessage.error('删除消息异常');
  }
};

const close = () => {
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
  emit('close');
};

watch(() => themeStore.isDark, (newVal) => {
  isDark.value = newVal;
});

onMounted(() => {
  scrollToBottom();
});
</script>

<style scoped lang="less">
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &.dark-mode {
    background: #1a1a1a;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }
}

.ai-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
  color: #ffffff;

  .header-title {
    display: flex;
    align-items: center;
    gap: 12px;

    .ai-avatar {
      box-shadow: 0 4px 12px rgba(255, 255, 255, 0.3);
    }

    .title-text {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;

    .el-button {
      color: rgba(255, 255, 255, 0.8);
      transition: all 0.3s ease;

      &:hover {
        color: #fff;
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f8f9fa;
  transition: background 0.3s ease;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }

  .dark-mode & {
    background: #0d0d0d;

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease;

  &.user-message {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }
  }

  &.ai-message {
    .message-content {
      align-items: flex-start;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  flex-shrink: 0;

  .ai-avatar {
    background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
    color: #fff;
    box-shadow: 0 2px 8px rgba(58, 100, 255, 0.3);
  }

  .el-avatar {
    transition: transform 0.3s ease;

    &:hover {
      transform: scale(1.05);
    }
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 75%;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  position: relative;
  transition: all 0.3s ease;

  .delete-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    opacity: 0;
    transition: opacity 0.3s ease, color 0.3s ease;
    color: #999;
    z-index: 10;

    &:hover {
      color: #ff4d4f;
    }
  }

  &:hover .delete-btn {
    opacity: 1;
  }

  &.user-bubble .delete-btn {
    color: rgba(255, 255, 255, 0.7);

    &:hover {
      color: #fff;
    }
  }

  &.user-bubble {
    background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
    color: #fff;
    border-bottom-right-radius: 6px;
    box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
  }

  &.ai-bubble {
    background: #ffffff;
    color: #333;
    border-bottom-left-radius: 6px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
    }

    &.dark-bubble {
      background: #2d2d2d;
      color: #ffffff;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);

      &:hover {
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
      }
    }

    :deep(pre) {
      background: #f4f4f4;
      padding: 16px;
      border-radius: 10px;
      overflow-x: auto;
      margin: 12px 0;
      border-left: 4px solid #3a64ff;

      code {
        font-family: 'SF Mono', Monaco, 'Courier New', monospace;
        font-size: 13px;
        line-height: 1.5;
      }

      .dark-mode & {
        background: #1a1a1a;
        border-left-color: #5a84ff;
      }
    }

    :deep(code) {
      background: #f4f4f4;
      padding: 4px 8px;
      border-radius: 6px;
      font-family: 'SF Mono', Monaco, 'Courier New', monospace;
      font-size: 13px;

      .dark-mode & {
        background: #1a1a1a;
      }
    }

    :deep(strong) {
      color: #3a64ff;

      .dark-mode & {
        color: #5a84ff;
      }
    }

    :deep(em) {
      color: #666;
      font-style: italic;

      .dark-mode & {
        color: #aaa;
      }
    }
  }
}

.message-time {
  font-size: 12px;
  color: #999;
  opacity: 0.7;
  transition: opacity 0.3s ease;

  .message-item:hover & {
    opacity: 1;
  }

  .dark-mode & {
    color: #666;
  }
}

.streaming-content {
  .cursor {
    display: inline-block;
    width: 2px;
    height: 1em;
    background: #667eea;
    margin-left: 4px;
    animation: blink 1s infinite;
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 18px;
  border-bottom-left-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

  .dark-mode & {
    background: #2d2d2d;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  }

  span {
    width: 10px;
    height: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-12px) scale(1.1);
    opacity: 1;
  }
}

.ai-chat-input {
  padding: 20px 24px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  transition: all 0.3s ease;

  &.dark-input {
    background: #1a1a1a;
    border-top-color: #333;
  }

  .input-wrapper {
    display: flex;
    gap: 16px;
    align-items: flex-end;

    :deep(.el-textarea) {
      flex: 1;

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

        &.dark-textarea {
          background: #2d2d2d;
          border-color: #3d3d3d;
          color: #ffffff;

          &::placeholder {
            color: #666;
          }

          &:hover {
            box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
          }

          &:focus {
            box-shadow: 0 0 0 2px rgba(58, 100, 255, 0.4);
            border-color: #3a64ff;
          }
        }
      }
    }
  }

  .input-actions {
    .send-btn {
      height: 40px;
      padding: 0 24px;
      border-radius: 24px;
      background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
      border: none;
      color: #ffffff;
      font-weight: 600;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(58, 100, 255, 0.4);
        background: linear-gradient(135deg, #5a84ff 0%, #3a64ff 100%);
      }

      &:active:not(:disabled) {
        transform: translateY(0);
      }

      &:disabled {
        opacity: 0.6;
        transform: none;
        box-shadow: none;
      }
    }
  }

  .input-tips {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;
    font-size: 12px;
    color: #999;
    transition: all 0.3s ease;

    .dark-mode & {
      color: #666;
    }

    .el-icon {
      font-size: 14px;
    }
  }
}
</style>
