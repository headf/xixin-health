# UTS 与 TypeScript 类型差异对照手册

> **适用**: uni-app x (UTS)
> **维护日期**: 2026-06-24
> **错误码参考**: UTS110111163, UTS110111120

---

## 目录

1. [核心差异](#1-核心差异)
2. [逻辑运算符限制（`||` / `&&`）](#2-逻辑运算符限制--)
3. [类型断言限制](#3-类型断言限制)
4. [字符串默认值处理](#4-字符串默认值处理)
5. [变量声明与初始化](#5-变量声明与初始化)
6. [函数参数默认值](#6-函数参数默认值)
7. [Record 工具类型](#7-record-工具类型)
8. [泛型与 interface](#8-泛型与-interface)
9. [条件表达式要求](#9-条件表达式要求)
10. [附录：常用错误码速查](#10-附录常用错误码速查)

---

## 1. 核心差异

| 特性 | TypeScript | UTS | 原因 |
|------|-----------|-----|------|
| `\|\|` 操作数类型 | 任意类型（隐式转 boolean） | **仅 boolean** | UTS 编译到 Kotlin/Swift，`\|\|` 是纯布尔逻辑运算符 |
| `&&` 操作数类型 | 任意类型 | **仅 boolean** | 同上 |
| 类型断言（`as`） | 支持泛型断言 | **不支持泛型断言** | UTS 类型擦除限制 |
| `Record<K,V>` | 内置工具类型 | **不支持** | UTS 无该工具类型 |
| 变量未初始化 | 默认为 `undefined` | **必须初始化** | 无 `undefined` 概念 |
| 函数表达式默认参数 | ✅ 支持 | ❌ 不支持 | 仅函数声明支持 |
| 模板字面量 | ✅ | ✅ | 一致 |
| 泛型 | 完整支持 | **部分支持** | 类型擦除限制 |

---

## 2. 逻辑运算符限制（`||` / `&&`）

### 2.1 `||` 仅支持 boolean 类型

**错误码**: UTS110111120（条件语句必须使用 boolean 类型）

```ts
// TypeScript ✅
const name = userInput || '默认值'
const count = 0 || 42

// UTS ❌ 编译错误
const name = userInput || '默认值'
// 报错: Conditional statements must use boolean types (UTS110111120)

// UTS ✅ 正确写法
const name = userInput != null && userInput.length > 0 ? userInput : '默认值'
```

### 2.2 `||` 做字符串默认值

**错误码**: UTS110111163

```ts
// UTS ❌ UTS110111163
const msg = apiRes.message || '请求失败'

// UTS ✅ 三元表达式
const msg = apiRes.message != null && apiRes.message.length > 0 ? apiRes.message : '请求失败'
```

### 2.3 `&&` 安全访问

```ts
// TypeScript ✅
const result = obj && obj.name

// UTS ❌
const result = obj && obj.name

// UTS ✅
const result = obj != null ? obj.name : null
```

### 2.4 规则速查

| 写法 | TS | UTS |
|------|----|-----|
| `a \|\| b` (a,b 非 boolean) | ✅ | ❌ |
| `a \|\| b` (a,b 为 boolean) | ✅ | ✅ |
| `a && b` (a,b 非 boolean) | ✅ | ❌ |
| `a && b` (a,b 为 boolean) | ✅ | ✅ |
| `a ? b : c` | ✅ | ✅ |
| `a ?? b` | ✅ | ⚠️ 视版本支持 |

---

## 3. 类型断言限制

### 3.1 不支持直接断言为泛型类型

```ts
// UTS ❌ 不能将类型 unknown 分配给类型 ApiResponse<T>
const apiRes = res.data as ApiResponse<T>

// UTS ✅ 先转 any，再手动构造对象
const raw = res.data as any
const apiRes: ApiResponse<T> = {
    code: raw.code as string,
    message: raw.message as string,
    data: raw.data as T,
}
```

### 3.2 `unknown` → 泛型 的双重断言

```ts
// UTS ❌ 即使双重断言也可能失败
const apiRes = res.data as any as ApiResponse<T>

// UTS ✅ 显式构造对象
const raw = res.data as any
const apiRes: ApiResponse<T> = {
    code: raw.code as string,
    message: raw.message as string,
    data: raw.data as T,
}
```

---

## 4. 字符串默认值处理

### 4.1 函数内默认值

```ts
// UTS ✅ 推荐方式
function getMessage(msg: string): string {
    return msg != null && msg.length > 0 ? msg : '默认值'
}

// UTS ✅ 或使用空值判断
function getMessage(msg: string | null): string {
    if (msg == null || msg.length == 0) {
        return '默认值'
    }
    return msg
}
```

### 4.2 字符串拼接

```ts
// TypeScript ✅
let s = 'count: ' + 42

// UTS ❌ 不同类型不能相加
let s = 'count: ' + 42

// UTS ✅
let s = 'count: ' + 42.toString()
let s = `count: ${42}`  // 模板字面量 ✅
```

---

## 5. 变量声明与初始化

### 5.1 必须初始化

```ts
// TypeScript ✅
let name: string
let name2: string | undefined

// UTS ❌
let name: string          // 编译错误：变量必须初始化
let name2: string | null  // 编译错误：必须初始化

// UTS ✅
let name: string = ''
let name2: string | null = null
```

### 5.2 data() 中的类型推导

```ts
// UTS data() 中：
data() {
    return {
        s1: 'hello',         // 根据字面量推导为 string ✅
        s2: ``,              // 模板字面量，推导为 string ✅
        n1: 0,               // 推导为 number ✅
        n2: null as string | null,  // 显式声明联合类型 ✅
    }
}
```

---

## 6. 函数参数默认值

```ts
// UTS ✅ 函数声明方式支持
function greet(name: string = 'World'): string {
    return 'Hello ' + name
}

// UTS ❌ 函数表达式方式不支持
const greet = (name: string = 'World'): string => {
    return 'Hello ' + name
}

// UTS ✅ 函数表达式需要手动处理
const greet = (name: string): string => {
    const finalName = name != null && name.length > 0 ? name : 'World'
    return 'Hello ' + finalName
}
```

---

## 7. Record 工具类型

```ts
// TypeScript ✅
interface Options {
    header?: Record<string, string>
}

// UTS ❌ Record 不存在
interface Options {
    header?: Record<string, string>
}

// UTS ✅ 使用 any
interface Options {
    header?: any
}

// 或定义自定义接口
interface StringMap {
    [key: string]: string  // ⚠️ 索引签名视 UTS 版本支持
}
```

---

## 8. 泛型与 interface

### 8.1 泛型 interface 定义

```ts
// UTS ✅ 泛型 interface 可以定义
export interface ApiResponse<T = any> {
    code: string
    message: string
    data: T
}
```

### 8.2 泛型函数

```ts
// UTS ✅ 泛型函数可以定义
async function request<T = any>(
    method: string,
    url: string,
    data?: any,
): Promise<ApiResponse<T>> {
    // ...
}
```

### 8.3 限制：泛型参数不能用于类型断言

```ts
// UTS ❌
const res = raw as ApiResponse<T>  // 编译错误

// UTS ✅
const rawData = raw as any
const res: ApiResponse<T> = {
    code: rawData.code as string,
    message: rawData.message as string,
    data: rawData.data as T,
}
```

---

## 9. 条件表达式要求

### 9.1 if 条件必须是 boolean

```ts
// TypeScript ✅
if (someString) { ... }       // 隐式转 boolean
if (someNumber) { ... }       // 隐式转 boolean

// UTS ❌
if (someString) { ... }       // 编译错误
if (someNumber) { ... }       // 编译错误

// UTS ✅
if (someString != null && someString.length > 0) { ... }
if (someNumber != 0) { ... }
```

### 9.2 三元表达式结果类型必须一致

```ts
// UTS ✅
const result = condition ? 'yes' : 'no'  // 两侧都是 string

// UTS ❌ 类型不一致
const result = condition ? 'yes' : 123   // string vs number
```

---

## 10. 附录：常用错误码速查

| 错误码 | 含义 | 常见原因 |
|--------|------|----------|
| UTS110111120 | 条件语句必须使用 boolean 类型 | `if(str)` 或 `str \|\| 'default'` |
| UTS110111163 | ~~逻辑或运算~~ | `string \|\| string` 写法 |
| UTS110111163 | 类型断言失败 | `data as GenericType<T>` |
| UTS110111xxx | 类型不匹配 | 隐式类型转换被拒绝 |
| — | 找不到名称 Record | 使用了 `Record<K,V>` |
| — | 变量必须初始化 | 声明变量未赋值 |

### 通用解决模板

```ts
// 字符串默认值
const safe = raw != null && raw.length > 0 ? raw : 'default'

// 数字默认值
const count = num != null ? num : 0

// 对象类型转换
const obj = raw as any
const typed: TargetType = {
    field1: obj.field1 as string,
    field2: obj.field2 as number,
}

// boolean 条件判断
if (str != null && str.length > 0) { }

// 安全访问
const val = obj != null ? obj.field : null
```

---

> **参考链接**:
> - [uni-app x 开发注意](https://doc.dcloud.net.cn/uni-app-x/tutorial/codegap.html)
> - [UTS 与 TS 差异](https://doc.dcloud.net.cn/uni-app-x/uts/uts_diff_ts.html)
> - [UTS 数据类型](https://doc.dcloud.net.cn/uni-app-x/uts/data-type.html)
