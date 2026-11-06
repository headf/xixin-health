# uni-app API 文档

> **来源**: [uni-app 官网 API 概述](https://uniapp.dcloud.net.cn/api/)
> **更新日期**: 2026-06-24
> **适用版本**: uni-app (Vue 2 / Vue 3)

---

## 目录

- [API 概述](#api-概述)
- [API Promise 化](#api-promise-化)
- [API 完整列表](#api-完整列表)
  - [1. 基础](#1-基础)
  - [2. 网络](#2-网络)
  - [3. 页面和路由](#3-页面和路由)
  - [4. 数据缓存](#4-数据缓存)
  - [5. 位置](#5-位置)
  - [6. 媒体](#6-媒体)
  - [7. 设备](#7-设备)
  - [8. 键盘](#8-键盘)
  - [9. 界面](#9-界面)
  - [10. 文件](#10-文件)
  - [11. 画布](#11-画布)
  - [12. 组件上下文对象](#12-组件上下文对象)
  - [13. 广告](#13-广告)
  - [14. 第三方服务](#14-第三方服务)
  - [15. 平台扩展](#15-平台扩展)
  - [16. 其他](#16-其他)
- [Worker](#worker)
- [uniCloud](#unicloud)
- [uni ext api](#uni-ext-api)

---

## API 概述

`uni-app` 的 js API 由标准 ECMAScript 的 js API 和 uni 扩展 API 这两部分组成。

**标准 js 和浏览器 js 的区别**:
- `uni-app` 的 js 代码，web 端运行于浏览器中。
- 非 web 端（包含小程序和 App）:
  - Android 平台: 运行在 v8 引擎中
  - iOS 平台: 运行在 iOS 自带的 jscore 引擎中
- 非 web 端**不支持** `window`、`document`、`navigator` 等浏览器的 js API，但**支持标准 ECMAScript**。

**补充说明**:
- `uni.on` 开头的 API 是监听某个事件发生的 API 接口，接受一个 CALLBACK 函数作为参数。
- 如未特殊约定，其他 API 接口都接受一个 OBJECT 作为参数。
- OBJECT 中可以指定 `success`、`fail`、`complete` 来接收接口调用结果。
- **平台差异说明**: 若无特殊说明，则表示所有平台均支持。
- 异步 API 会返回 `errMsg` 字段，同步 API 则不会。

---

## API Promise 化

### 策略

1. **异步方法**，如果不传入 `success`、`fail`、`complete` 等 callback 参数，将以 Promise 返回数据。
   ```js
   // 传统回调方式
   uni.getImageInfo({
     success(res) { console.log(res) }
   })

   // Promise 化
   uni.getImageInfo().then(res => {
     console.log(res)
   })
   ```

2. **异步方法且有返回对象**，如果希望获取返回对象，必须至少传入一项 `success`、`fail`、`complete` 等 callback 参数。
   ```js
   // 正常使用 (可获取 task 返回对象)
   const task = uni.connectSocket({
     success(res) { console.log(res) }
   })

   // Promise 化 (无法获取 task 对象)
   uni.connectSocket().then(res => {
     console.log(res)
   })
   ```

3. **不进行 Promise 化** 的 API: 同步方法（以 Sync 结尾），以及以下方法:
   - `uni.addInterceptor`
   - `uni.removeInterceptor`
   - `uni.createMapContext`
   - `uni.createAnimation`
   - `uni.createSelectorQuery`
   - `uni.createIntersectionObserver`
   - `uni.createMediaQueryObserver`
   - `uni.createAudioContext`

### Vue 2 和 Vue 3 的 API Promise 化差异

| Vue 版本 | 返回格式 | 错误处理 |
|----------|----------|----------|
| **Vue 2** | 对部分 API 进行了 Promise 封装，返回数据的第一个参数是错误对象，第二个参数是返回数据 | 内部对错误进行了拦截，catch 拿不到报错信息 |
| **Vue 3** | 对部分 API 进行了 Promise 封装 | 调用成功进入 `then` 回调，调用失败进入 `catch` 回调 |

**Vue 2 转 Vue 3** (使用 `@dcloudio/uni-app` 的 `promise` 模块):
```js
import { promise } from '@dcloudio/uni-app'
// 将 uni-api 转为 Promise 返回
```

---

## API 完整列表

### 1. 基础

> 日志打印、定时器、数据转换、应用启动等。

#### 通用基础 API

| API | 说明 |
|-----|------|
| **日志打印** | 向控制台打印日志信息 |
| **定时器** | 在定时到期以后执行注册的回调函数 |
| `uni.base64ToArrayBuffer` | 将 Base64 字符串转成 ArrayBuffer 对象 |
| `uni.arrayBufferToBase64` | 将 ArrayBuffer 对象转成 Base64 字符串 |
| `uni.canIUse` | 判断应用的 API、回调、参数、组件等是否在当前版本可用 |

#### 启动

| API | 说明 |
|-----|------|
| `getLaunchOptionsSync` | 获取启动时的参数。返回值与 `App.onLaunch` 的回调参数一致 |
| `getEnterOptionsSync` | 获取启动时的参数 |

#### 应用级事件

| API | 说明 |
|-----|------|
| **应用级事件** | 监听应用事件（如 `onShow`、`onHide` 等） |

#### 拦截器

| API | 说明 |
|-----|------|
| `uni.addInterceptor` | 拦截 API 等调用并执行回调 |
| `uni.removeInterceptor` | 移除拦截器 |

#### 全局 API

| API | 说明 |
|-----|------|
| **全局 API** | 可全局调用的 API |

---

### 2. 网络

#### 发起请求

| API | 说明 |
|-----|------|
| `uni.request` | 发起网络请求 |

#### 上传、下载

| API | 说明 |
|-----|------|
| `uni.uploadFile` | 上传文件 |
| `uni.downloadFile` | 下载文件 |

#### WebSocket

| API | 说明 |
|-----|------|
| `uni.connectSocket` | 创建 WebSocket 连接 |
| `uni.onSocketOpen` | 监听 WebSocket 打开 |
| `uni.onSocketError` | 监听 WebSocket 错误 |
| `uni.sendSocketMessage` | 发送 WebSocket 消息 |
| `uni.onSocketMessage` | 接受 WebSocket 消息 |
| `uni.closeSocket` | 关闭 WebSocket 连接 |
| `uni.onSocketClose` | 监听 WebSocket 关闭 |

#### SocketTask

| API | 说明 |
|-----|------|
| `SocketTask.send` | 通过 WebSocket 连接发送数据 |
| `SocketTask.close` | 关闭 WebSocket 连接 |
| `SocketTask.onOpen` | 监听 WebSocket 连接打开事件 |
| `SocketTask.onClose` | 监听 WebSocket 连接关闭事件 |
| `SocketTask.onError` | 监听 WebSocket 错误事件 |
| `SocketTask.onMessage` | 监听 WebSocket 接受到服务器的消息事件 |

#### 其他网络服务

| API | 说明 |
|-----|------|
| **mDNS** | mDNS 服务 |
| **UDP 通信** | UDP 通信 |

---

### 3. 页面和路由

> 详细文档: [https://uniapp.dcloud.net.cn/api/router](https://uniapp.dcloud.net.cn/api/router)

#### 路由跳转

| API | 说明 |
|-----|------|
| `uni.navigateTo` | 保留当前页面，跳转到应用内的某个页面，使用 `uni.navigateBack` 可以返回到原页面 |
| `uni.redirectTo` | 关闭当前页面，跳转到应用内的某个页面 |
| `uni.reLaunch` | 关闭所有页面，打开到应用内的某个页面 |
| `uni.switchTab` | 跳转到 tabBar 页面，并关闭其他所有非 tabBar 页面 |
| `uni.navigateBack` | 关闭当前页面，返回上一页面或多级页面 |
| `uni.preloadPage` | 预加载页面，是一种性能优化技术。被预载的页面，在打开时速度更快 |

#### EventChannel（页面间事件通信）

| API | 说明 |
|-----|------|
| `EventChannel` | 页面间事件通信通道 |

#### 窗口动画

| API | 说明 |
|-----|------|
| **窗口动画** | 窗口的显示/关闭动画效果，支持在 API、组件、pages.json 中配置 |

#### 页面

| API | 说明 |
|-----|------|
| `getCurrentPages` | 函数用于获取当前页面栈的实例 |
| `$getAppWebview` | 当前 webview 的对象实例 |
| `$vm` | 当前页面的 Vue 实例 |
| **页面通讯** | `$emit`、`$on`、`$off`、`$once` |

#### subNVue 原生子窗体

| API | 说明 |
|-----|------|
| `getSubNVueById` | 通过 ID 获取 subNVues 原生子窗体的实例 |
| `$getCurrentSubNVue` | 在一个 subnvue 窗体的 nvue 页面代码中，获取当前 subNVues 原生子窗体的实例 |
| `subNVue.show` | 显示原生子窗体 |
| `subNVue.hide` | 隐藏原生子窗体 |
| `subNVue.setStyle` | 设置原生子窗体的样式 |
| `subNVue.postMessage` | 向宿主窗体发送消息 |
| `subNVue.onMessage` | 监听来自宿主窗体的消息 |

> 参考: [https://uniapp.dcloud.net.cn/api/window/subNVues](https://uniapp.dcloud.net.cn/api/window/subNVues)

---

### 4. 数据缓存

> 详细文档: [https://uniapp.dcloud.net.cn/api/storage/storage](https://uniapp.dcloud.net.cn/api/storage/storage)

| API | 说明 |
|-----|------|
| `uni.getStorage` | 获取本地数据缓存（异步） |
| `uni.getStorageSync` | 获取本地数据缓存（同步） |
| `uni.setStorage` | 设置本地数据缓存（异步） |
| `uni.setStorageSync` | 设置本地数据缓存（同步） |
| `uni.getStorageInfo` | 获取本地缓存的相关信息（异步） |
| `uni.getStorageInfoSync` | 获取本地缓存的相关信息（同步） |
| `uni.removeStorage` | 删除本地缓存内容（异步） |
| `uni.removeStorageSync` | 删除本地缓存内容（同步） |
| `uni.clearStorage` | 清理本地数据缓存（异步） |
| `uni.clearStorageSync` | 清理本地数据缓存（同步） |

---

### 5. 位置

#### 获取位置

| API | 说明 |
|-----|------|
| `uni.getLocation` | 获取当前位置 |
| `uni.chooseLocation` | 打开地图选择位置 |

#### 查看位置

| API | 说明 |
|-----|------|
| `uni.openLocation` | 打开内置地图 |

#### 位置更新

| API | 说明 |
|-----|------|
| `uni.onLocationChange` | 监听实时地理位置变化事件 |
| `uni.offLocationChange` | 移除实时地理位置变化事件的监听函数 |
| `uni.onLocationChangeError` | 监听持续定位接口返回失败时触发 |
| `uni.offLocationChangeError` | 取消注册位置更新错误回调 |
| `uni.startLocationUpdate` | 开启应用进入前台时接收位置消息 |
| `uni.stopLocationUpdate` | 关闭监听实时位置变化，前后台都停止消息接收 |
| `uni.startLocationUpdateBackground` | 开始监听实时地理位置信息变化事件，应用进入前后台时均接收实时地理位置信息 |

#### 地图组件控制

| API | 说明 |
|-----|------|
| `uni.createMapContext` | 地图组件控制 |

---

### 6. 媒体

#### 图片

| API | 说明 |
|-----|------|
| `uni.chooseImage` | 从相册选择图片，或者拍照 |
| `uni.previewImage` | 预览图片 |
| `uni.closePreviewImage` | 关闭预览图片 |
| `uni.getImageInfo` | 获取图片信息 |
| `uni.saveImageToPhotosAlbum` | 保存图片到系统相册 |
| `uni.compressImage` | 压缩图片接口，可选压缩质量 |

#### 文件

| API | 说明 |
|-----|------|
| `uni.chooseFile` | 从本地选择文件 |

#### 录音管理

| API | 说明 |
|-----|------|
| `uni.getRecorderManager` | 录音管理 |

#### 背景音频播放管理

| API | 说明 |
|-----|------|
| `uni.getBackgroundAudioManager` | 背景音频播放管理 |

#### 音频组件管理

| API | 说明 |
|-----|------|
| `uni.createInnerAudioContext` | 音频组件管理 |

#### 视频

| API | 说明 |
|-----|------|
| `uni.chooseVideo` | 从相册选择视频，或者拍摄 |
| `uni.chooseMedia` | 拍摄或从手机相册中选择图片或视频 |
| `uni.saveVideoToPhotosAlbum` | 保存视频到系统相册 |
| `uni.getVideoInfo` | 获取视频详细信息 |
| `uni.compressVideo` | 压缩视频接口 |
| `uni.openVideoEditor` | 打开视频编辑器 |

#### 视频组件管理

| API | 说明 |
|-----|------|
| `uni.createVideoContext` | 视频组件管理 |

#### 相机组件管理

| API | 说明 |
|-----|------|
| `uni.createCameraContext` | 相机组件管理 |

#### 直播组件管理

| API | 说明 |
|-----|------|
| `uni.createLivePlayerContext` | 直播组件管理 |
| `uni.createLivePusherContext` | 创建 live-pusher 上下文 LivePusherContext 对象 |

#### 富文本

| API | 说明 |
|-----|------|
| `editorContext` | 富文本编辑器 |

#### 音视频合成

| API | 说明 |
|-----|------|
| `uni.createMediaContainer` | 创建音视频处理容器，最终可将容器中的轨道合成一个视频 |

---

### 7. 设备

> 系统信息、网络状态、加速度计、罗盘、陀螺仪、蓝牙、NFC 等。

#### 系统信息

| API | 说明 |
|-----|------|
| `uni.getSystemInfo` | 异步获取系统信息 |
| `uni.getDeviceInfo` | 获取设备基础信息 |
| `uni.getWindowInfo` | 获取窗口信息 |
| `uni.getAppBaseInfo` | 获取 APP 基础信息 |
| `uni.getAppAuthorizeSetting` | 获取 APP 授权设置 |
| `uni.getSystemSetting` | 获取设备设置 |
| `uni.openAppAuthorizeSetting` | 跳转系统授权管理页 |
| `uni.createRequestPermissionListener` | 创建一个监听权限申请 |
| `uni.onMemoryWarning` | 监听内存不足告警事件 |

#### 网络状态

| API | 说明 |
|-----|------|
| `uni.getNetworkType` | 获取网络类型 |
| `uni.onNetworkStatusChange` | 监听网络状态变化 |
| `uni.offNetworkStatusChange` | 取消监听网络状态变化 |

#### 系统主题

| API | 说明 |
|-----|------|
| `uni.onThemeChange` | 监听系统主题状态变化 |
| `uni.offThemeChange` | 取消监听系统主题状态变化 |

#### 加速度计

| API | 说明 |
|-----|------|
| `uni.onAccelerometerChange` | 监听加速度数据 |
| `uni.offAccelerometerChange` | 取消监听加速度数据 |
| `uni.startAccelerometer` | 开始监听加速度数据 |
| `uni.stopAccelerometer` | 停止监听加速度数据 |

#### 罗盘

| API | 说明 |
|-----|------|
| `uni.onCompassChange` | 监听罗盘数据 |
| `uni.offCompassChange` | 取消监听罗盘数据 |
| `uni.startCompass` | 开始监听罗盘数据 |
| `uni.stopCompass` | 停止监听罗盘数据 |

#### 陀螺仪

| API | 说明 |
|-----|------|
| `uni.onGyroscopeChange` | 监听陀螺仪数据 |
| `uni.startGyroscope` | 开始监听陀螺仪数据 |
| `uni.stopGyroscope` | 停止监听陀螺仪数据 |

#### 拨打电话

| API | 说明 |
|-----|------|
| `uni.makePhoneCall` | 拨打电话 |

#### 扫码

| API | 说明 |
|-----|------|
| `uni.scanCode` | 扫码 |

#### 剪贴板

| API | 说明 |
|-----|------|
| `uni.setClipboardData` | 设置剪贴板内容 |
| `uni.getClipboardData` | 获取剪贴板内容 |

#### 屏幕亮度

| API | 说明 |
|-----|------|
| `uni.setScreenBrightness` | 设置屏幕亮度 |
| `uni.getScreenBrightness` | 获取屏幕亮度 |
| `uni.setKeepScreenOn` | 设置是否保持常亮状态 |

#### 用户截屏事件

| API | 说明 |
|-----|------|
| `uni.onUserCaptureScreen` | 监听用户截屏事件 |

#### 振动

| API | 说明 |
|-----|------|
| `uni.vibrate` | 使手机发生振动 |
| `uni.vibrateLong` | 使手机发生较长时间的振动 |
| `uni.vibrateShort` | 使手机发生较短时间的振动 |

#### 手机联系人

| API | 说明 |
|-----|------|
| `uni.addPhoneContact` | 添加手机通讯录 |

#### 蓝牙

| API | 说明 |
|-----|------|
| `uni.openBluetoothAdapter` | 初始化蓝牙模块 |
| `uni.startBluetoothDevicesDiscovery` | 搜寻附近的蓝牙外围设备 |
| `uni.onBluetoothDeviceFound` | 监听寻找到新设备的事件 |
| `uni.stopBluetoothDevicesDiscovery` | 停止搜寻 |
| `uni.onBluetoothAdapterStateChange` | 监听蓝牙适配器状态变化事件 |
| `uni.getConnectedBluetoothDevices` | 根据 uuid 获取处于已连接状态的设备 |
| `uni.getBluetoothDevices` | 获取已发现的蓝牙设备 |
| `uni.getBluetoothAdapterState` | 获取本机蓝牙适配器状态 |
| `uni.closeBluetoothAdapter` | 关闭蓝牙模块 |

#### 低功耗蓝牙

| API | 说明 |
|-----|------|
| `uni.writeBLECharacteristicValue` | 向低功耗蓝牙设备特征值中写入二进制数据 |
| `uni.readBLECharacteristicValue` | 读取低功耗蓝牙设备的特征值的二进制数据值 |
| `uni.onBLEConnectionStateChange` | 监听低功耗蓝牙连接状态的改变事件 |
| `uni.onBLECharacteristicValueChange` | 监听低功耗蓝牙设备的特征值变化事件 |
| `uni.notifyBLECharacteristicValueChange` | 启用蓝牙低功耗设备特征值变化时的 notify 功能，订阅特征 |
| `uni.getBLEDeviceServices` | 获取蓝牙设备所有服务(service) |
| `uni.getBLEDeviceCharacteristics` | 获取蓝牙设备某个服务中所有特征值(characteristic) |
| `uni.createBLEConnection` | 连接低功耗蓝牙设备 |
| `uni.closeBLEConnection` | 断开与低功耗蓝牙设备的连接 |

#### iBeacon

| API | 说明 |
|-----|------|
| `uni.onBeaconServiceChange` | 监听 iBeacon 服务状态变化事件 |
| `uni.onBeaconUpdate` | 监听 iBeacon 设备更新事件 |
| `uni.getBeacons` | 获取所有已搜索到的 iBeacon 设备 |
| `uni.startBeaconDiscovery` | 开始搜索附近的 iBeacon 设备 |
| `uni.stopBeaconDiscovery` | 停止搜索附近的 iBeacon 设备 |

#### Wi-Fi

| API | 说明 |
|-----|------|
| **Wi-Fi** | Wi-Fi 相关功能 |

#### 电量

| API | 说明 |
|-----|------|
| **电量** | 电量相关功能 |

#### NFC

| API | 说明 |
|-----|------|
| **NFC** | NFC 相关功能 |

#### 设备方向

| API | 说明 |
|-----|------|
| **设备方向** | 设备方向相关功能 |

#### 生物认证

| API | 说明 |
|-----|------|
| `uni.startSoterAuthentication` | 开始生物认证 |
| `uni.checkIsSupportSoterAuthentication` | 获取本机支持的生物认证方式 |
| `uni.checkIsSoterEnrolledInDevice` | 获取设备内是否录入如指纹等生物信息的接口 |

---

### 8. 键盘

> 详细文档: [https://uniapp.dcloud.net.cn/api/key](https://uniapp.dcloud.net.cn/api/key)

| API | 说明 |
|-----|------|
| `uni.hideKeyboard` | 隐藏已经显示的软键盘，如果软键盘没有显示则不做任何操作 |
| `uni.onKeyboardHeightChange` | 监听键盘高度变化 |
| `uni.offKeyboardHeightChange` | 取消监听键盘高度变化事件 |
| `uni.getSelectedTextRange` | 在 input、textarea 等 focus 之后，获取输入框的光标位置 |

---

### 9. 界面

#### 交互反馈

| API | 说明 |
|-----|------|
| `uni.showToast` | 显示提示框 |
| `uni.showLoading` | 显示加载提示框 |
| `uni.hideToast` | 隐藏提示框 |
| `uni.hideLoading` | 隐藏加载提示框 |
| `uni.showModal` | 显示模态弹窗 |
| `uni.showActionSheet` | 显示菜单列表 |

#### 设置导航条

| API | 说明 |
|-----|------|
| `uni.setNavigationBarTitle` | 设置当前页面标题 |
| `uni.setNavigationBarColor` | 设置页面导航条颜色 |
| `uni.showNavigationBarLoading` | 显示导航条加载动画 |
| `uni.hideNavigationBarLoading` | 隐藏导航条加载动画 |

#### 设置 TabBar

| API | 说明 |
|-----|------|
| `uni.setTabBarItem` | 动态设置 tabBar 某一项的内容 |
| `uni.setTabBarStyle` | 动态设置 tabBar 的整体样式 |
| `uni.hideTabBar` | 隐藏 tabBar |
| `uni.showTabBar` | 显示 tabBar |
| `uni.setTabBarBadge` | 为 tabBar 某一项的右上角添加文本 |
| `uni.removeTabBarBadge` | 移除 tabBar 某一项右上角的文本 |
| `uni.showTabBarRedDot` | 显示 tabBar 某一项的右上角的红点 |
| `uni.hideTabBarRedDot` | 隐藏 tabBar 某一项的右上角的红点 |

#### 背景

| API | 说明 |
|-----|------|
| `uni.setBackgroundColor` | 动态设置窗口的背景色 |
| `uni.setBackgroundTextStyle` | 动态设置下拉背景字体、loading 图的样式 |

#### 动画

| API | 说明 |
|-----|------|
| `uni.createAnimation` | 创建一个动画实例 animation。调用实例的方法来描述动画。最后通过动画实例的 export 方法导出动画数据传递给组件的 animation 属性 |

#### 滚动

| API | 说明 |
|-----|------|
| `uni.pageScrollTo` | 将页面滚动到目标位置 |

#### 窗口

| API | 说明 |
|-----|------|
| `uni.onWindowResize` | 监听窗口尺寸变化事件 |
| `uni.offWindowResize` | 取消监听窗口尺寸变化事件 |

#### 宽屏适配

| API | 说明 |
|-----|------|
| `uni.getTopWindowStyle` | 获取 topWindow 的样式 |
| `uni.getLeftWindowStyle` | 获取 leftWindow 的样式 |
| `uni.getRightWindowStyle` | 获取 rightWindow 的样式 |
| `uni.setTopWindowStyle` | 设置 topWindow 的样式 |
| `uni.setLeftWindowStyle` | 设置 leftWindow 的样式 |
| `uni.setRightWindowStyle` | 设置 rightWindow 的样式 |

#### 字体

| API | 说明 |
|-----|------|
| `uni.loadFontFace` | 动态加载字体 |
| `uni.rpx2px` | 将 rpx 单位值转换成 px |
| `uni.upx2px` | 将 upx 单位值转换成 px |

#### 下拉刷新

| API | 说明 |
|-----|------|
| `onPullDownRefresh` | 监听该页面用户下拉刷新事件 |
| `uni.startPullDownRefresh` | 开始下拉刷新 |
| `uni.stopPullDownRefresh` | 停止当前页面下拉刷新 |

#### 节点信息

| API | 说明 |
|-----|------|
| `uni.createSelectorQuery` | 创建查询请求 |
| `selectorQuery.select` | 根据选择器选择单个节点 |
| `selectorQuery.selectAll` | 根据选择器选择全部节点 |
| `selectorQuery.selectViewport` | 选择显示区域 |
| `selectorQuery.exec` | 执行查询请求 |
| `nodesRef.boundingClientRect` | 获取布局位置和尺寸 |
| `nodesRef.scrollOffset` | 获取滚动位置 |
| `nodesRef.fields` | 获取任意字段 |

#### 节点布局相交状态

| API | 说明 |
|-----|------|
| `uni.createIntersectionObserver` | 创建 IntersectionObserver 对象 |
| `intersectionObserver.relativeTo` | 指定参照节点 |
| `intersectionObserver.relativeToViewport` | 指定页面显示区域作为参照区域 |
| `intersectionObserver.observe` | 指定目标节点并开始监听 |
| `intersectionObserver.disconnect` | 停止监听 |

#### 媒体查询

| API | 说明 |
|-----|------|
| `uni.createMediaQueryObserver` | 创建并返回一个 MediaQueryObserver 对象实例 |

#### 菜单

| API | 说明 |
|-----|------|
| `getMenuButtonBoundingClientRect` | 获取右上角悬浮按钮布局 |

#### 语言

| API | 说明 |
|-----|------|
| `uni.getLocale` | 获取当前设置的语言 |
| `uni.setLocale` | 设置当前语言 |
| `uni.onLocaleChange` | 用于监听应用语言切换 |

---

### 10. 文件

> 详细文档: [https://uniapp.dcloud.net.cn/api/file/file](https://uniapp.dcloud.net.cn/api/file/file)

| API | 说明 |
|-----|------|
| `uni.saveFile` | 保存文件 |
| `uni.getSavedFileList` | 获取已保存的文件列表 |
| `uni.getSavedFileInfo` | 获取已保存的文件信息 |
| `uni.removeSavedFile` | 删除已保存的文件信息 |
| `uni.getFileInfo` | 获取文件信息 |
| `uni.openDocument` | 打开文件 |
| `uni.getFileSystemManager` | 获取文件管理器 |

> 参考: [getFileSystemManager 详细文档](https://uniapp.dcloud.net.cn/api/file/getFileSystemManager.html)

---

### 11. 画布

| API | 说明 |
|-----|------|
| `uni.createCanvasContext` | 创建绘图上下文 |
| `uni.canvasToTempFilePath` | 将画布内容保存成文件 |
| `uni.canvasGetImageData` | 获取画布图像数据 |
| `uni.canvasPutImageData` | 设置画布图像数据 |

---

### 12. 组件上下文对象

| API | 说明 |
|-----|------|
| `uni.createWebviewContext` | 创建 web-view 组件的上下文对象，用于操作 web-view 的行为 |

---

### 13. 广告

| API | 说明 |
|-----|------|
| **激励视频广告** | 激励视频广告，是 cpm 收益最高的广告形式 |
| **全屏视频广告** | 全屏视频广告 |
| **内容联盟广告** | 内容联盟广告 |
| **插屏广告** | 插屏广告 |

---

### 14. 第三方服务

| API | 说明 |
|-----|------|
| `uni.getProvider` | 获取服务供应商 |
| `uni.login` | 登录 |
| `uni.getUserInfo` | 获取用户信息 |
| `uni.getUserProfile` | 获取用户信息。每次请求都会弹出授权窗口，用户同意后返回 userInfo |
| `uni.checkSession` | 检查登录状态是否过期 |
| `uni.preLogin` | 预登录 |
| `uni.closeAuthView` | 关闭一键登录页面 |
| `uni.getCheckBoxState` | 获取一键登录条款勾选框状态 |
| `uni.getUniverifyManager` | 获取全局唯一的一键登录管理器 univerifyManager |
| `uni.share` | 分享 |
| `uni.shareWithSystem` | 使用系统分享 |
| `uni.requestPayment` | 支付 |
| `uni.requestVirtualPayment` | 虚拟支付 |
| `uni.requestMerchantTransfer` | 商家转账用户确认模式下，拉起页面请求用户确认收款 |
| `uni.getPushClientId` | 获取客户端唯一的推送标识 |
| `uni.onPushMessage` | 启动监听推送消息事件 |
| `uni.offPushMessage` | 关闭推送消息监听事件 |
| `uni.getChannelManager` | 获取通知渠道管理器，Android 8 以上系统才可以设置通知渠道 |
| `uni.createPushMessage` | 创建本地通知栏消息（HBuilderX 3.5.2 起支持） |
| `uni.getFacialRecognitionMetaInfo` | 获取实人认证设备信息 |
| `uni.startFacialRecognitionVerify` | 实人认证 |
| `uni.requireNativePlugin` | 引入 App 原生插件 |

---

### 15. 平台扩展

| API | 说明 |
|-----|------|
| `uni.requireNativePlugin` | 引入 App 原生插件 |

---

### 16. 其他

#### 授权

| API | 说明 |
|-----|------|
| `uni.authorize` | 提前向用户发起授权请求 |

#### 设置

| API | 说明 |
|-----|------|
| `uni.openSetting` | 调起客户端小程序设置界面，返回用户设置的操作结果 |
| `uni.getSetting` | 获取用户的当前设置 |

#### 收货地址

| API | 说明 |
|-----|------|
| `uni.chooseAddress` | 获取用户收货地址 |

#### 获取发票抬头

| API | 说明 |
|-----|------|
| `uni.chooseInvoiceTitle` | 选择用户的发票抬头，需要用户授权 `scope.invoiceTitle` |

#### 小程序跳转

| API | 说明 |
|-----|------|
| `uni.navigateToMiniProgram` | 打开另一个小程序 |
| `uni.navigateBackMiniProgram` | 跳转回上一个小程序，只有当另一个小程序跳转到当前小程序时才会能调用成功 |

#### 模板消息

| API | 说明 |
|-----|------|
| `addTemplate` | 组合模板并添加至帐号下的个人模板库 |
| `deleteTemplate` | 删除帐号下的某个模板 |
| `getTemplateLibraryById` | 获取模板库某个模板标题下关键词库 |
| `getTemplateLibraryList` | 获取 APP 模板库标题列表 |
| `getTemplateList` | 获取帐号下已存在的模板列表 |
| `sendTemplateMessage` | 发送模板消息 |
| `alipay.open.app.mini.templatemessage.send` | 支付宝小程序通过 openapi 给用户触达消息，主要为支付后的触达（通过消费 id）和用户提交表单后的触达（通过 formId） |

#### 小程序更新

| API | 说明 |
|-----|------|
| `uni.getUpdateManager` | 返回全局唯一的版本更新管理器对象 `updateManager`，用于管理小程序更新 |

#### 调试

| API | 说明 |
|-----|------|
| `uni.setEnableDebug` | 设置是否打开调试开关。此开关对正式版也能生效 |

#### 获取第三方平台数据

| API | 说明 |
|-----|------|
| `uni.getExtConfig` | 获取第三方平台自定义的数据字段 |
| `uni.getExtConfigSync` | `uni.getExtConfig` 的同步版本 |

---

## Worker

> 详细文档: [https://uniapp.dcloud.net.cn/api/worker.html](https://uniapp.dcloud.net.cn/api/worker.html)

Worker 为 uni-app 提供多线程能力，用于执行耗时的计算任务，避免阻塞主线程。

---

## uniCloud

> 详细文档: [https://uniapp.dcloud.net.cn/api/uniCloud.html](https://uniapp.dcloud.net.cn/api/uniCloud.html)

uniCloud 是 DCloud 联合阿里云、腾讯云提供的云服务，包括云函数、云数据库、云存储等能力。

---

## uni ext api

> 详细文档: [https://uniapp.dcloud.net.cn/api/extapi.html](https://uniapp.dcloud.net.cn/api/extapi.html)

uni ext api 是 uni-app 的扩展 API 机制，允许开发者通过插件方式扩展 uni-app 的 API 能力。

---

## 附录：API 分类汇总

| 分类 | API 数量 | 主要能力 |
|------|----------|----------|
| 基础 | ~10 | 日志、定时器、数据转换、启动参数 |
| 网络 | ~15 | 请求、上传下载、WebSocket |
| 页面和路由 | ~20 | 页面跳转、页面通讯、子窗体 |
| 数据缓存 | 10 | 本地存储读写 |
| 位置 | ~12 | 定位、地图、位置更新 |
| 媒体 | ~22 | 图片、音频、视频、相机、直播 |
| 设备 | ~55 | 系统信息、蓝牙、NFC、传感器 |
| 键盘 | 4 | 键盘显隐、高度监听 |
| 界面 | ~50 | 交互反馈、导航、TabBar、动画、节点查询 |
| 文件 | 7 | 文件保存、读取、管理 |
| 画布 | 4 | 绘图、图像数据 |
| 组件上下文 | 1 | web-view 控制 |
| 广告 | 4 | 激励视频、插屏等 |
| 第三方服务 | ~20 | 登录、支付、推送、分享、实人认证 |
| 其他 | ~17 | 授权、设置、小程序跳转、模板消息 |

---

> **文档来源**: [uni-app 官网 API 概述](https://uniapp.dcloud.net.cn/api/)
> **最后更新**: 2026-06-24
> **说明**: 本文档根据 uni-app 官方 API 文档整理，详细参数和平台差异请参考官方文档中各 API 对应的详细页面。
