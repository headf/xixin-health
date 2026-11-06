# 熙心健康体检管理系统 — 全局布局对齐规范

> **版本**: v1.0  
> **适用范围**: 所有前端页面（护士端、医生端、审核端）  
> **核心原则**: 统一边距基准、消除零散硬编码、模块边缘共享同一条对齐线

---

## 一、间距系统（Spacing System）

所有间距、内边距、外边距必须使用以下 CSS 变量，禁止零散 `px` 硬编码。

| 变量名 | 值 | 适用场景 |
|--------|-----|----------|
| `--space-4` | 4px | 图标与文字最小间距、标签内间距 |
| `--space-8` | 8px | 表单 label 与 input 间距、按钮组 gap、图标与标题间距 |
| `--space-12` | 12px | 表格单元格垂直内边距、卡片 header 底部内边距 |
| `--space-16` | 16px | 模块间垂直间距、网格 gap、表单 field gap |
| `--space-20` | 20px | 统计卡片内部 padding |
| `--space-24` | 24px | **页面级左右内边距**、卡片 body 内边距、卡片 header 水平内边距 |
| `--space-32` | 32px | 大区块间距 |
| `--space-48` | 48px | 空状态垂直内边距 |

### 间距使用黄金法则

```
模块垂直间距   = --space-16
模块水平对齐   = --space-24（距卡片/容器边缘）
内部小组件间距 = --space-8
紧凑场景间距   = --space-4
```

---

## 二、页面级对齐基准线（Page Baseline）

### 2.1 页面容器

```css
.page {
  max-width: 1280px;
  margin: 0 auto;
}
```

- 页面内容区外层由 `main.content` 提供 `padding: 24px`
- 所有模块以 `.page` 容器为基准对齐左右边缘

### 2.2 页面标题区域（.page-hd）

```css
.page-hd {
  margin-bottom: 24px;          /* --space-24 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.page-hd h2 { font-size: 22px; font-weight: 700; }
.page-hd .desc { font-size: 14px; color: var(--n-500); margin-top: 4px; }
```

**对齐规则**:
- 标题 `.page-hd` 的 **左边缘** 即所有模块的左对齐基准线
- 标题下方间距固定 `--space-24`

---

## 三、模块对齐规范

### 3.1 卡片（Card）

```css
.card {
  background: var(--card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
}
.card-hd {
  padding: 16px 24px 12px;           /* 左/右边距 24px */
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
}
.card-bd {
  padding: 20px 24px 24px;           /* 左/右边距 24px，与 card-hd 对齐 */
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
}
```

**对齐要求**:
- `card-hd` 与 `card-bd` 的 **水平内边距严格统一**（均为 `24px`）
- 卡片与卡片之间垂直间距：`--space-16`（通过 `.card + .card { margin-top: 16px; }`）
- 卡片 header 的图标 + 标题使用 `display: inline-flex; gap: 8px` 保证对齐

### 3.2 表格（Table）

```css
table.tbl {
  width: 100%;
  border-collapse: collapse;
}
table.tbl thead th {
  padding: 12px 16px;
  text-align: left;
  vertical-align: middle;             /* 强制垂直居中 */
}
table.tbl tbody td {
  padding: 12px 16px;
  vertical-align: middle;             /* 强制垂直居中 */
}
/* pad-0 容器中表格首/末列与 card-hd 对齐 */
.card-bd.pad-0 table.tbl thead th:first-child,
.card-bd.pad-0 table.tbl tbody td:first-child {
  padding-left: 24px;
}
.card-bd.pad-0 table.tbl thead th:last-child,
.card-bd.pad-0 table.tbl tbody td:last-child {
  padding-right: 24px;
}
```

**对齐要求**:
- 表头列与内容列 **严格一一对齐**（同宽度、同起始位置）
- 所有单元格内容 **垂直居中**
- 单元格内标签（Tag）使用 `display: inline-flex; align-items: center` 保持与文字对齐
- 表格在 `pad-0` 卡片中铺满容器宽度，但 **首列 padding-left 和末列 padding-right 必须为 `24px`**，与 card-hd 的文字起始位置保持一致
- 空状态需有 `padding: 48px 24px` 保持水平对齐

### 3.3 表单（Form）

```css
.form-grid { display: grid; gap: 16px; }
.form-2 { grid-template-columns: 1fr 1fr; }
.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--n-600);
}
.finput, .fselect {
  height: 40px;
  padding: 0 14px;
  border-radius: var(--radius-md);
}
```

**对齐要求**:
- 同网格内多列表单项：**标签顶部对齐**、输入框顶部对齐
- 所有输入框 `height: 40px` 统一
- label 与 input 间距：`--space-8`
- 标签文字垂直居中对齐输入框内文字基线
- 底部按钮组垂直方向与输入框基线对齐

### 3.4 统计卡片（Stats）

```css
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);   /* 固定 4 列，宽度均等 */
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 84px;                        /* 保证四张卡片高度一致 */
}
.stat-icon {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}
```

**对齐要求**:
- 所有卡片等宽等高、间距均等
- 图标与文字区块垂直居中
- 卡片左右边缘与下方表单/表格模块边缘严格对齐（通过 `.stats` 本身的宽度继承 `.page` 约束实现）
- 响应式断点：`≤900px` 时降为 2 列，`≤500px` 时降为 1 列

### 3.5 步骤条/指引（Steps）

```css
.steps {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 0;
}
.step-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  white-space: nowrap;
}
.step-arrow {
  display: inline-flex;
  align-items: center;
  margin: 0 4px;
  color: var(--n-300);
}
```

**对齐要求**:
- 所有步骤项的图标与文字垂直居中
- 步骤项之间箭头间距均等（`margin: 0 4px`）
- 步骤条不换行（`flex-wrap: nowrap`）或在换行时保持居中对齐
- 步骤条模块的左右边缘必须与上层模块对齐

---

## 四、双栏/多栏布局

### 4.1 Two-Column Layout

```css
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: stretch;
}
.two-col > .card { height: 100%; }
```

**对齐要求**:
- 左右两个卡片**顶部严格对齐**（grid 默认顶部对齐）
- 底部视觉高度平衡（`align-items: stretch` + `.card-bd { flex: 1 }`）
- 左右模块间 gap 固定 `--space-16`
- 响应式：`≤1000px` 时降为单列

### 4.2 Three-Column Layout

```css
.three-col {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
```

---

## 五、常见对齐问题排查清单

在开发新页面或修复现有页面时，逐项检查以下内容：

### 全局级
- [ ] 页面所有模块的左右边缘是否对齐同一条基准线（`--space-24`）
- [ ] 是否使用 `--space-*` 变量，而不是硬编码 `px`
- [ ] 页面标题 `.page-hd` 与下方内容区是否左对齐
- [ ] 模块间的垂直间距是否统一为 `--space-16`

### 统计卡片
- [ ] 4 张卡片是否等宽等高
- [ ] 卡片间距是否均等
- [ ] 图标与文字是否垂直居中
- [ ] 卡片左右边缘是否与下方模块对齐

### 双栏区域
- [ ] 左右两个模块顶部是否对齐
- [ ] 两个模块的高度是否一致（stretch 生效）
- [ ] 中间间距是否为 `--space-16`

### 表单
- [ ] 多列表单的 label 是否在同一水平线上
- [ ] 所有输入框高度是否统一（`40px`）
- [ ] label 与输入框间距是否为 `--space-8`
- [ ] 底部按钮是否与输入框基线对齐

### 表格
- [ ] 表头列与内容列是否一一对齐
- [ ] 单元格内容是否垂直居中（`vertical-align: middle`）
- [ ] 表格左右边缘与卡片内边距是否协调（`pad-0` 时表格贴边，空状态有 24px padding）

### 步骤条
- [ ] 图标、文字、箭头是否统一垂直居中
- [ ] 步骤间距是否均等
- [ ] 步骤条与上下模块的边缘是否对齐

---

## 六、样式覆写规范

当某页面需要覆写全局样式时，遵循以下优先级：

1. **优先使用全局 CSS 变量**（`--space-*`、`--radius-*`、`--n-*`）
2. **次选组件的 scoped CSS**（`.page{...} .card-hd{...}` 在 `<style scoped>` 中）
3. **禁止使用** `!important`（特殊情况除外，须加注释说明）
4. **禁止使用** `style="..."` 内联样式控制布局边距（颜色/字体等视觉属性除外）

### 可接受的 inline style 场景

| 场景 | 示例 | 原因 |
|------|------|------|
| SvgIcon 颜色 | `style="color:var(--blue)"` | SvgIcon 组件不支持通过 CSS 变量传入颜色 |
| 动态数据样式 | `:style="{ color: status === 'ok' ? 'green' : 'red' }"` | 条件渲染的样式 |
| 尺寸覆盖 | `:size="22"` | 组件 prop，不是 CSS |

---

## 七、CSS 变量速查

### 间距
```
--space-4  →  4px
--space-8  →  8px
--space-12 → 12px
--space-16 → 16px
--space-20 → 20px
--space-24 → 24px
--space-32 → 32px
--space-40 → 40px
--space-48 → 48px
```

### 圆角
```
--radius-sm →  6px   （标签、小元素）
--radius-md →  8px   （输入框、按钮、卡片内部区块）
--radius-lg → 12px   （卡片、统计卡片）
--radius-xl → 16px   （大容器）
--radius-pill → 9999px（标签、胶囊）
```

### 阴影
```
--shadow-card        → 基础卡片阴影
--shadow-card-hover  → 卡片 hover 增强阴影
--shadow-dropdown    → 下拉菜单阴影
--shadow-input-focus → 输入框 focus 发光
```

---

## 附录：本规范涉及的 CSS 变量定义位置

所有 CSS 变量在 `App.vue` 的根样式块中定义：

```css
:root {
  /* 颜色、间距、圆角、阴影、字体 */
  ...
}
```

页面局部覆写时 `var(--variable-name)` 会自动向上查找根作用域，无需重复声明。

---

> **维护者**: 前端开发组  
> **更新日期**: 2024-06  
> **配套文件**: `/staff/src/App.vue`（全局样式）、`/staff/src/views/nurse/Checkin.vue`（本规范首批实践页面）
