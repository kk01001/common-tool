-- ====================== 用户表 ======================
CREATE TABLE IF NOT EXISTS chat_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(128) NOT NULL,
    nickname        VARCHAR(100) NOT NULL,
    avatar_file_id  BIGINT,
    status          SMALLINT     NOT NULL DEFAULT 1,
    last_login_time TIMESTAMP,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ====================== 好友关系表 ======================
CREATE TABLE IF NOT EXISTS chat_friendship (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    friend_id   BIGINT       NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 0,
    request_msg VARCHAR(200),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, friend_id)
);

CREATE INDEX IF NOT EXISTS idx_friendship_user ON chat_friendship(user_id);
CREATE INDEX IF NOT EXISTS idx_friendship_friend ON chat_friendship(friend_id);

-- ====================== 群组表 ======================
CREATE TABLE IF NOT EXISTS chat_group (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    avatar_file_id BIGINT,
    owner_id       BIGINT       NOT NULL,
    notice         TEXT,
    max_members    INT          NOT NULL DEFAULT 200,
    status         SMALLINT     NOT NULL DEFAULT 1,
    create_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ====================== 群成员表 ======================
CREATE TABLE IF NOT EXISTS chat_group_member (
    id        BIGSERIAL PRIMARY KEY,
    group_id  BIGINT    NOT NULL,
    user_id   BIGINT    NOT NULL,
    role      SMALLINT  NOT NULL DEFAULT 0,
    nickname  VARCHAR(100),
    join_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_group_member_group ON chat_group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user ON chat_group_member(user_id);

-- ====================== 文件表 ======================
CREATE TABLE IF NOT EXISTS chat_file (
    id            BIGSERIAL PRIMARY KEY,
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(128) NOT NULL,
    file_size     BIGINT       NOT NULL,
    url           VARCHAR(500) NOT NULL,
    storage_type  VARCHAR(20)  NOT NULL DEFAULT 'local',
    uploader_id   BIGINT       NOT NULL,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_file_uploader ON chat_file(uploader_id);

-- ====================== 消息表 ======================
CREATE TABLE IF NOT EXISTS chat_message (
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT       NOT NULL,
    receiver_id BIGINT,
    group_id    BIGINT,
    chat_type   VARCHAR(10)  NOT NULL,
    msg_type    VARCHAR(20)  NOT NULL,
    content     TEXT,
    file_id     BIGINT,
    status      SMALLINT     NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_msg_sender ON chat_message(sender_id);
CREATE INDEX IF NOT EXISTS idx_msg_receiver ON chat_message(receiver_id);
CREATE INDEX IF NOT EXISTS idx_msg_group ON chat_message(group_id);
CREATE INDEX IF NOT EXISTS idx_msg_time ON chat_message(create_time DESC);
CREATE INDEX IF NOT EXISTS idx_msg_private ON chat_message(sender_id, receiver_id, create_time DESC);

-- ====================== 会话表 ======================
CREATE TABLE IF NOT EXISTS chat_conversation (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    target_id        BIGINT      NOT NULL,
    chat_type        VARCHAR(10) NOT NULL,
    last_msg_id      BIGINT,
    last_msg_content VARCHAR(200),
    last_msg_time    TIMESTAMP,
    unread_count     INT         NOT NULL DEFAULT 0,
    status           SMALLINT    NOT NULL DEFAULT 1,
    create_time      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, target_id, chat_type)
);

CREATE INDEX IF NOT EXISTS idx_conv_user ON chat_conversation(user_id, status, last_msg_time DESC);

-- ====================== 测试用户（密码 BCrypt 加密后的 123456）======================
INSERT INTO chat_user (username, password, nickname) VALUES
    ('alice',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice'),
    ('bob',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob'),
    ('charlie', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlie')
ON CONFLICT (username) DO NOTHING;
