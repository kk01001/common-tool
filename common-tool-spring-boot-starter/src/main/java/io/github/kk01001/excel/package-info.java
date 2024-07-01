/**
 * @author kk01001
 * date:  2024-06-24 9:17
 * 1. 收到写请求, 放入队列
 * 2. 队列实现
 * 2.1 本地内存队列等,
 * 2.2 redis队列
 * 2.3 mq
 * 3. 启一个消费线程
 * 4. while循环, 无任务sleep
 * 5. 数据来源, 交由用户自定义实现
 */
package io.github.kk01001.excel;
