# K-CARD KPOP小卡交易平台

> 版本 v1.5.0 · [系统说明书](docs/K-CARD系统说明书.md)

## 目录
- `sql/kpop_trade_schema.sql`：MySQL 5.7 完整建表 SQL
- `backend/`：Spring Boot 后端源码
- `frontend/`：Vue3 + Vite + Element Plus 前端源码
- `docs/`：系统说明书（Markdown / Word）
- `scripts/`：测试与文档导出脚本

## 主要功能
- 小卡发布、搜索、购物车、合并订单、支付宝沙箱支付与退款
- 收藏商品、关注卖家、收货地址与物流
- 商品审核、管理后台（数据统计 ECharts、用户管理）
- 校园面交专区、换卡专区

## 环境要求
- JDK 1.8
- Maven
- MySQL 5.7
- Node.js 18+ / npm
- ngrok（用于异步回调公网地址）

## 数据库部署
1. 启动 MySQL 5.7
2. 执行 `sql/kpop_trade_schema.sql`
3. 确认数据库名为 `kpop_trade`

## 后端启动
1. 编辑 `backend/src/main/resources/application.yml`
   - 修改 `spring.datasource.username` 和 `password`
   - 填写 `alipay.app-id`
   - 填写 `alipay.private-key` 与 `alipay.public-key`
   - 将 `alipay.notify-url` 替换为 ngrok HTTPS 地址，例如 `https://xxxxxxx.ngrok.io/api/pay/notify`
2. 运行：
   ```bash
   cd backend
   mvn clean package
   mvn spring-boot:run
   ```

## 前端启动
1. 进入 `frontend`
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
4. 打开 `http://localhost:3000`

## 管理后台
- 默认管理员：首次启动自动创建 `admin` / `admin123`（请尽快修改密码）
- 访问：`http://localhost:3000/admin`

## ngrok 使用
1. 在本地安装 ngrok
2. 启动命令：
   ```bash
   ngrok http 8080
   ```
3. 拷贝 HTTPS 地址，填入 `backend/src/main/resources/application.yml` 的 `alipay.notify-url`

## 测试流程
1. 在首页发布 KPOP 商品，填写团体、爱豆、品相、交易模式、价格、库存等信息
2. 进入商品详情，加入购物车
3. 进入购物车，创建订单并选择支付宝支付
4. 支付跳转到支付宝沙箱页面，登录买家账号：`fjagrj6958@sandbox.com` / `111111`
5. 支付成功后，页面返回 `http://localhost:8080/pay/success`
6. 后端异步回调 `notify-url` 收到 `TRADE_SUCCESS` 交易状态，更新订单状态并写入 `payment_log`
7. 在个人中心执行退款请求，后端调用支付宝退款接口，写入 `refund_log`
8. 每日定时任务自动关闭 3 天未支付订单，状态变为 `4`

## 核心完成点
- 支付宝网页支付表单生成
- RSA2 验签
- 异步回调幂等性判断
- 订单状态机：0=待支付 1=已支付待发货 2=已发货 3=交易完成 4=交易关闭 5=退款完成
- 支付流水 `payment_log`
- 退款流水 `refund_log`
- 校园同城商品专区
- 换卡专区
- 预留订单超时自动关闭

## 注意
- 同步返回地址 `return_url` 仅用于展示，支付成功确认必须依赖异步回调
- 若部署到生产，务必使用真实支付配置替换沙箱参数
