-- 本地消息表结构
-- 支持MySQL、PostgreSQL等数据库

-- MySQL版本
CREATE TABLE `local_message`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `business_type`   VARCHAR(50)  NOT NULL COMMENT '业务类型',
    `business_id`     VARCHAR(100) NOT NULL COMMENT '业务ID',
    `message_content` TEXT         NOT NULL COMMENT '消息内容，JSON格式',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '消息状态：0-待处理，1-处理中，2-处理成功，3-处理失败，4-达到最大重试次数',
    `retry_count`     INT          NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    `max_retry_count` INT          NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `next_retry_time` DATETIME     NULL COMMENT '下次重试时间',
    `error_message`   TEXT         NULL COMMENT '错误信息',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `process_time`    BIGINT       NULL COMMENT '处理时间',
    `ext_data`        TEXT         NULL COMMENT '扩展字段，JSON格式',
    `version`         INT          NOT NULL DEFAULT 0 COMMENT '版本号，用于乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_business_type_id` (`business_type`, `business_id`),
    INDEX `idx_status_create_time` (`status`, `create_time`),
    INDEX `idx_retry_time` (`next_retry_time`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='本地消息表';

-- PostgreSQL版本
/*
CREATE TABLE local_message (
    id BIGSERIAL PRIMARY KEY,
    business_type VARCHAR(50) NOT NULL,
    business_id VARCHAR(100) NOT NULL,
    message_content TEXT NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 3,
    next_retry_time TIMESTAMP NULL,
    error_message TEXT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    process_time BIGINT NULL,
    ext_data TEXT NULL,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_business_type_id ON local_message (business_type, business_id);
CREATE INDEX idx_status_create_time ON local_message (status, create_time);
CREATE INDEX idx_retry_time ON local_message (next_retry_time);
CREATE INDEX idx_create_time ON local_message (create_time);

COMMENT ON TABLE local_message IS '本地消息表';
COMMENT ON COLUMN local_message.id IS '主键ID';
COMMENT ON COLUMN local_message.business_type IS '业务类型';
COMMENT ON COLUMN local_message.business_id IS '业务ID';
COMMENT ON COLUMN local_message.message_content IS '消息内容，JSON格式';
COMMENT ON COLUMN local_message.status IS '消息状态：0-待处理，1-处理中，2-处理成功，3-处理失败，4-达到最大重试次数';
COMMENT ON COLUMN local_message.retry_count IS '当前重试次数';
COMMENT ON COLUMN local_message.max_retry_count IS '最大重试次数';
COMMENT ON COLUMN local_message.next_retry_time IS '下次重试时间';
COMMENT ON COLUMN local_message.error_message IS '错误信息';
COMMENT ON COLUMN local_message.create_time IS '创建时间';
COMMENT ON COLUMN local_message.update_time IS '更新时间';
COMMENT ON COLUMN local_message.process_time IS '处理时间';
COMMENT ON COLUMN local_message.ext_data IS '扩展字段，JSON格式';
COMMENT ON COLUMN local_message.version IS '版本号，用于乐观锁';
*/
