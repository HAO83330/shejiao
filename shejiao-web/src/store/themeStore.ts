import { defineStore } from "pinia";
import { ref, watch } from "vue";
import { storage } from "@/utils/storage";
import { store } from "@/store";

export const themeStore = defineStore("themeStore", () => {
  const isDark = ref(false);

  const toggleTheme = () => {
    isDark.value = !isDark.value;
    applyTheme();
  };

  const applyTheme = () => {
    const html = document.documentElement;
    if (isDark.value) {
      html.classList.add("dark");
      html.style.setProperty('--bg-primary', '#121212');
      html.style.setProperty('--bg-secondary', '#1e1e1e');
      html.style.setProperty('--bg-tertiary', '#2c2c2c');
      html.style.setProperty('--bg-hover', '#3a3a3a');
      html.style.setProperty('--text-primary', '#ffffff');
      html.style.setProperty('--text-secondary', 'rgba(255, 255, 255, 0.85)');
      html.style.setProperty('--text-tertiary', 'rgba(255, 255, 255, 0.6)');
      html.style.setProperty('--border-color', 'rgba(255, 255, 255, 0.1)');
      html.style.setProperty('--shadow-color', 'rgba(0, 0, 0, 0.4)');
      storage.set("theme", "dark");
    } else {
      html.classList.remove("dark");
      html.style.setProperty('--bg-primary', '#ffffff');
      html.style.setProperty('--bg-secondary', '#f8f8f8');
      html.style.setProperty('--bg-tertiary', '#f0f0f0');
      html.style.setProperty('--bg-hover', '#e8e8e8');
      html.style.setProperty('--text-primary', '#333333');
      html.style.setProperty('--text-secondary', 'rgba(51, 51, 51, 0.8)');
      html.style.setProperty('--text-tertiary', 'rgba(51, 51, 51, 0.6)');
      html.style.setProperty('--border-color', 'rgba(0, 0, 0, 0.08)');
      html.style.setProperty('--shadow-color', 'rgba(0, 0, 0, 0.1)');
      storage.set("theme", "light");
    }
  };

  const initTheme = () => {
    const savedTheme = storage.get("theme");
    const html = document.documentElement;
    if (savedTheme === "dark") {
      isDark.value = true;
      html.classList.add("dark");
      html.style.setProperty('--bg-primary', '#121212');
      html.style.setProperty('--bg-secondary', '#1e1e1e');
      html.style.setProperty('--bg-tertiary', '#2c2c2c');
      html.style.setProperty('--bg-hover', '#3a3a3a');
      html.style.setProperty('--text-primary', '#ffffff');
      html.style.setProperty('--text-secondary', 'rgba(255, 255, 255, 0.85)');
      html.style.setProperty('--text-tertiary', 'rgba(255, 255, 255, 0.6)');
      html.style.setProperty('--border-color', 'rgba(255, 255, 255, 0.1)');
      html.style.setProperty('--shadow-color', 'rgba(0, 0, 0, 0.4)');
    } else {
      isDark.value = false;
      html.classList.remove("dark");
      html.style.setProperty('--bg-primary', '#ffffff');
      html.style.setProperty('--bg-secondary', '#f8f8f8');
      html.style.setProperty('--bg-tertiary', '#f0f0f0');
      html.style.setProperty('--bg-hover', '#e8e8e8');
      html.style.setProperty('--text-primary', '#333333');
      html.style.setProperty('--text-secondary', 'rgba(51, 51, 51, 0.8)');
      html.style.setProperty('--text-tertiary', 'rgba(51, 51, 51, 0.6)');
      html.style.setProperty('--border-color', 'rgba(0, 0, 0, 0.08)');
      html.style.setProperty('--shadow-color', 'rgba(0, 0, 0, 0.1)');
    }
  };

  watch(isDark, () => {
    applyTheme();
  });

  return { isDark, toggleTheme, initTheme };
});

export function useThemeStore() {
  return themeStore(store);
}
