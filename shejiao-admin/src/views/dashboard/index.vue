<template>
  <div class="dashboard-editor-container">

    <!-- 导出按钮 -->
    <el-row class="mb-4">
      <el-col :span="24" class="text-right">
        <el-button
          type="primary"
          plain
          icon="Download"
          @click="handleExport"
        >
          导出数据
        </el-button>
      </el-col>
    </el-row>

    <!-- 顶部内容 -->
    <el-row class="panel-group" :gutter="40">
      <!-- IP数 -->
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="btnClick('1')">
          <div class="card-panel-icon-wrapper icon-money">
            <svg-icon icon-class="eye" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日IP数：</div>
            <CountTo
              class="card-panel-num"
              :startVal="0"
              :endVal="visitAddTotal"
              :duration="3200"
            />
          </div>
        </div>
      </el-col>
      <!-- 用户数 -->
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="btnClick('2')">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="peoples" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">社交用户数:</div>
            <CountTo
              class="card-panel-num"
              :startVal="0"
              :endVal="userTotal"
              :duration="2600"
            />
          </div>
        </div>
      </el-col>
      <!-- 笔记数 -->
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="btnClick('4')">
          <div class="card-panel-icon-wrapper icon-shoppingCard">
            <svg-icon icon-class="form" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">笔记数:</div>
            <CountTo
              class="card-panel-num"
              :startVal="0"
              :endVal="blogTotal"
              :duration="3200"
            />
          </div>
        </div>
      </el-col>
      <!-- 评论数 -->
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel" @click="btnClick('3')">
          <div class="card-panel-icon-wrapper icon-message">
            <svg-icon icon-class="message" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">评论数：</div>
            <CountTo
              class="card-panel-num"
              :startVal="0"
              :endVal="commentTotal"
              :duration="3000"
            />
          </div>
        </div>
      </el-col>
    </el-row>

    <!--文章贡献度-->
    <el-row>
      <CalendarChart />
    </el-row>

    <!-- 访问趋势折线图 -->
    <el-row>
      <el-col :xs="24" :sm="24" :lg="24">
        <div class="chart-wrapper">
          <LineChart v-if="showLineChart" :chartData="lineChartData" title="访问趋势" />
        </div>
      </el-col>
    </el-row>

    <!-- 分类图-->
    <el-row :gutter="32">
      <el-col :xs="24" :sm="24" :lg="24">
        <div class="chart-wrapper">
          <PieChart
            ref="blogSortPie"
            @clickPie="clickBlogSortPie"
            v-if="showPieBlogSortChart"
            :value="blogCountByBlogSort"
            :tagName="blogSortNameArray"
            title="笔记类型占比"
          />
        </div>
      </el-col>

      <!-- <el-col :xs="24" :sm="24" :lg="8">
        <div class="chart-wrapper">
          <PieChart
            v-if="showPieChart"
            @clickPie="clickBlogTagPie"
            :value="blogCountByTag"
            :tagName="tagNameArray"
          />
        </div>
      </el-col> -->
    </el-row>

    <!-- 用户增长趋势图 -->
    <el-row>
      <el-col :xs="24" :sm="24" :lg="24">
        <div class="chart-wrapper">
          <div ref="userGrowthChart" style="width: 100%; height: 350px;"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import * as echarts from "echarts";
import "echarts/theme/macarons";
import {
  init,
  getVisitByWeek,
  getBlogCountByTag,
  getBlogCountByBlogSort,
  getUserGrowthTrend,
  exportDashboardData,
} from "@/api/web/index";
import { list as listLoginInfor } from "@/api/monitor/logininfor";
import { ElMessage } from "element-plus";
import CountTo from "@/components/CountTo/index.js";
import GithubCorner from "@/components/GithubCorner";
import CalendarChart from "@/components/CalendarChart";
import PieChart from "@/components/PieChart";
import LineChart from "@/components/LineChart";

const router = useRouter();
const userGrowthChart = ref(null);
let userGrowthChartInstance = null;

const blogTotal = ref(0);
const commentTotal = ref(0);
const userTotal = ref(0);
const visitAddTotal = ref(0);

const lineChartData = ref({
  date: [],
  expectedData: [],
  actualData: [],
});
const blogCountByTag = ref({});
const blogCountByBlogSort = ref({});
const tagNameArray = ref([]);
const blogSortNameArray = ref([]);
const showLineChart = ref(false);
const showPieChart = ref(false);
const showPieBlogSortChart = ref(false);

function clickBlogSortPie(index) {
  const blogSort = blogCountByBlogSort.value[index];
  router.push({
    path: "/note/note",
    query: { blogSort: JSON.stringify(blogSort) },
  });
}

function clickBlogTagPie(index) {
  const tag = blogCountByTag.value[index];
  router.push({
    path: "/note/note",
    query: { tag: JSON.stringify(tag) },
  });
}

async function fetchData() {
  try {
    const [initResponse, visitResponse, tagResponse, blogSortResponse, loginInforResponse, userGrowthResponse] = await Promise.all([
      init(),
      getVisitByWeek(),
      getBlogCountByTag(),
      getBlogCountByBlogSort(),
      listLoginInfor({ pageNum: 1, pageSize: 1000 }),
      getUserGrowthTrend()
    ]);

    if (initResponse.code === 200) {
      blogTotal.value = initResponse.data.blogCount;
      commentTotal.value = initResponse.data.commentCount;
      userTotal.value = initResponse.data.userCount;
    } else {
      console.error("Failed to fetch init data");
    }

    // 使用登录日志数据更新今日ip数
    if (loginInforResponse.code === 200) {
      // 计算今日IP数量（去重）
      const today = new Date().toISOString().split('T')[0];
      const todayLogs = loginInforResponse.rows.filter(log => {
        return log.loginTime && log.loginTime.startsWith(today);
      });
      // 去重IP地址
      const uniqueIps = new Set(todayLogs.map(log => log.ipaddr));
      visitAddTotal.value = uniqueIps.size;
    } else {
      console.error("Failed to fetch login info data");
      // 如果登录日志获取失败，使用原来的visitCount
      if (initResponse.code === 200) {
        visitAddTotal.value = initResponse.data.visitCount;
      }
    }

    if (visitResponse.code === 200) {
      const visitByWeek = visitResponse.data;
      lineChartData.value = {
        date: visitByWeek.date,
        expectedData: visitByWeek.pv,
        actualData: visitByWeek.uv
      };
      showLineChart.value = true;
    } else {
      console.error("Failed to fetch visit data");
    }

    if (tagResponse.code === 200) {
      blogCountByTag.value = tagResponse.data;
      const tagList = tagResponse.data;
      tagNameArray.value = tagList.map((tag) => tag.name);
      showPieChart.value = true;
    } else {
      console.error("Failed to fetch blog count by tag");
    }

    if (blogSortResponse.code === 200) {
      blogCountByBlogSort.value = blogSortResponse.data;
      const blogSortList = blogSortResponse.data;
      blogSortNameArray.value = blogSortList.map((blogSort) => blogSort.name);
      showPieBlogSortChart.value = true;
    } else {
      console.error("Failed to fetch blog count by blog sort");
    }

    // 初始化用户增长趋势图
    if (userGrowthResponse.code === 200) {
      initUserGrowthChart(userGrowthResponse.data);
    }
  } catch (error) {
    console.error("An error occurred while fetching data:", error);
  }
}

// 初始化用户增长趋势图
function initUserGrowthChart(data) {
  if (!userGrowthChart.value) return;
  
  userGrowthChartInstance = echarts.init(userGrowthChart.value, "macarons");
  
  // 将日期格式从 yyyy-MM-dd 转换为 MM-dd
  const formattedDates = (data.dates || []).map(date => {
    if (date && date.length >= 10) {
      return date.substring(5); // 提取 MM-dd 部分
    }
    return date;
  });
  
  const option = {
    title: {
      text: "用户增长趋势（最近30天）",
      left: "center"
    },
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "cross"
      }
    },
    legend: {
      data: ["新增用户", "累计用户"],
      bottom: 0
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "10%",
      containLabel: true
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: formattedDates
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        name: "新增用户",
        type: "line",
        smooth: true,
        data: data.newUsers || [],
        itemStyle: {
          color: "#FF005A"
        },
        areaStyle: {
          color: "rgba(255, 0, 90, 0.1)"
        }
      },
      {
        name: "累计用户",
        type: "line",
        smooth: true,
        data: data.totalUsers || [],
        itemStyle: {
          color: "#3888fa"
        },
        areaStyle: {
          color: "rgba(56, 136, 250, 0.1)"
        }
      }
    ]
  };
  
  userGrowthChartInstance.setOption(option);
}

// 处理窗口大小调整
function resizeCharts() {
  if (userGrowthChartInstance) {
    userGrowthChartInstance.resize();
  }
}

const btnClick = (id) => {
  const routes = {
    1: "/system/log/logininfor",
    2: "/member/member",
    3: "/message/comment",
    4: "/note/note",
  };
  router.push({ path: routes[id] });
};

/** 导出按钮操作 */
function handleExport() {
  exportDashboardData()
    .then(response => {
      const blob = new Blob([response]);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `dashboard_${new Date().getTime()}.xlsx`;
      document.body.appendChild(link);
      link.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(link);
    })
    .catch(error => {
      ElMessage.error('导出失败');
    });
}

onMounted(() => {
  fetchData();
  window.addEventListener("resize", resizeCharts);
});
</script>

<style scoped>
.dashboard-editor-container {
  padding: 42px;
  background-color: rgb(240, 242, 245);
  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }
}
.btn {
  width: 80px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 14px;
  display: inline-block;
  white-space: nowrap;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.btnClick {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.statistics-item {
  width: 600px;
  height: 400px;
  float: left;
  margin: 20px auto;
}

.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, 0.05);
    border-color: rgba(0, 0, 0, 0.05);
    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }
      .icon-people {
        background: #40c9c6;
      }
      .icon-message {
        background: #36a3f7;
      }
      .icon-money {
        background: #f4516c;
      }
      .icon-shoppingCard {
        background: #34bfa3;
      }
    }
    .icon-people {
      color: #40c9c6;
    }
    .icon-message {
      color: #36a3f7;
    }
    .icon-money {
      color: #f4516c;
    }
    .icon-shoppingCard {
      color: #34bfa3;
    }
    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }
    .card-panel-icon {
      float: left;
      font-size: 48px;
    }
    .card-panel-description {
      float: left;
      font-weight: bold;
      margin: 26px 0 0 70px;
      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }
      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}
</style>
