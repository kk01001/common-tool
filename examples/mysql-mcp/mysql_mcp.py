#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
@author archer099
@date 2024-03-14 10:00:00
@description MySQL 元数据查询工具
"""

import os
import json
import sys
from datetime import datetime
import mysql.connector
from mysql.connector import Error
from tabulate import tabulate
from dotenv import load_dotenv

class DateTimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, datetime):
            return obj.strftime('%Y-%m-%d %H:%M:%S')
        return super().default(obj)

class MySQLMCP:
    def __init__(self):
        """
        初始化 MySQL MCP
        """
        load_dotenv()
        self.connection = None
        self.connect()

    def connect(self):
        """
        连接到 MySQL 数据库
        """
        try:
            self.connection = mysql.connector.connect(
                host=os.getenv('MYSQL_HOST', 'localhost'),
                port=int(os.getenv('MYSQL_PORT', 3306)),
                user=os.getenv('MYSQL_USER', 'root'),
                password=os.getenv('MYSQL_PASSWORD', ''),
                database=os.getenv('MYSQL_DATABASE', '')
            )
            return {"success": True, "message": f"成功连接到 MySQL 数据库 {os.getenv('MYSQL_HOST')}:{os.getenv('MYSQL_PORT')}"}
        except Error as e:
            return {"success": False, "message": f"连接数据库时出错: {e}"}

    def list_tables(self):
        """
        列出所有表
        """
        try:
            cursor = self.connection.cursor()
            cursor.execute("SHOW TABLES")
            tables = cursor.fetchall()
            table_list = [table[0] for table in tables]
            return {"success": True, "data": table_list}
        except Error as e:
            return {"success": False, "message": f"获取表列表时出错: {e}"}
        finally:
            cursor.close()

    def describe_table(self, table_name):
        """
        描述表结构
        """
        try:
            cursor = self.connection.cursor()
            cursor.execute(f"DESCRIBE {table_name}")
            columns = cursor.fetchall()
            column_info = []
            for col in columns:
                column_info.append({
                    "field": col[0],
                    "type": col[1],
                    "null": col[2],
                    "key": col[3],
                    "default": col[4],
                    "extra": col[5]
                })
            return {"success": True, "data": column_info}
        except Error as e:
            return {"success": False, "message": f"获取表结构时出错: {e}"}
        finally:
            cursor.close()

    def query_data(self, table_name, limit=10):
        """
        查询表数据
        """
        try:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(f"SELECT * FROM {table_name} LIMIT {limit}")
            rows = cursor.fetchall()
            return {"success": True, "data": rows}
        except Error as e:
            return {"success": False, "message": f"查询数据时出错: {e}"}
        finally:
            cursor.close()

    def close(self):
        """
        关闭数据库连接
        """
        if self.connection and self.connection.is_connected():
            self.connection.close()

def handle_command(args):
    """
    处理命令行参数
    """
    try:
        mcp = MySQLMCP()
        action = args[0] if len(args) > 0 else None
        params = args[1:] if len(args) > 1 else []

        if action == "list_tables":
            result = mcp.list_tables()
        elif action == "describe_table":
            if not params:
                return {"success": False, "message": "缺少表名参数"}
            result = mcp.describe_table(params[0])
        elif action == "query_data":
            if not params:
                return {"success": False, "message": "缺少表名参数"}
            limit = int(params[1]) if len(params) > 1 else 10
            result = mcp.query_data(params[0], limit)
        else:
            result = {
                "success": False, 
                "message": """未知的命令。支持的命令：
1. list_tables
2. describe_table <表名>
3. query_data <表名> [限制条数]"""
            }

        mcp.close()
        return result
    except Exception as e:
        return {"success": False, "message": str(e)}

def main():
    """
    主函数 - 作为 MCP 服务运行
    """
    if len(sys.argv) > 1:
        result = handle_command(sys.argv[1:])
        print(json.dumps(result, ensure_ascii=False, cls=DateTimeEncoder))
    else:
        print(json.dumps({
            "success": False, 
            "message": """使用方法：
1. list_tables
2. describe_table <表名>
3. query_data <表名> [限制条数]"""
        }, ensure_ascii=False))

if __name__ == "__main__":
    main() 