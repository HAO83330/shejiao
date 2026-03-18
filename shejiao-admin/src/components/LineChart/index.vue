<template>
  <div :class="className" :style="{ height: height, width: width }"></div>
</template>

<script>
import * as echarts from "echarts";
import "echarts/theme/macarons";
import { debounce } from "@/utils";

export default {
  props: {
    className: {
      type: String,
      default: "chart",
    },
    width: {
      type: String,
      default: "100%",
    },
    height: {
      type: String,
      default: "350px",
    },
    autoResize: {
      type: Boolean,
      default: true,
    },
    chartData: {
      type: Object,
    },
    title: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      chart: null,
      option: null,
    };
  },
  mounted() {
    this.initChart();
    if (this.autoResize) {
      this.__resizeHanlder = debounce(() => {
        if (this.chart) {
          this.chart.resize();
        }
      }, 100);
      window.addEventListener("resize", this.__resizeHanlder);
    }

    // 监听侧边栏的变化
    const sidebarElm = document.getElementsByClassName("sidebar-container")[0];
    if (sidebarElm) {
      sidebarElm.addEventListener("transitionend", this.__resizeHanlder);
    }
  },
  beforeDestroy() {
    if (!this.chart) {
      return;
    }
    if (this.autoResize) {
      window.removeEventListener("resize", this.__resizeHanlder);
    }

    const sidebarElm = document.getElementsByClassName("sidebar-container")[0];
    if (sidebarElm) {
      sidebarElm.removeEventListener("transitionend", this.__resizeHanlder);
    }

    this.chart.dispose();
    this.chart = null;
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.updateChart(val);
      },
    },
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el, "macarons");
      this.option = this.getBaseOption();
      this.chart.setOption(this.option);
      
      if (this.chartData) {
        this.updateChart(this.chartData);
      }
    },
    getBaseOption() {
      return {
        title: {
          text: this.title,
          left: "center",
        },
        grid: {
          left: 10,
          right: 10,
          bottom: 40,
          top: 50,
          containLabel: true,
        },
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "cross",
          },
          padding: [5, 10],
        },
        legend: {
          data: ["访问量(PV)", "独立用户(UV)"],
          bottom: 5,
        },
        xAxis: {
          type: "category",
          boundaryGap: false,
          axisTick: {
            show: false,
          },
          data: [],
        },
        yAxis: {
          type: "value",
          axisTick: {
            show: false,
          },
        },
        series: [
          {
            name: "访问量(PV)",
            type: "line",
            smooth: true,
            data: [],
            itemStyle: {
              color: "#FF005A",
            },
            lineStyle: {
              color: "#FF005A",
              width: 2,
            },
            animationDuration: 2800,
            animationEasing: "cubicInOut",
          },
          {
            name: "独立用户(UV)",
            type: "line",
            smooth: true,
            data: [],
            itemStyle: {
              color: "#3888fa",
            },
            lineStyle: {
              color: "#3888fa",
              width: 2,
            },
            areaStyle: {
              color: "#f3f8ff",
            },
            animationDuration: 2800,
            animationEasing: "quadraticOut",
          },
        ],
      };
    },
    updateChart({ date, expectedData, actualData } = {}) {
      if (!this.chart) return;
      
      // 只更新数据和x轴，保留图例的选中状态
      this.chart.setOption({
        xAxis: {
          data: date || [],
        },
        series: [
          {
            name: "访问量(PV)",
            data: expectedData || [],
          },
          {
            name: "独立用户(UV)",
            data: actualData || [],
          },
        ],
      });
    },
  },
};
</script>
