-- 用户表
DROP TABLE IF EXISTS user_info;

CREATE TABLE user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    age INT,
    department VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_username ON user_info(username);
CREATE INDEX idx_department ON user_info(department);

-- 产品表
DROP TABLE IF EXISTS product;

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10, 2),
    stock INT DEFAULT 0,
    version INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_product_status ON product(status);
