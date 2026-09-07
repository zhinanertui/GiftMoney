# 礼单送还系统 — Spring Boot 3 + Vue 3 全栈项目

一个用于管理礼金往来的全栈系统：用户登录（RSA 非对称加密传输 + BCrypt 加盐存储）、礼单增删改查与 Excel/CSV 导入、按年/月/方向/事由的统计分析（饼图 + 树状图）。

## 目录结构

```
demo/
├── server/          # Spring Boot 3 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/demo/
│       │   ├── DemoApplication.java          # 启动类
│       │   ├── common/                       # Result、全局异常
│       │   ├── config/                       # 跨域、Sa-Token、RSA、BCrypt、分页插件、权限接口实现
│       │   ├── auth/                         # 认证（登录/注册/找回/公钥下发/RSA 工具）
│       │   ├── gift/                         # 礼单模块（CRUD + 导入 + 统计）
│       │   ├── system/                       # RBAC（角色/菜单/关联）
│       │   └── user/                         # 用户模块
│       └── resources/application.yml
└── web/             # Vue 3 前端
    ├── package.json
    ├── vite.config.js                        # 含 /api 代理
    └── src/
        ├── main.js / App.vue
        ├── router/                           # 路由 + 登录守卫 + 动态菜单
        ├── api/                              # axios 封装（带 token）+ 接口
        ├── store/permission.js               # 权限状态（菜单/按钮/角色）
        ├── directives/permission.js          # v-permission 按钮权限指令
        ├── components/MenuItem.vue           # 递归菜单组件
        ├── utils/auth.js、rsa.js
        └── views/
            ├── Login.vue / Register.vue / ForgotPassword.vue
            ├── Layout.vue                    # 主布局（动态菜单/用户/退出）
            ├── GiftList.vue                  # 礼单管理
            ├── Statistics.vue                # 统计（饼图 + 树状图）
            ├── UserList.vue                  # 用户管理（含角色分配）
            ├── RoleManage.vue                # 角色管理
            └── MenuManage.vue                # 菜单管理
```

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18+（本机 22）
- MySQL 8.0

## 数据库

- 地址：`127.0.0.1:3306`，账号 `root`，密码 `##Root1.`
- 库：`demo`，共 6 张表：`tb_user`（用户）、`tb_gift_record`（礼单）、`tb_role`（角色）、`tb_menu`（菜单）、`tb_user_role`、`tb_role_menu`
- **初始化脚本**：`sql/init.sql`（建库建表 + 角色/菜单/关联 + 示例数据，可重复执行重建）
- **开发进度**：`docs/开发进度.md`（功能清单、关键决策、踩坑记录、进度时间线）

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| zhangsan | 123456 | 管理员（admin） |
| lisi | 123456 | 租户（tenant） |

> 首次启动时，密码为空的用户会被自动设置为 BCrypt 加盐后的 `123456`。登录页可直接「注册账号」或「忘记密码」（用户名 + 注册邮箱匹配后重置）。

## 权限模型（RBAC）

- **角色**：预置 `admin`（管理员）、`tenant`（租户），支持自定义新增角色。
- **菜单/按钮**：菜单分「目录 / 菜单 / 按钮」三级，按钮对应权限标识（如 `gift:add`）。
- **权限隔离**：管理员可见全部菜单（含用户管理/角色管理/菜单管理）；租户仅见礼单管理、统计分析。
- **前端**：登录后按角色动态渲染菜单 + 动态注册路由；按钮用 `v-permission` 指令控制显隐。
- **后端**：接口用 `@SaCheckRole("admin")`（用户/角色/菜单管理）与 `@SaCheckPermission("gift:add")`（礼单操作）双重校验。

## 启动步骤

### 1. 启动后端（端口 8080）

```bash
cd demo/server
bash /d/iderProject/tools/mvnb.sh spring-boot:run
```

> 本机 Git Bash 下 `mvn` 脚本有 classpath 兼容问题，统一用 `bash /d/iderProject/tools/mvnb.sh <参数>` 替代 `mvn`。

### 2. 启动前端（端口 5173）

```bash
cd demo/web
npm install
npm run dev
```

浏览器访问 http://localhost:5173 ，用 `zhangsan / 123456` 登录。

## 安全设计

- **传输加密**：登录时前端用 RSA 公钥加密密码，后端私钥解密（公钥通过 `GET /api/auth/public-key` 下发）。
- **存储加密**：密码以 BCrypt 加盐哈希落库，不可逆。
- **会话**：Sa-Token 签发 token，除登录/公钥接口外均需登录（未登录返回 401）。

## 礼单导入

- 支持 `.xlsx` / `.xls` / `.csv`，表头：`姓名,金额,方向,日期,事由,关系,备注`
- 方向填 `送出`（或 `1`）／`收到`、`还礼`（或 `2`）；日期支持 `yyyy-MM-dd`、`yyyy/M/d`、`yyyy.MM.dd` 等
- 礼单页右上角可「下载模板」

## 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/auth/public-key | 获取 RSA 公钥 |
| POST | /api/auth/login | 登录（密码为密文） |
| POST | /api/auth/register | 注册（账号密码，邮箱选填） |
| POST | /api/auth/reset-password | 忘记密码（用户名+邮箱匹配后重置） |
| POST | /api/auth/logout | 登出 |
| GET | /api/auth/me | 当前用户（含角色） |
| GET | /api/auth/menus | 当前用户菜单树 + 按钮权限 + 角色 |
| GET | /api/gifts?current=&size=&keyword=&direction=&year= | 礼单分页 |
| POST | /api/gifts | 新增礼单（需 gift:add） |
| PUT | /api/gifts/{id} | 修改礼单（需 gift:edit） |
| DELETE | /api/gifts/{id} | 删除礼单（需 gift:delete） |
| POST | /api/gifts/import | 导入（需 gift:import） |
| GET | /api/gifts/template | 下载导入模板 |
| GET | /api/stats/overview | 总览（送/收/差额/笔数） |
| GET | /api/stats/direction | 方向汇总（饼图） |
| GET | /api/stats/occasion | 事由汇总（饼图） |
| GET | /api/stats/treemap | 树状图数据（年→月→方向） |
| GET | /api/users?current=&size=&keyword= | 用户分页（需 admin） |
| GET/PUT | /api/users/{id}/role | 查询/设置用户角色（需 admin） |
| GET | /api/roles | 角色列表（需 admin） |
| POST/PUT/DELETE | /api/roles/{id} | 角色增改删（需 admin） |
| GET/PUT | /api/roles/{id}/menus | 查询/分配角色菜单（需 admin） |
| GET | /api/menus/tree | 菜单树（需 admin） |
| POST/PUT/DELETE | /api/menus/{id} | 菜单增改删（需 admin） |

统一响应体：`{ "code": 200, "message": "success", "data": ... }`
