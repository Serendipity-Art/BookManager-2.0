# BookManager-2.0

一个用**原生 Servlet + MyBatis + Thymeleaf** 写的图书管理系统，Java Web 的练习项目。
没上 Spring，也没上前后端分离，就是把「一个请求从浏览器打到数据库再回来」这条路亲手铺一遍。

代码是练手产物，能跑通，但不完美。下面的「已知问题」是如实记录的，不是客套。

---

## 功能现状

| 模块 | 路径 | 状态 |
|---|---|---|
| 登录 | `GET/POST /login` | ✅ 可用 |
| 登录拦截 | `MainFilter`（`/*`） | ✅ 未登录一律跳登录页 |
| 仪表盘 | `GET /dashboard` | ✅ 可用（统计数字是写死的假数据） |
| 图书列表 | `GET /books` | ✅ 服务端渲染 |
| 图书新增 | `GET/POST /books/new` | ✅ 可用 |
| 图书删除 | `POST /books/delete?id=` | ✅ 可用 |
| 借阅列表 | `GET /borrows` | ✅ 服务端渲染 |
| 借阅 JSON | `GET /borrows/list` | ✅ 返回 `{success,total,data}` |
| 借阅增改还删 | `POST /borrows/{add,update,return,delete}` | ✅ 可用 |

## 技术栈

- Servlet 6.1（`jakarta.servlet`）+ 注解式 `@WebServlet` / `@WebFilter`
- MyBatis 3.5.19，Mapper 接口 + 注解 SQL（没有 XML mapper、没有 Spring 托管）
- Thymeleaf 3.1.5，手写 `ClassLoaderTemplateResolver`，服务端渲染
- Jackson（`databind` + `jsr310`）输出 JSON，`LocalDate` 走 `@JsonFormat`
- Bootstrap 5 + 手写 CSS/JS，无 npm、无打包工具
- Maven（war 打包）+ JDK 17 + Logback

## 分层与包结构

```
servlet   → 表现层/控制层     接收参数、调 service、组织 HTML 或 JSON 响应
filter    → 表现层横切        登录拦截
service   → 业务逻辑层        业务规则、把书名/姓名翻译成主键、算借阅状态
mapper    → 数据访问层        SQL 与结果集映射
entity    → 实体层            被各层共享的数据契约，不属于任何一层
utils     → 基础设施          MybatisUtil / ThymeleafUtil 两个手写工厂
```

调用方向单向递减：`servlet → service → mapper`，反向一律不允许。

## 如何运行

1. 准备一个支持 **Servlet 6.1** 的容器（Tomcat 11），JDK 17+
2. 数据库建库建表（沿用 `admin` / `book` / `borrow` / `student` 四张表）
3. 复制凭据模板并填上自己的账号：

   ```bash
   copy src\main\resources\db.properties.example src\main\resources\db.properties
   ```

   > `db.properties` 已被 `.gitignore` 排除，不会进版本库。
   > 数据库地址和库名在 `mybatis-config.xml` 里 —— 因为 `.properties` 按平台默认字符集读取，
   > 中文库名放进去会变成 `????` 报 `Unknown database`。

4. `mvn clean package`，把 `target/BookManager-1.0-SNAPSHOT.war` 丢进容器
5. 访问 `/login`，用 `admin` 表里的账号登录

## 已知问题（没做完的部分）

- `/logout`、`/users`、`/settings` 三个入口只有前端链接，**没有对应 Servlet**，点了 404
- `books.html` 里的 `/books/edit?id=` 编辑页未实现
- 密码在库里是明文存储，也没有 HTTPS —— 练习项目，不要照搬到真实环境
- `UserServiceImpl` 里有一句没初始化的 `Log log;`，一旦进 catch 会 NPE
- 仪表盘的统计数字是硬编码的假数据，没接真实聚合查询
- 没有单元测试

## 关于这份代码是谁写的

老实说清楚：

- **前端**（所有 HTML / CSS / JS，含 Bootstrap 布局、fetch 调用、表单校验、弹窗交互）**全部由 AI 生成**
- **后端**：登录模块与图书模块，是和 DeepSeek 网页版一句一句对着报错调出来的；
  借阅模块（SQL、Service、Servlet）由终端 AI Agent 完整实现并对着真实数据库逐条验证
- **作者本人**负责的：提需求、跑起来、看报错、判断哪句是废话哪句能信、把零件拼成一个能跑的东西

这不是一份「独立手写」的作品集，是一次**借助 AI 把 Java Web 的链路完整走一遍**的学习记录。
把它放在这里，是因为它确实跑起来了，而且里面每一个坑都是真的踩过。
