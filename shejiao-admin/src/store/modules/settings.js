import defaultSettings from '@/settings'
import { useDynamicTitle } from '@/utils/dynamicTitle'

const { sideTheme, showSettings, topNav, tagsView, fixedHeader, sidebarLogo, dynamicTitle } = defaultSettings

const storageSetting = JSON.parse(localStorage.getItem('layout-setting')) || ''

const useSettingsStore = defineStore(
  'settings',
  {
    state: () => ({
      title: '',
      theme: storageSetting.theme || '#409EFF',
      sideTheme: storageSetting.sideTheme || sideTheme,
      showSettings: showSettings,
      topNav: storageSetting.topNav === undefined ? topNav : storageSetting.topNav,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle
    }),
    actions: {
      // 修改布局设置
      changeSetting(data) {
        const { key, value } = data
        if (this.hasOwnProperty(key)) {
          this[key] = value
          // 保存到本地存储
          this.saveSetting()
        }
      },
      // 保存设置到本地存储
      saveSetting() {
        const setting = {
          theme: this.theme,
          sideTheme: this.sideTheme,
          topNav: this.topNav,
          tagsView: this.tagsView,
          fixedHeader: this.fixedHeader,
          sidebarLogo: this.sidebarLogo,
          dynamicTitle: this.dynamicTitle
        }
        localStorage.setItem('layout-setting', JSON.stringify(setting))
      },
      // 设置网页标题
      setTitle(title) {
        this.title = title
        useDynamicTitle();
      }
    },
    watch: {
      // 监听所有属性变化并自动保存
      'theme': {
        handler() {
          this.saveSetting()
        },
        deep: true
      },
      'sideTheme': {
        handler() {
          this.saveSetting()
        },
        deep: true
      },
      'topNav': {
        handler() {
          this.saveSetting()
        },
        deep: true
      },
      'tagsView': {
        handler() {
          this.saveSetting()
        },
        deep: true
      },
      'fixedHeader': {
        handler() {
          this.saveSetting()
        },
        deep: true
      },
      'sidebarLogo': {
        handler() {
          this.saveSetting()
        },
        deep: true
      },
      'dynamicTitle': {
        handler() {
          this.saveSetting()
        },
        deep: true
      }
    }
  })

export default useSettingsStore
