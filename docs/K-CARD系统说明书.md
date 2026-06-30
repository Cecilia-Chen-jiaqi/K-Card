# K-CARD KPOP 小卡交易平台 — 系统说明书

| 文档属性 | 内容 |
|----------|------|
| 文档名称 | K-CARD 系统说明书 |
| 文档版本 | 1.5.0 |
| 编制日期 | 2026-06-30 |
| 项目名称 | K-CARD KPOP 小卡交易平台 |
| 项目路径 | CodeGeeXProjects |
| 文档类型 | 软件设计说明书 / 用户操作手册 |
| 编制依据 | 参照 GB/T 8567-2006《计算机软件文档编制规范》 |

---

## 目录

**第1章 引言**

1.1 编写目的 · 1.2 项目背景 · 1.3 适用范围 · 1.4 术语与缩略语 · 1.5 参考资料 · 1.6 文档约定

**第2章 系统概述**

2.1 系统目标 · 2.2 系统定位 · 2.3 系统特点 · 2.4 用户角色

**第3章 系统总体设计**

3.1 总体架构 · 3.2 技术选型 · 3.3 功能结构 · 3.4 通信约定

**第4章 功能需求说明**

4.1～4.11 各功能模块（用户、商品、购物车、订单、地址、支付、专区、上传、元数据、收藏关注、管理后台）

**第5章 详细设计**

5.1 数据库设计 · 5.2 接口设计 · 5.3 核心业务流程 · 5.4 支付与退款设计

**第6章 界面与人机交互设计**

6.1 前台界面 · 6.2 管理后台界面 · 6.3 UI 规范

**第7章 安全设计**

7.1 认证与授权 · 7.2 业务权限 · 7.3 数据与支付安全

**第8章 部署与运行维护**

8.1 环境要求 · 8.2 安装部署 · 8.3 生产配置

**第9章 测试说明**

9.1 功能测试清单 · 9.2 自动化脚本 · 9.3 端到端路径

**第10章 用户操作手册**

10.1 普通用户 · 10.2 卖家 · 10.3 管理员

**第11章 项目目录结构**

**第12章 已知限制与扩展建议**

**附录** A～F · **文档修订记录**

---

## 第1章 引言

### 1.1 编写目的

本文档是 **K-CARD KPOP 小卡交易平台** 的正式系统说明书，旨在：

1. 为开发、测试、运维人员提供系统架构、模块设计与接口规范的技术依据；
2. 为项目验收、答辩与交付提供完整的功能说明与业务流程描述；
3. 为最终用户提供前台交易与管理后台的操作指引；
4. 作为后续功能扩展与版本迭代的基准文档。

预期读者：项目开发人员、测试人员、系统管理员、指导教师及平台用户。

### 1.2 项目背景

K-POP 粉丝群体对小卡（Photocard）的交易需求旺盛，但通用二手平台缺乏团体、成员、品相、换卡等垂直字段，且校园面交场景需要专门支持。K-CARD 针对上述痛点，构建集商品发布、审核、交易、支付、履约、统计于一体的 Web 应用系统。

### 1.3 适用范围

本文档适用于 K-CARD v1.5.0 及与之匹配的代码库，涵盖：

- 前台用户端（`http://localhost:3000`）
- 管理后台（`http://localhost:3000/admin`）
- 后端 API 服务（`http://localhost:8080`）
- MySQL 数据库 `kpop_trade`

不适用于已废弃的历史版本接口或未纳入本仓库的第三方服务内部实现。

### 1.4 术语与缩略语

| 术语/缩略语 | 说明 |
|-------------|------|
| K-CARD | 本系统产品名称 |
| 小卡 / Photocard | K-POP 专辑卡、特典卡、签售卡等收藏卡片 |
| GMV | 成交总额（Gross Merchandise Volume） |
| JWT | JSON Web Token，用于用户身份认证 |
| order_item | 订单明细行，合并订单的核心数据单元 |
| 合并订单 | 购物车多商品一次结算生成的单一订单 |
| 沙箱 | 支付宝开放平台测试环境 |
| ER | 实体关系（Entity-Relationship） |
| KPI | 关键绩效指标（Key Performance Indicator） |

### 1.5 参考资料

| 序号 | 资料名称 | 说明 |
|------|----------|------|
| 1 | GB/T 8567-2006 | 计算机软件文档编制规范 |
| 2 | sql/kpop_trade_schema.sql | 数据库 DDL 脚本 |
| 3 | backend/src/main/resources/application.yml | 后端配置文件 |
| 4 | 支付宝开放平台文档 | 网页支付、扫码支付、退款接口 |
| 5 | Vue 3 / Spring Boot 2.7 官方文档 | 框架技术参考 |

### 1.6 文档约定

- **标题层级**：章（第 N 章）→ 节（N.M）→ 条（N.M.K），不超过四级；
- **接口描述**：`方法 路径` 格式，权限标注为「公开」「需登录」「管理员」；
- **状态枚举**：以表格列出取值与含义，与代码常量保持一致；
- **版本管理**：每次重大功能变更须更新文档版本号及「文档修订记录」；
- **图表**：架构图、流程图以文本/ASCII 或 Mermaid 描述，Word 版由导出脚本排版。

---

## 第2章 系统概述

### 2.1 系统目标
构建安全、便捷、垂直于 K-POP 小卡场景的 C2C 交易平台，实现从发品、审核、浏览、下单、支付到发货、收货、退款的完整业务闭环，并为运营方提供数据统计与内容审核能力。

### 2.2 项目背景

| 维度 | 说明 |
|------|------|
| 目标用户 | K-POP 粉丝、校园二手交易者 |
| 核心品类 | 专辑卡、特典卡、签售卡、周边卡等 |
| 交易模式 | 出售、可交换、支持预留 |
| 交付方式 | 普通邮寄、校园同城面交 |
**K-CARD** 是一个面向 K-POP 粉丝群体的小卡（Photocard）二手交易平台。平台围绕「小卡」这一垂直品类，提供商品发布、浏览搜索、购物车、订单管理、支付宝在线支付、校园面交专区、换卡专区等完整交易能力，帮助粉丝在校园或线上安全、便捷地完成小卡买卖与交换。

### 2.3 系统定位

- **垂直品类设计**：团体、成员、卡种、品相、专辑时期等 K-POP 专属字段
- **双专区运营**：首页全站集市 + 校园面交专区 + 换卡专区
- **完整交易闭环**：发布 → 浏览 → 加购/直购 → 支付 → 发货 → 确认收货 → 退款
- **库存联动下架**：**支付成功**后扣减库存，库存为 0 时自动下架（下单/加购不扣库存）
- **合并订单支付**：购物车可多选商品，生成一个订单、一次支付宝支付
- **明细级履约**：合并单支持按明细发货、部分确认收货、部分退款
- **收藏与关注**：收藏心仪小卡、关注卖家，个人中心集中管理
- **商品审核机制**：新发布商品进入待审核，管理员通过后上架
- **管理后台**：数据统计（ECharts 动态图表）、商品审核、用户管理
- **防自购机制**：用户不能购买自己发布的商品
- **支付宝沙箱集成**：支持网页支付、扫码支付、异步回调、主动同步、退款
- **蓝白科技风 UI**：玻璃拟态、粒子背景、响应式布局

### 2.4 用户角色

| 角色 | 标识 | 主要能力 |
|------|------|----------|
| 普通用户 | role=0 | 买家/卖家：浏览、发布、交易、收藏、关注 |
| 系统管理员 | role=1 | 商品审核、用户管理、数据统计、账号管控 |

---

## 第3章 系统总体设计

### 3.1 总体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     浏览器 (用户)                            │
│                  http://localhost:3000                       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / Axios
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              前端 (Vue 3 + Vite + Element Plus)              │
│  路由守卫 · JWT 拦截 · 页面组件 · 主题样式 · 元数据常量       │
└──────────────────────────┬──────────────────────────────────┘
                           │ /api/* 、/upload/* (Vite 代理)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              后端 (Spring Boot 2.7 + MyBatis-Plus)           │
│  Controller → Service → Mapper → MySQL                      │
│  JWT 拦截器 · 支付宝 SDK · 文件上传 · 定时任务               │
└──────────────┬────────────────────────────┬─────────────────┘
               │                            │
               ▼                            ▼
┌──────────────────────────┐   ┌────────────────────────────┐
│   MySQL 5.7 (kpop_trade)  │   │  支付宝开放平台 (沙箱网关)   │
│   10+ 张业务表 + 流水表    │   │  page.pay / precreate /    │
└──────────────────────────┘   │  query / refund              │
                               └────────────────────────────┘
```

### 3.2 技术选型

#### 3.2.1 后端

| 技术 | 版本/说明 |
|------|-----------|
| Java | JDK 1.8 |
| Spring Boot | 2.7.15 |
| MyBatis-Plus | 3.5.3.1 |
| MySQL Connector | 5.1.49（兼容 MySQL 5.7） |
| 支付宝 Java SDK | 4.34.0.ALL |
| JJWT | HS256 签发 Token |
| BCrypt | 密码加密（Spring Security Crypto） |
| Fastjson | JSON 序列化 |
| Lombok | 简化实体类 |

#### 3.2.2 前端

| 技术 | 版本/说明 |
|------|-----------|
| Vue | 3.4 |
| Vue Router | 4.3 |
| Element Plus | 2.4 |
| Axios | 1.5 |
| ECharts | 6.x（管理后台动态图表） |
| Vite | 5.4 |

#### 3.2.3 数据库

- MySQL 5.7+
- 字符集：`utf8mb4`
- 数据库名：`kpop_trade`

### 3.3 功能结构

```
K-CARD
├── 前台用户端
│   ├── 账户（注册/登录/资料）
│   ├── 商品（浏览/搜索/发布/收藏）
│   ├── 交易（购物车/订单/支付/退款）
│   ├── 社交（关注卖家）
│   └── 专区（首页/校园/换卡）
└── 管理后台
    ├── 数据统计（KPI + ECharts）
    ├── 商品审核
    └── 用户管理
```

### 3.4 通信约定

**统一响应格式**（`R<T>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": { }
}
```

- `code = 0`：成功
- `code = 401`：未登录或 Token 失效
- `code = 500`：业务错误或系统异常

**认证方式**：请求头携带 `Authorization: Bearer <token>`

---

## 第4章 功能需求说明

### 4.1 用户模块

| 功能 | 说明 |
|------|------|
| 注册 | 用户名、密码（≥6 位）、手机号、校园、可选头像 |
| 登录 | 用户名 + 密码，返回 JWT 与用户信息 |
| 编辑资料 | 个人中心修改昵称、校园、简介、头像（`POST /api/auth/profile`） |
| 个人中心 | 买家/卖家订单、我的发布、我的收藏、关注卖家、收货地址、编辑资料 |
| 退出登录 | 清除本地 Token 与用户信息 |

**用户角色**（`role`）：

| role | 含义 |
|------|------|
| 0 | 普通用户（买家/卖家） |
| 1 | 系统管理员 |

**账号状态**（`account_status`）：

| 值 | 含义 |
|----|------|
| 1 | 正常 |
| 0 | 禁用（无法登录） |

管理员可在后台禁用普通用户；不能禁用自己或其他管理员。

### 4.2 商品模块

| 功能 | 说明 |
|------|------|
| 发布商品 | 填写标题、价格、库存、团体、成员、卡种、品相、交易模式等 |
| 商品列表 | 首页展示所有上架且有库存的商品 |
| 高级搜索 | 关键词、团体、成员、卡种、交易类型、品相、价格区间 |
| 商品详情 | 封面、瑕疵图、捆卡说明、换卡说明、预留截止、卖家信息 |
| 我的发布 | 查看自己发布的全部商品；展示待审核/已拒绝/上架/下架状态 |
| 手动下架 | 卖家在「我的发布」主动下架（`POST /api/goods/delist/{id}`） |
| 重新上架 | 已下架且库存 > 0 时提交重新审核（`POST /api/goods/relist/{id}` → status=2 待审核） |
| 自动下架 | 支付成功后库存扣减至 0 时 `status` 自动变为 0（下架） |
| 自动上架 | 退款恢复库存后，若 `stock>0` 且 `status=0` 则恢复 `status→1`（已审核通过的商品） |

**商品状态**（`status`）：

| status | 含义 | 前台可见 |
|--------|------|----------|
| 0 | 下架（卖家主动或售罄） | 否 |
| 1 | 上架（审核通过，可购买） | 是 |
| 2 | 待审核（新发布或重新上架） | 否 |
| 3 | 已拒绝（含 `reject_reason`） | 否 |

**审核相关字段**：`reject_reason`（拒绝原因）、`reviewed_at`（审核时间）、`reviewed_by`（审核管理员 ID）。

**发布与审核流程**：

```
卖家发布 → status=2（待审核）
  → 管理员通过 → status=1，出现在首页/专区
  → 管理员拒绝 → status=3，卖家可在「我的发布」查看原因
卖家重新上架 → 再次进入 status=2，需重新审核
```

> 历史数据中 `status=1` 的已上架商品不受影响；仅**新发布**与**重新上架**走审核流程。

**交付模式**（`delivery_mode`）：

| 值 | 含义 |
|----|------|
| 1 | 普通邮寄 |
| 2 | 校园同城面交 |

**交易类型**（`trade_type`）：

- 仅出售
- 可交换
- 支持预留

### 4.3 购物车模块

| 功能 | 说明 |
|------|------|
| 加入购物车 | 同一商品重复加购会合并数量 |
| 购物车列表 | 展示商品缩略图、单价、数量、小计 |
| 调整数量 | 购物车页可加减数量，受商品库存上限约束 |
| 批量结算 | 勾选 **多件** 商品，合并为一个订单、一次支付（`POST /api/cart/checkout-batch`） |
| 单件结算 | `POST /api/cart/checkout/{cartId}` 仍可用，等价于批量接口传 1 个 cartId |
| 删除商品 | 从购物车移除（仅能操作本人购物车项） |

### 4.4 订单模块

| 功能 | 说明 |
|------|------|
| 创建订单 | 购物车批量/单件结算或直接购买；邮寄商品可自动绑定默认收货地址 |
| 合并订单 | 一单可含多条 `order_item` 明细；`orders.amount` 为明细小计之和 |
| 订单查询 | 买家/卖家分别查看自己的订单 |
| 订单详情 | 返回 `items[]` 明细列表；卖家视角额外返回 `sellerItems[]` |
| 取消订单 | 仅「待支付」状态可取消（未支付订单不涉及库存变动） |
| 发货 | 卖家对自己名下的明细发货；**全部明细发货后**订单变为「已发货」 |
| 物流查询 | 买家/卖家在订单列表与详情页查看物流（多明细分条展示） |
| 确认收货 | 买家在「已支付」或「已发货」状态下操作 |
| 超时关闭 | 每日 0 点定时任务关闭超过 3 天未支付的订单 |

**订单状态机**：

```
                    ┌──────────────┐
                    │  0 待支付     │
                    └──────┬───────┘
           取消/超时关闭    │  支付成功
                    ┌──────▼───────┐
                    │  1 已支付     │
                    │  (待发货)     │
                    └──────┬───────┘
              退款完成      │  卖家发货
         ┌──────────────────┼──────────────────┐
         │                  ▼                  │
         │           ┌──────────────┐          │
         │           │  2 已发货     │          │
         │           └──────┬───────┘          │
         │                  │  买家确认收货      │
         │                  ▼                  │
         │           ┌──────────────┐          │
         │           │  3 交易完成   │          │
         │           └──────────────┘          │
         ▼                                       │
  ┌──────────────┐                               │
  │  5 已退款     │◄──────────────────────────────┘
  └──────────────┘

  ┌──────────────┐
  │  4 交易关闭   │  ← 取消或超时
  └──────────────┘
```

| status | 含义 |
|--------|------|
| 0 | 待支付 |
| 1 | 已支付（待发货） |
| 2 | 已发货 |
| 3 | 交易完成 |
| 4 | 交易关闭 |
| 5 | 已退款 |

### 4.5 地址与收货模块

| 功能 | 说明 |
|------|------|
| 地址管理 | 个人中心「收货地址」Tab：新增、编辑、删除、设为默认 |
| 结算选址 | 待支付订单在结算页选择/绑定收货地址 |
| 自动绑址 | 创建订单或购物车结算时，邮寄商品自动绑定用户默认地址 |
| 面交免址 | `delivery_mode = 2`（校园面交）无需填写邮寄地址 |

### 4.6 支付模块

| 功能 | 说明 |
|------|------|
| 网页支付 | 生成支付宝收银台 URL，浏览器跳转 |
| 扫码支付 | 生成二维码，手机支付宝沙箱 App 扫码 |
| 异步回调 | 支付宝服务器 POST 通知，RSA2 验签后更新订单 |
| 主动同步 | 查询支付宝交易状态，本地标记已支付 |
| 退款 | 买家可 **整单退款** 或 **按明细部分退款**（支付宝部分退款）；对应明细回补库存，全部明细退完则订单状态为已退款 |

### 4.7 专区模块

| 专区 | 路由 | 筛选条件 |
|------|------|----------|
| 首页集市 | `/` | 全部上架且有库存商品 |
| 校园面交 | `/campus` | `delivery_mode = 2` |
| 换卡专区 | `/exchange` | `trade_type != 仅出售` |

### 4.8 文件上传模块

| 功能 | 说明 |
|------|------|
| 图片上传 | 支持 jpg/jpeg/png/webp，最大 5MB |
| 存储方式 | UUID 文件名，存于 `backend/upload/` |
| 访问路径 | `http://localhost:8080/upload/<文件名>` |
| 使用场景 | 注册头像、商品封面、瑕疵图 |

### 4.9 元数据模块

提供 K-POP 领域常量：团体列表、成员映射、卡种、品相、交易类型、快递公司。前端优先从 `/api/meta/kpop`、`/api/meta/express` 加载，失败时使用本地 `kpopMeta.js` / `expressMeta.js` 兜底。

支持的团体（示例）：BTS、SEVENTEEN、NCT、Stray Kids、aespa、NewJeans、IVE、LE SSERAFIM、TWICE、BLACKPINK、EXO、ENHYPEN 等。

### 4.10 收藏与关注

| 功能 | 说明 |
|------|------|
| 收藏商品 | 登录用户可收藏上架中的小卡；**首页商品卡片**与详情页均可操作 |
| 关注卖家 | 商品详情页可关注发布者；个人中心「关注卖家」查看已关注列表 |
| 限制 | 不能收藏/购买自己的商品，不能关注自己 |

### 4.11 管理后台

管理后台采用独立布局 `LayoutAdmin`，与前台 `LayoutMain` 分离，路由前缀 `/admin`，需 `role=1` 且已登录。

| 功能 | 路由 | 说明 |
|------|------|------|
| 数据统计 | `/admin/dashboard` | 运营 KPI、ECharts 动态图表、60 秒自动刷新 |
| 商品审核 | `/admin/goods` | 待审核/全部/已上架/已拒绝筛选，通过或拒绝 |
| 用户管理 | `/admin/users` | 用户列表、设管理员、启用/禁用账号 |

**数据统计面板**（`GET /api/admin/stats/overview`）：

| 指标 | 说明 |
|------|------|
| userCount / todayNewUsers | 注册用户总数 / 今日新增 |
| goodsPending / goodsOnSale / goodsRejected | 待审核 / 在售 / 已拒绝 |
| orderCount / paidOrderCount / totalGmv | 订单总数 / 已支付 / 累计成交额 |
| ordersByStatus / goodsByStatus | 订单与商品状态分布（饼图数据源） |
| trend7d | 近 7 日每日新增用户、订单、商品及成交额（折线/柱状图） |

**图表组件**：`AdminEChart.vue`（基于 ECharts 6），含近 7 日增长趋势折线图、成交额柱状图、状态分布环形饼图（统一蓝色系）。

**商品审核**：

- 通过：`POST /api/admin/goods/audit` `{ goodsId, approved: true }` → status=1
- 拒绝：`{ goodsId, approved: false, reason }` → status=3，写入 `reject_reason`

**用户管理规则**：

- 不能取消自己的管理员权限
- 不能禁用自己或其他管理员
- 至少保留一名管理员

**入口**：管理员登录后，顶栏用户菜单 → **管理后台**；或直接访问 `/admin`。

**默认账号**：首次启动由 `DatabaseInitializer` 自动创建 `admin` / `admin123`（请尽快修改密码）。

---

## 第5章 详细设计

### 5.1 数据库设计

数据库脚本：`sql/kpop_trade_schema.sql`

### 5.1.1 ER 关系概览

```
user ──┬── goods (seller_id)
       ├── cart (user_id)
       ├── goods_favorite (user_id)
       ├── seller_follow (user_id / seller_id)
       ├── orders (buyer_id / seller_id)
       ├── payment_log (buyer_id)
       └── address (user_id)

goods ──┬── kpop_goods_detail (goods_id, 1:1)
        ├── cart (goods_id)
        ├── goods_favorite (goods_id)
        ├── orders (goods_id, 单商品兼容)
        └── order_item (goods_id)

orders ──┬── order_item (order_id, 1:N 明细)
         ├── payment_log (order_no)
         ├── refund_log (order_no)
         └── address (address_id, 邮寄订单)
```

### 5.1.2 数据表说明

#### user — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(64) | 登录名，唯一 |
| password | VARCHAR(128) | BCrypt 加密密码 |
| nickname | VARCHAR(64) | 昵称（可选） |
| phone | VARCHAR(32) | 手机号，唯一 |
| campus | VARCHAR(128) | 所在校园 |
| avatar | VARCHAR(255) | 头像 URL |
| intro | VARCHAR(255) | 个人简介 |
| role | TINYINT | 0=普通用户 1=管理员 |
| account_status | TINYINT | 1=正常 0=禁用 |

#### goods — 商品表

| 字段 | 类型 | 说明 |
|------|------|------|
| seller_id | BIGINT | 卖家 ID |
| title | VARCHAR(128) | 标题 |
| price | DECIMAL(10,2) | 价格 |
| stock | INT | 库存 |
| group_name | VARCHAR(64) | 团体 |
| idol_name | VARCHAR(64) | 成员 |
| card_type | VARCHAR(64) | 卡种 |
| album_era | VARCHAR(128) | 专辑/时期 |
| quality | VARCHAR(16) | 品相：无暇/微瑕/重瑕 |
| trade_type | VARCHAR(32) | 交易模式 |
| reserve_support | TINYINT | 是否支持预留 |
| delivery_mode | TINYINT | 1=邮寄 2=面交 |
| cover_image | VARCHAR(255) | 封面图 |
| defect_images | VARCHAR(512) | 瑕疵图（逗号分隔） |
| status | TINYINT | 0=下架 1=上架 2=待审核 3=拒绝 |
| reject_reason | VARCHAR(255) | 审核拒绝原因 |
| reviewed_at | DATETIME | 审核时间 |
| reviewed_by | BIGINT | 审核管理员 ID |

#### kpop_goods_detail — KPOP 商品扩展表

| 字段 | 说明 |
|------|------|
| card_bundle | 捆卡说明 |
| exchange_info | 换卡说明 |
| reserve_deadline | 预留截止时间 |
| extra_info | 补充信息 |

#### cart — 购物车表

| 字段 | 说明 |
|------|------|
| user_id + goods_id | 联合唯一，防重复 |
| quantity | 数量 |
| selected | 是否选中 |

#### orders — 订单主表

| 字段 | 说明 |
|------|------|
| order_no | 订单号，格式 `ORD` + 时间戳 |
| buyer_id | 买家 |
| seller_id | 卖家（单卖家兼容；多卖家合并单可为空） |
| goods_id | 商品（单商品兼容；多商品合并单可为空） |
| quantity | 总数量（明细 quantity 之和） |
| amount | 订单应付总额 |
| item_count | 明细行数 |
| pay_type | 1=支付宝 |
| status | 订单状态 0~5（由明细聚合） |
| is_reserved | 是否含预留商品 |
| address_id | 收货地址（邮寄订单） |
| express_company / tracking_no 等 | 订单级物流摘要（多明细时取首条已发） |

#### order_item — 订单明细表

| 字段 | 说明 |
|------|------|
| order_id / order_no | 所属订单 |
| goods_id / seller_id | 商品与卖家 |
| quantity | 数量 |
| unit_price / amount | 单价与小计 |
| status | 明细状态 0待支付 1已支付 2已发货 3已完成 5已退款 |
| express_company / tracking_no / logistics_note / shipped_at | 明细级物流 |

> 历史单商品订单在启动时由 `DatabaseInitializer` 自动迁移至 `order_item`，保证兼容。

#### payment_log — 支付流水表

记录支付宝交易号、金额、回调原文，用于幂等判断。

#### refund_log — 退款流水表

记录退款单号、交易号、金额、状态。

#### goods_favorite — 商品收藏表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 收藏用户 |
| goods_id | BIGINT | 商品 ID |
| created_at | DATETIME | 收藏时间 |

唯一约束：`(user_id, goods_id)`

#### seller_follow — 卖家关注表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 关注者 |
| seller_id | BIGINT | 被关注卖家 |
| created_at | DATETIME | 关注时间 |

唯一约束：`(user_id, seller_id)`

#### address — 地址表

用户收货地址 CRUD，结算时绑定至订单 `address_id`。

---

### 5.2 接口设计

基础地址：`http://localhost:8080`

#### 5.2.1 认证接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/auth/register` | 公开 | 注册 |
| POST | `/api/auth/login` | 公开 | 登录，返回 token + user |
| GET | `/api/auth/me` | 需登录 | 当前用户信息 |
| POST | `/api/auth/profile` | 需登录 | 更新昵称/校园/简介/头像 |

#### 5.2.2 商品接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/goods/list` | 公开 | 全部上架商品 |
| GET | `/api/goods/search` | 公开 | 多条件搜索 |
| GET | `/api/goods/campus` | 公开 | 校园面交商品 |
| GET | `/api/goods/exchange` | 公开 | 换卡专区商品 |
| GET | `/api/goods/detail?id=` | 公开 | 商品详情（含 KPOP 扩展 + 卖家；登录时含 `favorited`、`followingSeller`） |
| GET | `/api/goods/{id}` | 公开 | 基础商品信息 |
| GET | `/api/goods/my` | 需登录 | 我的发布 |
| POST | `/api/goods/create` | 需登录 | 发布商品（status=2 待审核） |
| POST | `/api/goods/delist/{id}` | 需登录（卖家） | 下架商品 |
| POST | `/api/goods/relist/{id}` | 需登录（卖家） | 重新提交审核（status=2） |

**搜索参数**：`keyword`, `groupName`, `idolName`, `cardType`, `tradeType`, `quality`, `minPrice`, `maxPrice`

#### 5.2.3 购物车接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/cart/add` | 需登录 | 加入购物车 |
| GET | `/api/cart/list` | 需登录 | 购物车列表 |
| POST | `/api/cart/checkout/{cartId}` | 需登录 | 单件结算（内部走批量接口） |
| POST | `/api/cart/checkout-batch` | 需登录 | 批量结算，body: `{ "cartIds": [1,2,3] }` |
| POST | `/api/cart/update` | 需登录 | 更新数量/选中 |
| DELETE | `/api/cart/remove/{id}` | 需登录 | 删除购物车项 |

#### 5.2.4 订单接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/orders/create` | 需登录 | 直接购买 |
| GET | `/api/orders/{orderNo}` | 需登录（买家/卖家） | 订单详情（含 `items`、`sellerItems`） |
| GET | `/api/orders/buyer/list` | 需登录 | 买家订单列表 |
| GET | `/api/orders/seller/list` | 需登录 | 卖家订单列表 |
| POST | `/api/orders/ship` | 需登录（卖家） | 发货（body: orderNo, expressCompany, trackingNo, logisticsNote） |
| POST | `/api/orders/bind-address` | 需登录（买家） | 待支付订单绑定收货地址 |
| POST | `/api/orders/complete` | 需登录（买家） | 确认收货；body: `{ orderNo, orderItemIds? }`，不传 `orderItemIds` 则确认全部明细 |
| POST | `/api/orders/cancel` | 需登录（买家） | 取消待支付订单 |

#### 5.2.5 地址接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/address/list` | 需登录 | 我的收货地址列表 |
| GET | `/api/address/default` | 需登录 | 默认收货地址 |
| GET | `/api/address/{id}` | 需登录 | 地址详情 |
| POST | `/api/address/create` | 需登录 | 新增地址 |
| POST | `/api/address/update` | 需登录 | 更新地址 |
| DELETE | `/api/address/{id}` | 需登录 | 删除地址 |
| POST | `/api/address/set-default/{id}` | 需登录 | 设为默认地址 |

#### 5.2.6 支付接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/pay/submit` | 需登录（买家） | 获取网页支付 URL |
| GET | `/api/pay/qrcode?orderNo=` | 需登录（买家） | 获取扫码支付二维码 |
| POST | `/api/pay/notify` | 公开（支付宝回调） | 异步支付通知 |
| GET | `/api/pay/return` | 公开 | 支付同步返回页（HTML） |
| GET | `/api/pay/sync-status?orderNo=` | 公开 | 主动查询并同步支付状态 |
| POST | `/api/pay/refund` | 需登录（买家） | 发起退款；body: `{ orderNo, reason?, orderItemIds? }`，支持明细级部分退款 |
| GET | `/api/pay/success` | 公开 | 静态成功提示 |

#### 5.2.7 其他接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/upload/image` | 公开 | 上传图片 |
| GET | `/api/meta/kpop` | 公开 | KPOP 元数据 |
| GET | `/api/meta/express` | 公开 | 快递公司列表 |
| GET | `/upload/**` | 公开 | 静态图片访问 |

#### 5.2.8 收藏与关注接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/favorite/add` | 需登录 | 收藏商品，body: `{ goodsId }` |
| DELETE | `/api/favorite/remove/{goodsId}` | 需登录 | 取消收藏 |
| GET | `/api/favorite/list` | 需登录 | 我的收藏列表 |
| GET | `/api/favorite/check/{goodsId}` | 需登录 | 是否已收藏 |
| POST | `/api/follow/add` | 需登录 | 关注卖家，body: `{ sellerId }` |
| DELETE | `/api/follow/remove/{sellerId}` | 需登录 | 取消关注 |
| GET | `/api/follow/list` | 需登录 | 我的关注列表 |
| GET | `/api/follow/check/{sellerId}` | 需登录 | 是否已关注 |

#### 5.2.9 管理后台接口（需管理员 role=1）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats/overview` | 数据统计概览（含 `trend7d`） |
| GET | `/api/admin/goods/pending` | 待审核商品列表 |
| GET | `/api/admin/goods` | 商品列表（可按 status、keyword 筛选） |
| POST | `/api/admin/goods/audit` | 审核 `{ goodsId, approved, reason? }` |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/role` | 更新角色 `{ role: 0|1 }` |
| PUT | `/api/admin/users/{id}/status` | 更新账号状态 `{ accountStatus: 0|1 }` |

**统计概览响应示例**（`data` 字段摘要）：

```json
{
  "userCount": 86,
  "goodsPending": 0,
  "goodsOnSale": 16,
  "orderCount": 73,
  "totalGmv": 266.05,
  "todayNewUsers": 2,
  "todayNewOrders": 0,
  "ordersByStatus": { "0": 64, "1": 5, "3": 2 },
  "goodsByStatus": { "0": 3, "1": 16 },
  "trend7d": [
    { "date": "06-24", "newUsers": 0, "newOrders": 0, "newGoods": 0, "gmv": 0 },
    { "date": "06-30", "newUsers": 2, "newOrders": 0, "newGoods": 0, "gmv": 0 }
  ]
}
```

`403` 表示当前用户非管理员。

---

### 5.3 核心业务流程

#### 5.3.1 用户注册与登录

```
用户填写注册信息 → POST /api/auth/register
  → 校验用户名/手机号唯一性
  → BCrypt 加密密码
  → 写入 user 表
  → 返回成功

用户登录 → POST /api/auth/login
  → 校验用户名密码
  → 签发 JWT（24 小时有效，claim: userId）
  → 返回 { token, user }
  → 前端存入 localStorage
```

#### 5.3.2 商品发布与审核

```
卖家进入发布页 → 填写商品信息 + 上传封面
  → POST /api/goods/create
  → 写入 goods 表（status=2 待审核）
  → 写入 kpop_goods_detail 扩展表
  → 卖家在「我的发布」看到「待审核」

管理员进入 /admin/goods → 审核通过/拒绝
  → POST /api/admin/goods/audit
  → 通过：status=1，商品出现在首页/对应专区
  → 拒绝：status=3，写入 reject_reason，卖家可查看原因

卖家重新上架（已下架且 stock>0）
  → POST /api/goods/relist/{id} → status=2，再次进入审核队列
```

#### 5.3.3 购买流程（购物车 · 支持批量合并）

```
浏览商品 → 加入购物车 POST /api/cart/add
  → 进入购物车页勾选一件或多件，可调整数量 POST /api/cart/update
  → 点击结算 POST /api/cart/checkout-batch { cartIds: [...] }
    → 批量校验：自购、上架、库存
    → 写入 orders（amount=明细合计）+ 多条 order_item
    → 邮寄商品自动绑定默认收货地址（若有）
    → 移除对应购物车项
  → 跳转结算页 /checkout/{orderNo}（展示多商品列表）
  → 选择/确认收货地址 → 一次支付（支付宝整单金额）
  → 支付成功：逐条明细扣库存，售罄则自动下架
```

单件结算：`POST /api/cart/checkout/{cartId}`，逻辑同上，仅 1 条明细。

#### 5.3.4 购买流程（立即购买）

```
商品详情页 → 点击「立即购买」
  → POST /api/orders/create
  → POST /api/orders/create → 写入 1 条 order_item
  → 同上校验 + 创建订单（不扣库存）+ 自动绑默认地址
  → 跳转结算页 → 支付成功后扣库存
```

#### 5.3.5 卖家履约

```
买家支付成功（status=1，各明细 status=1）
  → 各卖家在个人中心「卖家订单」对自己名下明细点击发货
  → POST /api/orders/ship（仅更新该卖家 order_item 的物流）
  → 当 **全部明细** 均已发货 → 订单 status=2
  → 买家在订单详情查看分条物流；可按明细确认收货 POST /api/orders/complete { orderNo, orderItemIds? }
  → 全部有效明细完成后 → 订单 status=3
```

多卖家合并单：买家一次支付，各卖家分别发货；卖家列表通过 `order_item.seller_id` 关联展示。

#### 5.3.6 库存与下架联动

| 事件 | 库存变化 | 商品状态 |
|------|----------|----------|
| 创建订单 / 加购 | 无 | 无 |
| 支付成功 | 各明细 stock -= quantity | 各商品 stock≤0 时 status→0 |
| 退款成功 | 对应明细 stock += quantity | 各商品若 stock>0 且 status=0 则恢复 status→1 |
| 取消待支付订单 | 无 | 无 |
| 超时关闭订单 | 无 | 无 |
| 卖家手动下架 | 无 | status→0 |
| 卖家重新上架 | 无 | status→2（待审核，需管理员再次通过） |

系统启动时 `DatabaseInitializer` 会修复历史数据中「库存为 0 但仍上架」「已支付但未扣库存/下架」等异常记录。

---

### 5.4 支付与退款设计

#### 5.4.1 支付宝沙箱配置

配置文件：`backend/src/main/resources/application.yml`

```yaml
alipay:
  gateway: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  app-id: <你的沙箱 APPID>
  private-key: <应用私钥>
  public-key: <支付宝公钥>
  notify-url: http://localhost:8080/api/pay/notify
  return-url: http://localhost:3000/pay-success
  omit-notify-in-sandbox: true      # 沙箱不传 notify_url，避免网关预探测失败
  omit-return-url-in-sandbox: true  # 沙箱不传 return_url，避免手机支付跳转异常
```

#### 5.4.2 支付流程（推荐：扫码支付）

```
1. 用户创建订单（status=0）
2. 进入结算页 /checkout/{orderNo}
3. 选择「手机扫码（推荐）」标签页
4. 前端 GET /api/pay/qrcode?orderNo=xxx
   → 后端优先调用 alipay.trade.precreate
   → 失败时降级为 page.pay URL 生成二维码
5. 使用「支付宝沙箱版」App（Android）登录买家账号
6. 扫描结算页上的二维码（注意：不要扫收银台页面内的二维码）
7. 在手机上完成支付
8. 回到电脑端点击「我已支付，同步状态」
   → GET /api/pay/sync-status?orderNo=xxx
   → 查询支付宝 trade_status
   → 若为 TRADE_SUCCESS / TRADE_FINISHED：
      → 写入 payment_log（幂等）
      → 订单 status→1
      → 扣减库存（stock -= quantity）
      → 售罄商品自动下架（status→0）
```

#### 5.4.3 支付流程（网页支付）

```
1. 结算页选择「电脑网页支付」
2. POST /api/pay/submit { orderNo }
3. 返回支付宝收银台 URL，浏览器跳转
4. 沙箱买家账号登录并完成支付
5. 支付完成后手动点击同步，或访问 /pay-success 页自动同步
```

#### 5.4.4 异步回调（生产环境）

```
支付宝 POST /api/pay/notify
  → RSA2 验签
  → 检查 trade_status
  → 幂等：payment_log 中 orderNo+tradeNo 不重复处理
  → markPaid + 扣减库存 + 售罄下架 → 返回纯文本 "success"
```

沙箱开发模式下因 `omit-notify-in-sandbox: true`，主要依赖 **sync-status** 主动同步。

#### 5.4.5 退款流程

```
买家在个人中心 / 订单详情 → 已支付/已发货订单 → 申请退款（整单或指定明细）
  → POST /api/pay/refund { orderNo, reason?, orderItemIds? }
  → 校验：仅买家本人、状态为 1 或 2（部分明细已退时订单仍可操作）
  → 计算退款金额（整单为剩余可退金额，部分为所选明细金额之和）
  → 查询支付宝交易状态
  → 调用 alipay.trade.refund（支持部分退款）
  → 写入 refund_log
  → 标记对应 order_item 为已退款，回补库存
  → 全部明细退完 → 订单 status=5；否则保持原状态并同步
```

#### 5.4.6 沙箱测试账号

在 [支付宝开放平台沙箱](https://open.alipay.com/develop/sandbox/account) 获取：

- **买家账号**：用于付款（如 `fjagrj6958@sandbox.com` / `111111`）
- **卖家账号**：用于收款

---

## 第6章 界面与人机交互设计

### 6.1 前台路由与页面

| 路由 | 页面 | 需登录 | 功能 |
|------|------|--------|------|
| `/` | 首页 | 否 | 商品集市 + 筛选 |
| `/goods/detail?id=` | 商品详情 | 否 | 查看详情、购买、收藏 |
| `/campus` | 校园专区 | 否 | 面交商品 |
| `/exchange` | 换卡专区 | 否 | 可交换商品 |
| `/publish` | 发布商品 | 是 | 卖家发品 |
| `/cart` | 购物车 | 是 | 加购管理 |
| `/checkout/:orderNo` | 结算支付 | 是 | 订单确认 + 支付 |
| `/pay-success` | 支付结果 | 否 | 支付后落地页 |
| `/user` | 个人中心 | 是 | 订单、发布、收藏、关注、地址 |
| `/login` | 登录 | 否 | 用户登录 |
| `/register` | 注册 | 否 | 用户注册 |
| `/admin/dashboard` | 数据统计 | 是（管理员） | ECharts 运营面板 |
| `/admin/goods` | 商品审核 | 是（管理员） | 待审核/通过/拒绝 |
| `/admin/users` | 用户管理 | 是（管理员） | 角色与账号状态 |

前台路由挂载于 `LayoutMain`（`/`），管理后台挂载于 `LayoutAdmin`（`/admin`）。

### 6.2 全局布局（LayoutMain）

- **顶栏**：Logo、导航（首页/校园/换卡/发布）、购物车角标、用户头像下拉
- **移动端**：宽度 ≤900px 时导航折叠为汉堡菜单
- **用户菜单**：个人中心、我的发布、购物车、**管理后台**（仅管理员可见）、退出
- **首页卡片**：商品列表支持快捷收藏（需登录）
- **背景**：TechBackground 粒子动画

### 6.3 管理后台布局（LayoutAdmin）

- **侧栏**：数据统计、商品审核、用户管理；底部「返回前台」
- **顶栏**：页面标题、在线状态、管理员用户名
- **图表**：ECharts 折线图、柱状图、环形饼图；60 秒自动刷新

### 6.4 个人中心 Tab

| Tab | 功能 |
|-----|------|
| 买家订单 | 合并单展开明细；整单/按明细确认收货、退款 |
| 卖家订单 | 对自己名下明细发货 |
| 我的发布 | 待审核/已拒绝/上架/下架状态展示 |
| 我的收藏 | 已收藏商品列表 |
| 关注卖家 | 已关注卖家列表 |
| 收货地址 | 地址 CRUD、默认地址 |

### 6.5 UI 设计规范

主题文件：`frontend/src/styles/theme.css`

- 主色：`#4A90E2`（清凉蓝）
- 风格：蓝白科技风、玻璃拟态卡片
- 组件：Element Plus 二次定制

### 6.6 前端状态存储

| 键名 | 内容 |
|------|------|
| `localStorage.authToken` | JWT |
| `localStorage.currentUser` | 用户信息 JSON |

---

## 第7章 安全设计

### 7.1 JWT 认证

- 拦截器：`JwtAuthInterceptor`，拦截 `/api/**`
- Token 放在 `Authorization: Bearer <token>`
- 有效期：24 小时
- 401 时前端自动清除登录态并跳转登录页

### 7.2 公开接口白名单

无需 Token 即可访问：登录、注册、商品浏览类 GET、支付回调/同步、图片上传、元数据、`/upload/**`。

### 7.3 业务级权限

| 操作 | 校验 |
|------|------|
| 查看订单详情 | 当前用户必须是订单买家或卖家 |
| 发货 | 当前用户必须是订单中至少一条明细的卖家 |
| 确认收货 / 取消 / 退款 | 当前用户必须是订单买家 |
| 购买 | 买家不能是商品卖家 |
| 管理后台 | `/api/admin/**` 需 JWT + `role=1` |

### 7.4 密码与支付安全

- 密码：BCrypt 单向哈希存储
- 支付：RSA2 验签、流水幂等、退款前查询支付宝交易状态

### 7.5 管理员权限约束

- 不能取消自己的管理员权限或禁用自己
- 不能禁用其他管理员；至少保留一名管理员

---

## 第8章 部署与运行维护

### 8.1 环境要求

| 组件 | 版本 |
|------|------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| MySQL | 5.7+ |
| Node.js | 18+ |
| npm | 随 Node 安装 |

### 8.2 数据库初始化

```bash
# 1. 启动 MySQL
# 2. 执行建表脚本
mysql -u root -p < sql/kpop_trade_schema.sql
```

### 8.3 后端启动

```bash
cd backend

# 修改 application.yml 中的数据库账号密码、支付宝密钥

mvn clean package
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

### 8.4 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:3000`

Vite 开发代理（`vite.config.js`）：

- `/api` → `http://localhost:8080`
- `/upload` → `http://localhost:8080`

### 8.5 生产构建

```bash
# 前端
cd frontend
npm run build
# 产物在 frontend/dist/

# 后端
cd backend
mvn clean package
# 产物在 backend/target/kpop-trade-backend-1.0.0.jar

java -jar target/kpop-trade-backend-1.0.0.jar
```

生产部署时需：

1. 将 `alipay.gateway` 换为正式网关
2. 配置公网 HTTPS 的 `notify-url` 和 `return-url`
3. 关闭 `omit-notify-in-sandbox` 等沙箱选项
4. 配置 Nginx 反向代理前端静态资源与 `/api`

### 8.6 ngrok（可选）

```bash
ngrok http 8080
# 将 HTTPS 地址填入 application.yml 的 notify-url
```

注意：ngrok 免费版有中间页，可能导致手机支付跳转失败，沙箱环境推荐用 sync-status 同步。

---

## 第9章 测试说明

### 9.1 功能测试清单

| 序号 | 测试项 | 预期结果 |
|------|--------|----------|
| 1 | 注册新用户 | 成功，用户名/手机号不可重复 |
| 2 | 登录 | 返回 Token，跳转首页 |
| 3 | 发布商品 | 首页可见，字段完整 |
| 4 | 搜索筛选 | 按团体/成员/价格等过滤正确 |
| 5 | 加入购物车 | 角标数量更新 |
| 6 | 购物车调整数量 | 数量变更成功，不超过库存 |
| 7 | 购物车单件结算 | 生成 1 条明细的待支付订单 |
| 8 | 购物车批量结算 | 勾选多件 → 一个订单号、amount 为合计 |
| 9 | 立即购买 | 同上 |
| 10 | 购买自己商品 | 被拒绝，提示不能自购 |
| 11 | 支付宝扫码支付 | 同步后订单变为已支付，**各明细库存扣减** |
| 12 | 支付后商品售罄 | 对应商品自动下架 |
| 13 | 卖家发货 | 多卖家时各发各的；全部发完订单变为已发货 |
| 14 | 买家确认收货 | 订单状态变为交易完成 |
| 15 | 取消待支付订单 | 订单关闭，库存不变 |
| 16 | 申请退款 | 整单退款，各明细库存恢复 |
| 17 | 编辑资料 | 昵称/校园/头像更新 |
| 18 | 卖家下架/上架 | 我的发布中状态正确切换 |
| 19 | 校园专区 | 仅显示面交商品 |
| 20 | 换卡专区 | 仅显示非「仅出售」商品 |
| 21 | 收藏商品 | 首页/详情页收藏，个人中心可查看 |
| 22 | 关注卖家 | 详情页关注，个人中心可查看 |
| 23 | 发布商品审核 | 新发布为待审核，管理员通过后首页可见 |
| 24 | 拒绝后重新提交 | 重新上架进入待审核队列 |
| 25 | 合并单部分退款 | 指定 orderItemIds，对应明细库存恢复 |
| 26 | 合并单部分确认收货 | 指定 orderItemIds，全部完成后订单完成 |
| 27 | 管理后台统计 | `/admin/dashboard` 图表与 KPI 正常加载 |
| 28 | 管理员审核商品 | 通过/拒绝，卖家侧状态同步 |
| 29 | 管理员禁用用户 | 被禁用账号无法登录 |

### 9.2 自动化测试脚本

项目 `scripts/` 目录提供辅助脚本：

| 脚本 | 用途 |
|------|------|
| `test_stock_before_pay.py` | 验证下单/加购不扣库存、支付后才扣 |
| `test_all.py` | 综合 API 测试 |
| `test_delist.py` | 库存下架逻辑 |
| `test_qrcode.py` | 扫码支付二维码 |
| `test_purchase_refund.py` | 购买退款流程 |

### 9.3 推荐测试路径（端到端）

```
注册 → 登录 → 发布商品 → 另一账号登录
→ 浏览 → 加购多件 → 批量结算 → 扫码支付 → 同步状态（验证各明细库存扣减）
→ 各卖家分别发货 → 买家确认收货 → 整单退款
→ 验证库存恢复、商品重新上架
→ 管理员登录 → 审核待审商品 → 查看统计图表
→ 收藏/关注 → 个人中心验证
```

---

---

## 第10章 用户操作手册

### 10.1 普通用户（买家）

| 步骤 | 操作说明 |
|------|----------|
| 1. 注册登录 | 访问 `/register` 填写用户名、密码、手机号；登录后浏览首页 |
| 2. 浏览搜索 | 首页可按团体、成员、价格等筛选；校园/换卡专区入口在顶栏 |
| 3. 收藏关注 | 首页或详情页点击收藏；详情页可关注卖家 |
| 4. 加入购物车 | 详情页选数量后加入购物车，购物车页可批量勾选结算 |
| 5. 支付 | 结算页选择收货地址（邮寄）、扫码或网页支付，支付后点击「同步状态」 |
| 6. 订单管理 | 个人中心「买家订单」查看物流、确认收货、申请退款 |

### 10.2 卖家

| 步骤 | 操作说明 |
|------|----------|
| 1. 发布商品 | `/publish` 填写小卡信息、上传封面，提交后等待审核 |
| 2. 查看审核 | 「我的发布」查看待审核/已拒绝状态及拒绝原因 |
| 3. 重新上架 | 已下架商品在库存 > 0 时可重新提交审核 |
| 4. 发货 | 「卖家订单」对买家已支付明细填写快递公司与单号 |
| 5. 下架 | 「我的发布」可主动下架在售商品 |

### 10.3 系统管理员

| 步骤 | 操作说明 |
|------|----------|
| 1. 登录 | 使用管理员账号（默认 `admin` / `admin123`） |
| 2. 进入后台 | 用户菜单 → 管理后台，或访问 `/admin` |
| 3. 数据统计 | `/admin/dashboard` 查看 KPI 与趋势图表 |
| 4. 商品审核 | `/admin/goods` 筛选待审核商品，通过或填写拒绝原因 |
| 5. 用户管理 | `/admin/users` 设置管理员、启用/禁用普通用户 |

---

## 第11章 项目目录结构

```
CodeGeeXProjects/
├── sql/
│   └── kpop_trade_schema.sql      # 数据库 DDL
├── backend/
│   └── src/main/java/com/kpoptrade/
│       ├── config/                  # 配置类（Web、JWT、上传、数据库初始化）
│       ├── controller/              # REST 控制器（Auth/Goods/Cart/Orders/Pay/Admin 等 11 个）
│       ├── service/                 # 业务接口与实现
│       ├── entity/                  # 数据库实体（Orders、OrderItem 等）
│       ├── dto/                     # 数据传输对象（含 AdminStatsDto、DailyTrendPoint）
│       ├── mapper/                  # MyBatis-Plus Mapper
│       ├── interceptor/             # JWT 拦截器
│       ├── util/                    # 工具类（JWT、支付宝、AdminAuth、响应封装）
│       ├── constant/                # 常量（GoodsStatus、UserRole、AccountStatus 等）
│       └── scheduler/               # 定时任务（订单超时）
│   └── src/main/resources/
│       └── application.yml          # 应用配置
│   └── upload/                      # 上传图片存储目录
├── frontend/
│   └── src/
│       ├── components/              # 公共组件（LayoutMain、LayoutAdmin、AdminEChart 等）
│       ├── views/                   # 页面视图（前台 + admin/ 子目录）
│       ├── router/                  # 路由配置（前台/后台嵌套布局 + 守卫）
│       ├── utils/                   # auth.js（isAdmin、getCurrentUser）
│       ├── constants/               # KPOP / 快递元数据（kpopMeta.js、expressMeta.js）
│       ├── styles/                  # 全局主题
│       ├── directives/              # 自定义指令
│       └── main.js                  # 入口（Axios 拦截器）
├── scripts/                         # 测试脚本
├── alipay-sandbox-demo/             # 支付宝沙箱独立 Demo
├── docs/
│   └── K-CARD系统说明书.md           # 本文档
└── README.md                        # 快速启动说明
```

---

## 第12章 已知限制与扩展建议

### 12.1 当前限制

| 项目 | 说明 |
|------|------|
| 商品审核 | 新发布/重新上架需人工审核，无自动机审 |
| 部分确认收货 | 合并单支持按明细确认，全部有效明细完成后订单完成 |
| 部分退款 | 合并单支持按明细退款，可多次操作直至全部退完 |
| 立即购买 | 仍为单商品单订单（未与购物车合并入口） |
| 图片上传 | 接口无登录校验，生产环境需加鉴权 |
| 同步支付 | `/api/pay/sync-status` 为公开接口 |
| 移动端 | 已有响应式布局与汉堡菜单，非完整 PWA/App |
| 聊天/议价 | 未实现 IM 功能 |
| 消息通知 | 无站内信/邮件/短信；审核结果需卖家主动查看 |
| 统计实时性 | 管理后台图表 60 秒轮询刷新，非 WebSocket 推送 |

### 12.2 扩展建议

1. **评价系统**：交易完成后买卖双方互评
2. **实名/校园认证**：提升校园面交信任度
3. **审核通知**：商品审核结果站内信/邮件推送
4. **生产支付**：切换正式支付宝商户号，启用完整异步回调
5. **移动端增强**：PWA 离线能力或独立 App
6. **数据导出**：管理后台报表 CSV/Excel 导出

---

## 文档修订记录

| 版本 | 日期 | 主要变更 |
|------|------|----------|
| **1.5.0** | 2026-06-30 | 按 GB/T 8567 规范重构文档结构；管理后台 ECharts 图表；导出 Word 版 |
| **1.4.0** | 2026-06-28 | 管理后台（商品审核、用户管理、数据统计）；商品四态审核流；默认 admin 账号 |
| **1.3.0** | 2026-06-28 | 收藏商品、关注卖家；首页快捷收藏 |
| **1.2.1** | 2026-06-28 | 合并订单部分退款、部分确认收货 |
| **1.2.0** | 2026-06-27 | 购物车批量结算、order_item 明细、多卖家合并单 |
| **1.1.0** | 2026-06-26 | 收货地址、物流发货、编辑资料 |
| **1.0.0** | 2026-06-25 | 初版：商品/购物车/订单/支付宝沙箱/双专区 |

---

## 附录 A：订单号规则

```
ORD + System.currentTimeMillis()
示例：ORD1782606949899
```

## 附录 B：卡种枚举

专辑随机卡、特典卡、签售卡、打歌卡、周边卡、会员礼、演唱会卡、快闪卡、代言卡、其他

## 附录 C：品相枚举

无暇、微瑕、重瑕

## 附录 D：关键配置项速查

| 配置项 | 位置 | 默认值 |
|--------|------|--------|
| 后端端口 | application.yml | 8080 |
| 前端端口 | vite.config.js | 3000 |
| 数据库名 | application.yml | kpop_trade |
| JWT 有效期 | JwtUtil.java | 24 小时 |
| 图片最大大小 | application.yml | 5MB |
| 未支付超时 | OrderTimeoutScheduler | 3 天 |
| 上传目录 | application.yml | ./upload/ |

## 附录 E：商品状态速查

| status | 常量 | 含义 |
|--------|------|------|
| 0 | `GoodsStatus.OFF` | 下架 |
| 1 | `GoodsStatus.ON` | 上架（可购买） |
| 2 | `GoodsStatus.PENDING` | 待审核 |
| 3 | `GoodsStatus.REJECTED` | 已拒绝 |

## 附录 F：用户角色与账号状态

| 字段 | 值 | 含义 |
|------|-----|------|
| role | 0 | 普通用户 |
| role | 1 | 管理员 |
| account_status | 1 | 正常 |
| account_status | 0 | 禁用 |

---

*本文档基于 K-CARD v1.5.0 编写，参照 GB/T 8567-2006 组织章节。Markdown 源文件：`docs/K-CARD系统说明书.md`；Word 版：`docs/K-CARD_System_Manual_v1.5.0.docx`。*
