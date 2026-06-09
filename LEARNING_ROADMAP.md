# 校园社区平台 — 技术栈学习路线

> 面向只有 Java 基础、尚未掌握 Spring Boot / SQL / 中间件的初学者。配合 `BACKEND_GUIDE.md` 和项目源码阅读效果最佳。

---

## 目录

1. [技术栈速览](#1-技术栈速览)
2. [学习路线图](#2-学习路线图)
3. [第1阶段：SQL 基础](#3-第1阶段sql-基础-1-2-周)
4. [第2阶段：Maven 基础](#4-第2阶段maven-基础穿插-1-2-天)
5. [第3阶段：Spring Boot 核心](#5-第3阶段spring-boot-核心-2-3-周)
6. [第4阶段：MyBatis-Plus 数据库操作](#6-第4阶段mybatis-plus-数据库操作-1-2-周)
7. [第5阶段：Spring Security + JWT 安全体系](#7-第5阶段spring-security--jwt-安全体系-2-3-周)
8. [第6阶段：Redis 缓存](#8-第6阶段redis-缓存-3-5-天)
9. [第7阶段：周边工具收尾](#9-第7阶段周边工具收尾-穿插学习)
10. [推荐执行计划](#10-推荐执行计划)
11. [不需要学的内容](#11-不需要学的内容)
12. [如何阅读项目代码](#12-如何阅读项目代码)

---

## 1. 技术栈速览

| 类别 | 技术 | 版本 | 一句话作用 |
|------|------|------|-----------|
| 语言 | Java | 17+ | LTS 版本，支持 Record、Text Block 等现代语法 |
| 框架 | Spring Boot | 3.3.5 | 让 Java 程序变成 Web 服务，内嵌 Tomcat |
| 构建 | Maven | 3.9 | 依赖管理和项目构建工具 |
| ORM | MyBatis-Plus | 3.5.8 | 让 Java 代码操作数据库，提供分页、自动填充 |
| 数据库 | MySQL | 8.0 | 关系型数据库，存用户、帖子、评论等数据 |
| 安全 | Spring Security 6 | — | 认证授权框架，过滤器链机制 |
| JWT | JJWT | 0.12.6 | 生成和验证"电子身份证"（无状态认证） |
| 缓存 | Redis | 7.x | 内存数据库，存 Refresh Token 和黑名单 |
| API 文档 | Knife4j | 4.5.0 | Swagger 增强版，在线 API 文档和测试页面 |
| 工具 | Lombok | 1.18.34 | 注解自动生成 getter/setter/Builder |
| 工具 | Hutool | 5.8.32 | 国产 Java 工具集，本项目主要用 UUID 生成 |
| 连接池 | Lettuce | — | Redis 客户端，Spring Boot 默认包含 |

### 这些技术的关系

```
浏览器 / 前端
      │  HTTP 请求
      ▼
┌──────────────────────────────────────┐
│  Spring Boot (Tomcat 内嵌服务器)      │
│  ┌────────────────────────────────┐  │
│  │ Controller 层 (接收请求)        │  │
│  │   @RestController              │  │
│  └──────────┬─────────────────────┘  │
│             ▼                        │
│  ┌────────────────────────────────┐  │
│  │ Service 层 (业务逻辑)           │  │
│  └──┬──────────────────┬──────────┘  │
│     ▼                  ▼             │
│  ┌──────────────┐ ┌──────────────┐   │
│  │ MyBatis-Plus │ │ Spring       │   │
│  │ (操作数据库)  │ │ Security+JWT │   │
│  └──────┬───────┘ │ (认证授权)    │   │
│         │         └──────┬───────┘   │
└─────────┼────────────────┼───────────┘
          ▼                ▼
     ┌─────────┐    ┌──────────┐
     │  MySQL  │    │  Redis   │
     │ (数据)  │    │ (缓存)   │
     └─────────┘    └──────────┘
```

---

## 2. 学习路线图

```
  SQL 基础 ──► Maven 基础 ──► Spring Boot 核心
                                          │
                                          ▼
                              MyBatis-Plus 数据库操作
                                          │
                                          ▼
                          Spring Security + JWT 安全体系
                                          │
                                          ▼
                                  Redis 缓存
                                          │
                                          ▼
                              周边工具收尾 (Lombok/Knife4j/异常处理)
```

每个阶段学完后，**立即回来看项目对应代码**，理论对照实践。

---

## 3. 第1阶段：SQL 基础（1-2 周）

### 为什么先学 SQL

数据库是项目的根基。不学会 SQL，你看不懂数据表长什么样，也无法理解 Mapper 层在操作什么。

### 必学知识点

| 知识点 | 说明 | 重要度 |
|--------|------|--------|
| `CREATE TABLE` | 建表，定义字段和类型 | ⭐⭐ |
| `INSERT` | 插入数据 | ⭐⭐ |
| `SELECT` | 查询数据 | ⭐⭐⭐ |
| `UPDATE` | 更新数据 | ⭐⭐ |
| `DELETE` | 删除数据 | ⭐⭐ |
| `WHERE` | 条件筛选 | ⭐⭐⭐ |
| `ORDER BY` | 排序 | ⭐⭐ |
| `LIMIT` / `OFFSET` | 分页 | ⭐⭐⭐ |
| `JOIN` (INNER / LEFT) | 联表查询 | ⭐⭐⭐ |
| `INDEX` / `UNIQUE KEY` | 索引是什么，为什么要建 | ⭐⭐ |
| `FULLTEXT INDEX` | 全文搜索索引 | ⭐（了解即可） |

### 学到什么程度可以继续

能自己写出这样的 SQL，并理解每行的含义：

```sql
SELECT p.id, p.title, u.nickname AS author
FROM post p
JOIN user u ON p.user_id = u.id
WHERE p.status = 1
ORDER BY p.created_at DESC
LIMIT 10;
```

### 关联项目代码

| 文件 | 说明 |
|------|------|
| `sql/schema.sql` | 项目全部 4 张表的建表语句，学完 SQL 基础后第一份要读的代码 |
| `resources/mapper/PostMapper.xml` | 包含动态 SQL、全文检索、分页排序等实际查询 |

### 数据库 4 张表

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `user` | 用户 | username, password(BCrypt加密), role(USER/ADMIN), status |
| `post` | 帖子 | title, content(Markdown), user_id, category_id, status(0草稿/1发布/2软删除) |
| `comment` | 评论 | content, post_id, user_id, parent_id(子评论), reply_to_user_id |
| `category` | 分类 | name, description |

---

## 4. 第2阶段：Maven 基础（穿插，1-2 天）

### Maven 是什么

Maven 是 Java 项目的**构建工具**，功能类似于前端的 npm：
- 管理依赖（项目用了哪些第三方库，自动下载）
- 编译打包（把 `.java` 文件编译成 `.class`，打包成 `.jar`）
- 运行测试

### 必学知识点

| 知识点 | 说明 |
|--------|------|
| `pom.xml` | 项目对象模型，Maven 的核心配置文件 |
| `<dependency>` | 声明一个第三方依赖 |
| `<parent>` | 继承父项目的配置 |
| `mvn clean install` | 清理 + 编译 + 测试 + 打包 |
| `mvn spring-boot:run` | 启动 Spring Boot 应用 |
| 国内镜像 | 阿里云 Maven 镜像，加速依赖下载 |

### 关联项目代码

| 文件 | 说明 |
|------|------|
| `pom.xml` | 项目根目录，声明了 Spring Boot、MyBatis-Plus、JJWT、Lombok 等所有依赖 |

### Maven 镜像配置

编辑 `~/.m2/settings.xml`，添加阿里云镜像加速下载：

```xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <name>Aliyun Maven Mirror</name>
        <mirrorOf>central</mirrorOf>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 5. 第3阶段：Spring Boot 核心（2-3 周）

### Spring Boot 是什么

传统的 Java 程序执行完 `main` 方法就结束了。Spring Boot 应用会**一直运行**，在 `8080` 端口上监听 HTTP 请求，当有请求进来时分发到对应的 Controller 方法去处理，然后返回 JSON 数据。

一句话：**Spring Boot 让你用 Java 写 Web API**。

### 必学知识点

| 知识点 | 说明 | 重要度 |
|--------|------|--------|
| `@SpringBootApplication` | 启动类注解 | ⭐⭐ |
| `@RestController` | 标记一个类是 REST API 控制器 | ⭐⭐⭐ |
| `@RequestMapping("/api/xxx")` | 指定 URL 前缀 | ⭐⭐⭐ |
| `@GetMapping` | 处理 GET 请求（查询） | ⭐⭐⭐ |
| `@PostMapping` | 处理 POST 请求（新增） | ⭐⭐⭐ |
| `@PutMapping` | 处理 PUT 请求（修改） | ⭐⭐ |
| `@DeleteMapping` | 处理 DELETE 请求（删除） | ⭐⭐ |
| `@RequestBody` | 把请求体 JSON → Java 对象 | ⭐⭐⭐ |
| `@PathVariable` | 从 URL 路径提取参数 | ⭐⭐⭐ |
| `@RequestParam` | 从 URL 查询参数提取 | ⭐⭐ |
| `@Service` | 标记业务逻辑类 | ⭐⭐⭐ |
| `@Component` | 通用的"交给Spring管理"注解 | ⭐⭐ |
| `@Configuration` / `@Bean` | 配置类，手动创建 Bean | ⭐⭐ |
| `application.yml` | 项目的配置文件 | ⭐⭐⭐ |
| `@Valid` | 参数校验 | ⭐⭐ |

### 关键概念：IoC 和 DI

Spring 最核心的思想——**控制反转（IoC）和依赖注入（DI）**：

- **不用 Spring**：你自己 `new PostService()`，自己管理对象的创建和销毁
- **用 Spring**：你声明需要什么（`@Autowired` 或构造函数参数），Spring 帮你创建好并注入进来

比如 Controller 需要 Service：

```java
@RestController
@RequiredArgsConstructor  // Lombok 生成构造函数
public class PostController {
    private final PostService postService;  // Spring 会自动注入实现类
}
```

所有带 `@Service`、`@Component`、`@RestController` 的类都是 Spring 帮你 `new` 的。

### 关联项目代码

| 文件 | 说明 | 建议阅读顺序 |
|------|------|-------------|
| `CommunityApplication.java` | 启动类，`@SpringBootApplication` + `main` | ① 先看 |
| `application.yml` | 数据库/Redis/MyBatis-Plus/JWT 的全部配置 | ② 看配置 |
| `controller/AuthController.java` | 注册 + 登录 + 刷新 + 注销，最直观的 REST API 示例 | ③ 重点读 |
| `controller/PostController.java` | 帖子 CRUD，含分页查询、搜索、软删除 | ④ 重点读 |
| `controller/CommentController.java` | 评论发表 + 评论树查询 + 删除 | ⑤ 辅助读 |
| `service/AuthService.java` | 接口定义 | ⑥ 看接口 |
| `service/impl/AuthServiceImpl.java` | 注册登录的业务逻辑实现 | ⑦ 看实现 |

### 跟踪一个请求的完整链路

以 `POST /api/auth/register` 为例：

```
1. HTTP POST 请求到达 localhost:8080/api/auth/register
       │
2. DispatcherServlet 接收，根据 URL 匹配到 AuthController.register()
       │
3. @RequestBody 把 JSON 自动转成 RegisterRequest 对象
   @Valid 触发字段校验（@NotBlank, @Size 等）
       │
4. AuthController 调用 authService.register(request)
       │
5. AuthServiceImpl.register():
   - 校验两次密码是否一致
   - 用 LambdaQueryWrapper 查用户名/邮箱是否已存在
   - passwordEncoder.encode() 加密密码
   - userMapper.insert(user) 写入数据库
       │
6. 返回 ApiResponse.success("Registration successful", null)
   序列化为 JSON → {"code":200, "message":"Registration successful", "data":null}
```

---

## 6. 第4阶段：MyBatis-Plus 数据库操作（1-2 周）

### MyBatis-Plus 是什么

MyBatis 是 Java 世界里最流行的 ORM（对象关系映射）框架之一，让你用 Java 代码操作数据库，不用手写 JDBC。

MyBatis-Plus 是 MyBatis 的**增强版**，提供：
- 继承 `BaseMapper<T>` 就能获得 `insert`、`selectById`、`updateById`、`deleteById` 等方法
- `LambdaQueryWrapper` 类型安全的查询条件构造
- 内置分页插件
- 自动填充 `createdAt`、`updatedAt`

### 必学知识点

| 知识点 | 说明 | 重要度 |
|--------|------|--------|
| Entity 实体类 | `@TableName`、`@TableId`、`@TableField` | ⭐⭐⭐ |
| `BaseMapper<T>` | 继承后自动获得CRUD方法 | ⭐⭐⭐ |
| `LambdaQueryWrapper` | 类型安全的条件构造器 | ⭐⭐⭐ |
| Mapper XML | 写复杂SQL的地方，`<if>` 动态SQL | ⭐⭐⭐ |
| 分页插件 | `PaginationInnerInterceptor` | ⭐⭐ |
| `MetaObjectHandler` | 自动填充 `createdAt`/`updatedAt` | ⭐⭐ |
| `@Transactional` | 事务注解 | ⭐⭐ |
| 驼峰转下划线 | `user_id` ↔ `userId` 自动映射 | ⭐ |

### 核心代码对照

| 层 | 文件 | 说明 |
|----|------|------|
| **Entity** | `entity/User.java` | 映射 `user` 表，`@Data` + `@TableName("user")` |
| | `entity/Post.java` | 映射 `post` 表 |
| | `entity/Comment.java` | 映射 `comment` 表，含 `parentId` 实现嵌套回复 |
| | `entity/Category.java` | 映射 `category` 表 |
| **Mapper 接口** | `mapper/UserMapper.java` | 继承 `BaseMapper<User>`，一行不写就有关键方法 |
| | `mapper/PostMapper.java` | 自定义了分页查询、浏览量自增、评论数自增 |
| | `mapper/CommentMapper.java` | 自定义了按帖子ID查询评论 |
| | `mapper/CategoryMapper.java` | 继承 `BaseMapper<Category>` |
| **Mapper XML** | `resources/mapper/PostMapper.xml` | 复杂分页搜索SQL，含全文检索、动态排序 |
| | `resources/mapper/CommentMapper.xml` | 评论查询SQL |
| **配置** | `config/MybatisPlusConfig.java` | 注册分页插件 + 配置自动填充处理器 |

### LambdaQueryWrapper 示例

```java
// 查用户名是否存在
userMapper.exists(
    new LambdaQueryWrapper<User>()
        .eq(User::getUsername, request.getUsername())
);
// SQL: SELECT COUNT(*) FROM user WHERE username = ?

// 多个条件
new LambdaQueryWrapper<Comment>()
    .eq(Comment::getPostId, postId)
    .eq(Comment::getStatus, 1)  // 只要正常评论
    .orderByAsc(Comment::getCreatedAt);
// SQL: SELECT * FROM comment WHERE post_id = ? AND status = 1 ORDER BY created_at ASC
```

`User::getUsername` 是 Java 方法引用，编译器会检查拼写——比写字符串 `"username"` 安全得多。

### N+1 查询问题

**错误写法**（N+1 次数据库查询）：
```java
// 假设有20条帖子
for (Post post : posts) {
    User author = userMapper.selectById(post.getUserId());  // 每条帖子查一次
    Category cat = categoryMapper.selectById(post.getCategoryId());
}
// 总计：1(查帖子) + 20(查作者) + 20(查分类) = 41 次数据库查询
```

**正确写法**（3次数据库查询）：
```java
// 收集所有需要的ID
List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
List<Long> categoryIds = posts.stream().map(Post::getCategoryId).distinct().toList();
// 批量查询
Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));
Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));
// 总计：1 + 1 + 1 = 3 次数据库查询
```

对应代码：`PostServiceImpl.queryPosts()` 方法。

---

## 7. 第5阶段：Spring Security + JWT 安全体系（2-3 周）

这是整个项目最复杂的部分，**卡住是正常的**。建议先理解概念再看代码。

### 7.1 核心概念

#### 传统 Session vs JWT

| | Session（传统） | JWT（本项目） |
|------|------|------|
| 工作原理 | 登录后服务器存一个 session_id，每次请求带 cookie 去查 | 登录后服务器发一个加密签名的 token，客户端存 localStorage |
| 服务器状态 | 有状态（需要存 session） | 无状态（不需要存，验证签名即可） |
| 扩展性 | 多服务器需要 session 共享 | 天然支持多服务器 |
| 主动撤销 | 删 session 即可 | 需要额外实现（本项目用 Redis 黑名单） |

#### JWT 的结构

一个 JWT Token 由三部分组成，用 `.` 分隔：

```
eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiJ9.xxx
    │                    │                              │
  Header              Payload                       Signature
 (算法类型)         (用户数据：                       (签名，防篡改)
                    userId, role等)
```

- **Header + Payload 是 Base64 编码，任何人可以解码查看**——所以绝不能放敏感信息
- **Signature 用于验证**：服务器用密钥重新签名，对比是否一致，防止伪造

#### 双 Token 机制

| | Access Token | Refresh Token |
|------|------|------|
| 格式 | JWT | UUID（随机字符串） |
| 有效期 | 2小时 | 7天 |
| 存储 | 无状态（客户端保存） | 存 Redis（服务端可撤销） |
| 用途 | 每次请求都带 | Access Token 过期后换新的 |
| 安全策略 | 短有效期减少泄露风险 | 可主动撤销 |

**流程：**
1. 登录 → 拿到 Access Token + Refresh Token
2. 后续请求 → 带 Access Token
3. Access Token 过期 → 用 Refresh Token 换新的 Access Token
4. Refresh Token 也过期 → 重新登录

为什么 Refresh Token 用 UUID 存 Redis 而不是 JWT？
- JWT 签发后无法撤销（除非等过期）
- 存 Redis 后，删除 Key 即可立即撤销（用户被盗号后强制踢下线）

---

### 7.2 安全架构

```
HTTP 请求到达
      │
      ▼
┌─────────────────────────────┐
│ JwtAuthenticationFilter     │  ← 最先执行
│ 1. 从 Header 提取 Token      │
│ 2. 检查黑名单（Redis）       │
│ 3. 验证 Token 签名和有效期   │
│ 4. 设置 SecurityContext     │
└──────────┬──────────────────┘
           ▼
┌─────────────────────────────┐
│ SecurityFilterChain         │  ← URL 权限规则匹配
│ - /api/auth/** → 所有人     │
│ - GET /api/posts → 所有人   │
│ - POST /api/categories → 管理员│
│ - 其余 → 需要认证            │
└──────────┬──────────────────┘
           ▼
       Controller 方法
```

---

### 7.3 安全体系文件详解

#### 按请求处理顺序阅读

| 顺序 | 文件 | 职责 | 重点看 |
|------|------|------|--------|
| 1 | `security/JwtTokenProvider.java` | 生成 Token、验证签名、黑名单操作 | `generateAccessToken()` / `validateToken()` |
| 2 | `security/JwtAuthenticationFilter.java` | 每个请求进来时提取并验证 Token | `doFilterInternal()` 完整流程 |
| 3 | `config/SecurityConfig.java` | URL 权限规则 + 过滤器注册 | `filterChain()` Bean |
| 4 | `security/JwtAuthenticationEntryPoint.java` | 未登录 → 401 | `commence()` |
| 5 | `security/JwtAccessDeniedHandler.java` | 权限不足 → 403 | `handle()` |
| 6 | `security/UserDetailsServiceImpl.java` | 从数据库加载用户信息 | `loadUserByUsername()` |
| 7 | `util/SecurityUtils.java` | 获取当前登录用户 | `getCurrentUserId()` / `isAdmin()` |

#### JwtTokenProvider 核心方法

| 方法 | 功能 |
|------|------|
| `generateAccessToken(userId, username, role)` | 用 JJWT 生成 JWT 字符串 |
| `generateRefreshToken(userId)` | 生成 UUID，存入 Redis，双向映射 |
| `validateToken(token)` | 验证 JWT 签名和有效期 |
| `blacklistAccessToken(token)` | 注销时把 Access Token 加入 Redis 黑名单 |
| `refreshAccessToken(refreshToken)` | 用 Refresh Token 换新 Access Token |
| `deleteRefreshToken(userId)` | 删除用户的 Refresh Token（轮换/注销时） |

#### 从登录理解完整流程

```
POST /api/auth/login { username, password }
         │
         ▼
AuthController.login()
         │
         ▼
AuthServiceImpl.login():
  1. authenticationManager.authenticate() → 验证用户名密码
  2. userMapper.selectById() → 查用户信息
  3. jwtTokenProvider.generateAccessToken() → 生成 Access Token (JWT)
  4. jwtTokenProvider.generateRefreshToken() → 生成 Refresh Token (UUID存Redis)
  5. 返回 { accessToken, refreshToken, userInfo }
         │
         ▼
前端收到，存入 localStorage，后续请求带 Authorization: Bearer <accessToken>
         │
         ▼
下次请求 → JwtAuthenticationFilter.doFilterInternal():
  1. extractToken() → 从 Header 提取
  2. isTokenBlacklisted() → 查 Redis 黑名单
  3. validateToken() → 验证签名和有效期
  4. 解析 userId 和 role → 设置 SecurityContext
         │
         ▼
SecurityConfig 检查 URL 权限 → 放行或拒绝
         │
         ▼
Controller 中通过 SecurityUtils.getCurrentUserId() 获取当前用户
```

---

## 8. 第6阶段：Redis 缓存（3-5 天）

### Redis 是什么

Redis 是一个**把数据存在内存里的键值对数据库**，读写速度极快（微秒级），但内存比硬盘贵，所以一般用来存**临时的、高频访问的**数据。

### 项目的 Redis 用途

| 用途 | Key 格式 | Value | 过期时间 | 对应代码 |
|------|---------|-------|---------|---------|
| Refresh Token | `refresh:token:{uuid}` | userId | 7天 | `JwtTokenProvider.generateRefreshToken()` |
| 用户反向映射 | `refresh:user:{userId}` | token | 7天 | 同上（注销时根据用户找Token） |
| Token 黑名单 | `blacklist:{jwt}` | "" | Token剩余有效时间 | `JwtTokenProvider.blacklistAccessToken()` |

### 双向存储的设计

```
refresh:token:abc123 → 1001     （根据token找用户，刷新时用）
refresh:user:1001    → abc123   （根据用户找token，注销时用）
```

为什么要双向？
- 刷新操作：前端传来 Refresh Token → 查 `refresh:token:xxx` → 找到 userId → 生成新 Token
- 注销操作：用户点击注销 → 已知 userId → 查 `refresh:user:xxx` → 找到旧 Token → 删除两个 Key

### 关联项目代码

| 文件 | 说明 |
|------|------|
| `config/RedisConfig.java` | 配置 Key 用 String 序列化、Value 用 JSON 序列化（否则 Redis 里是二进制乱码） |
| `constant/RedisConstants.java` | Redis Key 前缀常量（`refresh:token:` / `refresh:user:` / `blacklist:`） |

---

## 9. 第7阶段：周边工具收尾（穿插学习）

### Lombok

**作用：** 用注解自动生成重复代码。

| 注解 | 生成什么 | 用在哪里 |
|------|---------|---------|
| `@Data` | getter + setter + toString + equals + hashCode | 所有 Entity 和 DTO |
| `@Builder` | Builder 构建器模式 | `LoginResponse`、`PostSummaryResponse` 等 |
| `@AllArgsConstructor` | 全参构造函数 | `ApiResponse` |
| `@RequiredArgsConstructor` | final 字段的构造函数（用于依赖注入） | 所有 Controller 和 Service |
| `@Slf4j` | 日志对象 `log` | `GlobalExceptionHandler` |

**注意：** Lombok 是编译期注解处理器，Maven 编译器需要额外配置 `annotationProcessorPaths`，否则编译报"找不到符号"。

关联代码：
- `pom.xml` — 需在 `maven-compiler-plugin` 中显式配置
- 所有 `entity/` 和 `dto/` 下的文件

---

### Knife4j（API 文档）

**作用：** 自动生成在线 API 文档页面，可以直接在页面上测试 API。

启动项目后访问：`http://localhost:8080/doc.html`

关联代码：
- `config/Knife4jConfig.java`

---

### 全局异常处理

**作用：** 统一捕获所有未处理的异常，返回统一的 `ApiResponse` 格式。

工作流程：
1. Controller 层抛出异常（不写 try-catch）
2. DispatcherServlet 捕获异常
3. 遍历 `@ExceptionHandler` 方法，找到匹配的处理器
4. 执行处理方法，返回 `ApiResponse`
5. Jackson 序列化成 JSON 返回前端

关联代码：
- `exception/GlobalExceptionHandler.java` — 核心处理器
- `exception/BusinessException.java` — 业务异常（如"用户名已存在"）
- `exception/UnauthorizedException.java` — 未认证（401）
- `exception/ForbiddenException.java` — 无权限（403）
- `exception/NotFoundException.java` — 资源不存在（404）

---

### 统一响应格式

**作用：** 所有 API 返回相同结构的 JSON。

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

关联代码：
- `dto/response/ApiResponse.java` — 泛型包装类，`@JsonInclude(NON_NULL)` 避免输出 null 字段
- `dto/response/PageResult.java` — 分页专用响应体
- `enums/ResultCode.java` — 业务错误码枚举

---

### CORS 跨域

**问题：** 浏览器同源策略阻止 `localhost:3000`（前端）向 `localhost:8080`（后端）发请求。

**解决：** 后端配置 CORS 响应头，告诉浏览器"我允许跨域"。

关联代码：
- `config/WebMvcConfig.java`

---

### BCrypt 密码加密

**作用：** 密码不存明文。

BCrypt 三个特点：
1. **自动加盐**：同一密码每次加密结果不同
2. **不可逆**：无法从密文反推明文
3. **可调节强度**：cost factor 越大越安全但也越慢

关联代码：
- `config/SecurityConfig.passwordEncoder()` — `new BCryptPasswordEncoder()`
- `AuthServiceImpl.register()` — `passwordEncoder.encode(password)`

---

## 10. 推荐执行计划

```
Week 1:  SQL 基础
         ▸ 能看懂 schema.sql 的全部 4 张表定义
         ▸ 理解 JOIN、INDEX、FULLTEXT 关键字

Week 2:  Maven 基础 + Spring Boot 入门
         ▸ 配置阿里云镜像，拉取依赖不再卡
         ▸ 跑起来一个最简 Spring Boot Demo（Hello World API）
         ▸ 读懂 CommunityApplication.java 和 application.yml

Week 3:  Spring Boot 核心
         ▸ 读懂 AuthController（注册/登录）完整流程
         ▸ 跟踪一个请求：HTTP → Controller → Service → Mapper → DB
         ▸ 理解 IoC/DI：谁帮你在 new 对象

Week 4:  MyBatis-Plus 数据库操作
         ▸ 读懂 Entity 注解（@TableName, @TableId, @TableField）
         ▸ 读懂 PostMapper + PostMapper.xml（动态SQL、全文搜索）
         ▸ 理解 LambdaQueryWrapper 和 N+1 问题

Week 5:  Spring Security + JWT（上）
         ▸ 理解 JWT 是什么，为什么用双 Token
         ▸ 读懂 JwtTokenProvider 的生成/验证/黑名单方法
         ▸ 读懂 JwtAuthenticationFilter 的完整过滤逻辑

Week 6:  Spring Security + JWT（下）
         ▸ 读懂 SecurityConfig 的 URL 权限规则
         ▸ 从登录到后续请求，跟踪完整的安全链路
         ▸ 理解 AuthenticationEntryPoint（401）和 AccessDeniedHandler（403）

Week 7:  Redis 缓存
         ▸ 理解 Redis 在项目中的三个用途
         ▸ 读懂 JwtTokenProvider 中 Redis 相关代码
         ▸ 理解双向存储和 TTL 过期

Week 8:  周边收尾 + 全局串联
         ▸ 异常处理、统一响应、Lombok、Knife4j、CORS、BCrypt
         ▸ 从 0 到 1 梳理项目的完整请求生命周期
```

---

## 11. 不需要学的内容

以下技术本项目**没有用到**，学完当前项目之前不用碰：

- **前端（Vue.js / frontend 目录）** — 后端没学完之前不要分心
- **Docker / Kubernetes** — 容器化和编排，本项目用不到
- **微服务 / Spring Cloud** — 这是单体应用
- **JPA / Hibernate** — 另一种 ORM，项目用的是 MyBatis-Plus
- **RabbitMQ / Kafka** — 消息队列，本项目没用到
- **Elasticsearch** — 搜索引擎，本项目用 MySQL FULLTEXT 代替
- **Nginx** — 反向代理，单机开发用不到
- **Jenkins / GitHub Actions** — CI/CD，还没到那个阶段

---

## 12. 如何阅读项目代码

### 推荐的代码阅读顺序

```
① pom.xml                    了解用了哪些依赖
② sql/schema.sql             了解4张表结构
③ application.yml            了解项目配置
④ CommunityApplication.java  看看启动入口
⑤ entity/User.java           最简单的Entity
⑥ entity/Post.java           带更多注解的Entity
⑦ mapper/UserMapper.java     最简单的Mapper
⑧ mapper/PostMapper.java     带自定义SQL的Mapper
⑨ resources/mapper/PostMapper.xml  复杂SQL
⑩ dto/response/ApiResponse.java    统一响应格式
⑪ exception/GlobalExceptionHandler.java  全局异常处理
⑫ service/AuthService.java + impl/AuthServiceImpl.java  业务逻辑
⑬ controller/AuthController.java     REST API入口
⑭ security/JwtTokenProvider.java     Token管理
⑮ security/JwtAuthenticationFilter.java  认证过滤器
⑯ config/SecurityConfig.java         安全配置
⑰ PostServiceImpl + PostController   帖子完整链路
⑱ CommentServiceImpl + CommentController  评论树构建
```

### 学习方法

1. **带着问题看代码**：打开一个 Controller，找到一个接口，看它调了什么 Service，Service 里调了什么 Mapper，Mapper 执行了什么 SQL
2. **只抄不改**：先把项目中一个类似的复制出来，改成自己的需求，不要在空白文件里凭空写
3. **调试优先**：在关键方法打日志或断点，看变量值，比看代码更容易理解
4. **BACKEND_GUIDE.md 常驻**：每个设计决策都有"为什么"，遇到不理解的设计回查文档

---

> 本路线图与 `BACKEND_GUIDE.md` 互补——它解释"怎么做的和为什么做"，这份路线图告诉你"应该以什么顺序学"。两份文档配合项目源码一起阅读效果最佳。
