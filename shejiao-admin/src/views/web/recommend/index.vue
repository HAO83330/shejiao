<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>推荐算法管理</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <!-- 算法配置 -->
        <el-col :span="12">
          <el-card shadow="hover" class="config-card">
            <template #header>
              <div class="config-header">
                <span>算法参数配置</span>
              </div>
            </template>
            
            <el-table :data="algorithmParams" style="width: 100%">
              <el-table-column prop="paramName" label="参数名称" width="180" />
              <el-table-column prop="value" label="参数值">
                <template #default="scope">
                  <el-input-number 
                    v-model="scope.row.value" 
                    :min="scope.row.min" 
                    :max="scope.row.max" 
                    :step="scope.row.step" 
                    :precision="scope.row.precision" 
                    @change="updateAlgorithmParam(scope.row)" 
                  />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
            </el-table>
            
            <div style="margin-top: 20px; text-align: right">
              <el-button type="primary" @click="saveConfig">保存配置</el-button>
              <el-button @click="resetConfig">重置</el-button>
            </div>
          </el-card>
        </el-col>
        
        <!-- 算法信息 -->
        <el-col :span="12">
          <el-card shadow="hover" class="info-card">
            <template #header>
              <div class="info-header">
                <span>算法信息</span>
              </div>
            </template>
            
            <el-descriptions :column="1" border>
              <el-descriptions-item label="算法类型">
                <el-tag type="primary">混合协同过滤</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="核心实现">
                WebRecommendServiceImpl.java
              </el-descriptions-item>
              <el-descriptions-item label="数据来源">
                点赞、收藏、评论、浏览、搜索、关注
              </el-descriptions-item>
              <el-descriptions-item label="时间衰减">
                指数衰减 (半衰期约{{ calculateHalfLife() }}天)
              </el-descriptions-item>
              <el-descriptions-item label="相似度计算">
                调整后的余弦相似度
              </el-descriptions-item>
              <el-descriptions-item label="Fallback策略">
                热门推荐 → 最新推荐
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <!-- 行为权重配置 -->
          <el-card shadow="hover" class="weight-card" style="margin-top: 20px;">
            <template #header>
              <div class="weight-header">
                <span>行为权重配置</span>
                <div>
                  <el-button type="primary" size="small" @click="saveBehaviorWeights">保存配置</el-button>
                  <el-button size="small" @click="resetBehaviorWeights">重置</el-button>
                </div>
              </div>
            </template>
            
            <el-table :data="behaviorWeights" style="width: 100%">
              <el-table-column prop="behavior" label="行为类型" width="180" />
              <el-table-column prop="weight" label="权重值">
                <template #default="scope">
                  <el-input-number v-model="scope.row.weight" :min="0" :max="5" :step="0.1" :precision="1" @change="updateBehaviorWeight(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 推荐指标 -->
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="metric-header">
            <span>推荐算法指标</span>
            <el-button type="primary" size="small" @click="loadMetrics">刷新指标</el-button>
          </div>
        </template>
        
        <div v-if="metrics.length > 0">
          <el-table :data="metrics" style="width: 100%">
              <el-table-column prop="algorithmType" label="算法类型" width="120">
                <template #default="scope">
                  <el-tag :type="getAlgorithmTypeTagType(scope.row.algorithmType)">
                    {{ getAlgorithmTypeText(scope.row.algorithmType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="precision" label="准确率" width="100">
                <template #default="scope">
                  {{ (scope.row.precision * 100).toFixed(2) }}%
                </template>
              </el-table-column>
              <el-table-column prop="recall" label="召回率" width="100">
                <template #default="scope">
                  {{ (scope.row.recall * 100).toFixed(2) }}%
                </template>
              </el-table-column>
              <el-table-column prop="f1Score" label="F1分数" width="100">
                <template #default="scope">
                  {{ scope.row.f1Score.toFixed(3) }}
                </template>
              </el-table-column>
              <el-table-column prop="clickThroughRate" label="点击率" width="100">
                <template #default="scope">
                  {{ (scope.row.clickThroughRate * 100).toFixed(2) }}%
                </template>
              </el-table-column>
              <el-table-column prop="collectCount" label="收藏数量" width="100" />
              <el-table-column prop="commentCount" label="评论数量" width="100" />
              <el-table-column prop="recommendCount" label="推荐数量" width="100" />
              <el-table-column prop="clickCount" label="点击数量" width="100" />
              <el-table-column prop="calculateTime" label="计算时间" width="180" />
            </el-table>
        </div>
        <div v-else style="text-align: center; padding: 40px;">
          <el-empty description="暂无指标数据" />
        </div>
      </el-card>

      <!-- 算法测试 -->
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="test-header">
            <span>算法测试</span>
          </div>
        </template>
        
        <el-form :model="testForm" label-width="120px">
          <el-form-item label="用户ID">
            <el-input v-model="testForm.userId" placeholder="请输入用户ID" />
          </el-form-item>
          
          <el-form-item label="推荐数量">
            <el-input-number v-model="testForm.recommendCount" :min="1" :max="100" :step="10" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="testRecommend">测试推荐</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 用户信息 -->
        <div v-if="userInfo.id" style="margin-top: 20px;">
          <el-card shadow="hover">
            <template #header>
              <div class="user-info-header">
                <span>用户信息</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="用户ID">
                    {{ userInfo.id }}
                  </el-descriptions-item>
                  <el-descriptions-item label="用户名">
                    {{ userInfo.username }}
                  </el-descriptions-item>
                  <el-descriptions-item label="点赞笔记数">
                    {{ userInfo.likedNotes.length }}
                  </el-descriptions-item>
                  <el-descriptions-item label="收藏笔记数">
                    {{ userInfo.collectedNotes.length }}
                  </el-descriptions-item>
                  <el-descriptions-item label="关注用户数">
                    {{ userInfo.followedUsers.length }}
                  </el-descriptions-item>
                  <el-descriptions-item label="粉丝数">
                    {{ userInfo.followers.length }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-col>
              
              <el-col :span="12">
                <el-tabs type="border-card">
                  <el-tab-pane label="点赞笔记">
                    <el-table :data="userInfo.likedNotes" style="width: 100%">
                      <el-table-column prop="id" label="笔记ID" width="100" />
                      <el-table-column prop="title" label="标题" />
                    </el-table>
                  </el-tab-pane>
                  <el-tab-pane label="收藏笔记">
                    <el-table :data="userInfo.collectedNotes" style="width: 100%">
                      <el-table-column prop="id" label="笔记ID" width="100" />
                      <el-table-column prop="title" label="标题" />
                    </el-table>
                  </el-tab-pane>
                  <el-tab-pane label="关注用户">
                    <el-table :data="userInfo.followedUsers" style="width: 100%">
                      <el-table-column prop="id" label="用户ID" width="100" />
                      <el-table-column prop="username" label="用户名" />
                    </el-table>
                  </el-tab-pane>
                  <el-tab-pane label="粉丝">
                    <el-table :data="userInfo.followers" style="width: 100%">
                      <el-table-column prop="id" label="用户ID" width="100" />
                      <el-table-column prop="username" label="用户名" />
                    </el-table>
                  </el-tab-pane>
                </el-tabs>
              </el-col>
            </el-row>
          </el-card>
        </div>
        
        <!-- 测试结果 -->
        <div v-if="testResult.length > 0" style="margin-top: 20px;">
          <el-table :data="testResult" style="width: 100%">
            <el-table-column prop="id" label="笔记ID" width="120" />
            <el-table-column prop="categoryName" label="分类" width="150" />
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="urls" label="图片" width="150">
              <template #default="scope">
                <div v-if="scope.row.urls && getImageArray(scope.row.urls).length > 1" class="image-group">
                  <el-avatar
                    v-for="(url, index) in getImageArray(scope.row.urls)"
                    :key="index"
                    :size="60"
                    :src="url"
                    shape="square"
                    @mouseover="showPreview(url, $event)"
                    @mouseleave="hidePreview"
                    class="image-item"
                  />
                </div>
                <el-avatar
                  v-else
                  :size="60"
                  :src="getFirstImage(scope.row.urls)"
                  shape="square"
                  @mouseover="showPreview(getFirstImage(scope.row.urls), $event)"
                  @mouseleave="hidePreview"
                />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="150">
              <template #default="scope">
                <el-popover
                  placement="top"
                  :width="800"
                  trigger="hover"
                >
                  <template #reference>
                    <span class="content-ellipsis">{{ scope.row.content && scope.row.content.length > 100 ? scope.row.content.substring(0, 100) + '...' : scope.row.content || '-' }}</span>
                  </template>
                  <div class="popover-content">
                    {{ scope.row.content || '-' }}
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column prop="likeCount" label="点赞数" width="100" />
            <el-table-column prop="collectionCount" label="收藏数" width="100" />
            <el-table-column prop="commentCount" label="评论数" width="100" />
            <el-table-column prop="viewCount" label="浏览数" width="100" />
            <el-table-column prop="time" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.time) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 放大预览图片的容器 -->
        <div v-if="previewVisible" class="image-preview" :style="previewStyle">
          <img :src="previewSrc" alt="Preview" />
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import { getRecommendConfig, updateRecommendConfig, resetRecommendConfig, getRecommendMetrics, testRecommend, getUserInfo } from '@/api/web/recommend';

export default {
  name: 'RecommendConfig',
  data() {
    return {
      loading: false,
      configForm: {
        userCfk: 10,
        itemCfk: 15,
        n: 100,
        lambda: 0.04,
        userCfWeight: 0.6,
        itemCfWeight: 0.4,
        hotWeight: 0.3,
        latestWeight: 0.2,
        collectWeight: 3.0,
        commentWeight: 2.5,
        searchWeight: 1.5,
        likeWeight: 1.5,
        visitWeight: 0.5,
        followWeight: 2.0
      },
      algorithmParams: [
        {
          paramName: '用户CF相似用户数(k)',
          value: 10,
          min: 1,
          max: 50,
          step: 1,
          precision: 0,
          description: '基于用户的协同过滤中，考虑的相似用户数量',
          key: 'userCfk'
        },
        {
          paramName: '物品CF相似物品数(k)',
          value: 15,
          min: 1,
          max: 50,
          step: 1,
          precision: 0,
          description: '基于物品的协同过滤中，考虑的相似物品数量',
          key: 'itemCfk'
        },
        {
          paramName: '推荐数量(n)',
          value: 100,
          min: 1,
          max: 500,
          step: 10,
          precision: 0,
          description: '每次推荐返回的笔记数量',
          key: 'n'
        },
        {
          paramName: '时间衰减系数(lambda)',
          value: 0.04,
          min: 0.01,
          max: 0.1,
          step: 0.01,
          precision: 2,
          description: '控制历史行为的时间衰减程度，值越大衰减越快',
          key: 'lambda'
        },
        {
          paramName: '用户CF权重',
          value: 0.6,
          min: 0,
          max: 1,
          step: 0.1,
          precision: 1,
          description: '混合协同过滤中用户CF的权重',
          key: 'userCfWeight'
        },
        {
          paramName: '物品CF权重',
          value: 0.4,
          min: 0,
          max: 1,
          step: 0.1,
          precision: 1,
          description: '混合协同过滤中物品CF的权重',
          key: 'itemCfWeight'
        },
        {
          paramName: '热门推荐权重',
          value: 0.3,
          min: 0,
          max: 1,
          step: 0.1,
          precision: 1,
          description: 'Fallback策略中热门推荐的权重',
          key: 'hotWeight'
        },
        {
          paramName: '最新推荐权重',
          value: 0.2,
          min: 0,
          max: 1,
          step: 0.1,
          precision: 1,
          description: 'Fallback策略中最新推荐的权重',
          key: 'latestWeight'
        }
      ],
      behaviorWeights: [
        { behavior: '收藏', weight: 3.0, description: '用户收藏行为，权重最高' },
        { behavior: '评论', weight: 2.5, description: '用户评论行为，权重较高' },
        { behavior: '关注', weight: 2.0, description: '用户关注行为，权重较高' },
        { behavior: '搜索', weight: 1.5, description: '用户搜索行为，权重中等' },
        { behavior: '点赞', weight: 1.5, description: '用户点赞行为，权重中等' },
        { behavior: '浏览', weight: 0.5, description: '用户浏览行为，权重较低' }
      ],
      testForm: {
        userId: '',
        recommendCount: 20
      },
      testResult: [],
      userInfo: {
        id: null,
        username: '',
        likedNotes: [],
        collectedNotes: [],
        followedUsers: [],
        followers: []
      },
      metrics: [],
      previewVisible: false,
      previewSrc: '',
      previewStyle: { top: '0px', left: '0px' }
    }
  },
  created() {
    this.loadConfig();
    this.loadMetrics();
  },
  methods: {
    async loadConfig() {
      this.loading = true;
      try {
        const response = await getRecommendConfig();
        if (response.code === 200) {
          const config = response.data;
          this.configForm = {
            userCfk: config.userCfk,
            itemCfk: config.itemCfk,
            n: config.n,
            lambda: config.lambda,
            userCfWeight: config.userCfWeight,
            itemCfWeight: config.itemCfWeight,
            hotWeight: config.hotWeight,
            latestWeight: config.latestWeight,
            collectWeight: config.collectWeight,
            commentWeight: config.commentWeight,
            searchWeight: config.searchWeight,
            likeWeight: config.likeWeight,
            visitWeight: config.visitWeight,
            followWeight: config.followWeight
          };
          // 更新算法参数
          this.algorithmParams.forEach(param => {
            if (config[param.key] !== undefined) {
              param.value = config[param.key];
            }
          });
          // 更新行为权重
          this.behaviorWeights[0].weight = config.collectWeight;
          this.behaviorWeights[1].weight = config.commentWeight;
          this.behaviorWeights[2].weight = config.followWeight;
          this.behaviorWeights[3].weight = config.searchWeight;
          this.behaviorWeights[4].weight = config.likeWeight;
          this.behaviorWeights[5].weight = config.visitWeight;
        }
      } catch (error) {
        this.$message.error('获取配置失败');
        console.error('获取配置失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async saveConfig() {
      this.loading = true;
      try {
        // 更新配置表单中的行为权重
        this.configForm.collectWeight = this.behaviorWeights[0].weight;
        this.configForm.commentWeight = this.behaviorWeights[1].weight;
        this.configForm.followWeight = this.behaviorWeights[2].weight;
        this.configForm.searchWeight = this.behaviorWeights[3].weight;
        this.configForm.likeWeight = this.behaviorWeights[4].weight;
        this.configForm.visitWeight = this.behaviorWeights[5].weight;
        
        const response = await updateRecommendConfig(this.configForm);
        if (response.code === 200) {
          this.$message.success('配置保存成功');
        }
      } catch (error) {
        this.$message.error('保存配置失败');
        console.error('保存配置失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async resetConfig() {
      this.loading = true;
      try {
        const response = await resetRecommendConfig();
        if (response.code === 200) {
          this.$message.success('配置已重置为默认值');
          this.loadConfig();
        }
      } catch (error) {
        this.$message.error('重置配置失败');
        console.error('重置配置失败:', error);
      } finally {
        this.loading = false;
      }
    },
    updateBehaviorWeight(row) {
      console.log('更新行为权重:', row);
    },
    updateAlgorithmParam(row) {
      if (row.key && this.configForm.hasOwnProperty(row.key)) {
        this.configForm[row.key] = row.value;
      }
    },
    async testRecommend() {
      if (!this.testForm.userId) {
        this.$message.warning('请输入用户ID');
        return;
      }
      
      this.loading = true;
      try {
        // 先获取用户信息
        const userInfoResponse = await getUserInfo(this.testForm.userId);
        if (userInfoResponse.code === 200) {
          this.userInfo = userInfoResponse.data;
        } else if (userInfoResponse.code === 601) {
          this.$message.warning(userInfoResponse.msg);
          this.loading = false;
          return;
        }
        
        // 然后获取推荐结果
        const response = await testRecommend(this.testForm.userId, this.testForm.recommendCount);
        if (response.code === 200) {
          this.testResult = response.data;
          // 打印返回的数据结构，查看分类信息
          console.log('推荐结果数据:', response.data);
          this.$message.success('测试完成，共返回 ' + this.testResult.length + ' 条推荐');
        } else if (response.code === 601) { // 601 是警告状态码
          this.testResult = response.data;
          this.$message.warning(response.msg);
        }
      } catch (error) {
        this.$message.error('测试失败');
        console.error('测试失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async loadMetrics() {
      this.loading = true;
      try {
        const response = await getRecommendMetrics();
        if (response.code === 200) {
          this.metrics = response.data;
        }
      } catch (error) {
        this.$message.error('获取指标失败');
        console.error('获取指标失败:', error);
      } finally {
        this.loading = false;
      }
    },
    getAlgorithmTypeText(type) {
      const typeMap = {
        'hybrid_cf': '混合CF',
        'user_cf': '用户CF',
        'item_cf': '物品CF'
      };
      return typeMap[type] || type;
    },
    getAlgorithmTypeTagType(type) {
      const typeMap = {
        'hybrid_cf': 'primary',
        'user_cf': 'success',
        'item_cf': 'warning'
      };
      return typeMap[type] || 'info';
    },
    formatDate(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    getFirstImage(urls) {
      if (!urls) return '';
      const imageArray = this.getImageArray(urls);
      return imageArray.length > 0 ? imageArray[0] : '';
    },
    getImageArray(urls) {
      if (!urls) return [];
      let url = urls;
      // 清理URL中的无效字符
      url = url.replace(/^\["'`\s]+|\["'`\s]+$/g, '');
      // 如果是数组格式，提取所有元素
      if (url.startsWith('[')) {
        try {
          const urlArray = JSON.parse(url);
          if (Array.isArray(urlArray)) {
            return urlArray.map(item => item.replace(/^['`\s]+|['`\s]+$/g, '')).filter(Boolean);
          }
        } catch (e) {
          console.error('解析URL数组失败:', e);
        }
      }
      // 如果包含多个URL，按逗号分割
      if (url.includes(',')) {
        return url.split(',').map(item => item.replace(/^['`\s]+|['`\s]+$/g, '')).filter(Boolean);
      }
      // 单个URL
      url = url.replace(/^['`\s]+|['`\s]+$/g, '');
      return url ? [url] : [];
    },
    showPreview(src, event) {
      if (!src) return;
      this.previewSrc = src;
      this.previewVisible = true;
      this.previewStyle.top = event.clientY + 10 + "px";
      this.previewStyle.left = event.clientX + 10 + "px";
    },
    hidePreview() {
      this.previewVisible = false;
      this.previewSrc = '';
    },
    calculateHalfLife() {
      // 半衰期计算公式: ln(2) / lambda
      const lambda = this.configForm.lambda || 0.04;
      const halfLife = Math.log(2) / lambda;
      return halfLife.toFixed(1);
    },
    async saveBehaviorWeights() {
      this.loading = true;
      try {
        // 更新配置表单中的行为权重
        this.configForm.collectWeight = this.behaviorWeights[0].weight;
        this.configForm.commentWeight = this.behaviorWeights[1].weight;
        this.configForm.followWeight = this.behaviorWeights[2].weight;
        this.configForm.searchWeight = this.behaviorWeights[3].weight;
        this.configForm.likeWeight = this.behaviorWeights[4].weight;
        this.configForm.visitWeight = this.behaviorWeights[5].weight;
        
        const response = await updateRecommendConfig(this.configForm);
        if (response.code === 200) {
          this.$message.success('行为权重配置保存成功');
        }
      } catch (error) {
        this.$message.error('保存配置失败');
        console.error('保存配置失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async resetBehaviorWeights() {
      this.loading = true;
      try {
        const response = await resetRecommendConfig();
        if (response.code === 200) {
          this.$message.success('行为权重配置已重置为默认值');
          this.loadConfig();
        }
      } catch (error) {
        this.$message.error('重置配置失败');
        console.error('重置配置失败:', error);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-card,
.info-card,
.weight-card {
  margin-bottom: 20px;
}

.config-header,
.info-header,
.weight-header,
.test-header,
.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-preview {
  position: fixed;
  z-index: 1000;
  border: 1px solid #ddd;
  background-color: #fff;
  padding: 5px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.image-preview img {
  max-width: 300px;
  max-height: 300px;
}

.image-group {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.image-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.content-ellipsis {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.popover-content {
  font-size: 15px;
  word-break: break-word;
  line-height: 1.4;
}

:deep(.el-popover) {
  border: 1px solid #000 !important;
  background-color: #fff !important;
  color: #000 !important;
  padding: 8px 12px !important;
  border-radius: 4px !important;
}
</style>