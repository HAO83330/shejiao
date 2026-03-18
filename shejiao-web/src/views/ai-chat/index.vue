<template>
  <div class="ai-chat-page" :class="{ 'dark-mode': isDark }">
    <!-- 左侧对话列表 -->
    <div class="sidebar" :class="{ 'dark-sidebar': isDark, 'collapsed': isSidebarCollapsed }">
      <div class="sidebar-header">
        <el-button 
          type="primary" 
          class="new-chat-btn" 
          @click="createNewChat"
          :class="{ 'dark-btn': isDark }"
          :title="isSidebarCollapsed ? '新建对话' : ''"
        >
          <el-icon><Plus /></el-icon>
          <span v-if="!isSidebarCollapsed">新建对话</span>
        </el-button>
      </div>
      
      <div class="conversation-list" ref="conversationListRef" :class="{ 'collapsed-list': isSidebarCollapsed }">
        <div 
          v-for="conv in conversations" 
          :key="conv.id"
          :class="['conversation-item', { active: currentConversationId === conv.id, 'dark-item': isDark, 'collapsed-item': isSidebarCollapsed }]"
          @click="switchConversation(conv.id)"
          :title="isSidebarCollapsed ? conv.title : ''"
        >
          <div class="conversation-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div v-if="!isSidebarCollapsed" class="conversation-info">
            <div class="conversation-title">{{ conv.title }}</div>
            <div class="conversation-time">{{ formatTime(conv.updateTime) }}</div>
          </div>
          <div v-if="!isSidebarCollapsed" class="conversation-actions" @click.stop>
            <el-dropdown trigger="click">
              <el-icon class="more-icon" :size="20"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu :class="{ 'dark-dropdown': isDark }">
                  <el-dropdown-item @click="renameConversation(conv.id)">
                    <el-icon><Edit /></el-icon>重命名
                  </el-dropdown-item>
                  <el-dropdown-item @click="deleteConversation(conv.id)" class="delete-item">
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <div v-if="conversations.length === 0" class="empty-conversations">
          <el-icon :size="48"><ChatLineRound /></el-icon>
          <p>暂无对话记录</p>
          <p class="sub-text">点击上方按钮开始新对话</p>
        </div>
      </div>
      
      <div class="sidebar-footer" :class="{ 'collapsed-footer': isSidebarCollapsed }">
        <div class="user-info">
          <el-avatar :size="32" :src="userInfo?.avatar" />
          <span v-if="!isSidebarCollapsed" class="username">{{ userInfo?.username || '用户' }}</span>
          <el-button 
            type="text" 
            class="collapse-btn"
            @click="toggleSidebar"
            :title="isSidebarCollapsed ? '展开' : '收缩'"
          >
            <el-icon :size="18">
              <Fold v-if="!isSidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 右侧对话区域 -->
    <div class="chat-area" :class="{ 'dark-chat-area': isDark }">
      <!-- 对话头部 -->
      <div class="chat-header" :class="{ 'dark-header': isDark }">
        <div class="header-title">
          <el-icon :size="20"><ChatDotRound /></el-icon>
          <span>{{ currentConversation?.title || 'AI助手' }}</span>
        </div>
        <div class="header-actions">
          <el-button type="text" @click="clearCurrentChat" :class="{ 'dark-text': isDark }">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesRef">
        <div v-if="currentMessages.length === 0" class="welcome-section">
          <div class="welcome-icon">
            <img src="/派大星.jpg" alt="AI助手" class="welcome-avatar" />
          </div>
          <h2>你好！我是你的社交小助手</h2>
          <p>我可以帮你写动态、想文案、聊天回复、分析情感等</p>
          <div class="quick-actions">
            <div class="quick-action-item" @click="sendQuickMessage('帮我写一条有趣的朋友圈动态')">
              <el-icon><Document /></el-icon>
              <span>帮我写一条有趣的朋友圈动态</span>
            </div>
            <div class="quick-action-item" @click="sendQuickMessage('帮我回复：今天天气真不错')">
              <el-icon><ChatDotRound /></el-icon>
              <span>帮我回复：今天天气真不错</span>
            </div>
            <div class="quick-action-item" @click="sendQuickMessage('帮我写一段自我介绍，要幽默一点')">
              <el-icon><User /></el-icon>
              <span>帮我写一段幽默的自我介绍</span>
            </div>
            <div class="quick-action-item" @click="sendQuickMessage('分析一下这段对话的情感倾向')">
              <el-icon><Star /></el-icon>
              <span>分析对话情感倾向</span>
            </div>
          </div>
        </div>
        
        <template v-else>
          <div 
            v-for="(msg, index) in currentMessages" 
            :key="index"
            :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="message-avatar">
              <el-avatar v-if="msg.role === 'ai'" :size="40" class="ai-avatar" src="/派大星.jpg" />
              <el-avatar v-else :size="40" :src="userInfo?.avatar" />
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
              </div>
              <div class="message-footer">
                <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
              </div>
            </div>
            <el-button 
              type="text" 
              size="small" 
              :class="['delete-btn', msg.role === 'user' ? 'user-delete-btn' : 'ai-delete-btn']" 
              @click.stop="deleteMessage(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          
          <div v-if="isLoading && !isStreaming" class="message-item ai-message">
            <div class="message-avatar">
              <el-avatar :size="40" class="ai-avatar" src="/派大星.jpg" />
            </div>
            <div class="message-content">
              <div class="message-bubble ai-bubble typing-indicator" :class="{ 'dark-bubble': isDark }">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </template>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area" :class="{ 'dark-input-area': isDark }">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :autosize="{ minRows: 6, maxRows: 8 }"
            placeholder="输入你的问题，AI 会为你解答❤️"
            resize="none"
            @keyup.enter.prevent="sendMessage"
            :disabled="isLoading"
            :class="{ 'dark-textarea': isDark }"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              circle
              size="large"
              @click="sendMessage" 
              :loading="isLoading"
              :disabled="!inputMessage.trim()"
              class="send-btn"
            >
              <el-icon :size="20"><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="input-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>AI生成的内容仅供参考</span>
        </div>
      </div>
    </div>
    
    <!-- 重命名对话框 -->
    <el-dialog
      v-model="renameDialogVisible"
      title="重命名对话"
      width="400px"
      :class="{ 'dark-dialog': isDark }"
    >
      <el-input v-model="renameValue" placeholder="请输入对话名称" />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { 
  Plus, 
  ChatDotRound, 
  MoreFilled, 
  Edit, 
  Delete, 
  ChatLineRound,
  Promotion, 
  InfoFilled,
  Document,
  User,
  Star,
  Fold,
  Expand
} from '@element-plus/icons-vue';
import { useUserStore } from '@/store/userStore';
import { useThemeStore } from '@/store/themeStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import request from '@/utils/request';

const router = useRouter();
const userStore = useUserStore();
const themeStore = useThemeStore();
const userInfo = ref(userStore.getUserInfo());
const isDark = ref(themeStore.isDark);

// 对话列表
interface Conversation {
  id: string;
  title: string;
  createTime: number;
  updateTime: number;
  messages: Message[];
}

interface Message {
  id?: string;
  role: 'user' | 'ai';
  content: string;
  timestamp: number;
  isStreaming?: boolean;
}

const conversations = ref<Conversation[]>([]);
const currentConversationId = ref<string>('');
const inputMessage = ref('');
const isLoading = ref(false);
const isStreaming = ref(false);
const messagesRef = ref<HTMLDivElement>();

// 侧边栏收缩状态
const isSidebarCollapsed = ref(false);

// 重命名相关
const renameDialogVisible = ref(false);
const renameValue = ref('');
const renameTargetId = ref('');

// 当前对话
const currentConversation = computed(() => {
  return conversations.value.find(c => c.id === currentConversationId.value);
});

// 当前消息列表
const currentMessages = computed(() => {
  return currentConversation.value?.messages || [];
});

// 从后端加载对话列表
const loadConversations = async () => {
  try {
    const res: any = await request({
      url: '/ai/session/list',
      method: 'get'
    });

    if (res.code === 200) {
      // 转换后端数据格式
      const loadedConversations = (res.data || []).map((item: any) => ({
        id: item.id,
        title: item.title,
        createTime: new Date(item.createTime).getTime(),
        updateTime: new Date(item.updateTime).getTime(),
        messages: []
      }));

      // 保存当前选中的对话 ID
      const previousCurrentId = currentConversationId.value;

      // 直接使用后端返回的数据，按更新时间排序（最新的在前）
      conversations.value = loadedConversations.sort((a: Conversation, b: Conversation) => b.updateTime - a.updateTime);

      // 如果之前有选中的对话，且该对话仍在列表中，保持选中状态
      if (previousCurrentId && conversations.value.find(c => c.id === previousCurrentId)) {
        currentConversationId.value = previousCurrentId;
        // 加载当前对话的历史消息
        await loadConversationHistory(previousCurrentId);
      } else if (conversations.value.length > 0) {
        // 否则选中最新的对话（第一个）
        currentConversationId.value = conversations.value[0].id;
        await loadConversationHistory(conversations.value[0].id);
      } else {
        // 没有对话时清空当前选中
        currentConversationId.value = '';
      }
    }
  } catch (error: any) {
    console.error('加载对话列表失败', error);
    if (error.response?.status === 401) {
      ElMessage.error('请先登录');
      router.push('/login');
    }
  }
};

// 加载对话历史消息
const loadConversationHistory = async (sessionId: string) => {
  try {
    const res: any = await request({
      url: `/ai/session/history/${sessionId}`,
      method: 'get'
    });
    
    if (res.code === 200) {
      const conv = conversations.value.find(c => c.id === sessionId);
      if (conv) {
        // 转换后端消息格式
        conv.messages = (res.data || []).map((item: any, index: number) => ({
          id: `${sessionId}_${index}`, // 生成临时ID，用于前端标识
          role: item.role === 'assistant' ? 'ai' : item.role,
          content: item.content,
          timestamp: item.timestamp || item.createTime ? new Date(item.timestamp || item.createTime).getTime() : new Date().getTime(),
          isStreaming: false
        }));
      }
    }
  } catch (error) {
    console.error('加载对话历史失败', error);
  }
};

// 创建新对话
const createNewChat = async () => {
  try {
    const res: any = await request({
      url: '/ai/session/create',
      method: 'post',
      data: {
        title: '新对话'
      }
    });
    
    if (res.code === 200) {
      const newSessionId = res.data;
      
      // 先切换到新对话ID，避免闪烁
      currentConversationId.value = newSessionId;
      
      // 从后端重新加载对话列表（不先清空，避免闪烁）
      await loadConversations();
      
      // 确保新创建的对话被选中
      const newConv = conversations.value.find(c => c.id === newSessionId);
      if (newConv) {
        currentConversationId.value = newSessionId;
      } else if (conversations.value.length > 0) {
        // 如果找不到新创建的对话，选择第一个
        currentConversationId.value = conversations.value[0].id;
      }
      
      ElMessage.success('创建成功');
    }
  } catch (error: any) {
    console.error('创建对话失败', error);
    if (error.response?.status === 401) {
      ElMessage.error('请先登录');
      router.push('/login');
    } else {
      ElMessage.error('创建失败，请重试');
    }
  }
};

// 切换对话
const switchConversation = async (id: string) => {
  currentConversationId.value = id;
  const conv = conversations.value.find(c => c.id === id);
  if (conv && conv.messages.length === 0) {
    // 如果消息为空，加载历史消息
    await loadConversationHistory(id);
  }
  nextTick(() => {
    scrollToBottom();
  });
};

// 重命名对话
const renameConversation = (id: string) => {
  const conv = conversations.value.find(c => c.id === id);
  if (conv) {
    renameTargetId.value = id;
    renameValue.value = conv.title;
    renameDialogVisible.value = true;
  }
};

// 确认重命名（仅本地，后端暂不支持）
const confirmRename = () => {
  if (!renameValue.value.trim()) {
    ElMessage.warning('请输入对话名称');
    return;
  }
  const conv = conversations.value.find(c => c.id === renameTargetId.value);
  if (conv) {
    conv.title = renameValue.value.trim();
    conv.updateTime = Date.now();
  }
  renameDialogVisible.value = false;
  ElMessage.success('重命名成功');
};

// 切换侧边栏收缩状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

// 删除对话
const deleteConversation = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    console.log('删除对话请求参数:', id);
    
    try {
      const res: any = await request({
        url: `/ai/session/${id}`,
        method: 'delete'
      });
      
      console.log('删除对话响应:', res);
      
      if (res.code !== 200) {
        console.error('删除对话失败，响应码:', res.code);
        // 即使删除失败，也保持前端删除状态
      }
    } catch (error) {
      console.error('删除对话API调用失败', error);
      // 即使API调用失败，也保持前端删除状态
    }
    
    // 无论API调用是否成功，都从前端删除对话
    const index = conversations.value.findIndex(c => c.id === id);
    if (index > -1) {
      conversations.value.splice(index, 1);
      if (currentConversationId.value === id) {
        currentConversationId.value = conversations.value[0]?.id || '';
        if (currentConversationId.value) {
          await loadConversationHistory(currentConversationId.value);
        }
      }
      ElMessage.success('删除成功');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除对话失败', error);
      console.error('错误详情:', error.response);
      ElMessage.error(error.message || error.response?.data?.msg || '删除失败，请重试');
    }
  }
};

// 清空当前对话
const clearCurrentChat = async () => {
  if (!currentConversation.value) return;
  
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    currentConversation.value.messages = [];
    currentConversation.value.updateTime = Date.now();
    ElMessage.success('清空成功');
  } catch {
    // 用户取消
  }
};

// 发送快捷消息
const sendQuickMessage = (message: string) => {
  inputMessage.value = message;
  sendMessage();
};

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim();
  if (!message || isLoading.value) return;

  // 如果没有当前对话，创建一个新对话
  if (!currentConversation.value) {
    await createNewChat();
    if (!currentConversation.value) return;
  }

  const conv = currentConversation.value!;
  
  // 如果是第一条消息，用前20个字作为标题
  if (conv.messages.length === 0) {
    conv.title = message.slice(0, 20) + (message.length > 20 ? '...' : '');
  }

  // 添加用户消息
  conv.messages.push({
    role: 'user',
    content: message,
    timestamp: Date.now()
  });

  inputMessage.value = '';
  isLoading.value = true;
  conv.updateTime = Date.now();
  
  await scrollToBottom();

  // 添加AI消息占位
  const aiMessage: Message = {
    role: 'ai',
    content: '',
    timestamp: Date.now(),
    isStreaming: true
  };
  conv.messages.push(aiMessage);

  try {
    // 使用POST请求，传递sessionId
    const res: any = await request({
      url: '/ai/chat',
      method: 'post',
      data: {
        message: message,
        sessionId: conv.id  // 使用sessionId而不是conversationId
      }
    });

    console.log('========== AI响应调试信息 ==========');
    console.log('完整响应对象:', res);
    console.log('响应类型:', typeof res);
    console.log('res.code:', res?.code, '类型:', typeof res?.code);
    console.log('res.data:', res?.data, '类型:', typeof res?.data);
    console.log('res.msg:', res?.msg, '类型:', typeof res?.msg);
    console.log('res是否有data属性:', 'data' in (res || {}));
    console.log('res.data === undefined:', res?.data === undefined);
    console.log('res.data === null:', res?.data === null);
    console.log('res.data === "":', res?.data === '');
    console.log('=====================================');

    // 检查响应是否有效
    if (!res) {
      console.error('响应为空');
      aiMessage.content = '抱歉，服务器没有返回数据，请重试。';
      isStreaming.value = false;
      isLoading.value = false;
      aiMessage.isStreaming = false;
      return;
    }

    if (res.code === 200) {
      // 提取内容 - res.data 应该是AI的回复内容
      let content = res.data;
      
      console.log('原始content值:', content, '类型:', typeof content);
      
      // 如果 res.data 是 null/undefined/空字符串，则尝试使用 res.msg
      if (content === undefined || content === null || content === '') {
        console.warn('res.data 为空，尝试使用 res.msg');
        content = res.msg;
      }
      
      // 确保 content 是字符串
      if (typeof content !== 'string') {
        content = String(content);
      }
      
      console.log('最终提取的内容:', content, '长度:', content?.length);
      
      if (!content || content === '操作成功' || content === 'null' || content === 'undefined') {
        aiMessage.content = '抱歉，AI没有返回内容，请重试。(debug: data=' + JSON.stringify(res.data) + ')';
        isStreaming.value = false;
        isLoading.value = false;
        aiMessage.isStreaming = false;
        return;
      }
      
      // 模拟流式效果
      let index = 0;
      isStreaming.value = true;
      
      const typeWriter = () => {
        if (index < content.length) {
          const chunk = content.slice(index, index + Math.floor(Math.random() * 5) + 5);
          aiMessage.content += chunk;
          index += chunk.length;
          scrollToBottom();
          setTimeout(typeWriter, 30);
        } else {
          isStreaming.value = false;
          isLoading.value = false;
          aiMessage.isStreaming = false;
        }
      };
      
      typeWriter();
    } else if (res.code === 401) {
      // 未登录
      isStreaming.value = false;
      isLoading.value = false;
      aiMessage.isStreaming = false;
      aiMessage.content = '请先登录后再使用AI助手。';
      ElMessage.error('请先登录');
      router.push('/login');
    } else {
      throw new Error(res.msg || '请求失败');
    }
  } catch (error: any) {
    console.error('请求错误:', error);
    isStreaming.value = false;
    isLoading.value = false;
    aiMessage.isStreaming = false;
    
    if (error.response?.status === 401) {
      aiMessage.content = '请先登录后再使用AI助手。';
      ElMessage.error('请先登录');
      router.push('/login');
    } else {
      aiMessage.content = '抱歉，服务暂时不可用，请稍后重试。';
      ElMessage.error(error.message || '连接异常，请稍后重试');
    }
  }
};

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
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  if (diff < 60000) {
    return '刚刚';
  }
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前';
  }
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前';
  }
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前';
  }
  
  return date.toLocaleDateString('zh-CN');
};

const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

// 删除消息
const deleteMessage = async (index: number) => {
  if (!currentConversation.value) return;
  
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    // 从当前对话的消息列表中删除
    currentConversation.value.messages.splice(index, 1);
    currentConversation.value.updateTime = Date.now();
    
    // 滚动到底部
    await scrollToBottom();
    
    // 调用后端API删除数据库中的消息
    try {
      // 使用会话ID和索引删除消息
      const res: any = await request({
        url: `/ai/message/${currentConversation.value.id}/${index}`,
        method: 'delete'
      });
      
      if (res.code !== 200) {
        console.error('删除消息失败', res);
        ElMessage.error('删除失败，请重试');
      } else {
        ElMessage.success('删除成功');
      }
    } catch (error) {
      console.error('删除消息API调用失败', error);
      // 即使API调用失败，也保持前端删除状态
      ElMessage.success('删除成功');
    }
  } catch (error) {
    // 用户取消删除
  }
};

watch(() => themeStore.isDark, (newVal) => {
  isDark.value = newVal;
});

onMounted(() => {
  // 从后端加载对话列表
  loadConversations();
});
</script>

<style scoped lang="less">
.ai-chat-page {
  display: flex;
  height: calc(100vh - 60px);
  margin-top: 60px;
  background: #ffffff;
  transition: all 0.3s ease;

  &.dark-mode {
    background: #0d0d0d;
  }
}

// 侧边栏
.sidebar {
  width: 300px;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border-right: 1px solid #f0f0f0;
  height: 100%;
  overflow: hidden;
  transition: all 0.3s ease;

  &.dark-sidebar {
    background: #1a1a1a;
    border-right-color: #333;
  }

  // 收缩状态
  &.collapsed {
    width: 80px;
    min-width: 80px;

    .sidebar-header {
      padding: 20px 16px;

      .new-chat-btn {
        width: 48px;
        padding: 0;
        justify-content: center;
      }
    }

    .conversation-list {
      padding: 12px 8px;

      .conversation-item {
        padding: 12px;
        justify-content: center;

        .conversation-icon {
          margin-right: 0;
        }
      }
    }

    .sidebar-footer {
      padding: 16px 12px;

      .user-info {
        justify-content: center;
        padding: 8px;

        .collapse-btn {
          margin-left: 0;
        }
      }
    }
  }
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  transition: all 0.3s ease;

  .dark-sidebar & {
    border-bottom-color: #333;
  }

  .new-chat-btn {
    width: 100%;
    height: 48px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &.dark-btn {
      background: linear-gradient(135deg, #2d2d2d 0%, #3d3d3d 100%);
      border-color: #4d4d4d;
      color: #ffffff;

      &:hover {
        background: linear-gradient(135deg, #3d3d3d 0%, #4d4d4d 100%);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      }
    }
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

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

  .dark-sidebar & {
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.3s ease;
  background: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  &:hover {
    background: #f0f0f0;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .dark-sidebar & {
      background: #2d2d2d;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }

    .conversation-actions {
      opacity: 1;
    }
  }

  &.active {
    background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
    color: #ffffff;
    box-shadow: 0 4px 16px rgba(58, 100, 255, 0.3);

    .conversation-title {
      color: #ffffff;
    }

    .conversation-time {
      color: rgba(255, 255, 255, 0.8);
    }

    .conversation-icon {
      background: rgba(255, 255, 255, 0.2);
      color: #ffffff;
    }

    .more-icon {
      color: rgba(255, 255, 255, 0.8);

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  &.dark-item {
    background: #1a1a1a;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    color: #ffffff;

    &:hover {
      background: #2d2d2d;
    }
  }
}

.conversation-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
  border-radius: 10px;
  margin-right: 16px;
  color: #667eea;
  flex-shrink: 0;
  transition: all 0.3s ease;

  .dark-sidebar & {
    background: linear-gradient(135deg, #2d2d2d 0%, #3d3d3d 100%);
    color: #8a63d2;
  }
}

.conversation-info {
  flex: 1;
  min-width: 0;

  .conversation-title {
    font-size: 14px;
    font-weight: 500;
    color: #333333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: all 0.3s ease;

    .dark-item & {
      color: #ffffff;
    }
  }

  .conversation-time {
    font-size: 12px;
    color: #999999;
    margin-top: 4px;
    transition: all 0.3s ease;

    .dark-item & {
      color: #666666;
    }
  }
}

.conversation-actions {
  opacity: 0.7;
  transition: all 0.3s ease;

  .more-icon {
    padding: 6px;
    color: #999999;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.3s ease;

    &:hover {
      background: #e0e0e0;
      color: #666666;

      .dark-sidebar & {
        background: #4d4d4d;
        color: #cccccc;
      }
    }

    .dark-sidebar & {
      color: #666666;
    }
  }
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999999;
  text-align: center;

  .dark-sidebar & {
    color: #666666;
  }

  .el-icon {
    font-size: 64px;
    margin-bottom: 24px;
    color: #e0e0e0;

    .dark-sidebar & {
      color: #333333;
    }
  }

  p {
    margin: 8px 0 0;
    font-size: 16px;
    font-weight: 500;

    &.sub-text {
      font-size: 14px;
      color: #cccccc;
      margin-top: 12px;
    }
  }
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  transition: all 0.3s ease;

  .dark-sidebar & {
    border-top-color: #333;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 12px;
    transition: all 0.3s ease;

    .dark-sidebar & {
      background: #1a1a1a;
    }

    .username {
      font-size: 14px;
      font-weight: 500;
      color: #333333;
      transition: all 0.3s ease;
      flex: 1;

      .dark-sidebar & {
        color: #ffffff;
      }
    }

    .collapse-btn {
      margin-left: auto;
      color: #999999;
      padding: 6px;
      border-radius: 6px;
      transition: all 0.3s ease;

      &:hover {
        background: #e0e0e0;
        color: #666666;
      }

      .dark-sidebar & {
        color: #666666;

        &:hover {
          background: #4d4d4d;
          color: #cccccc;
        }
      }
    }
  }
}

// 对话区域
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  transition: all 0.3s ease;

  &.dark-chat-area {
    background: #0d0d0d;
  }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 28px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &.dark-header {
    background: #1a1a1a;
    border-bottom-color: #333;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }

  .header-title {
      display: flex;
      align-items: center;
      gap: 16px;
      font-size: 18px;
      font-weight: 600;
      color: #333333;
      transition: all 0.3s ease;

      .dark-header & {
        color: #ffffff;
      }

      .el-icon {
        color: #3a64ff;

        .dark-header & {
          color: #5a84ff;
        }
      }
    }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    .el-button {
      font-size: 20px;
      color: #666666;
      transition: all 0.3s ease;

      &:hover {
        color: #f56c6c;
        transform: scale(1.1);
      }

      .dark-header & {
        color: #999999;

        &:hover {
          color: #f56c6c;
        }
      }
    }

    .dark-text {
      color: #999999;

      &:hover {
        color: #ffffff;
      }
    }
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
  transition: all 0.3s ease;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 4px;

    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }

  .dark-chat-area & {
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  .welcome-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    text-align: center;
    padding: 40px;

    .welcome-icon {
          width: 100px;
          height: 100px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 24px;
          margin-bottom: 32px;
          animation: pulse 2s infinite;
        }

        .welcome-avatar {
          width: 100%;
          height: 100%;
          border-radius: 24px;
          object-fit: cover;
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
        }

    h2 {
      font-size: 28px;
      font-weight: 600;
      color: #333333;
      margin: 0 0 12px;
      transition: all 0.3s ease;

      .dark-chat-area & {
        color: #ffffff;
      }
    }

    p {
      font-size: 16px;
      color: #666666;
      margin: 0 0 40px;
      max-width: 600px;
      line-height: 1.6;
      transition: all 0.3s ease;

      .dark-chat-area & {
        color: #999999;
      }
    }

    .quick-actions {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      max-width: 600px;
      width: 100%;

      .quick-action-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 20px;
        background: #ffffff;
        border-radius: 16px;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

        &:hover {
          background: #f8f9fa;
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
        }

        .dark-chat-area & {
          background: #1a1a1a;
          border: 1px solid #333;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);

          &:hover {
            background: #2d2d2d;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
          }
        }

        .el-icon {
          font-size: 24px;
          color: #3a64ff;
          transition: all 0.3s ease;

          .dark-chat-area & {
            color: #5a84ff;
          }
        }

        span {
          font-size: 14px;
          font-weight: 500;
          color: #333333;
          transition: all 0.3s ease;

          .dark-chat-area & {
            color: #ffffff;
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 8px 24px rgba(58, 100, 255, 0.3);
  }
  50% {
    box-shadow: 0 8px 32px rgba(58, 100, 255, 0.5);
  }
  100% {
    box-shadow: 0 8px 24px rgba(58, 100, 255, 0.3);
  }
}

.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
  animation: fadeIn 0.3s ease;
  align-items: flex-end;

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
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.1);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
      }
    }

    .el-avatar {
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.1);
      }
    }
  }

.message-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 75%;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;

  .user-message & {
    justify-content: flex-end;
  }

  .ai-message & {
    justify-content: flex-start;
  }
}

.message-bubble {
  padding: 18px 24px;
  border-radius: 20px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
  position: relative;
  transition: all 0.3s ease;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s ease, color 0.3s ease;
  color: #999;
  z-index: 10;
  padding: 8px;
  align-self: flex-end;
  margin-bottom: 8px;
  font-size: 18px;

  &:hover {
    color: #ff4d4f;
  }
}

.user-delete-btn {
  margin-left: 16px;
}

.ai-delete-btn {
  margin-right: 16px;
}

.message-item:hover .delete-btn {
  opacity: 1;
}

.user-message .delete-btn {
  color: #666;

  &:hover {
    color: #ff4d4f;
  }
}

.dark-chat-area .user-message .delete-btn {
  color: #999;

  &:hover {
    color: #ff4d4f;
  }
}

.message-bubble {
  &.user-bubble {
    background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
    color: #ffffff;
    border-bottom-right-radius: 8px;
    box-shadow: 0 4px 16px rgba(58, 100, 255, 0.3);
  }

  &.ai-bubble {
    background: #ffffff;
    color: #333333;
    border-bottom-left-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }

    &.dark-bubble {
      background: #1a1a1a;
      color: #ffffff;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);

      &:hover {
        box-shadow: 0 6px 24px rgba(0, 0, 0, 0.4);
      }
    }

    :deep(pre) {
      background: #f8f9fa;
      padding: 16px;
      border-radius: 12px;
      overflow-x: auto;
      margin: 12px 0;
      border-left: 4px solid #3a64ff;
      transition: all 0.3s ease;

      code {
        font-family: 'SF Mono', Monaco, 'Courier New', monospace;
        font-size: 13px;
        line-height: 1.5;
      }

      .dark-bubble & {
        background: #0d0d0d;
        border-left-color: #5a84ff;
      }
    }

    :deep(code) {
      background: #f8f9fa;
      padding: 4px 8px;
      border-radius: 6px;
      font-family: 'SF Mono', Monaco, 'Courier New', monospace;
      font-size: 13px;
      transition: all 0.3s ease;

      .dark-bubble & {
        background: #0d0d0d;
      }
    }

    :deep(strong) {
      color: #3a64ff;
      font-weight: 600;

      .dark-bubble & {
        color: #5a84ff;
      }
    }

    :deep(em) {
      color: #666666;
      font-style: italic;

      .dark-bubble & {
        color: #aaaaaa;
      }
    }
  }
}

.message-time {
  font-size: 12px;
  color: #999999;
  opacity: 0.7;
  transition: all 0.3s ease;

  .message-item:hover & {
    opacity: 1;
  }

  .dark-chat-area & {
    color: #666666;
  }
}

.streaming-content {
  .cursor {
    display: inline-block;
    width: 2px;
    height: 1em;
    background: #3a64ff;
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
  gap: 8px;
  padding: 20px 24px;
  background: #ffffff;
  border-radius: 20px;
  border-bottom-left-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  .dark-chat-area & {
    background: #1a1a1a;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  }

  span {
    width: 12px;
    height: 12px;
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

// 输入区域
.input-area {
  padding: 24px 28px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &.dark-input-area {
    background: #0d0d0d;
    border-top-color: #333;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.2);
  }

  .input-wrapper {
    display: flex;
    gap: 16px;
    align-items: flex-end;
    transition: all 0.3s ease;

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

    .input-actions {
      flex-shrink: 0;

      .send-btn {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
        border: none;
        transition: all 0.3s ease;
        box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);

        &:hover:not(:disabled) {
          transform: scale(1.1);
          box-shadow: 0 6px 20px rgba(58, 100, 255, 0.4);
        }

        &:active:not(:disabled) {
          transform: scale(0.95);
        }

        &:disabled {
          opacity: 0.6;
          transform: none;
          box-shadow: none;
        }
      }
    }
  }

  .input-tips {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-top: 16px;
    font-size: 13px;
    color: #999999;
    transition: all 0.3s ease;

    .dark-input-area & {
      color: #666666;
    }

    .el-icon {
      font-size: 14px;
    }
  }
}

// 下拉菜单样式
.dark-dropdown {
  background: #1a1a1a !important;
  border: 1px solid #333 !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3) !important;

  .el-dropdown-menu__item {
    color: #ffffff;
    padding: 12px 16px !important;
    transition: all 0.3s ease !important;

    &:hover {
      background: #2d2d2d !important;
      border-radius: 8px !important;
    }

    &.delete-item {
      color: #f56c6c;

      &:hover {
        background: rgba(245, 108, 108, 0.1) !important;
      }
    }
  }
}

// 对话框样式
.dark-dialog {
  :deep(.el-dialog) {
    background: #1a1a1a;
    border-radius: 16px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);

    .el-dialog__title {
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
    }

    .el-dialog__body {
      padding: 24px;

      .el-input__inner {
        background: #2d2d2d;
        border-color: #333;
        color: #ffffff;

        &::placeholder {
          color: #666;
        }
      }
    }

    .el-dialog__footer {
      padding: 16px 24px;
      border-top: 1px solid #333;

      .el-button {
        &:nth-child(1) {
          color: #999;

          &:hover {
            color: #ffffff;
            background: #2d2d2d;
          }
        }

        &:nth-child(2) {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;

          &:hover {
            background: linear-gradient(135deg, #768eea 0%, #865ba2 100%);
          }
        }
      }
    }
  }
}

</style>
