# hello-uvue 示例项目 — uvue 完整用法指南

> 基于 `hello-uvue` (v1.0.29) 项目整理，涵盖 uni-app-x 框架下 uvue 开发的核心模式。
> 项目中每个功能点同时提供了 **Options API** 和 **Composition API** 两种写法（Options API 在 Vapor 模式下不可用）。

---

## 目录

1. [项目结构速览](#1-项目结构速览)
2. [App 入口与生命周期](#2-app-入口与生命周期)
3. [路由配置 (pages.json)](#3-路由配置-pagesjson)
4. [页面创建与生命周期](#4-页面创建与生命周期)
5. [组件开发模式](#5-组件开发模式)
6. [模板语法与指令](#6-模板语法与指令)
7. [响应式系统](#7-响应式系统)
8. [状态管理](#8-状态管理)
9. [插件系统](#9-插件系统)
10. [全局属性与依赖注入](#10-全局属性与依赖注入)
11. [样式方案](#11-样式方案)
12. [条件编译](#12-条件编译)
13. [UTS 类型系统](#13-uts-类型系统)
14. [渲染函数 (h)](#14-渲染函数-h)
15. [内置组件](#15-内置组件)
16. [测试](#16-测试)
17. [自动化测试模式](#17-自动化测试模式)

---

## 1. 项目结构速览

```
UserPhone/
├── App.uvue                # App 根组件（入口壳）
├── main.uts                # 应用启动引导（createSSRApp、注册全局组件/插件/mixin）
├── pages.json              # 路由配置（~100 条路由）
├── manifest.json           # 项目配置（appid、版本、平台设置）
├── package.json            # 包元数据 (name: hello-uvue)
├── uni.scss                # SCSS 变量
├── index.html              # Web 平台 HTML 壳
│
├── pages/                  # 页面文件（~100+）
│   ├── index/              # 首页（动态导航菜单）
│   ├── reactivity/         # 响应式相关
│   ├── directive/          # 指令相关
│   ├── lifecycle/          # 生命周期
│   ├── component-instance/ # 组件实例
│   ├── template-syntax/    # 模板语法
│   ├── built-in/           # 内置组件
│   └── ...
├── components/             # 共享组件
├── store/                  # 状态管理（index.uts）
├── plugins/                # Vue 插件
├── styles/                 # 全局样式
├── static/                 # 静态资源
└── uni_modules/            # uni-app-x 模块
```

---

## 2. App 入口与生命周期

### 2.1 App.uvue — 根组件

`App.uvue` 是应用的根组件，使用 `<script setup lang="uts">`（Composition API + UTSCript）。

```vue
<script setup lang="uts">
import { onLaunch, onAppShow, onAppHide, onLastPageBackPress, onExit, onError } from '@dcloudio/uni-app'
import { onShow, onHide } from '@dcloudio/uni-app'

// ---------- App 生命周期 hook ----------
onLaunch(() => {
  // 应用启动时触发（仅一次）
  setLifeCycleNum(state.lifeCycleNum + 1)
})

onAppShow(() => {
  // 应用从后台切到前台时触发
  setLifeCycleNum(state.lifeCycleNum + 1)
})

onAppHide(() => {
  // 应用从前台切到后台时触发
  setLifeCycleNum(state.lifeCycleNum - 1)
})

onLastPageBackPress(() => {
  // Android/Harmony: 双击返回键退出应用（toast 提示）
  uni.showToast({ title: '再按一次退出应用', icon: 'none' })
  return true  // 返回 true 表示拦截默认行为
})

onExit(() => {
  // Android/Harmony: 应用退出时触发
  console.log('应用退出')
})

onError((err) => {
  // 全局错误捕获
  console.log('全局错误:', err)
})
</script>
```

### 2.2 main.uts — 应用启动引导

`main.uts` 是应用入口引导文件，进行全局初始化：

```typescript
import { createSSRApp } from 'vue'
import App from './App.uvue'

export function createApp() {
  const app = createSSRApp(App)

  // 1. 注册全局组件
  app.component('CompForAppComponent', CompForAppComponent)

  // 2. 提供全局数据 (provide/inject)
  app.provide('globalProvideMsg', '这是全局 provide 注入的数据')

  // 3. 注册全局 mixin（非 Vapor 模式）
  app.mixin(defineMixin(...))

  // 4. 注册插件
  app.use(plugin1)
  app.use(plugin2)
  app.use(plugin3)
  app.use(plugin4)

  // 5. 设置全局属性
  app.config.globalProperties.$myGlobal = 'hello'

  return { app }
}
```

---

## 3. 路由配置 (pages.json)

### 3.1 基本路由

```json
{
  "pages": [
    {
      "path": "pages/index/index",
      "style": {
        "navigationBarTitleText": "hello uvue"
      }
    },
    {
      "path": "pages/reactivity/core/ref/ref",
      "style": {
        "navigationBarTitleText": "ref"
      }
    }
  ],
  "globalStyle": {
    "navigationBarTextStyle": "white",
    "navigationBarTitleText": "hello uvue",
    "navigationBarBackgroundColor": "#007AFF",
    "backgroundColor": "#F8F8F8",
    "pageOrientation": "portrait"
  },
  "easycom": {
    "custom": {
      "custom-(.*)": "@/components/custom-$1.uvue"
    }
  }
}
```

### 3.2 路由路径规范

- `path` 字段映射到 `pages/` 目录下的 `.uvue` 文件，无需写扩展名
- 每个功能点按目录层级组织，如 `pages/reactivity/core/ref/ref`
- 每个概念同时提供 `*-options` 和 `*-composition` 两个变体

### 3.3 EasyCom 自动引入

```json
{
  "easycom": {
    "custom": {
      "custom-(.*)": "@/components/custom-$1.uvue"
    }
  }
}
```

配置后，模板中使用 `<custom-call-easy-method>` 会自动引入 `components/custom-call-easy-method.uvue`，无需手动 import。

### 3.4 页面跳转

```typescript
// 携带参数跳转
uni.navigateTo({ url: '/pages/lifecycle/page/page-composition?type=composition' })

// 获取页面参数（在 onLoad 中）
onLoad((options: OnLoadOptions) => {
  console.log(options?.type)    // 'composition'
  console.log(options?.query)   // 完整 query 字符串
  // 注意: options 类型是 UTSJSONObject，需要通过 options['type'] 或 options!.type 访问
})
```

---

## 4. 页面创建与生命周期

### 4.1 页面文件 (.uvue)

uvue 页面是一个 Vue 单文件组件（SFC），包含 `<template>`、`<script>`、`<style>` 三个部分，使用 `.uvue` 扩展名。

### 4.2 Composition API 页面

```vue
<script setup lang="uts">
import { ref } from 'vue'

// 页面生命周期（Options 风格，从 @dcloudio/uni-app 导入）
import { onLoad, onShow, onReady, onPullDownRefresh, onReachBottom,
         onPageScroll, onBackPress, onHide, onUnload, onResize } from '@dcloudio/uni-app'

// 页面生命周期（Composition 风格）
import { onPageShow, onPageHide } from '@dcloudio/uni-app'

const count = ref(0)

// 注意：导入的 hook 名称与 Options API 不同
onLoad((options: OnLoadOptions) => {
  // 页面加载，可获取路由参数
  console.log('页面加载')
})

onShow(() => {
  console.log('页面显示')
})

onReady(() => {
  console.log('页面初次渲染完成')
})

onPageScroll((options : OnPageScrollOptions) => {
  console.log('页面滚动', options.scrollTop)
})

onPullDownRefresh(() => {
  console.log('下拉刷新')
  uni.stopPullDownRefresh()
})

onBackPress((options : OnBackPressOptions) => {
  // 返回物理键/导航栏返回按钮
  return false  // true = 阻止返回
})

onReachBottom(() => {
  console.log('触底')
})

onUnload(() => {
  console.log('页面卸载')
})

onResize((options : OnResizeOptions) => {
  console.log('窗口尺寸变化', options.size?.windowWidth, options.size?.windowHeight)
})
</script>

<template>
  <view class="page">
    <text>{{ count }}</text>
  </view>
</template>
```

### 4.3 Options API 页面

```vue
<script lang="uts">
export default {
  data() {
    return { count: 0 }
  },
  onLoad(options: OnLoadOptions) {
    // 页面加载
  },
  onShow() {
    // 页面显示
  },
  onReady() {
    // 页面初次渲染完成
  },
  onPullDownRefresh() {
    // 下拉刷新
  },
  onPageScroll(options: OnPageScrollOptions) {
    // 页面滚动
  },
  onReachBottom() {
    // 触底
  },
  onBackPress(options: OnBackPressOptions) : boolean {
    return false
  },
  onHide() {
    // 页面隐藏
  },
  onUnload() {
    // 页面卸载
  },
  onResize(options: OnResizeOptions) {
    // 窗口尺寸变化
  }
}
</script>
```

### 4.4 页面生命周期对照表

| Options API | Composition API | 触发时机 |
|---|---|---|
| `onLoad` | `onLoad` | 页面加载（仅一次） |
| `onShow` | `onPageShow` | 页面显示/切换到前台 |
| `onReady` | `onReady` | 页面初次渲染完成 |
| `onHide` | `onPageHide` | 页面隐藏/切到后台 |
| `onUnload` | `onUnload` | 页面卸载 |
| `onPullDownRefresh` | `onPullDownRefresh` | 下拉刷新 |
| `onPageScroll` | `onPageScroll` | 页面滚动 |
| `onReachBottom` | `onReachBottom` | 页面触底 |
| `onBackPress` | `onBackPress` | 点击返回 |
| `onResize` | `onResize` | 窗口尺寸变化 |

### 4.5 组件生命周期 (Composition API)

```typescript
import { onBeforeMount, onMounted, onBeforeUpdate, onUpdated,
         onBeforeUnmount, onUnmounted, onActivated, onDeactivated } from 'vue'

onBeforeMount(() => { /* 挂载前 */ })
onMounted(() => { /* 挂载后 */ })
onBeforeUpdate(() => { /* 更新前 */ })
onUpdated(() => { /* 更新后 */ })
onBeforeUnmount(() => { /* 卸载前 */ })
onUnmounted(() => { /* 卸载后 */ })
onActivated(() => { /* keep-alive 激活 */ })
onDeactivated(() => { /* keep-alive 失活 */ })
```

> **注意**：`onActivated`/`onDeactivated` 在 Android 平台的 Composition API 中不触发，Options API 中正常工作。项目中 Counter.uvue 使用条件编译解决了这个问题。

---

## 5. 组件开发模式

### 5.1 组件目录组织

```
components/
├── Foo.uvue                          # 简单组件
├── Bar.uvue                          # 简单组件
├── CompForAppComponent.uvue           # app.component 注册的全局组件
├── CompForAppUse.uvue                 # 通过 inject 获取插件数据的组件
├── CompForHFunction.uvue              # 用于渲染函数 h() 的组件
├── CompForHFunctionWithSlot.uvue      # 带插槽的渲染函数组件
├── custom-call-easy-method.uvue       # EasyCom 自动引入组件
├── define-expose-foo/                 # defineExpose 测试组件
│   └── define-expose-foo.uvue
├── keep-alive/                        # keep-alive 组件（含平台差异化实现）
│   ├── Counter.uvue
│   └── Message.uvue
└── ...
```

### 5.2 Composition API 组件（推荐）

```vue
<script setup lang="uts">
import { ref, computed, watch, defineProps, defineEmits, defineExpose, defineModel } from 'vue'

// 1. Props — 类型安全
const props = defineProps({
  title: { type: String, default: '默认标题' },
  count: { type: Number, required: true },
  disabled: { type: Boolean, default: false }
})

// 2. Emits — 函数重载语法
const emit = defineEmits<{
  (e: 'click', val: string): void
  (e: 'change', val: number): void
}>()

// 3. v-model（双向绑定）
const model = defineModel<string>({ default: '' })

// 4. 响应式数据
const innerCount = ref(0)

// 5. 计算属性
const doubled = computed(() => innerCount.value * 2)

// 6. 监听器
watch(innerCount, (newVal, oldVal) => {
  emit('change', newVal)
})

// 7. 方法
const increment = () => {
  innerCount.value++
  emit('click', 'clicked')
}

// 8. 暴露给父组件（供 $callMethod 或模板 ref 调用）
defineExpose({ innerCount, increment, doubled })

// 9. 插槽类型
defineSlots<{
  default(props: {}): any
  header(props: { msg: string }): any
}>()
</script>

<template>
  <view>
    <text :class="{ 'is-disabled': disabled }">{{ title }}: {{ innerCount }}</text>
    <button @click="increment">+1</button>
    <slot name="header" :msg="title"></slot>
    <slot></slot>
  </view>
</template>

<style scoped>
.is-disabled { opacity: 0.5; }
</style>
```

### 5.3 Options API 组件

```vue
<script lang="uts">
export default {
  name: "component-name",
  props: {
    title: { type: String, default: '' },
    count: { type: Number, default: 0 }
  },
  emits: ['click', 'change'],
  data() {
    return {
      innerCount: 0
    }
  },
  computed: {
    doubled(): number {
      return this.innerCount * 2
    }
  },
  watch: {
    innerCount(newVal: number) {
      this.$emit('change', newVal)
    }
  },
  methods: {
    increment() {
      this.innerCount++
      this.$emit('click', 'clicked')
    }
  },
  onLoad() {
    // 页面生命周期（仅页面组件）
  }
}
</script>
```

### 5.4 跨平台差异化实现

uvue 支持在同一个组件中写两套 `<script>` 实现，通过条件编译切换：

```vue
<script setup lang="uts">
// #ifdef APP-ANDROID
// Android 平台的 Composition API 实现
// #endif
</script>

<script setup lang="uts">
// #ifndef APP-ANDROID
// 其他平台的 Composition API 实现
// #endif
</script>
```

或者使用 Options API 与 Composition API 混合：

```vue
<!-- Android 使用 Options API -->
<script lang="uts">
// #ifdef APP-ANDROID
export default { /* Options API 实现 */ }
// #endif
</script>

<!-- 其他平台使用 Composition API -->
<script setup lang="uts">
// #ifndef APP-ANDROID
// Composition API 实现
// #endif
</script>
```

### 5.5 组件通信方式

| 方式 | API | 方向 |
|---|---|---|
| Props | `defineProps` / `props` | 父 → 子 |
| Emits | `defineEmits` / `$emit` | 子 → 父 |
| v-model | `defineModel` | 双向 |
| 插槽 | `<slot>` / `v-slot` | 父 → 子（模板） |
| Provide/Inject | `provide` / `inject` | 祖先 → 后代 |
| 全局属性 | `app.config.globalProperties` | 全局 |
| $callMethod | `$callMethod('methodName', args)` | 子 → 父链 |
| $dispatch | `$parent` 链遍历 + `$callMethod` | 子 → 祖先 |
| defineExpose | `defineExpose` + 模板 ref | 父获取子实例 |
| 全局 Store | `reactive()` 单例 | 全局 |

### 5.6 `defineExpose` 与 `$callMethod`

子组件暴露方法：

```vue
<script setup lang="uts">
const foo = () => 'bar'
const add = (a: number, b: number) => a + b

defineExpose({ foo, add })
</script>
```

父组件调用子组件方法（通过模板 ref）：

```vue
<script setup lang="uts">
import { ref } from 'vue'

const childRef = ref<ComponentPublicInstance | null>(null)

const callChild = () => {
  childRef.value?.$callMethod('foo')
  childRef.value?.$callMethod('add', 1, 2)
}
</script>

<template>
  <custom-call-easy-method ref="childRef" />
  <button @click="callChild">调用子组件方法</button>
</template>
```

### 5.7 `$dispatch` 向上通信（示例项目中 uni-collapse 的模式）

```typescript
// util.uts — 通过 $parent 链向上查找
function $dispatch(instance: ComponentPublicInstance | null, eventName: string, ...args: any[]) {
  let parent = instance?.$parent
  while (parent) {
    const result = parent.$callMethod(eventName, ...args)
    if (result !== undefined) return result
    parent = parent.$parent
  }
}
```

---

## 6. 模板语法与指令

### 6.1 文本插值

```vue
<text>{{ message }}</text>
<text>{{ count + 1 }}</text>
<text>{{ isActive ? '激活' : '未激活' }}</text>
```

### 6.2 指令一览

| 指令 | 示例 | 说明 |
|---|---|---|
| `v-bind` / `:` | `:title="msg"` | 绑定属性 |
| `v-model` | `v-model="value"` | 双向绑定 |
| `v-if` / `v-else-if` / `v-else` | `v-if="show"` | 条件渲染 |
| `v-show` | `v-show="visible"` | 显示/隐藏 |
| `v-for` | `v-for="(item, idx) in list"` | 列表渲染 |
| `v-on` / `@` | `@click="handler"` | 事件绑定 |
| `v-html` | `v-html="htmlStr"` | 渲染 HTML（注意 XSS） |
| `v-text` | `v-text="text"` | 纯文本渲染 |
| `v-pre` | `v-pre` | 跳过编译 |
| `v-once` | `v-once` | 只渲染一次 |
| `v-memo` | `v-memo="[dep]"` | 缓存子树（Vapor） |
| `v-slot` | `v-slot:name="props"` | 插槽 |
| `v-cloak` | `v-cloak` | 未编译时隐藏 |

### 6.3 条件渲染 (v-if)

```vue
<view v-if="type === 'A'">类型 A</view>
<view v-else-if="type === 'B'">类型 B</view>
<view v-else>其他</view>

<!-- template 上的条件组 -->
<template v-if="showGroup">
  <text>组内元素 1</text>
  <text>组内元素 2</text>
</template>
```

### 6.4 列表渲染 (v-for)

```vue
<!-- 数组遍历 -->
<view v-for="(item, index) in items" :key="item.id">
  <text>{{ index }}: {{ item.name }}</text>
</view>

<!-- 对象遍历 -->
<view v-for="(value, key, index) in obj" :key="key">
  <text>{{ key }}: {{ value }}</text>
</view>

<!-- 范围遍历 -->
<view v-for="n in 10" :key="n">
  <text>{{ n }}</text>
</view>
```

### 6.5 事件处理 (v-on)

```vue
<!-- 基本用法 -->
<button @click="handleClick">点击</button>

<!-- 内联语句 -->
<button @click="count++">+1</button>

<!-- 事件对象 -->
<button @click="handleEvent($event, '参数')">点击</button>

<!-- 修饰符 -->
<button @click.stop="onClick">停止冒泡</button>
<button @click.once="onClick">只触发一次</button>
```

**注意**：`@click` 更推荐使用 `@tap`（uni-app 跨平台点击事件）。

### 6.6 类与样式绑定

```vue
<!-- 对象语法 -->
<view :class="{ active: isActive, 'text-danger': hasError }">内容</view>

<!-- 数组语法 -->
<view :class="[activeClass, errorClass]">内容</view>

<!-- 内联样式 -->
<view :style="{ color: activeColor, fontSize: fontSize + 'px' }">内容</view>
```

### 6.7 v-model 双向绑定

```vue
<script setup lang="uts">
import { ref } from 'vue'
const name = ref('')
const checkbox = ref(false)
const picked = ref('')
const selected = ref('')
</script>

<template>
  <input v-model="name" />
  <switch v-model="checkbox" />
  <radio-group v-model="picked">
    <radio value="A">A</radio>
    <radio value="B">B</radio>
  </radio-group>
  <picker v-model="selected" :range="options" />
</template>
```

### 6.8 具名插槽与作用域插槽

```vue
<!-- 子组件 (Child.uvue) -->
<template>
  <view>
    <slot name="header" :msg="title"></slot>
    <slot :data="item"></slot>
  </view>
</template>

<!-- 父组件 -->
<Child>
  <template v-slot:header="{ msg }">
    <text>头部: {{ msg }}</text>
  </template>
  <template v-slot:default="{ data }">
    <text>默认插槽: {{ data }}</text>
  </template>
</Child>
```

---

## 7. 响应式系统

### 7.1 核心 API

| API | 用途 | 示例 |
|---|---|---|
| `ref()` | 创建响应式引用（推荐基本类型） | `const count = ref(0)` |
| `reactive()` | 创建响应式对象 | `const state = reactive({ count: 0 })` |
| `computed()` | 计算属性 | `const doubled = computed(() => count.value * 2)` |
| `watch()` | 监听单个值 | `watch(count, (n, o) => {})` |
| `watchEffect()` | 自动追踪依赖执行副作用 | `watchEffect(() => {})` |
| `shallowRef()` | 浅层响应式引用 | 仅追踪 `.value` 变更 |
| `customRef()` | 自定义 ref（显式控制 get/set） | 用于防抖等场景 |
| `shallowReactive()` | 浅层响应式对象 | 仅第一层响应式 |
| `shallowReadonly()` | 浅层只读 | 浅层只读 |
| `toRaw()` | 获取原始对象 | 跳过代理 |
| `markRaw()` | 标记为不可响应 | 永远不追踪 |
| `effectScope()` | 收集副作用统一管理 | 组件卸载时自动清理 |

### 7.2 ref

```typescript
import { ref } from 'vue'

const count = ref(0)
const text = ref('hello')
const isActive = ref(true)
const items = ref<string[]>([])   // 泛型数组

// 读写
count.value = 1
console.log(count.value)
```

### 7.3 reactive

```typescript
import { reactive } from 'vue'

const state = reactive({
  count: 0,
  nested: {
    value: ''
  }
})

// 直接修改（无需 .value）
state.count++
state.nested.value = 'hello'
```

### 7.4 computed

```typescript
import { computed } from 'vue'

const count = ref(0)
const doubled = computed(() => count.value * 2)

// 可写计算属性
const writable = computed({
  get: () => count.value + 1,
  set: (val: number) => { count.value = val - 1 }
})
```

### 7.5 watch 与 watchEffect

```typescript
import { watch, watchEffect } from 'vue'

// 监听单个 ref
watch(count, (newValue, oldValue) => {
  console.log(`count 从 ${oldValue} 变为 ${newValue}`)
})

// 监听 reactive 对象的属性（需要 getter）
watch(() => state.count, (newVal, oldVal) => {})

// 深度监听
watch(() => state, (newVal, oldVal) => {}, { deep: true })

// 立即执行
watch(count, (newVal) => {}, { immediate: true })

// watchEffect — 自动追踪依赖
watchEffect(() => {
  console.log(`count 变化了: ${count.value}`)
})
```

### 7.6 类型定义示例

项目中 `ref.uvue` 展示了完整的响应式类型测试，包括：

```typescript
// ref 可以持有各种类型
const numRef = ref(0)
const strRef = ref('')
const boolRef = ref(true)
const arrRef = ref<number[]>([])
const objRef = ref<{ name: string }>({ name: '' })
const setRef = ref(new Set<number>())
const mapRef = ref(new Map<string, number>())
```

---

## 8. 状态管理

### 8.1 简单 Store 模式（推荐）

项目没有使用 Vuex/Pinia，而是使用 `reactive()` 单例 + 导出 setter：

```typescript
// store/index.uts — 全局状态

export type State = {
  lifeCycleNum: number
  componentMsg: number
  appLaunchPath: string
  appShowPath: string
  statusBarHeight: number
  devicePixelRatio: number
}

// 创建响应式状态
export const state = reactive<State>({
  lifeCycleNum: 0,
  componentMsg: 0,
  appLaunchPath: '',
  appShowPath: '',
  statusBarHeight: 0,
  devicePixelRatio: 0
})

// setter 函数（封装修改逻辑）
export const setLifeCycleNum = (num: number) => { state.lifeCycleNum = num }
export const setComponentMsg = (msg: number) => { state.componentMsg = msg }
export const setAppLaunchPath = (path: string) => { state.appLaunchPath = path }
export const setAppShowPath = (path: string) => { state.appShowPath = path }
```

在组件中使用：

```typescript
import { state, setLifeCycleNum } from '@/store'

// 读取状态
console.log(state.lifeCycleNum)

// 修改状态（通过 setter）
setLifeCycleNum(state.lifeCycleNum + 1)
// 或直接修改（不推荐）
state.lifeCycleNum++
```

---

## 9. 插件系统

### 9.1 definePlugin — 创建插件

**对象形式插件：**

```typescript
// plugins/plugin1.uts
import { definePlugin } from '@dcloudio/uni-app'

export default definePlugin({
  install(app) {
    app.config.globalProperties.plugin1 = '插件1-对象-属性'
    app.provide('plugin-key', value)
  }
})
```

**函数形式插件：**

```typescript
// plugins/plugin2.uts
import { definePlugin } from '@dcloudio/uni-app'

export default definePlugin(function(app) {
  app.config.globalProperties.plugin2 = '插件2-函数-属性'
})
```

### 9.2 注册插件（main.uts）

```typescript
app.use(plugin1)
app.use(plugin2)
app.use(plugin3, { /* 可选参数 */ })

// 匿名插件
app.use({
  install(app) {
    app.component('CompForPlugin1', CompForPlugin1)
  }
})
```

### 9.3 在组件中使用插件注入的数据

```vue
<script setup lang="uts">
const plugin3 = inject('plugin3')  // 从插件 provide 获取
</script>
```

---

## 10. 全局属性与依赖注入

### 10.1 全局属性设置

```typescript
// main.uts
app.config.globalProperties.$myGlobal = 'hello'
app.config.globalProperties.globalNum = 123
app.config.globalProperties.globalObj = { key: 'value' }
app.config.globalProperties.globalReactive = reactive({ count: 0 })
```

在 Options API 中通过 `this.$myGlobal` 访问。

### 10.2 依赖注入 (Provide/Inject)

**父级/全局 provide：**

```typescript
// main.uts — 全局 provide
app.provide('globalProvideMsg', '这是全局 provide 注入的数据')

// 父组件中 provide
import { provide } from 'vue'
provide('parentMsg', '父组件消息')
provide('count', ref(0))          // 注入响应式数据
```

**后代组件 inject：**

```vue
<script setup lang="uts">
import { inject } from 'vue'

const globalMsg = inject('globalProvideMsg')
const parentMsg = inject('parentMsg')
const count = inject('count')
</script>

<template>
  <text>全局消息: {{ globalMsg }}</text>
  <text>父级消息: {{ parentMsg }}</text>
</template>
```

---

## 11. 样式方案

### 11.1 样式写法

```vue
<style>
/* 全局样式 - 不写 scoped */
.page { background-color: #f8f8f8; }
</style>

<style scoped>
/* 局部样式 - 默认 scoped */
.container { padding: 20rpx; }
</style>
```

### 11.2 支持多个 `<style>` 块

```vue
<style>
/* 全局样式 */
</style>
<style scoped>
/* 局部样式 */
</style>
```

### 11.3 全局样式文件

在 `App.uvue` 中导入：

```vue
<style>
@import "@/styles/common.css";
</style>
```

`styles/common.css` 提供了基础工具类：

```css
.page { width: 750rpx; min-height: 100vh; }
.flex { display: flex; }
.flex-row { flex-direction: row; }
.flex-col { flex-direction: column; }
.flex-wrap { flex-wrap: wrap; }
.flex-1 { flex: 1; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.items-center { align-items: center; }
.my-10 { margin-top: 10rpx; margin-bottom: 10rpx; }
.mt-10 { margin-top: 10rpx; }
.mb-10 { margin-bottom: 10rpx; }
.p-10 { padding: 10rpx; }
.bold { font-weight: bold; }
```

### 11.4 SCSS 变量

```scss
// uni.scss
$uni-color-primary: #007aff;
$uni-color-success: #4cd964;
$uni-color-warning: #f0ad4e;
$uni-color-error: #dd524d;
```

---

## 12. 条件编译

### 12.1 语法说明

| 指令 | 含义 |
|---|---|
| `#ifdef APP` | App 平台（Android + iOS） |
| `#ifdef APP-ANDROID` | Android 平台 |
| `#ifdef APP-IOS` | iOS 平台 |
| `#ifdef APP-HARMONY` | Harmony 平台 |
| `#ifdef H5` / `WEB` | Web (H5) 平台 |
| `#ifdef MP` | 小程序平台 |
| `#ifndef VUE3-VAPOR` | 非 Vapor 模式 |
| `#ifndef MP` | 非小程序 |

### 12.2 在模板中使用

```vue
<!-- #ifdef APP-ANDROID -->
<text>仅在 Android 显示</text>
<!-- #endif -->

<!-- #ifndef MP -->
<text>小程序以外平台显示</text>
<!-- #endif -->
```

### 12.3 在 Script 中使用

```typescript
// #ifdef APP-ANDROID
// Android 特定实现
const info = uni.getSystemInfoSync()
console.log('Android', info)
// #endif

// #ifndef VUE3-VAPOR
// 非 Vapor 模式代码
// #endif
```

### 12.4 在 Style 中使用

```css
/* #ifdef APP-ANDROID */
.android-only {
  elevation: 4;
}
/* #endif */
```

### 12.5 在 pages.json 中使用

```json
{
  "path": "pages/reactivity/core/ref/ref-composition",
  "style": {
    "navigationBarTitleText": "ref (composition)"
  }
  // #ifndef VUE3-VAPOR
  ,
  {
    "path": "pages/reactivity/core/ref/ref-options",
    "style": {
      "navigationBarTitleText": "ref (options)"
    }
  }
  // #endif
}
```

---

## 13. UTS 类型系统

### 13.1 UTS 与 TypeScript 差异

在 `<script lang="uts">` 中使用的 UTS（Uni-app TypeScript）与标准 TypeScript 有所不同：

```typescript
// 类型导入
import { State } from '@/store'

// 类型断言：使用 as 关键字
const json = data as UTSJSONObject

// 泛型 ref
const list = ref<string[]>([])

// 组件实例类型
const childRef = ref<ComponentPublicInstance | null>(null)
```

### 13.2 UTSJSONObject 操作

uVue 中使用 `UTSJSONObject` 类型处理 JSON 数据：

```typescript
// 解析 JSON
const jsonStr = '{"name":"hello","count":42}'
const obj = JSON.parse(jsonStr)  // 返回 UTSJSONObject

// 访问属性
const name = obj['name'] as string     // 推荐方式
const count = obj['count'] as number

// 或使用 getter 方法
const name2 = obj.getString('name')     // 返回 string | null
const count2 = obj.getNumber('count')   // 返回 number | null
```

### 13.3 内置类型引用

| 类型 | 说明 |
|---|---|
| `UniElement` | 原生 DOM 元素类型 |
| `UniTextElement` | Text 元素类型 |
| `ComponentPublicInstance` | 组件公开实例类型 |
| `OnLoadOptions` | onLoad 参数类型 |
| `OnShowOptions` | onShow 参数类型 |
| `OnPageScrollOptions` | onPageScroll 参数类型 |
| `OnBackPressOptions` | onBackPress 参数类型 |
| `OnResizeOptions` | onResize 参数类型 |
| `UTSJSONObject` | JSON 对象类型（核心差异类型） |
| `VueApp` | Vue 应用实例类型 |

---

## 14. 渲染函数 (h)

### 14.1 基本用法

```vue
<script setup lang="uts">
import { h, ref } from 'vue'
import CompForHFunction from '@/components/CompForHFunction.uvue'

const count = ref(0)

const render = () => {
  return h('view', { class: 'container' }, [
    h('text', {}, `计数: ${count.value}`),
    h('button', { onClick: () => count.value++ }, '+1'),
    h(CompForHFunction, { msg: '来自渲染函数' })
  ])
}
</script>

<template>
  <render />
</template>
```

### 14.2 渲染函数工具 API

```typescript
import { h, mergeProps, cloneVNode, isVNode, resolveComponent, withDirectives, withModifiers } from 'vue'

// mergeProps — 合并多个 props 对象
const merged = mergeProps(props1, props2)

// cloneVNode — 克隆 VNode
const cloned = cloneVNode(vnode, { class: 'extra' })

// isVNode — 检查是否为 VNode
isVNode(someValue)  // 返回 boolean

// resolveComponent — 按名称解析已注册组件
const MyComp = resolveComponent('CompForAppComponent')

// withDirectives — 给 VNode 添加指令
const vnode = withDirectives(h('view'), [[vMove, 10]])

// withModifiers — 添加事件修饰符
const handler = withModifiers(onClick, ['stop', 'prevent'])
```

---

## 15. 内置组件

### 15.1 keep-alive

```vue
<keep-alive>
  <component :is="currentComponent" />
</keep-alive>
```

### 15.2 Teleport

```vue
<teleport to="body">
  <view class="modal">浮层内容</view>
</teleport>
```

### 15.3 component（动态组件）

```vue
<component :is="dynamicComponent" :props="props" />
```

---

## 16. 测试

### 16.1 测试配置

```javascript
// jest.config.js
module.exports = {
  testMatch: ['<rootDir>/pages/**/*test.[jt]s?(x)'],
  setupFiles: ['<rootDir>/jest-setup.js'],
  testSequencer: '<rootDir>/testSequencer.js',
  testTimeout: 20000
}
```

### 16.2 编写测试

```typescript
// pages/index/index.test.js
describe('首页', () => {
  beforeAll(async () => {
    // 跳转到目标页面
    await program.reLaunch({ url: '/pages/index/index' })
  })

  it('检查页面元素', async () => {
    const page = await program.currentPage()
    // 等待元素出现
    await page.waitFor('view')
    // 获取文本
    const title = await page.text('view:first-child')
    expect(title).toBe('hello uvue')
  })

  it('页面间跳转测试', async () => {
    // 跳转
    await program.navigateTo({ url: '/pages/lifecycle/page/page-composition' })
    // 回退
    await program.navigateBack()
  })
})
```

---

## 17. 自动化测试模式

### 17.1 通过 defineExpose 暴露方法给测试

页面可以通过 `defineExpose` 暴露方法，供自动化测试调用：

```vue
<script setup lang="uts">
import { ref } from 'vue'

const count = ref(0)

const checkLaunchPath = () => {
  return state.appLaunchPath
}

defineExpose({ count, checkLaunchPath })
</script>
```

### 17.2 测试中调用暴露的方法

```typescript
const page = await program.currentPage()
const result = await page.callMethod('checkLaunchPath')
expect(result).toBe('/pages/index/index')
```

### 17.3 跨页面状态校验

使用 `store/index.uts` 中的响应式状态来验证生命周期触发的次数：

```typescript
it('生命周期触发次数', async () => {
  const page = await program.currentPage()
  const lifeCycleNum = await page.callMethod('getLifeCycleNum')
  expect(lifeCycleNum).toBeGreaterThan(0)
})
```

---

## 附录

### A. 常用 uni-app API

| API | 用途 |
|---|---|
| `uni.navigateTo({ url })` | 跳转到页面 |
| `uni.redirectTo({ url })` | 重定向 |
| `uni.reLaunch({ url })` | 重新启动（清空页面栈） |
| `uni.navigateBack()` | 返回上一页 |
| `uni.showToast({ title, icon })` | Toast 提示 |
| `uni.showModal({ title, content })` | 模态弹窗 |
| `uni.getSystemInfoSync()` | 获取系统信息 |
| `uni.getSystemInfo()` | 异步获取系统信息 |
| `uni.stopPullDownRefresh()` | 停止下拉刷新 |

### B. manifest.json 关键配置

```json
{
  "name": "hello-uvue",
  "appid": "__UNI__XXXXXXX",
  "versionName": "1.0.29",
  "versionCode": "1000029",
  "uni-app-x": {
    "singleThread": true
  }
}
```

### C. package.json 的依赖要求

```json
{
  "name": "hello-uvue",
  "version": "1.0.29",
  "uni-app": {
    "scripts": {
      "hbuilderx": "^3.96",
      "uni-app-x": "^4.03"
    }
  }
}
```

---

> 本文档基于 `hello-uvue` v1.0.29 示例项目整理，覆盖了 uni-app-x / uvue 框架的主要开发模式。
> 建议结合 `pages/` 目录下的具体示例文件对照学习，每个概念都有独立的示例页面。
