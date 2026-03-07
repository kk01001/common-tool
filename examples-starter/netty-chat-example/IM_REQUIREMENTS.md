# Netty Chat IM - 生产级即时通讯系统需求文档

## 一、系统概述

基于 `netty-spring-boot-starter` 构建的生产级 IM 即时通讯系统，支持单聊、群聊、好友管理、多媒体消息等核心功能。

## 二、技术架构

| 层次 | 技术选型 |
|------|---------|
| 通信层 | Netty WebSocket (netty-spring-boot-starter) |
| 应用层 | Spring Boot 3.x |
| 鉴权 | JWT (jjwt) |
| 持久层 | MyBatis-Plus + PostgreSQL |
| 文件存储 | PostgreSQL BYTEA |
| 前端 | Vue 3 (CDN) + 单页 HTML |

## 三、功能模块

### 模块 1：用户认证（JWT）

| 编号 | 功能 | 说明 |
|------|------|------|
| 1.1 | 用户注册 | 用户名+密码+昵称，密码 BCrypt 加密 |
| 1.2 | 用户登录 | 返回 JWT Token（含 userId、nickname） |
| 1.3 | Token 刷新 | 支持 Token 过期前刷新 |
| 1.4 | WebSocket 鉴权 | 连接时 URL 参数携带 JWT，服务端验证签名和过期时间 |
| 1.5 | 用户信息查询 | 查询自己和其他用户的基本信息 |
| 1.6 | 修改个人信息 | 修改昵称、头像 |

### 模块 2：好友管理

| 编号 | 功能 | 说明 |
|------|------|------|
| 2.1 | 搜索用户 | 按用户名或昵称模糊搜索 |
| 2.2 | 发送好友申请 | 发送申请，附带验证消息 |
| 2.3 | 处理好友申请 | 同意/拒绝，同意后双向建立好友关系 |
| 2.4 | 好友列表 | 查询好友列表，含在线状态 |
| 2.5 | 删除好友 | 双向删除好友关系 |
| 2.6 | 好友申请通知 | 实时 WebSocket 推送好友申请通知 |

### 模块 3：单聊

| 编号 | 功能 | 说明 |
|------|------|------|
| 3.1 | 文本消息 | 发送/接收文本消息 |
| 3.2 | 图片消息 | 发送/接收图片，支持预览和下载 |
| 3.3 | 文件消息 | 发送/接收文件，显示文件名和大小 |
| 3.4 | 音频消息 | 发送/接收音频，支持浏览器录音 |
| 3.5 | 消息持久化 | 所有消息存入数据库 |
| 3.6 | 离线消息 | 用户上线后拉取未读消息 |
| 3.7 | 消息已读回执 | 标记消息已读，通知发送方 |
| 3.8 | 历史消息 | 分页查询聊天记录 |

### 模块 4：群聊

| 编号 | 功能 | 说明 |
|------|------|------|
| 4.1 | 创建群组 | 指定群名称，创建者为群主 |
| 4.2 | 邀请入群 | 群主/管理员邀请好友入群 |
| 4.3 | 退出群组 | 成员主动退出 |
| 4.4 | 解散群组 | 群主解散群组 |
| 4.5 | 群成员列表 | 查看群成员及角色 |
| 4.6 | 群消息 | 支持文本/图片/文件/音频，广播给所有群成员 |
| 4.7 | 群消息历史 | 分页查询群聊记录 |
| 4.8 | 群公告 | 群主/管理员发布公告 |

### 模块 5：会话管理

| 编号 | 功能 | 说明 |
|------|------|------|
| 5.1 | 会话列表 | 最近聊天列表，按最后消息时间排序 |
| 5.2 | 未读计数 | 每个会话的未读消息数 |
| 5.3 | 清除未读 | 进入会话时清除未读计数 |
| 5.4 | 删除会话 | 删除会话记录（不删消息） |

### 模块 6：文件管理

| 编号 | 功能 | 说明 |
|------|------|------|
| 6.1 | 文件上传 | REST API 上传，存入 PG BYTEA |
| 6.2 | 文件下载 | 根据 fileId 下载 |
| 6.3 | 图片预览 | 直接返回图片二进制流 |
| 6.4 | 音频播放 | 直接返回音频流 |
| 6.5 | 头像上传 | 用户头像上传和更新 |

## 四、数据库设计

### 4.1 用户表 (chat_user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 用户ID |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password | VARCHAR(128) | BCrypt 加密密码 |
| nickname | VARCHAR(100) | 昵称 |
| avatar_file_id | BIGINT | 头像文件ID |
| status | SMALLINT | 1-正常 0-禁用 |
| last_login_time | TIMESTAMP | 最后登录时间 |
| create_time | TIMESTAMP | 创建时间 |
| update_time | TIMESTAMP | 更新时间 |

### 4.2 好友关系表 (chat_friendship)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 主键 |
| user_id | BIGINT | 用户ID |
| friend_id | BIGINT | 好友ID |
| status | SMALLINT | 0-申请中 1-已通过 2-已拒绝 |
| request_msg | VARCHAR(200) | 验证消息 |
| create_time | TIMESTAMP | 创建时间 |
| update_time | TIMESTAMP | 更新时间 |

### 4.3 群组表 (chat_group)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 群组ID |
| name | VARCHAR(100) | 群名称 |
| avatar_file_id | BIGINT | 群头像文件ID |
| owner_id | BIGINT | 群主ID |
| notice | TEXT | 群公告 |
| max_members | INT | 最大成员数 |
| status | SMALLINT | 1-正常 0-已解散 |
| create_time | TIMESTAMP | 创建时间 |
| update_time | TIMESTAMP | 更新时间 |

### 4.4 群成员表 (chat_group_member)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 主键 |
| group_id | BIGINT | 群组ID |
| user_id | BIGINT | 用户ID |
| role | SMALLINT | 0-成员 1-管理员 2-群主 |
| nickname | VARCHAR(100) | 群内昵称 |
| join_time | TIMESTAMP | 加入时间 |

### 4.5 文件表 (chat_file)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 文件ID |
| file_name | VARCHAR(255) | 原始文件名 |
| content_type | VARCHAR(128) | MIME类型 |
| file_size | BIGINT | 文件大小(字节) |
| file_data | BYTEA | 文件二进制数据 |
| uploader_id | BIGINT | 上传者ID |
| create_time | TIMESTAMP | 创建时间 |

### 4.6 消息表 (chat_message)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 消息ID |
| sender_id | BIGINT | 发送者ID |
| receiver_id | BIGINT | 接收者ID（单聊时为用户ID） |
| group_id | BIGINT | 群组ID（群聊时使用） |
| chat_type | VARCHAR(10) | PRIVATE-单聊 GROUP-群聊 |
| msg_type | VARCHAR(20) | TEXT/IMAGE/FILE/AUDIO/SYSTEM |
| content | TEXT | 消息内容 |
| file_id | BIGINT | 关联文件ID |
| status | SMALLINT | 0-未读 1-已读 2-已撤回 |
| create_time | TIMESTAMP | 创建时间 |

### 4.7 会话表 (chat_conversation)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL PK | 主键 |
| user_id | BIGINT | 所属用户ID |
| target_id | BIGINT | 对方用户ID或群组ID |
| chat_type | VARCHAR(10) | PRIVATE-单聊 GROUP-群聊 |
| last_msg_id | BIGINT | 最后一条消息ID |
| last_msg_content | VARCHAR(200) | 最后消息摘要 |
| last_msg_time | TIMESTAMP | 最后消息时间 |
| unread_count | INT | 未读消息数 |
| status | SMALLINT | 1-正常 0-已删除 |
| create_time | TIMESTAMP | 创建时间 |
| update_time | TIMESTAMP | 更新时间 |

## 五、WebSocket 消息协议

### 5.1 消息格式（JSON）

```json
{
  "action": "CHAT_MSG | FRIEND_REQUEST | FRIEND_ACCEPT | READ_RECEIPT | SYSTEM",
  "chatType": "PRIVATE | GROUP",
  "targetId": 123,
  "msgType": "TEXT | IMAGE | FILE | AUDIO",
  "content": "消息内容",
  "fileId": 456,
  "fileName": "xxx.png",
  "timestamp": 1709827200000
}
```

### 5.2 服务端推送消息格式

```json
{
  "action": "CHAT_MSG | FRIEND_REQUEST | FRIEND_RESPONSE | READ_RECEIPT | SYSTEM | ONLINE_STATUS",
  "chatType": "PRIVATE | GROUP",
  "senderId": 1,
  "senderNickname": "Alice",
  "targetId": 2,
  "groupId": null,
  "msgType": "TEXT",
  "content": "Hello",
  "messageId": 789,
  "fileId": null,
  "timestamp": 1709827200000
}
```

## 六、REST API 列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |
| POST | /api/auth/refresh | 刷新 Token |
| GET | /api/user/info | 获取当前用户信息 |
| GET | /api/user/search?keyword= | 搜索用户 |
| PUT | /api/user/profile | 修改个人信息 |
| POST | /api/friend/request | 发送好友申请 |
| GET | /api/friend/requests | 获取好友申请列表 |
| POST | /api/friend/accept/{id} | 同意好友申请 |
| POST | /api/friend/reject/{id} | 拒绝好友申请 |
| GET | /api/friend/list | 好友列表 |
| DELETE | /api/friend/{friendId} | 删除好友 |
| POST | /api/group/create | 创建群组 |
| POST | /api/group/{groupId}/invite | 邀请入群 |
| POST | /api/group/{groupId}/leave | 退出群组 |
| DELETE | /api/group/{groupId} | 解散群组 |
| GET | /api/group/{groupId}/members | 群成员列表 |
| PUT | /api/group/{groupId}/notice | 更新群公告 |
| GET | /api/group/list | 我的群组列表 |
| POST | /api/file/upload | 文件上传 |
| GET | /api/file/download/{fileId} | 文件下载 |
| GET | /api/file/preview/{fileId} | 文件预览 |
| GET | /api/message/history | 聊天记录（分页） |
| GET | /api/conversation/list | 会话列表 |
| PUT | /api/conversation/{id}/read | 清除未读 |
| DELETE | /api/conversation/{id} | 删除会话 |

## 七、Netty Starter 功能覆盖

| Starter 功能 | 使用场景 |
|-------------|---------|
| @WebSocketEndpoint | 聊天端点 /ws/chat |
| @OnOpen | 用户上线，推送在线状态 |
| @OnMessage | 处理文本 JSON 消息 |
| @OnBinaryMessage | 处理二进制文件传输 |
| @OnClose | 用户下线，推送离线状态 |
| @OnError | 异常处理和日志 |
| WebSocketAuthenticator | JWT Token 验证 |
| WebSocketSession | 会话管理、消息发送 |
| session.sendMessage() | 单聊推送 |
| session.broadcastAll() | 群聊广播 |
| session.setAttribute() | 存储用户上下文 |
| MessageFilter | 消息过滤（敏感词等） |
| 心跳检测 | 保持连接活跃 |
| 最大连接数限制 | 服务端保护 |

## 八、实现计划

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| P1 | JWT 鉴权改造 + 密码加密 | 高 |
| P2 | 数据库 Schema 升级 | 高 |
| P3 | 好友管理（搜索、申请、同意、列表） | 高 |
| P4 | 单聊（文本/图片/文件/音频 + 离线消息） | 高 |
| P5 | 群聊（创建/邀请/消息广播） | 高 |
| P6 | 会话管理（列表、未读计数） | 中 |
| P7 | 消息已读回执 | 中 |
| P8 | 前端页面重构（完整 IM UI） | 高 |
| P9 | 编译测试验证 | 高 |
