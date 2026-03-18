<template>
  <div class="container" id="container">
    <div v-if="isLogin" class="push-container">
      <div class="header"><span class="header-icon"></span><span class="header-title">发布笔记📚（图文）</span></div>
      <div class="img-list">
        <el-upload
          v-model:file-list="fileList"
          action="http://localhost:88/api/util/oss/saveBatch/0"
          list-type="picture-card"
          multiple
          :limit="9"
          :headers="uploadHeader"
          :auto-upload="false"
          :on-preview="handlePreview"
        >
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>

        <el-dialog v-model="dialogVisible" width="60%">
            <img w-full :src="dialogImageUrl" alt="Preview Image" style="width: 100%; height: auto;" />
          </el-dialog>

        <!-- 网络图片上传 -->
        <div class="net-img-upload" v-if="fileList.length < 9">
          <el-input
            v-model="netImgUrl"
            placeholder="输入网络图片URL（例如：https://images.unsplash.com/...）"
            clearable
            style="width: 600px; margin-right: 10px;"
          />
          <el-button type="primary" @click="addNetImg" :disabled="!netImgUrl || fileList.length >= 9">
            添加网络图片
          </el-button>
        </div>
      </div>
      <el-divider style="margin: 0.75rem; width: 96%" />
      <div class="push-content">
        <el-input
          v-model="note.title"
          maxlength="20"
          show-word-limit
          type="text"
          placeholder="请输入标题"
          class="input-title"
        />
        <el-input
          v-model="note.content"
          maxlength="250"
          show-word-limit
          :autosize="{ minRows: 6, maxRows: 8 }"
          type="textarea"
          placeholder="填写更全面的描述信息，让更多的人看到你吧❤️"
        />
        <div class="btns">
          <button class="css-fm44j css-osq2ks dyn" @click="addTag">
            <span class="btn-content"># 话题</span></button
          ><button class="css-fm44j css-osq2ks dyn" @click="handleAtUser">
            <span class="btn-content"><span>@</span> 用户</span></button
          ><button class="css-fm44j css-osq2ks dyn" @click="handleEmoji">
            <span class="btn-content">
              <div class="smile"></div>
              表情
            </span>
          </button>
        </div>
        <div class="tag-list">
          <el-tag
            v-for="tag in dynamicTags"
            :key="tag"
            closable
            :disable-transitions="false"
            @close="handleClose(tag)"
            class="tag-item"
            type="danger"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="inputVisible"
            ref="InputRef"
            v-model="inputValue"
            style="width: 3.125rem"
            size="small"
            @keyup.enter="handleInputConfirm"
            @blur="handleInputBlur"
          />
          <el-button
            v-else
            type="warning"
            size="small"
            @click="showInput"
            plain
            id="tagContainer"
            :disabled="dynamicTags.length >= 5"
          >
            + 标签
          </el-button>
        </div>
        <div class="hot-tag">
          <span class="tag-title-text">推荐标签：</span>
          <el-tag
            v-for="tag in hotTags"
            :key="tag"
            class="hot-tag-item"
            type="warning"
            @click="selectHotTag(tag)"
            :disabled="dynamicTags.length >= 5"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      
      <el-dialog v-if="userDialogVisible" v-model="userDialogVisible" title="@用户" width="500px">
        <el-input
          v-model="userSearchKeyword"
          placeholder="搜索用户名"
          clearable
          @input="searchUsers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="user-list" v-loading="userLoading">
          <div
            v-for="user in userList"
            :key="user.id"
            class="user-item"
            @click="selectUser(user)"
          >
            <img :src="user.avatar" class="user-avatar" />
            <span class="user-name">{{ user.username }}</span>
          </div>
          <el-empty v-if="userList.length === 0 && !userLoading" description="暂无用户" />
        </div>
      </el-dialog>

      <el-dialog v-if="emojiDialogVisible" v-model="emojiDialogVisible" title="选择表情" width="500px" :before-close="handleEmojiDialogClose" :destroy-on-close="true">
        <div class="emoji-grid">
          <div
            v-for="emoji in emojiList"
            :key="emoji"
            class="emoji-item"
            @click="selectEmoji(emoji)"
          >
            {{ emoji }}
          </div>
        </div>
      </el-dialog>
      
      <el-dialog v-if="topicDialogVisible" v-model="topicDialogVisible" title="选择话题" width="500px" :before-close="handleTopicDialogClose">
        <el-input
          v-model="topicSearchKeyword"
          placeholder="搜索话题"
          clearable
          @input="searchTopics"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="topic-list" v-loading="topicLoading">
          <div
            v-for="topic in topicList"
            :key="topic.id"
            class="topic-item"
            @click="selectTopic(topic)"
          >
            <span class="topic-title">#{{ topic.title }}</span>
            <span class="topic-count">{{ topic.count || 0 }}篇笔记</span>
          </div>
          <el-empty v-if="topicList.length === 0 && !topicLoading" description="暂无话题" />
        </div>
      </el-dialog>
      
      <el-divider style="margin: 0.75rem; width: 96%" />
      <div class="categorys">
        <el-cascader
          ref="CascaderRef"
          v-model="categoryList"
          :options="options"
          @change="handleChange"
          :props="props"
          placeholder="请选择分类"
        />
      </div>
      <div class="submit">
        <el-button type="primary" loading :disabled="true" v-if="pushLoading">发布</el-button>
        <button class="publishBtn" @click="pubslish()" v-else>
          <span class="btn-content">发布</span>
        </button>
        <button class="clearBtn">
          <span class="btn-content" @click="resetData">取消</span>
        </button>
      </div>
    </div>
    <div v-else>
      <el-empty description="用户未登录" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, nextTick } from "vue";
import { Plus, Search } from "@element-plus/icons-vue";
import { useRoute } from "vue-router";
import type { UploadUserFile, CascaderProps, ElInput } from "element-plus";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/userStore";
import { getCategoryTreeData } from "@/api/category";
import { saveNoteByDTO, getNoteById, updateNoteByDTO } from "@/api/note";
import { getTagByKeyword } from "@/api/tag";
import { getFileFromUrl } from "@/utils/util";

import { getNoticeFollower } from "@/api/follower";
// import Schema from "async-validator";
// import Crop from "@/components/Crop.vue";
const props: CascaderProps = {
  label: "title",
  value: "id",
  checkStrictly: true, // 允许选择父级节点
};
const CascaderRef = ref<any>(null);

// const rules = {
//   title: { required: true, message: "标题不能为空" },
//   content: { required: true, message: "内容不能为空" },
//   category: { required: true, message: "分类不能为空" },
// };
// const validator = new Schema(rules);

const userStore = useUserStore();
const route = useRoute();

const fileList = ref<UploadUserFile[]>([]);

const dialogImageUrl = ref("");
const dialogVisible = ref(false);

const handlePreview = (file: any) => {
  if (!file.url) return;
  dialogImageUrl.value = file.url;
  dialogVisible.value = true;
};

const uploadHeader = ref({
  accessToken: userStore.getToken(),
});
const categoryList = ref<Array<any>>([]);
const options = ref<any[]>([]);
const note = ref<any>({});
const pushLoading = ref(false);
const isLogin = ref(false);
const inputValue = ref("");
const dynamicTags = ref<Array<string>>([]);
const inputVisible = ref(false);
const InputRef = ref<InstanceType<typeof ElInput>>();
const hotTags = ref<Array<string>>([]);

const userDialogVisible = ref(false);
const userSearchKeyword = ref("");
const userList = ref<Array<any>>([]);
const userLoading = ref(false);
const followUserList = ref<Array<any>>([]);

const topicDialogVisible = ref(false);
const topicSearchKeyword = ref("");
const topicList = ref<Array<any>>([]);
const topicLoading = ref(false);

// 网络图片上传相关
const netImgUrl = ref("");

const emojiDialogVisible = ref(false);
const emojiList = ref<string[]>([
  "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
  "🙂", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗",
  "😚", "😙", "😋", "😛", "😜", "🤪", "😝", "🤑",
  "🤗", "🤭", "🤫", "🤔", "🤐", "🤨", "😐", "😑",
  "😶", "😏", "😒", "🙄", "😬", "🤥", "😌", "😔",
  "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
  "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳",
  "😎", "🤓", "🧐", "😕", "😟", "🙁", "☹️", "😮",
  "😯", "😲", "😳", "🥺", "😦", "😧", "😨", "😰",
  "😥", "😢", "😭", "😱", "😖", "😣", "😞", "😓",
  "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈",
  "👿", "💀", "☠️", "💩", "🤡", "👹", "👺", "👻",
  "👽", "👾", "🤖", "😺", "😸", "😹", "😻", "😼",
  "😽", "🙀", "😿", "😾", "🙈", "🙉", "🙊", "💋",
  "💌", "💘", "💝", "💖", "💗", "💓", "💞", "💕",
  "💟", "❣️", "💔", "❤️", "🧡", "💛", "💚", "💙",
  "💜", "🤎", "🖤", "🤍", "💯", "💢", "💥", "💫",
  "💦", "💨", "🕳️", "💣", "💬", "🗯️", "💭", "💤",
  "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏",
  "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆",
  "🖕", "👇", "☝️", "👍", "👎", "✊", "👊", "🤛",
  "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️",
  "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂",
  "🦻", "👃", "🧠", "🦷", "🦴", "👀", "👁️", "👅",
  "👄", "💋", "🩸", "👶", "🧒", "👦", "👧", "🧑",
  "👱", "👨", "🧔", "👩", "🧓", "👴", "👵", "🙍",
  "🙎", "🙅", "🙆", "💁", "🙋", "🙇", "🤦", "🤷",
  "👨‍⚕️", "👩‍⚕️", "👨‍🎓", "👩‍🎓", "👨‍🏫", "👩‍🏫", "👨‍⚖️", "👩‍⚖️",
  "👨‍🌾", "👩‍🌾", "👨‍🍳", "👩‍🍳", "👨‍🔧", "👩‍🔧", "👨‍🏭", "👩‍🏭",
  "👨‍💼", "👩‍💼", "👨‍🔬", "👩‍🔬", "👨‍💻", "👩‍💻", "👨‍🎤", "👩‍🎤",
  "👨‍🎨", "👩‍🎨", "👨‍✈️", "👩‍✈️", "👨‍🚀", "👩‍🚀", "👨‍🚒", "👩‍🚒",
  "👮", "🕵️", "💂", "👷", "🤴", "👸", "👳", "👲",
  "🧕", "🤵", "👰", "🤰", "🤱", "👼", "🎅", "🤶",
  "🦸", "🦹", "🧙", "🧚", "🧛", "🧜", "🧝", "🧞",
  "🧟", "💆", "💇", "🚶", "🏃", "💃", "🕺", "🕴️",
  "👯", "🧖", "🧘", "🧗", "🤺", "🏇", "⛷️", "🏂",
  "🏌️", "🏄", "🚣", "🏊", "⛹️", "🏋️", "🚴", "🚵",
  "🤸", "🤼", "🤽", "🤾", "🤹", "🛀", "🛌", "👭",
  "👫", "👬", "💏", "💑", "👪", "🗣️", "👤", "👥",
  "👣", "🦰", "🦱", "🦲", "🦳", "🐶", "🐱", "🐭",
  "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁",
  "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊",
  "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆",
  "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝",
  "🐛", "🦋", "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️",
  "🕸️", "🦂", "🐢", "🐍", "🦎", "🦖", "🦕", "🐙",
  "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬",
  "🐳", "🐋", "🦈", "🐊", "🐅", "🐆", "🦓", "🦍",
  "🦧", "🐘", "🦛", "🦏", "🐪", "🐫", "🦒", "🦘",
  "🐃", "🐂", "🐄", "🐎", "🐖", "🐏", "🐑", "🦙",
  "🐐", "🦌", "🐕", "🐩", "🦮", "🐕‍🦺", "🐈", "🐓",
  "🦃", "🦚", "🦜", "🦢", "🦩", "🐇", "🦝", "🦨",
  "🦡", "🦦", "🦥", "🐁", "🐀", "🐿️", "🦔", "🐾",
  "🐉", "🐲", "🌵", "🎄", "🌲", "🌳", "🌴", "🌱",
  "🌿", "☘️", "🍀", "🎍", "🎋", "🍃", "🍂", "🍁",
  "🍄", "🐚", "🌾", "💐", "🌷", "🌹", "🥀", "🌺",
  "🌸", "🌼", "🌻", "🌞", "🌝", "🌛", "🌜", "🌚",
  "🌕", "🌖", "🌗", "🌘", "🌑", "🌒", "🌓", "🌔",
  "🌙", "🌎", "🌍", "🌏", "🪐", "💫", "⭐", "🌟",
  "✨", "⚡", "☄️", "💥", "🔥", "🌪️", "🌈", "☀️",
  "🌤️", "⛅", "🌥️", "☁️", "🌦️", "🌧️", "⛈️", "🌩️",
  "🌨️", "❄️", "☃️", "⛄", "🌬️", "💨", "💧", "💦",
  "☔", "☂️", "🌊", "🌫️", "🍏", "🍎", "🍐", "🍊",
  "🍋", "🍌", "🍉", "🍇", "🍓", "🍈", "🍒", "🍑",
  "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦",
  "🥬", "🥒", "🌶️", "🌽", "🥕", "🧄", "🧅", "🥔",
  "🍠", "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚",
  "🍳", "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖",
  "🦴", "🌭", "🍔", "🍟", "🍕", "🥪", "🥙", "🧆",
  "🌮", "🌯", "🥗", "🥘", "🥫", "🍝", "🍜", "🍲",
  "🍛", "🍣", "🍱", "🥟", "🦪", "🍤", "🍙", "🍚",
  "🍘", "🍥", "🥠", "🥮", "🍢", "🍡", "🍧", "🍨",
  "🍦", "🥧", "🧁", "🍰", "🎂", "🍮", "🍭", "🍬",
  "🍫", "🍿", "🍩", "🍪", "🌰", "🥜", "🍯", "🥛",
  "🍼", "☕", "🍵", "🧃", "🥤", "🍶", "🍺", "🍻",
  "🥂", "🍷", "🥃", "🍸", "🍹", "🧉", "🍾", "🧊",
  "🥄", "🍴", "🍽️", "🥣", "🥡", "🥢", "🧂", "⚽",
  "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏",
  "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏",
  "🥅", "⛳", "🪁", "🏹", "🎣", "🤿", "🥊", "🥋",
  "🎽", "🛹", "🛷", "⛸️", "🥌", "🎿", "⛷️", "🏂",
  "🪂", "🏋️", "🤼", "🤸", "⛹️", "🤺", "🤾", "🏌️",
  "🏇", "🧘", "🏄", "🏊", "🤽", "🚣", "🧗", "🚴",
  "🚵", "🎖️", "🏆", "🏅", "🥇", "🥈", "🥉", "⚽",
  "⚾", "🥎", "🏀", "🏐", "🏈", "🏉", "🎾", "🥏",
  "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏",
  "🥅", "⛳", "🪁", "🏹", "🎣", "🤿", "🥊", "🥋",
  "🥎", "🏀", "🏐", "🏈", "🏉", "🎾", "🥏", "🎱",
  "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🥅",
  "⛳", "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽",
  "🛹", "🛷", "⛸️", "🥌", "🎿", "⛷️", "🏂", "🪂",
  "🏋️", "🤼", "🤸", "⛹️", "🤺", "🤾", "🏌️", "🏇",
  "🧘", "🏄", "🏊", "🤽", "🚣", "🧗", "🚴", "🚵"
]);

const handleClose = (tag: string) => {
  dynamicTags.value.splice(dynamicTags.value.indexOf(tag), 1);
};

const handleInputBlur = () => {
  inputVisible.value = false;
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    InputRef.value!.input!.focus();
  });
};

const handleInputConfirm = () => {
  if (inputValue.value) {
    dynamicTags.value.push(inputValue.value);
  }
  inputVisible.value = false;
  inputValue.value = "";
};

const selectHotTag = (val: string) => {
  if (dynamicTags.value.length < 5) {
    dynamicTags.value.push(val);
  } else {
    ElMessage.warning("最多只能选择 5 个标签");
  }
};

const addTag = () => {
  topicDialogVisible.value = true;
  topicSearchKeyword.value = "";
  topicList.value = [];
  loadAllTopics();
};

const loadAllTopics = () => {
  topicLoading.value = true;
  getTagByKeyword(1, 20, "")
    .then((res) => {
      topicList.value = res.data.records || [];
    })
    .catch(() => {
      topicList.value = [];
    })
    .finally(() => {
      topicLoading.value = false;
    });
};

const searchTopics = () => {
  if (!topicSearchKeyword.value) {
    topicList.value = [];
    return;
  }
  topicLoading.value = true;
  getTagByKeyword(1, 20, topicSearchKeyword.value)
    .then((res) => {
      topicList.value = res.data.records || [];
    })
    .catch(() => {
      topicList.value = [];
    })
    .finally(() => {
      topicLoading.value = false;
    });
};

const selectTopic = (topic: any) => {
  const topicText = `#${topic.title}`;
  if (note.value.content) {
    note.value.content += topicText;
  } else {
    note.value.content = topicText;
  }
  topicDialogVisible.value = false;
  topicSearchKeyword.value = "";
  topicList.value = [];
};

const handleTopicDialogClose = (done: () => void) => {
  done();
};

const handleAtUser = () => {
  userDialogVisible.value = true;
  userSearchKeyword.value = "";
  userList.value = [];
  loadFollowUsers();
};

const handleEmoji = () => {
  emojiDialogVisible.value = true;
};

const loadFollowUsers = () => {
  userLoading.value = true;
  getNoticeFollower(1, 100)
    .then((res) => {
      followUserList.value = res.data.records || [];
      userList.value = followUserList.value;
    })
    .catch(() => {
      userList.value = [];
    })
    .finally(() => {
      userLoading.value = false;
    });
};

const searchUsers = () => {
  if (!userSearchKeyword.value) {
    userList.value = followUserList.value;
    return;
  }
  const keyword = userSearchKeyword.value.toLowerCase();
  userList.value = followUserList.value.filter((user: any) => 
    user.username && user.username.toLowerCase().includes(keyword)
  );
};

const selectUser = (user: any) => {
  const atText = `@${user.username}`;
  if (note.value.content) {
    note.value.content += atText;
  } else {
    note.value.content = atText;
  }
  userDialogVisible.value = false;
};

const selectEmoji = (emoji: string) => {
  if (note.value.content) {
    note.value.content += emoji;
  } else {
    note.value.content = emoji;
  }
  emojiDialogVisible.value = false;
};

// 添加网络图片
const addNetImg = async () => {
  if (!netImgUrl.value) {
    ElMessage.warning("请输入网络图片URL");
    return;
  }
  
  // 验证URL格式 - 更宽松的规则，允许所有HTTP/HTTPS URL
    const urlRegex = /^(https?:\/\/.*)$/i;
    if (!urlRegex.test(netImgUrl.value)) {
      ElMessage.error("请输入有效的图片URL");
      return;
    }
    
    console.log("URL验证通过:", netImgUrl.value);
  
  try {
    // 从URL获取文件名
    const url = new URL(netImgUrl.value);
    const pathname = url.pathname;
    const fileName = pathname.substring(pathname.lastIndexOf("/") + 1) || `net-img-${Date.now()}.jpg`;
    
    console.log("开始获取网络图片:", netImgUrl.value);
    
    // 使用工具函数将网络图片转换为文件对象
    const file = await getFileFromUrl(netImgUrl.value, fileName);
    
    console.log("网络图片获取成功:", file);
    
    // 添加到文件列表
    fileList.value.push({
      name: fileName,
      url: netImgUrl.value,
      raw: file
    });
    
    // 清空输入框
    netImgUrl.value = "";
    
    ElMessage.success("网络图片添加成功");
  } catch (error) {
    console.error("添加网络图片失败:", error);
    
    // 更详细的错误信息
    let errorMessage = "添加网络图片失败";
    if (error instanceof Error) {
      errorMessage += `: ${error.message}`;
    }
    
    ElMessage.error(errorMessage);
  }
};



const handleEmojiDialogClose = (done: () => void) => {
  done();
};

const handleChange = (ids: Array<any>) => {
  categoryList.value = ids;
  CascaderRef.value.togglePopperVisible();
};

const getHotTag = () => {
  getTagByKeyword(1, 10, "").then((res) => {
    const { records } = res.data;
    records.forEach((item: any) => {
      hotTags.value.push(item.title);
    });
  });
};

const getNoteByIdMethod = (noteId: string) => {
  getNoteById(noteId).then((res) => {
    const { data } = res;
    note.value = data;
    const urls = JSON.parse(data.urls);
    urls.forEach((item: string) => {
      const fileName = item.substring(item.lastIndexOf("/") + 1);

      getFileFromUrl(item, fileName).then((res: any) => {
        fileList.value.push({ name: fileName, url: item, raw: res });
      });
    });
    categoryList.value.push(data.cpid);
    categoryList.value.push(data.cid);

    data.tagList.forEach((item: any) => {
      dynamicTags.value.push(item.title);
    });
  });
};

// 上传图片功能
const pubslish = () => {
  //验证
  if (fileList.value.length <= 0 || note.value.title === null || categoryList.value.length <= 0) {
    ElMessage.error("请选择图片，标签，分类～");
    return;
  }
  pushLoading.value = true;
  let params = new FormData();
  //注意此处对文件数组进行了参数循环添加

  fileList.value.forEach((file: any) => {
    params.append("uploadFiles", file.raw);
  });

  note.value.count = fileList.value.length;
  note.value.type = 1;
  // note.value.content = document.getElementById("post-textarea")!.innerHTML.replace(/<[^>]*>[^<]*(<[^>]*>)?/gi, "");
  note.value.cpid = categoryList.value[0];
  note.value.cid = categoryList.value[1];
  note.value.tagList = dynamicTags.value;
  const coverImage = new Image();
  coverImage.src = fileList.value[0].url!;
  coverImage.onload = () => {
    const size = coverImage.width / coverImage.height;
    note.value.noteCoverHeight = size >= 1.3 ? 200 : 300;
    const noteData = JSON.stringify(note.value);
    params.append("noteData", noteData);
    if (note.value.id !== null && note.value.id !== undefined) {
      updateNote(params);
    } else {
      saveNote(params);
    }
  };
};

const updateNote = (params: FormData) => {
  updateNoteByDTO(params)
    .then(() => {
      resetData();
      ElMessage.success("修改成功");
    })
    .catch(() => {
      ElMessage.error("修改失败");
    })
    .finally(() => {
      pushLoading.value = false;
    });
};

const saveNote = (params: FormData) => {
  saveNoteByDTO(params)
    .then(() => {
      resetData();
      ElMessage.success("发布成功,请等待审核结果");
    })
    .catch(() => {
      ElMessage.error("发布失败");
    })
    .finally(() => {
      pushLoading.value = false;
    });
};

const resetData = () => {
  note.value = {};
  categoryList.value = [];
  fileList.value = [];
  pushLoading.value = false;
  dynamicTags.value = [];
};

const initData = () => {
  isLogin.value = userStore.isLogin();
  if (isLogin.value) {
    const noteId = route.query.noteId as string;
    if (noteId !== "" && noteId !== undefined) {
      getNoteByIdMethod(noteId);
    }
    getCategoryTreeData().then((res) => {
      options.value = res.data;
    });
    getHotTag();
  }
};

initData();
</script>

<style lang="less" scoped>
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  transition: all 0.3s ease;
  overflow: hidden;
}

:deep(.el-upload-list--picture-card .el-upload-list__item:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-upload-list__item.is-success .el-upload-list__item-status-label) {
  display: none;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 2px dashed #3a64ff;
  background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
  transition: all 0.3s ease;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #5a84ff;
  background: linear-gradient(135deg, #e0e8ff 0%, #fff 100%);
  transform: scale(1.02);
}

:deep(.el-upload--picture-card .el-icon) {
  color: #3a64ff;
  font-size: 28px;
}

a {
  text-decoration: none;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #eee;
  margin: 0;
}

:deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  font-size: 20px;
}

:deep(.el-dialog__close) {
  color: #999;
  transition: all 0.3s ease;
}

:deep(.el-dialog__close:hover) {
  color: #3a64ff;
  transform: rotate(90deg);
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #999;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #3a64ff;
}

:deep(.el-overlay) {
  background-color: rgba(0, 0, 0, 0.5);
}

:deep(.el-dialog__wrapper) {
  transition: all 0.3s ease;
}

:deep(.el-dialog__wrapper .el-dialog) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.container {
  flex: 1;
  padding-top: 72px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: calc(100vh - 72px);

  .push-container {
    margin-left: 11vw;
    margin-top: 1vw;
    width: 900px;
    border-radius: 16px;
    box-sizing: border-box;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    background: #fff;
    overflow: hidden;
    transition: all 0.3s ease;
  }

  .push-container:hover {
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }

  .header {
    padding: 20px 24px;
    line-height: 20px;
    font-size: 18px;
    font-weight: 600;
    background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
    color: #fff;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(58, 100, 255, 0.2);

    .header-icon {
      display: inline-block;
      width: 4px;
      height: 20px;
      background: #fff;
      border-radius: 2px;
      margin-right: 12px;
      box-shadow: 0 0 8px rgba(255, 255, 255, 0.6);
    }
  }

  .img-list {
    width: 850px;
    margin: auto;
    padding: 20px 6px 10px;
    
    .net-img-upload {
      margin-top: 16px;
      display: flex;
      align-items: center;
      padding: 12px;
      background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
      border-radius: 12px;
      border: 1px solid #e0e8ff;
      
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 4px 12px rgba(58, 100, 255, 0.15);
        }
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba(58, 100, 255, 0.2);
        }
      }
      
      :deep(.el-button--primary) {
        background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
        border: none;
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          background: linear-gradient(135deg, #5a84ff 0%, #3a64ff 100%);
          box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
          transform: translateY(-2px);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
  }

  .push-content {
    padding: 24px 24px;
    position: relative;

    .hot-tag {
      margin-top: 24px;
      padding: 20px;
      background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
      border-radius: 12px;
      border: 1px solid #e0e8ff;

      .tag-title-text {
        font-size: 0.875rem;
        color: #3a64ff;
        font-weight: 600;
        margin: 0.125rem 0;
      }

      .hot-tag-item {
        cursor: pointer;
        margin: 0.5rem 0.5rem 0 0.5rem;
        background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
        border: none;
        color: #fff;
        transition: all 0.3s ease;
        padding: 8px 16px;
      }

      .hot-tag-item:hover {
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
      }
    }

    .tag-list {
      margin: 2rem 0;

      .tag-item {
        margin-right: 0.5rem;
        background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
        border: none;
        color: #fff;
        padding: 8px 16px;
        font-size: 14px;
        transition: all 0.3s ease;
      }

      .tag-item:hover {
        transform: scale(1.05);
        box-shadow: 0 2px 8px rgba(58, 100, 255, 0.3);
      }
    }

    .input-title {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 500;
    }

    :deep(.input-title .el-input__wrapper) {
      border-radius: 10px;
      padding: 12px 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
    }

    :deep(.input-title .el-input__wrapper:hover) {
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.15);
    }

    :deep(.input-title .el-input__wrapper.is-focus) {
      box-shadow: 0 0 0 2px rgba(58, 100, 255, 0.2);
    }

    :deep(.el-textarea__inner) {
      border-radius: 10px;
      padding: 12px 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
      font-size: 14px;
      line-height: 1.6;
    }

    :deep(.el-textarea__inner:hover) {
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.15);
    }

    :deep(.el-textarea__inner:focus) {
      box-shadow: 0 0 0 2px rgba(58, 100, 255, 0.2);
    }

    .input-content {
      font-size: 12px;
    }

    .post-content:empty::before {
      content: attr(placeholder);
      color: #ccc;
      font-size: 14px;
    }

    .post-content {
      cursor: text;
      margin-top: 10px;
      width: 100%;
      min-height: 90px;
      max-height: 300px;
      margin-bottom: 10px;
      background: #fff;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      padding: 6px 12px 22px;
      outline: none;
      overflow-y: auto;
      text-rendering: optimizeLegibility;
      font-size: 14px;
      line-height: 22px;
    }

    .post-content:focus,
    .post-content:hover {
      border: 1px solid #3a64ff;
      box-shadow: 0 0 0 3px rgba(58, 100, 255, 0.1);
    }
  }

  .btns {
    padding: 8px 24px 16px 24px;

    button {
      min-width: 70px;
      width: 70px;
      margin: 0 8px 0 0;
      height: 28px;
    }

    .css-fm44j {
      -webkit-font-smoothing: antialiased;
      appearance: none;
      font-family:
        RedNum,
        RedZh,
        RedEn,
        -apple-system;
      vertical-align: middle;
      text-decoration: none;
      border: 1px solid #3a64ff;
      outline: none;
      user-select: none;
      cursor: pointer;
      display: inline-flex;
      -webkit-box-pack: center;
      justify-content: center;
      -webkit-box-align: center;
      align-items: center;
      margin-right: 16px;
      border-radius: 20px;
      background: linear-gradient(135deg, #fff 0%, #f0f4ff 100%);
      color: #3a64ff;
      height: 28px;
      font-size: 13px;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .css-fm44j:hover {
      background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
      color: #fff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
    }

    .smile {
      width: 16px;
      height: 16px;
      background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
      border-radius: 50%;
      display: inline-block;
      margin-right: 4px;
    }
  }

  .categorys {
    padding: 0 24px 24px 24px;

    :deep(.el-cascader) {
      width: 100%;
    }

    :deep(.el-cascader .el-input__wrapper) {
      border-radius: 10px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
      padding: 12px 16px;
    }

    :deep(.el-cascader .el-input__inner) {
      font-size: 16px;
      color: #3a64ff;
    }

    :deep(.el-cascader .el-input__inner::placeholder) {
      color: #999;
    }

    :deep(.el-cascader .el-input__wrapper:hover) {
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.15);
    }

    :deep(.el-cascader .el-input__wrapper.is-focus) {
      box-shadow: 0 0 0 2px rgba(58, 100, 255, 0.2);
    }

    :deep(.el-cascader-node) {
      padding: 0 20px;
    }

    :deep(.el-cascader-node__label) {
      flex: 1;
      cursor: pointer;
    }

    :deep(.el-radio) {
      width: 100%;
      height: 100%;
      margin-right: 0;
    }

    :deep(.el-radio__input) {
      position: absolute;
      right: 10px;
    }

    :deep(.el-cascader-node__postfix) {
      position: absolute;
      right: 10px;
    }
  }

  .submit {
    padding: 16px 24px 24px 24px;
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
    border-top: 1px solid #e0e8ff;

    button {
      width: 110px;
      height: 40px;
      font-size: 16px;
      display: inline-block;
      margin-left: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-weight: 600;
    }

    button:hover {
      transform: translateY(-2px);
    }

    .publishBtn {
      background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
      color: #fff;
      border-radius: 24px;
      border: none;
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
    }

    .publishBtn:hover {
      box-shadow: 0 6px 20px rgba(58, 100, 255, 0.4);
      background: linear-gradient(135deg, #5a84ff 0%, #3a64ff 100%);
    }

    .publishBtn:active {
      transform: translateY(0);
    }

    .clearBtn {
      border-radius: 24px;
      margin-left: 10px;
      border: 2px solid #3a64ff;
      background: #fff;
      color: #3a64ff;
      box-shadow: 0 2px 8px rgba(58, 100, 255, 0.1);
    }

    .clearBtn:hover {
      background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
      color: #fff;
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
    }
  }
}

.topic-list {
  max-height: 400px;
  overflow-y: auto;
  margin-top: 16px;

  .topic-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
      transform: translateX(4px);
    }

    .topic-title {
      font-size: 14px;
      color: #333;
      font-weight: 500;
    }

    .topic-count {
      font-size: 12px;
      color: #999;
    }
  }
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
  margin-top: 16px;

  .user-item {
    display: flex;
    align-items: center;
    padding: 12px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
      transform: translateX(4px);
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid #e0e8ff;
      margin-right: 12px;
    }

    .user-name {
      font-size: 14px;
      color: #333;
      font-weight: 500;
    }
  }
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;

  .emoji-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    font-size: 24px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.3s ease;
    background: #f5f7fa;

    &:hover {
      background: linear-gradient(135deg, #3a64ff 0%, #5a84ff 100%);
      transform: scale(1.2);
      box-shadow: 0 4px 12px rgba(58, 100, 255, 0.3);
    }
  }
}
</style>
