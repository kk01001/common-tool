# MySQL MCP (Metadata Control Panel)

这是一个简单的 MySQL 元数据查询工具，可以帮助你快速查看数据库中的表结构、字段信息等。

## 功能特点

- 列出数据库中的所有表
- 查看指定表的详细结构
- 查询表数据（支持限制返回条数）
- 使用环境变量配置数据库连接信息
- 支持自定义数据库端口

## 安装依赖

```bash
pip install -r requirements.txt
```

## 配置

1. 复制 `.env.example` 文件为 `.env`
2. 修改 `.env` 文件中的数据库连接信息：
   - MYSQL_HOST：数据库主机地址
   - MYSQL_PORT：数据库端口号（默认3306）
   - MYSQL_USER：数据库用户名
   - MYSQL_PASSWORD：数据库密码
   - MYSQL_DATABASE：数据库名称

## 使用方法

运行以下命令启动程序：

```bash
python mysql_mcp.py
```

程序启动后，你可以：

1. 选择 "1" 列出所有表
2. 选择 "2" 查看指定表的结构
3. 选择 "3" 查询表数据
4. 选择 "4" 退出程序

## 注意事项

- 确保已正确配置数据库连接信息
- 查询数据时默认限制返回 10 条记录
- 所有查询结果都会以表格形式展示
- 如果使用非标准端口，请确保在 .env 文件中正确配置 MYSQL_PORT 