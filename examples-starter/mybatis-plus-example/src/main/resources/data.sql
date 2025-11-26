-- 插入测试数据
INSERT INTO user_info (username, email, age, department) VALUES
('张三', 'zhangsan@example.com', 25, '技术部'),
('李四', 'lisi@example.com', 30, '产品部'),
('王五', 'wangwu@example.com', 28, '技术部'),
('赵六', 'zhaoliu@example.com', 32, '市场部'),
('钱七', 'qianqi@example.com', 27, '技术部'),
('孙八', 'sunba@example.com', 29, '产品部'),
('周九', 'zhoujiu@example.com', 26, '市场部'),
('吴十', 'wushi@example.com', 31, '技术部');

-- 插入产品测试数据
INSERT INTO product (name, price, stock, version, status) VALUES
('Apple MacBook Pro', 12999.00, 50, 0, 1),
('华为 MateBook', 6999.00, 100, 0, 1),
('小米 13 Pro', 4999.00, 200, 0, 1),
('索尼 WH-1000XM5 耳机', 2399.00, 150, 0, 1),
('罗技 MX Master 3S 鼠标', 799.00, 300, 0, 1),
('戴尔 U2723DE 显示器', 3999.00, 80, 0, 1),
('联想 ThinkPad X1', 9999.00, 60, 0, 1),
('三星 Galaxy Tab S9', 5999.00, 0, 0, 2),
('惠普 战66', 4599.00, 120, 0, 1),
('Apple AirPods Pro 2', 1899.00, 0, 0, 3);
