# 校园信息聚合与互动平台 — 实施方案

## 项目背景

解决校园信息分散（公告栏、微信群、论坛各自为政）和互动低效的问题。构建一个集中的信息发布与互动平台，提供用户认证、帖子管理、评论互动三大核心能力。

---

## 1. 技术选型

| 层次 | 技术 | 版本 | 理由 |
|------|------|------|------|
| 语言 | Java | 17+ | LTS，支持 records/sealed classes/text blocks |
| 框架 | Spring Boot | 3.3.5 | 最新稳定 3.x，Jakarta EE 10，虚拟线程支持 |
| ORM | MyBatis-Plus | 3.5.8 | 复杂条件查询(LambdaQueryWrapper)比 JPA Specifications 更简洁；内置分页插件；sql写在xml中可读可调试 |
| 安全 | Spring Security 6 + JJWT | 0.12.6 | 无状态JWT认证 |
| 数据库 | MySQL | 8.0 | utf8mb4支持emoji |
| 缓存 | Redis | 7.x | JWT黑名单(token注销)、刷新令牌存储、热点帖子缓存、接口限流 |
| 构建 | Maven | 3.9+ | 更成熟的插件生态 |
| API文档 | Knife4j | 4.5.0 | OpenAPI 3.0，中文友好 |
| 工具 | Lombok + Hutool | 1.18.34 / 5.8.32 | 减少样板代码 |

---

## 2. 项目结构

```
p1/
├── pom.xml
├── sql/schema.sql
└── src/main/java/com/campus/community/
    ├── CommunityApplication.java
    ├── config/         (SecurityConfig, MybatisPlusConfig, RedisConfig, Knife4jConfig, WebMvcConfig)
    ├── security/       (JwtTokenProvider, JwtAuthenticationFilter, EntryPoint, UserDetailsServiceImpl)
    ├── controller/     (AuthController, PostController, CommentController, CategoryController, UserController)
    ├── service/        (接口)
    ├── service/impl/   (实现)
    ├── mapper/         (MyBatis-Plus BaseMapper)
    ├── entity/         (User, Post, Comment, Category)
    ├── dto/request/    (LoginRequest, RegisterRequest, PostCreateRequest, PostQueryRequest, ...)
    ├── dto/response/   (ApiResponse<T>, PageResult<T>, LoginResponse, PostDetailResponse, CommentTreeResponse, ...)
    ├── enums/          (ResultCode, PostStatusEnum, UserRoleEnum)
    ├── exception/      (BusinessException, GlobalExceptionHandler)
    ├── util/           (SecurityUtils)
    └── constant/       (RedisConstants)
```

---

## 3. 数据库设计 (4张表)

### user
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| username | VARCHAR(50) UNIQUE | 登录名 |
| password | VARCHAR(255) | BCrypt加密 |
| nickname | VARCHAR(50) | 显示昵称 |
| email | VARCHAR(100) UNIQUE | 邮箱 |
| avatar | VARCHAR(500) | 头像URL |
| role | VARCHAR(20) | USER / ADMIN |
| status | TINYINT | 0=禁用, 1=正常 |
| created_at / updated_at | DATETIME | 时间戳 |

### category
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| name | VARCHAR(50) UNIQUE | 分类名 |
| description | VARCHAR(200) | 描述 |
| sort_order | INT | 排序 |
| created_at | DATETIME | |

### post
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| title | VARCHAR(200) | 标题 |
| content | LONGTEXT | 正文(Markdown) |
| summary | VARCHAR(500) | 摘要，列表展示 |
| user_id | BIGINT FK | 作者 |
| category_id | BIGINT FK | 分类 |
| view_count | INT | 浏览数 |
| like_count | INT | 点赞数 |
| comment_count | INT | 评论数(反范式，避免COUNT查询) |
| status | TINYINT | 0=草稿,1=发布,2=隐藏 |
| is_pinned | TINYINT | 置顶标志 |
| created_at / updated_at | DATETIME | |
| 索引 | | idx_user_id, idx_category_id, idx_created_at, FULLTEXT(title,content) |

### comment
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| content | TEXT | 评论内容 |
| post_id | BIGINT FK | 所属帖子 |
| user_id | BIGINT FK | 评论者 |
| parent_id | BIGINT | 父评论ID(NULL=顶级评论) |
| reply_to_user_id | BIGINT | 被回复的用户 |
| like_count | INT | 点赞数 |
| status | TINYINT | 0=已删除, 1=正常 |
| created_at | DATETIME | |

---

## 4. REST API 设计

统一响应格式：`{ "code": 200, "message": "success", "data": {...} }`

### 4.1 认证 (AuthController)
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | /api/auth/register | 无 | 注册 |
| POST | /api/auth/login | 无 | 登录，返回JWT |
| POST | /api/auth/refresh | 无 | 刷新token |
| POST | /api/auth/logout | 需要 | 注销(token黑名单) |
| GET | /api/auth/me | 需要 | 当前用户信息 |

### 4.2 帖子 (PostController)
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | /api/posts | 需要 | 发帖 |
| GET | /api/posts | 无 | 帖子列表(分页/搜索/分类/排序) |
| GET | /api/posts/{id} | 无 | 帖子详情(+浏览数+1) |
| PUT | /api/posts/{id} | 需要(作者/管理员) | 编辑 |
| DELETE | /api/posts/{id} | 需要(作者/管理员) | 软删除 |

### 4.3 评论 (CommentController)
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/posts/{postId}/comments | 无 | 评论列表(树形结构) |
| POST | /api/posts/{postId}/comments | 需要 | 发表评论/回复 |
| DELETE | /api/comments/{id} | 需要(作者/管理员) | 软删除 |

### 4.4 分类 + 用户
| 方法 | 端点 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | /api/categories | 分类CRUD(增删改需ADMIN) |
| GET/PUT | /api/users/profile | 查看/编辑个人信息 |
| GET | /api/users/{id}/posts | 某用户的帖子列表 |

---

## 5. 安全设计

- **Access Token**：JWT，2小时有效，客户端存储(Authorization: Bearer xxx)
- **Refresh Token**：UUID存Redis，7天有效，刷新时轮换(旧token删除)
- **注销**：access token加入Redis黑名单(TTL=剩余有效期)
- **密码**：BCrypt加密
- **角色**：ROLE_USER(默认) / ROLE_ADMIN
- **权限控制**：Spring Security + @PreAuthorize

---

## 6. 实施顺序 (5步)

### 第1步：项目初始化
- 创建pom.xml、application.yml、启动类
- 创建schema.sql（含种子数据：6个分类+admin用户）

### 第2步：公共基础设施
- Entity类 + Mapper接口 + Mapper XML
- 统一响应类 ApiResponse<T>、PageResult<T>
- ResultCode枚举、异常体系、GlobalExceptionHandler
- 配置类：MybatisPlusConfig、WebMvcConfig(CORS)、RedisConfig

### 第3步：安全与认证
- JwtTokenProvider（生成/验证/黑名单）
- JwtAuthenticationFilter + UserDetailsServiceImpl
- SecurityConfig（过滤器链、URL权限规则）
- SecurityUtils 工具类
- AuthService + AuthController（注册/登录/刷新/注销）

### 第4步：核心业务
- PostService + PostController（CRUD、分页搜索、全文检索）
- CommentService + CommentController（评论树构建）
- CategoryService + CategoryController
- UserService + UserController

### 第5步：文档与完善
- Knife4j配置
- 输入校验注解(@Valid + Jakarta Validation)
- Redis缓存热点帖子
- 单元测试

---

## 7. 关键设计决策

- **评论数反范式**：post.comment_count 在插入评论时同步 +1（同一事务），避免每次列表查询COUNT
- **软删除**：帖子和评论删除只设 status=0/2，不物理删除
- **评论树**：全量查询帖子评论后，在Service层内存构建（校园规模足够）
- **全文搜索**：MySQL FULLTEXT索引，mapper XML中写 MATCH...AGAINST
- **DTO隔离**：Entity不泄露到Controller层，Request/Response独立
