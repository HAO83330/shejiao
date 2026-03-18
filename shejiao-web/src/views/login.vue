<template>
  <div class="reds-modal login-modal" role="presentation">
    <i tabindex="-1" class="reds-mask" aria-label="弹窗遮罩"></i>
    <div class="login-container">
      <div class="icon-btn-wrapper close-button" @click="close">
        <Close style="width: 1em; height: 1em" />
      </div>
      <div class="login-content">
        <div class="login-reason">登录后推荐更懂你的笔记</div>
        <img class="logo" src="@/assets/logo.png" />
        <div class="title">手机号登录</div>
        <div class="input-container">
          <form onsubmit="return false">
            <label class="phone">
              <input placeholder="请输入手机号" type="text" name="blur" v-model="userLogin.phone" />
              <svg class="reds-icon clear" width="24" height="24" fill="#xhs-pc-web-phone" style="display: none">
                <use xlink:href="#clear"></use>
              </svg>
            </label>
            <div style="height: 16px"></div>
            <label class="auth-code">
              <input :type="showPassword ? 'text' : 'password'" placeholder="请输入密码" autocomplete="false" v-model="userLogin.password" :class="{ 'password-input': true }" />
              <span class="password-toggle" @click="togglePasswordVisibility">
                {{ showPassword ? '👁️‍🗨️' : '👁️' }}
              </span>
            </label>
            <div class="err-msg"></div>
            <button class="submit" @click="loginMethod">登录</button>
          </form>
        </div>

        <div class="oauth-tip">
          <span class="oauth-tip-line">新用户可直接登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Close } from "@element-plus/icons-vue";
import type { UserLogin } from "@/type/user";
import { login } from "@/api/user";
import { ref } from "vue";
import { storage } from "@/utils/storage";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";

const userStore = useUserStore();
const router = useRouter();

const showPassword = ref(false);

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const userLogin = ref<UserLogin>({
  phone: "",
  email: "",
  code: "",
  username: "",
  password: "",
});

const emit = defineEmits(["clickChild"]);
const close = () => {
  //传递给父组件
  emit("clickChild", false);
};
const phoneRegex = /^1[3-9]\d{9}$/; // 正则表达式校验手机号格式

const loginMethod = () => {
  const phoneNumber = userLogin.value.phone;
  if (!phoneNumber) {
    ElMessage.warning("请输入手机号");
    return;
  }
  if (!phoneRegex.test(phoneNumber)) {
    ElMessage.warning("手机号格式不正确");
    return;
  }
  if (!userLogin.value.password) {
    ElMessage.warning("请输入密码");
    return;
  }
  login(userLogin.value)
    .then((res: any) => {
      const { data } = res;
      const currentUser = data.userInfo;
      storage.set("accessToken", data.accessToken);
      storage.set("refreshToken", data.refreshToken);
      userStore.setUserInfo(currentUser);
      router.push({ path: "/", query: { date: Date.now() } });
      emit("clickChild", false);
    })
    .catch((error: any) => {
      console.log(error);
      ElMessage.error("登录失败");
    });
  // loginByCode(userLogin.value).then((res: any) => {
  //   const { data } = res;
  //   const currentUser = data.userInfo;
  //   storage.set("accessToken", data.accessToken);
  //   storage.set("refreshToken", data.refreshToken);
  //   userStore.setUserInfo(currentUser);
  //   router.push({ path: "/", query: { date: Date.now() } });
  //   emit("clickChild", false);
  // });
};
</script>

<style lang="less" scoped>
a {
  text-decoration: none;
  background-color: transparent;
}
.login-reason {
  height: 48px;
  padding: 0 20px;
  background: rgba(61, 138, 245, 0.1);
  color: #3d8af5;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  line-height: 120%;
  margin-top: 20px;
  max-width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}



.login-modal {
  z-index: 100010;
  flex-direction: column;
}

.reds-modal {
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000002;
  box-sizing: border-box;
  visibility: visible;
  opacity: 1;
  transition:
    opacity 0.2s,
    visibility 0.2s;

  .reds-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.25);
    z-index: -1;
  }

  .login-container {
        position: relative;
        width: 400px;
        background: #fff;
        border-radius: 16px;
        box-shadow:
          0 4px 32px 0 rgba(0, 0, 0, 0.08),
          0 1px 4px 0 rgba(0, 0, 0, 0.04);
        transition: all 0.2s;

    .close-button {
      position: absolute;
      right: 20px;
      top: 20px;
      cursor: pointer;
      color: rgba(51, 51, 51, 0.8);
    }

    .login-content {
      display: flex;
      align-items: center;
      flex-direction: column;
      padding: 40px 0;

      img {
        border-style: none;
      }

      .logo {
        margin: 20px 0;
        width: 80px;
        height: 35px;
        -webkit-user-select: none;
        user-select: none;
        pointer-events: none;
      }

      .title {
        font-size: 18px;
        color: rgba(51, 51, 51, 0.8);
        font-weight: 600;
        line-height: 120%;
      }

      .input-container {
        margin-top: 24px;
        width: 304px;
        display: flex;
        flex-direction: column;

        .auth-code,
        .phone {
          display: flex;
          align-items: center;
          font-size: 16px;
          line-height: 22px;
          color: rgba(51, 51, 51, 0.8);
          height: 48px;
          background: #fff;
          border-radius: 999px;
          transition: border-color 0.2s;
          border-bottom: 0.5px solid rgba(0, 0, 0, 0.08);
          position: relative;
          width: 100%;


        }

        .auth-code input,
        .phone input {
          font-size: 16px;
          width: 100%;
          height: 100%;
          caret-color: #409EFF;
          color: #333;
          text-align: center;
          padding: 0 20px;
          box-sizing: border-box;
        }

        .password-toggle {
          position: absolute;
          right: 20px;
          cursor: pointer;
          font-size: 18px;
          user-select: none;
          z-index: 10;
        }

        /* 禁用浏览器自带的密码显示/隐藏按钮 */
        input[type="password"]::-ms-reveal,
        input[type="password"]::-ms-clear {
          display: none;
        }

        /* 禁用WebKit浏览器自带的密码显示/隐藏按钮 */
        input[type="password"]::-webkit-contacts-auto-fill-button,
        input[type="password"]::-webkit-credentials-auto-fill-button {
          display: none !important;
          visibility: hidden;
          pointer-events: none;
          position: absolute;
          right: 0;
        }

        /* 针对Chrome浏览器的密码显示按钮 */
        input.password-input {
          -webkit-text-security: disc !important;
        }

        input.password-input[type="text"] {
          -webkit-text-security: none !important;
        }

        .code-button {
          font-size: 16px;
          color: #409EFF;
          cursor: pointer;
          opacity: 0.5;
        }

        .err-msg {
          margin-top: 9.5px;
          height: 10px;
          line-height: 10px;
          color: var(--color-red);
          font-size: 14px;
          font-weight: 400;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .submit {
          margin-top: 24px;
          height: 48px;
          background: #409EFF;
          color: #fff;
          opacity: 1;
          border-radius: 999px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
          width: 100%;
        }

        .submit:hover {
          opacity: 1;
        }

        .submit:active {
          transform: scale(0.98);
        }

        form {
          display: block;
          margin-top: 0em;
        }
      }

      .oauth-tip:after,
      .oauth-tip:before {
        margin-top: 20px;
        display: block;
        width: 50px;
        height: 1px;
        content: "";
        background-color: rgba(0, 0, 0, 0.08);
      }

      .oauth-tip-line {
        margin: 0 12px;
        margin-top: 20px;
      }

      .oauth-tip {
        margin-top: 29px;
        display: flex;
        align-items: center;
        font-weight: 400;
        font-size: 14px;
        color: rgba(51, 51, 51, 0.6);
      }
    }
  }
}
</style>
