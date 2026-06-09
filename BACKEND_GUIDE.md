# 校园社区平台 — 后端从零搭建完全指南

> 本文档面向初学者，从技术栈选型到完整后端搭建，一步一步带你理解每个技术点和设计决策。建议配合项目源码阅读。

---

## 目录

1. [技术栈全景](#1-技术栈全景)
2. [环境准备](#2-环境准备)
3. [项目初始化 — pom.xml](#3-项目初始化--pomxml)
4. [配置文件 — application.yml](#4-配置文件--applicationyml)
5. [数据库设计 — schema.sql](#5-数据库设计--schemasql)
6. [实体层 (Entity) — 对象与表的映射](#6-实体层-entity--对象与表的映射)
7. [数据访问层 (Mapper) — MyBatis-Plus 操作数据库](#7-数据访问层-mapper--mybatis-plus-操作数据库)
8. [统一响应 — ApiResponse 与全局异常处理](#8-统一响应--apiresponse-与全局异常处理)
9. [安全体系 — Spring Security + JWT 双Token](#9-安全体系--spring-security--jwt-双token)
10. [业务层 (Service) — 核心逻辑实现](#10-业务层-service--核心逻辑实现)
11. [控制层 (Controller) — REST API 暴露](#11-控制层-controller--rest-api-暴露)
12. [配置类详解](#12-配置类详解)
13. [关键设计决策与踩坑记录](#13-关键设计决策与踩坑记录)

---

## 1. 技术栈全景

| 类别 | 技术 | 版本 | 一句话作用 |
|------|------|------|-----------|
| 语言 | Java | 17+ | LTS版本，支持 Record、Text Block 等现代语法 |
| 框架 | Spring Boot | 3.3.5 | 快速构建独立运行的Spring应用，内嵌Tomcat |
| ORM | MyBatis-Plus | 3.5.8 | 增强版MyBatis，提供分页插件、Lambda查询、自动填充 |
| 数据库 | MySQL | 8.0 | 关系型数据库，utf8mb4字符集支持emoji |
| 安全 | Spring Security 6 | — | 认证授权框架，过滤器链机制 |
| JWT | JJWT | 0.12.6 | JSON Web Token的Java实现，无状态认证 |
| 缓存 | Redis | 7.x | 内存数据库，用于Token黑名单和刷新令牌存储 |
| API文档 | Knife4j | 4.5.0 | Swagger增强版，中文友好的API文档页面 |
| 工具 | Lombok | 1.18.34 | 注解自动生成getter/setter/Builder等样板代码 |
| 工具 | Hutool | 5.8.32 | 国产Java工具集，这里主要用UUID生成 |
| 连接池 | Lettuce | — | Redis客户端，Spring Boot默认包含 |

### 为什么选这些？

- **Spring Boot 3.x** 而不是 2.x：Spring Boot 3.x 基于 Jakarta EE 10（包名从 `javax.*` 迁移到 `jakarta.*`），支持虚拟线程，是未来的方向。
- **MyBatis-Plus** 而不是 JPA：复杂条件查询用 `LambdaQueryWrapper` 比 JPA 的 Specification 更直观；SQL写在XML中可读可调试；内置分页插件不需要额外配置。
- **JJWT 0.12.x** 而不是旧版：API经过重新设计，`Jwts.builder()` 链式调用更清晰，密钥类型从 `Key` 改为 `SecretKey`。
- **双Token机制**：Access Token（2小时，JWT）+ Refresh Token（7天，UUID存Redis）。Access Token泄露风险可控（短有效期），Refresh Token可主动撤销（存Redis）。

---

## 2. 环境准备

搭建项目前需要安装以下软件：

| 软件 | 最低版本 | 验证命令 |
|------|---------|---------|
| JDK | 17 | `java -version` |
| Maven | 3.9 | `mvn -v` |
| MySQL | 8.0 | `mysql -u root -p` |
| Redis | 7.x | `redis-cli ping` |

### 2.1 Maven配置（国内镜像加速）

编辑 `~/.m2/settings.xml`（用户目录下的 `.m2` 文件夹），添加阿里云镜像：

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

### 2.2 MySQL创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE campus_community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.3 Redis启动

```bash
redis-server
```

---

## 3. 项目初始化 — pom.xml

`pom.xml` 是Maven项目的核心配置文件，定义了项目的坐标、父项目、依赖和构建插件。

### 3.1 Spring Boot父项目

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
</parent>
```

`spring-boot-starter-parent` 的作用：
- 统一管理所有Spring Boot相关依赖的版本号（你不需要手动指定版本）
- 配置了 `maven-compiler-plugin`、`maven-resources-plugin` 等常用插件
- 指定了资源文件的过滤规则

### 3.2 核心依赖详解

#### spring-boot-starter-web
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
这是构建REST API的基础。包含：
- **Spring MVC**：处理HTTP请求（`@RestController`、`@RequestMapping`）
- **内嵌Tomcat**：无需单独部署，`java -jar` 直接运行
- **Jackson**：Java对象 ↔ JSON 的序列化/反序列化

#### spring-boot-starter-security
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
引入Spring Security。默认行为：所有请求需要认证、生成一个随机密码、启用CSRF保护、Session管理。本项目会通过 `SecurityConfig` 全部自定义。

#### spring-boot-starter-validation
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
提供 Jakarta Bean Validation（`@Valid`、`@NotNull`、`@Size` 等）的支持，用于请求参数的校验。

#### mybatis-plus-spring-boot3-starter
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.8</version>
</dependency>
```
注意 `boot3` 后缀——这是适配Spring Boot 3.x（Jakarta EE）的版本。

MyBatis-Plus 核心能力：
- **BaseMapper<T>**：继承后自动获得 `insert`、`selectById`、`selectList`、`updateById`、`deleteById` 等CRUD方法
- **LambdaQueryWrapper**：类型安全的条件构造器，避免字符串硬编码
- **分页插件**：`PaginationInnerInterceptor`
- **自动填充**：`MetaObjectHandler`，创建时间和更新时间自动填充

#### mysql-connector-j
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
MySQL的JDBC驱动。`scope=runtime` 表示编译时不需要，运行时才需要（因为编译时只用到JDBC接口）。

#### JJWT 三个模块
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>      <!-- API接口（编译时需要） -->
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>     <!-- 默认实现（运行时） -->
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>  <!-- Jackson序列化（运行时） -->
    <scope>runtime</scope>
</dependency>
```

JJWT 0.12.x 架构：API模块定义接口，impl提供默认实现，jackson用于JSON序列化。编译时只需要API，实际运行时才加载实现。

#### knife4j-openapi3-jakarta-spring-boot-starter
```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```
Knife4j 的 Jakarta 版本，适配 Spring Boot 3.x。启动后访问 `http://localhost:8080/doc.html` 即可看到Swagger风格的API文档页面，可以直接在页面上测试API。

#### Lombok
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
Lombok 是编译期注解处理器，在编译时生成 `getXxx()`、`setXxx()`、`toString()`、Builder等样板代码。`optional=true` 表示不传递给依赖此项目的其他项目。

**重要**：Lombok需要额外的Maven编译器配置：
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```
不配置的话，编译器找不到Lombok注解处理器，所有 `@Data`、`@Builder` 都会报"找不到符号"。

---

## 4. 配置文件 — application.yml

Spring Boot的配置文件，位于 `src/main/resources/application.yml`。

### 4.1 数据源配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campus_community?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 1234
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
```

- **driver-class-name**：MySQL 8.x 的驱动类是 `com.mysql.cj.jdbc.Driver`（注意 `cj`）
- **url 参数解读**：
  - `useUnicode=true`：启用Unicode编码
  - `characterEncoding=UTF-8`：使用UTF-8（注意Java中写 `UTF-8` 而不是 `utf8mb4`，后者Java不认识）
  - `serverTimezone=Asia/Shanghai`：时区，避免时间差8小时
- **HikariCP**：Spring Boot 2.x+默认的连接池，性能最好的Java连接池之一

### 4.2 Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
```

Lettuce 是 Spring Boot 默认的 Redis 客户端，基于 Netty，异步非阻塞。需要额外引入 `commons-pool2` 才能使用连接池。

### 4.3 MyBatis-Plus配置

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.campus.community.entity
  configuration:
    map-underscore-to-camel-case: true  # user_id → userId
  global-config:
    db-config:
      id-type: auto  # 主键自增
```

- **mapper-locations**：指定Mapper XML文件的位置，`classpath*` 会扫描所有jar包
- **map-underscore-to-camel-case**：数据库 `user_id` 自动映射为Java的 `userId`
- **id-type: auto**：主键使用数据库自增

### 4.4 JWT配置

```yaml
jwt:
  secret: Y2FtcHVzLWNvbW11bml0eS1zZWNyZXQta2V5...
  expiration: 7200        # Access Token 2小时
  refresh-expiration: 604800  # Refresh Token 7天
```

- **secret**：HMAC-SHA算法的密钥，至少256位，用Base64编码存储
- **expiration**：Access Token有效期（2小时），短有效期减少泄露风险
- **refresh-expiration**：Refresh Token有效期（7天），用户7天内不需要重新登录

---

## 5. 数据库设计 — schema.sql

### 5.1 4张核心表

#### user 表
```sql
CREATE TABLE `user` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50)   NOT NULL COMMENT '登录名',
  `password`      VARCHAR(255)  NOT NULL COMMENT 'BCrypt加密',
  `nickname`      VARCHAR(50)   DEFAULT NULL COMMENT '显示昵称',
  `email`         VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
  `avatar`        VARCHAR(500)  DEFAULT NULL COMMENT '头像URL',
  `phone`         VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
  `role`          VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT 'USER或ADMIN',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=正常',
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
);
```

设计要点：
- `password` 用 VARCHAR(255) 而非固定长度，因为BCrypt输出为60字符
- `role` 用字符串而非数字，可读性好
- `username` 和 `email` 都是唯一索引

#### post 表
```sql
CREATE TABLE `post` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `title`         VARCHAR(200)  NOT NULL,
  `content`       LONGTEXT      NOT NULL COMMENT 'Markdown格式正文',
  `summary`       VARCHAR(500)  DEFAULT NULL COMMENT '列表展示摘要',
  `user_id`       BIGINT        NOT NULL,
  `category_id`   BIGINT        DEFAULT NULL,
  `view_count`    INT           NOT NULL DEFAULT 0,
  `like_count`    INT           NOT NULL DEFAULT 0,
  `comment_count` INT           NOT NULL DEFAULT 0 COMMENT '反范式冗余',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '0=草稿, 1=发布, 2=隐藏(软删除)',
  `is_pinned`     TINYINT       NOT NULL DEFAULT 0 COMMENT '置顶',
  FULLTEXT KEY `ft_post_title_content` (`title`, `content`)
);
```

设计要点：
- **comment_count 反范式**：每次查询帖子列表时避免 `SELECT COUNT(*) FROM comment WHERE post_id=?`，在插入/删除评论时同步更新这个字段
- **FULLTEXT索引**：`MATCH(title, content) AGAINST('关键词')` 实现全文搜索
- **软删除**：`status=2` 而非物理删除，数据可恢复
- **content 用 LONGTEXT**：Markdown内容可能很长，TEXT最大64KB不够用

#### comment 表
```sql
CREATE TABLE `comment` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `content`           TEXT          NOT NULL,
  `post_id`           BIGINT        NOT NULL,
  `user_id`           BIGINT        NOT NULL,
  `parent_id`         BIGINT        DEFAULT NULL COMMENT 'NULL=顶级评论, 非NULL=回复',
  `reply_to_user_id`  BIGINT        DEFAULT NULL COMMENT '被回复的用户',
  `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '0=已删除, 1=正常',
  KEY `idx_comment_post_id` (`post_id`),
  KEY `idx_comment_parent_id` (`parent_id`)
);
```

设计要点：
- **parent_id** 实现嵌套回复：顶级评论 `parent_id=NULL`，回复时 `parent_id=被回复评论的ID`
- **reply_to_user_id**：记录"回复了谁"，用于显示 `小明 回复 小红：xxx`
- 评论树在Service层内存构建（校园规模，每篇帖子几百条评论足够）

---

## 6. 实体层 (Entity) — 对象与表的映射

Entity类使用MyBatis-Plus注解将Java对象与数据库表关联。

### 6.1 User实体

```java
@Data                           // Lombok: 生成getter/setter/toString/equals/hashCode
@TableName("user")              // 映射到 user 表
public class User {

    @TableId(type = IdType.AUTO) // 主键，数据库自增
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String phone;
    private String role;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)           // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)    // 插入和更新时自动填充
    private LocalDateTime updatedAt;
}
```

注解详解：
- `@Data`：Lombok核心注解，相当于 `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor`
- `@TableName("user")`：如果类名和表名不一致（user vs users），用这个注解指定
- `@TableId(type = IdType.AUTO)`：告诉MyBatis-Plus主键是数据库自增的，`insert` 后自动回填ID
- `@TableField(fill = ...)`：标记需要 `MetaObjectHandler` 自动填充的字段

**注意**：字段使用驼峰命名（如 `createdAt`），MyBatis-Plus会自动转为下划线（`created_at`），前提是配置了 `map-underscore-to-camel-case: true`。

---

## 7. 数据访问层 (Mapper) — MyBatis-Plus 操作数据库

### 7.1 Mapper接口

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

继承 `BaseMapper<User>` 后，自动获得以下方法（无需写一行SQL）：
- `insert(User)` — 插入
- `selectById(Long)` — 根据主键查询
- `selectList(Wrapper<User>)` — 条件查询
- `selectBatchIds(List<Long>)` — 批量查询
- `updateById(User)` — 根据主键更新
- `deleteById(Long)` — 根据主键删除
- `exists(Wrapper<User>)` — 判断是否存在

### 7.2 自定义SQL — PostMapper为例

当标准CRUD不够用时，在Mapper接口中定义方法，在XML中写SQL：

**接口**：
```java
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    Page<Post> selectPostPage(Page<Post> page,
            String keyword, Long categoryId, Long userId,
            Integer status, String sort);

    void incrementViewCount(@Param("postId") Long postId);

    void incrementCommentCount(@Param("postId") Long postId);
}
```

**XML**（`mapper/PostMapper.xml`）：
```xml
<mapper namespace="com.campus.community.mapper.PostMapper">

    <select id="selectPostPage" resultType="com.campus.community.entity.Post">
        SELECT p.* FROM post p
        <where>
            <if test="status != null">
                AND p.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
                AND MATCH(p.title, p.content) AGAINST(#{keyword} IN BOOLEAN MODE)
            </if>
        </where>
        ORDER BY p.is_pinned DESC
        <choose>
            <when test="sort == 'hot'">, p.comment_count DESC</when>
            <otherwise>, p.created_at DESC</otherwise>
        </choose>
    </select>
</mapper>
```

关键知识点：
- `<if test="...">`：MyBatis动态SQL，根据参数是否为空决定是否拼接条件
- `MATCH...AGAINST`：MySQL全文检索语法，`IN BOOLEAN MODE` 支持布尔操作符
- `<choose>/<when>/<otherwise>`：类似Java的 `switch-case-default`
- 分页参数 `Page<Post>` 会被MyBatis-Plus分页插件拦截，自动在SQL末尾加 `LIMIT`
- `resultType` 指定返回类型，MyBatis-Plus会根据字段名自动映射

---

## 8. 统一响应 — ApiResponse 与全局异常处理

### 8.1 统一响应格式

所有API返回统一的JSON结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

实现：

```java
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // null字段不序列化
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    // 静态工厂方法（比构造函数语义更清晰）
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

泛型 `T` 让不同类型的数据共用同一个响应结构。`@JsonInclude(NON_NULL)` 避免在JSON中出现 `"data": null`。

### 8.2 分页响应

```java
@Data
public class PageResult<T> {
    private List<T> records;    // 当前页数据
    private long total;         // 总记录数
    private int page;           // 当前页码
    private int size;           // 每页大小
    private int pages;          // 总页数
}
```

### 8.3 全局异常处理 — @RestControllerAdvice

```java
@Slf4j
@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException ex) {
        // 收集所有校验失败的字段和错误信息
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusiness(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)  // 兜底
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleUnknown(Exception ex) {
        log.error("Unexpected error", ex);
        return ApiResponse.error(500, "Internal server error");
    }
}
```

工作原理：
1. Controller层抛出异常 → `DispatcherServlet` 捕获
2. 遍历 `@ExceptionHandler` 方法，找到匹配的处理器
3. 执行处理方法，返回 `ApiResponse`
4. 转换成JSON响应给客户端

**为什么需要全局异常处理？**
- Controller不需要写 `try-catch`，代码更干净
- 统一错误响应格式，前端只需处理一种错误结构
- 最后的 `Exception.class` 兜底，避免500错误泄露敏感信息

### 8.4 结果码枚举

```java
@Getter
public enum ResultCode {
    SUCCESS(200, "success"),
    USERNAME_EXISTS(4001, "Username already exists"),
    INVALID_CREDENTIALS(4003, "Invalid username or password"),
    TOKEN_INVALID(4004, "Token is invalid or expired"),
    // ...
}
```

将业务错误码集中管理在枚举中，而不是在代码中硬编码数字。

---

## 9. 安全体系 — Spring Security + JWT 双Token

这是整个项目最复杂的部分。我们来拆解每一层。

### 9.1 整体架构

```
            请求到达
               ↓
    JwtAuthenticationFilter (提取Token)
               ↓
        Token校验 / 黑名单检查
               ↓
        设置 SecurityContext
               ↓
    SecurityFilterChain (URL权限检查)
               ↓
           Controller
```

### 9.2 JWT Token 提供器 (JwtTokenProvider)

核心职责：生成Token、验证Token、黑名单管理。

#### 生成AccessToken

```java
public String generateAccessToken(Long userId, String username, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration * 1000);

    return Jwts.builder()
            .subject(String.valueOf(userId))   // sub: 用户ID
            .claim("username", username)        // 自定义声明
            .claim("role", role)                // 自定义声明
            .issuedAt(now)                      // iat: 签发时间
            .expiration(expiryDate)             // exp: 过期时间
            .signWith(secretKey)                // 签名
            .compact();                         // 生成字符串
}
```

JJWT 0.12.x的链式API：
- `subject()` 设置JWT的 `sub` 字段，通常放用户ID
- `claim()` 添加自定义字段
- `signWith(secretKey)` 使用HMAC-SHA算法签名
- HMAC-SHA384的密钥需要至少384位（48字节），用Base64编码后存配置

**为什么userId放subject而不是单独claim？**
subject是JWT标准字段，后续提取时用 `parseSignedClaims(token).getPayload().getSubject()` 一行代码即可。

#### 生成RefreshToken（存Redis）

```java
public String generateRefreshToken(Long userId) {
    String token = IdUtil.simpleUUID();  // Hutool生成不带横线的UUID
    String key = "refresh:token:" + token;
    String userKey = "refresh:user:" + userId;

    // 双向存储，方便查找
    redisTemplate.opsForValue().set(key, userId, 7, TimeUnit.DAYS);
    redisTemplate.opsForValue().set(userKey, token, 7, TimeUnit.DAYS);

    return token;
}
```

**为什么Refresh Token用UUID存Redis而不是JWT？**
- Refresh Token需要主动撤销能力（用户换设备、账号被盗）
- JWT一经签发无法撤销（除非等过期）
- 存Redis后，删除对应Key即可撤销

**为什么双向存储？**
- `refresh:token:xxx → userId`：刷新时根据token找到用户
- `refresh:user:xxx → token`：注销时根据用户找到token并删除

#### 黑名单机制（注销）

```java
public void blacklistAccessToken(String token) {
    Claims claims = parseClaims(token);
    long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
    if (remainingTime > 0) {
        String key = "blacklist:" + token;
        // TTL = token剩余有效期，到期自动删除
        redisTemplate.opsForValue().set(key, "", remainingTime, TimeUnit.MILLISECONDS);
    }
}
```

为什么黑名单存Redis而不是数据库？
- 黑名单有TTL（等于token剩余有效期），Redis的 `expire` 天然支持
- 如果存数据库，需要定时任务清理过期黑名单
- 每次请求都查Redis黑名单，O(1)速度

### 9.3 JWT认证过滤器 (JwtAuthenticationFilter)

继承 `OncePerRequestFilter` 确保每个请求只过滤一次。

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response, FilterChain filterChain) {

    // 1. 从Header中提取Token
    String token = extractToken(request);
    if (token == null) {
        filterChain.doFilter(request, response);  // 无token，放行（给匿名访问的接口）
        return;
    }

    // 2. 检查黑名单
    if (jwtTokenProvider.isTokenBlacklisted(token)) {
        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
        return;
    }

    // 3. 验证Token签名和有效期
    if (!jwtTokenProvider.validateToken(token)) {
        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
        return;
    }

    // 4. 解析Token中的信息，设置Security上下文
    String userId = String.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    String role = jwtTokenProvider.parseClaims(token).get("role", String.class);

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    userId,          // principal (主体)
                    null,            // credentials (凭证，无状态所以null)
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))  // 权限
            );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
}
```

工作流程：
1. 从 `Authorization: Bearer xxx` 头提取token
2. 无token → 放行，后续由 `SecurityFilterChain` 根据URL规则决定是否能访问
3. 有token → 检查黑名单 → 验证签名 → 解析用户信息 → 设置 `SecurityContext`
4. Self4后，Controller中通过 `SecurityContextHolder.getContext().getAuthentication()` 获取当前用户

### 9.4 SecurityConfig — 配置安全规则

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 启用 @PreAuthorize 注解
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 关闭CSRF（前后端分离，JWT天然防CSRF）
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 无状态Session（JWT不需要服务器端Session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)   // 未认证返回401
                .accessDeniedHandler(accessDeniedHandler)) // 权限不足返回403

            // 4. URL权限规则（从上到下匹配，先匹配先生效）
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("GET", "/api/posts/**").permitAll()
                .requestMatchers("GET", "/api/categories").permitAll()
                // 管理员接口
                .requestMatchers("POST", "/api/categories").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/categories/**").hasRole("ADMIN")
                // 其余接口需要认证
                .anyRequest().authenticated())

            // 5. 在UsernamePasswordAuthenticationFilter之前插入JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // BCrypt特点：自动加盐、不可逆、同一密码每次加密结果不同
    }
}
```

### 9.5 SecurityUtils — 工具类

```java
@Component
public class SecurityUtils {
    private static UserMapper userMapper;

    // 构造函数注入，通过静态变量保存（@Component + 构造函数 = Spring管理的单例）
    public SecurityUtils(UserMapper userMapper) {
        SecurityUtils.userMapper = userMapper;
    }

    // 从SecurityContext获取当前用户ID
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        return principal instanceof String ? Long.valueOf((String) principal) : null;
    }

    // 判断当前用户是否为管理员
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
```

**为什么用静态方法？**
方便在Service中直接 `SecurityUtils.getCurrentUserId()` 调用，无需注入。

---

## 10. 业务层 (Service) — 核心逻辑实现

### 10.1 认证服务 (AuthServiceImpl)

#### 注册流程

```java
public void register(RegisterRequest request) {
    // 1. 校验两次密码一致
    if (!request.getPassword().equals(request.getConfirmPassword())) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "Passwords do not match");
    }

    // 2. 检查用户名唯一性
    boolean usernameExists = userMapper.exists(
            new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));

    // 3. 检查邮箱唯一性
    // 4. 密码BCrypt加密
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    // 5. 插入数据库
    userMapper.insert(user);
}
```

**LambdaQueryWrapper** 是 MyBatis-Plus 的类型安全条件构造器：
```java
new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
// 等价于 SQL: WHERE username = ?
// 优点：用方法引用代替字符串，编译器检查拼写错误
```

#### 登录流程

```java
public LoginResponse login(LoginRequest request) {
    // 1. Spring Security AuthenticationManager 验证用户名密码
    Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));

    // 2. 从数据库获取完整用户信息
    User user = userMapper.selectById(Long.valueOf(auth.getName()));

    // 3. 检查账户状态
    if (user.getStatus() == 0) throw ...;

    // 4. 生成双Token
    String accessToken = jwtTokenProvider.generateAccessToken(userId, username, role);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

    // 5. 返回
    return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userInfo(userInfo)
            .build();
}
```

#### Token刷新流程

```java
public LoginResponse refresh(RefreshTokenRequest request) {
    // 1. 在Redis中查找RefreshToken是否有效
    String userIdStr = jwtTokenProvider.refreshAccessToken(request.getRefreshToken());
    if (userIdStr == null) throw new UnauthorizedException("refresh token expired");

    // 2. 删除旧的RefreshToken（滚动刷新，防止重放攻击）
    jwtTokenProvider.deleteRefreshToken(userId);

    // 3. 生成新的双Token
    String newAccessToken = ...;
    String newRefreshToken = ...;
}
```

**为什么刷新时轮换RefreshToken？**
如果RefreshToken被盗，攻击者和正常用户都会尝试刷新。轮换后旧的RefreshToken失效，谁先刷新谁获得新Token，慢的那一方下次会失败并被迫重新登录。这就是"Refresh Token Rotation"策略。

### 10.2 帖子服务 (PostServiceImpl)

#### 发帖

```java
@Transactional  // 事务：多个数据库操作要么全成功，要么全回滚
public Long createPost(PostCreateRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();

    // 验证分类是否存在
    if (request.getCategoryId() != null) {
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
    }

    Post post = new Post();
    post.setTitle(request.getTitle());
    post.setSummary(/* 自动生成摘要 */);
    post.setUserId(userId);
    post.setStatus(1);  // 直接发布

    postMapper.insert(post);  // insert后id自动回填到post对象
    return post.getId();
}
```

**`@Transactional` 的作用**：
- 方法开始时开启事务，正常结束时提交，抛异常时回滚
- 这里只有一个insert操作，加事务是防止后续需求变更时忘记加

#### 帖子列表查询（核心查询逻辑）

```java
public PageResult<PostSummaryResponse> queryPosts(PostQueryRequest request) {
    // 1. 创建MyBatis-Plus分页对象
    Page<Post> page = new Page<>(request.getPage(), request.getSize());

    // 2. 执行分页查询（只查已发布的帖子）
    Page<Post> postPage = postMapper.selectPostPage(page, keyword, categoryId, userId, 1, sort);

    // 3. 批量查询作者和分类信息（避免N+1问题）
    List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
    List<Long> categoryIds = posts.stream().map(Post::getCategoryId).distinct().toList();

    Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
            .stream().collect(Collectors.toMap(User::getId, Function.identity()));
    Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds)
            .stream().collect(Collectors.toMap(Category::getId, Function.identity()));

    // 4. 组装响应
    List<PostSummaryResponse> summaries = posts.stream().map(post -> {
        User user = userMap.get(post.getUserId());
        Category category = categoryMap.get(post.getCategoryId());
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(toUserInfo(user))
                .category(toCategoryResponse(category))
                // ...
                .build();
    }).toList();

    return PageResult.of(summaries, totalCount, page, size);
}
```

**避免N+1查询**：如果循环每个post去查user和category（N次查询），20个帖子 = 1 + 20 + 20 = 41次数据库查询。用 `selectBatchIds` 批量查询 = 1 + 1 + 1 = 3次查询。

#### 软删除

```java
public void deletePost(Long id) {
    Post post = postMapper.selectById(id);
    // 权限检查：只有作者和管理员可以删除
    if (!canModify(post)) throw new ForbiddenException(...);

    post.setStatus(2);  // 改为"已删除"状态
    postMapper.updateById(post);  // 只更新修改的字段
}
```

### 10.3 评论服务 (CommentServiceImpl)

#### 评论树构建算法

```java
public List<CommentTreeResponse> getCommentTree(Long postId) {
    // 1. 查询所有评论（不区分顶级/子评论）
    List<Comment> comments = commentMapper.selectByPostId(postId);

    // 2. 批量查用户信息
    Map<Long, User> userMap = ...;

    // 3. 所有评论转为树节点
    List<CommentTreeResponse> allNodes = comments.stream()
            .map(c -> buildNode(c, userMap)).toList();

    // 4. 按parent_id分组（找出所有子评论）
    Map<Long, List<CommentTreeResponse>> childrenMap = allNodes.stream()
            .filter(node -> /* parentId != null */)
            .collect(Collectors.groupingBy(/* parentId */));

    // 5. 筛选顶级评论（parentId == null）
    List<CommentTreeResponse> roots = allNodes.stream()
            .filter(node -> /* parentId == null */).toList();

    // 6. 递归挂载子评论
    attachChildren(roots, childrenMap);
    return roots;
}

private void attachChildren(List<CommentTreeResponse> parents,
        Map<Long, List<CommentTreeResponse>> childrenMap) {
    for (CommentTreeResponse parent : parents) {
        List<CommentTreeResponse> children = childrenMap.get(parent.getId());
        parent.setChildren(children);
        if (children != null) attachChildren(children, childrenMap);  // 递归
    }
}
```

时间复杂度：O(n)，每个评论只处理一次。
为什么不在SQL中递归？MySQL递归CTE语法复杂、性能差；校园场景下单帖评论量小（几百条），内存构建足够了。

---

## 11. 控制层 (Controller) — REST API 暴露

以 PostController 为例：

```java
@RestController                    // = @Controller + @ResponseBody
@RequestMapping("/api/posts")      // 所有方法的基础路径
@RequiredArgsConstructor           // Lombok: 生成final字段的构造函数（构造函数注入）
@Tag(name = "帖子管理")            // Knife4j分组标签
public class PostController {

    private final PostService postService;

    @Operation(summary = "获取帖子列表")  // Knife4j接口描述
    @GetMapping
    public ApiResponse<PageResult<PostSummaryResponse>> list(
            @Valid PostQueryRequest request) {  // @Valid触发JSR-303校验
        return ApiResponse.success(postService.queryPosts(request));
    }

    @Operation(summary = "发帖")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody PostCreateRequest request) {
        Long postId = postService.createPost(request);
        return ApiResponse.success("Post created successfully", postId);
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.success("Post deleted successfully", null);
    }
}
```

REST API设计要点：
- `@RestController`：每个方法返回值自动序列化为JSON
- `@RequestBody`：将HTTP请求体JSON反序列化为Java对象
- `@PathVariable`：提取URL路径参数（`/api/posts/{id}`)
- `@Valid`：触发 Jakarta Validation，校验失败抛 `MethodArgumentNotValidException`
- Controller层只做参数接收和响应包装，不写业务逻辑

---

## 12. 配置类详解

### 12.1 MybatisPlusConfig

```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件：拦截SQL，在末尾加 LIMIT offset,size
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时自动填充 createdAt 和 updatedAt
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时自动填充 updatedAt
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

分页插件原理：MyBatis-Plus 拦截器在SQL执行前拦截，解析原始SQL，添加分页SQL（`LIMIT`），然后执行。

### 12.2 WebMvcConfig — CORS跨域

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                    // 所有路径
                .allowedOriginPatterns("*")           // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)               // 允许携带Cookie
                .maxAge(3600);                        // 预检请求缓存1小时
    }
}
```

为什么需要CORS？浏览器同源策略阻止 `localhost:3000`（前端）向 `localhost:8080`（后端）发请求。CORS配置告诉浏览器"这个后端允许跨域访问"。

### 12.3 RedisConfig — 序列化配置

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    // Key用String序列化（可读）
    template.setKeySerializer(new StringRedisSerializer());
    // Value用JSON序列化（支持复杂对象）
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
}
```

不配置序列化器的话，Redis中存的Key会是二进制格式（`\xac\xed\x00\x05t\x00\x04test`），不可读。

---

## 13. 关键设计决策与踩坑记录

### 13.1 为什么DTO和Entity分离？

- **Entity**：与数据库表一一对应，包含所有字段
- **DTO（Request/Response）**：面向API，只暴露需要的字段

例子：
- `Post` Entity 有 `userId`、`status` 等字段
- `PostDetailResponse` 不暴露 `userId`，而是展开为 `author: { nickname, avatar }`
- 未来数据库加字段（如`internal_notes`）也不会意外泄露到API

### 13.2 为什么comment_count反范式？

帖子列表需要显示评论数。两种方案：

| 方案 | 优点 | 缺点 |
|------|------|------|
| 每次查询时COUNT | 数据绝对准确 | 每行帖子一个子查询，性能差 |
| comment_count冗余字段 | O(1)读取 | 需要维护一致性 |

选择反范式因为：帖子列表读取频率远高于评论写入频率，用 `@Transactional` 保证写入时的一致性。

### 13.3 Lombok踩坑

Maven编译时报"找不到符号 getXxx()"：
- 原因：Maven编译器默认不加载Lombok注解处理器
- 解决：在 `maven-compiler-plugin` 中显式配置 `<annotationProcessorPaths>`

### 13.4 MySQL字符集踩坑

JDBC URL中 `characterEncoding=utf8mb4` 会报错：
- 原因：Java不认识 `utf8mb4` 这个字符集名称
- 解决：改为 `characterEncoding=UTF-8`，数据库层面用 `utf8mb4` 建表

### 13.5 软删除后的查询过滤

本项目原本用 `SecurityUtils.isAdmin() ? null : 1` 来控制status过滤——管理员看全部，普通用户只看已发布。但这导致管理员在列表中也看到已删除的帖子。

修复：固定 `status=1`，只查已发布的帖子。如果需要查看已删除的帖子，应该走单独的后台管理接口。

### 13.6 前端computed的响应式陷阱

Pinia store中的 `computed(() => !!localStorage.getItem('token'))` 不响应式更新：
- 原因：`localStorage` 不是Vue的响应式对象
- 解决：改为 `computed(() => !!user.value)`，依赖Pinia的 `ref`

---

## 附录：项目文件清单

```
src/main/java/com/campus/community/
├── CommunityApplication.java          # Spring Boot启动类
├── config/
│   ├── Knife4jConfig.java            # API文档配置
│   ├── MybatisPlusConfig.java        # 分页插件 + 自动填充
│   ├── RedisConfig.java              # Redis序列化
│   ├── SecurityConfig.java           # Spring Security配置
│   └── WebMvcConfig.java             # CORS跨域
├── security/
│   ├── JwtAccessDeniedHandler.java   # 403处理器
│   ├── JwtAuthenticationEntryPoint.java  # 401处理器
│   ├── JwtAuthenticationFilter.java  # JWT过滤器（核心）
│   ├── JwtTokenProvider.java         # Token生成/验证/黑名单
│   └── UserDetailsServiceImpl.java   # 用户加载
├── controller/
│   ├── AuthController.java           # 注册/登录/刷新/注销
│   ├── CategoryController.java       # 分类CRUD
│   ├── CommentController.java        # 评论发表/树查询/删除
│   ├── PostController.java           # 帖子CRUD/搜索
│   └── UserController.java           # 个人信息/用户帖子
├── service/                          # 接口定义
├── service/impl/                     # 业务实现
│   ├── AuthServiceImpl.java
│   ├── CategoryServiceImpl.java
│   ├── CommentServiceImpl.java
│   ├── PostServiceImpl.java
│   └── UserServiceImpl.java
├── mapper/                           # MyBatis-Plus Mapper
├── entity/                           # 数据库实体 (User/Post/Comment/Category)
├── dto/request/                      # 请求体 (9个)
├── dto/response/                     # 响应体 (8个)
├── enums/                            # ResultCode/PostStatusEnum/UserRoleEnum
├── exception/                        # 自定义异常 + 全局异常处理器
├── util/SecurityUtils.java          # 安全工具方法
└── constant/RedisConstants.java     # Redis Key前缀常量

src/main/resources/
├── application.yml                   # 项目配置
└── mapper/
    ├── PostMapper.xml                # 帖子复杂查询SQL
    └── CommentMapper.xml             # 评论查询SQL
```

---

> 本文档随项目持续更新。对照源码阅读效果最佳。有问题欢迎提Issue。
