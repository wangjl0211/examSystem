import js from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import globals from "globals";

export default [
  {
    ignores: ["dist/", "node_modules/", "public/", "*.min.js", ".vite/", "mock/", "types/"]
  },
  js.configs.recommended,
  ...pluginVue.configs["flat/essential"],
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        // 添加 Mock 相关的全局变量，如果有的话
      }
    },
    rules: {
      // 放宽规则，方便开发
      'no-console': 'off',
      'no-debugger': 'off',
      'no-unused-vars': 'warn',  // 未使用变量改为警告
      'vue/multi-word-component-names': 'off'
    }
  }
];
