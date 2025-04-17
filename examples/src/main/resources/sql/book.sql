-- 创建图书表
CREATE TABLE IF NOT EXISTS `t_book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `book_name` varchar(100) NOT NULL COMMENT '图书名称',
  `author` varchar(50) NOT NULL COMMENT '作者',
  `price` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '价格',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `dept_id` bigint(20) NOT NULL COMMENT '所属部门ID',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 插入测试数据
INSERT INTO `t_book` (`book_name`, `author`, `price`, `publish_time`, `category`, `stock`, `dept_id`, `create_user`, `create_time`, `update_time`, `deleted`) VALUES
('Java编程思想', '埃克尔', 108.00, '2019-01-01 00:00:00', '计算机科学', 100, 1, 1, NOW(), NOW(), 0),
('Spring实战', '沃尔斯', 89.00, '2020-03-15 00:00:00', '计算机科学', 80, 1, 1, NOW(), NOW(), 0),
('算法导论', '科曼', 128.00, '2018-05-20 00:00:00', '计算机科学', 50, 2, 2, NOW(), NOW(), 0),
('数据结构与算法分析', '韦斯', 78.50, '2017-11-10 00:00:00', '计算机科学', 60, 2, 2, NOW(), NOW(), 0),
('深入理解计算机系统', '布莱恩特', 139.00, '2016-08-25 00:00:00', '计算机科学', 40, 3, 3, NOW(), NOW(), 0),
('Redis设计与实现', '黄健宏', 69.00, '2021-02-18 00:00:00', '数据库', 70, 3, 3, NOW(), NOW(), 0),
('MySQL技术内幕', '姜承尧', 89.00, '2020-09-30 00:00:00', '数据库', 55, 1, 1, NOW(), NOW(), 0),
('高性能MySQL', 'Baron Schwartz', 119.00, '2021-04-12 00:00:00', '数据库', 45, 2, 2, NOW(), NOW(), 0),
('Java并发编程实战', 'Brian Goetz', 99.00, '2019-07-05 00:00:00', '编程语言', 65, 3, 3, NOW(), NOW(), 0),
('Effective Java', 'Joshua Bloch', 85.00, '2018-12-15 00:00:00', '编程语言', 75, 1, 1, NOW(), NOW(), 0); 