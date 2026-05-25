package io.github.archer099.examples.sql;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

public class SqlConditionAdder {
    public static String addWhereCondition(String originalSql, String newCondition) throws Exception {
        // 解析原始SQL
        Statement statement = CCJSqlParserUtil.parse(originalSql);

        if (statement instanceof Select select) {
            // PlainSelect plainSelect = select.getPlainSelect();
            //
            // // 获取现有WHERE条件
            // Expression where = plainSelect.getWhere();
            //
            // // 解析新条件
            // Expression newExpr = CCJSqlParserUtil.parseCondExpression(newCondition);
            //
            // // 组合条件
            // if (where != null) {
            //     // 已有WHERE，使用AND连接
            //     plainSelect.setWhere(new AndExpression(where, newExpr));
            // } else {
            //     // 没有WHERE，直接设置
            //     plainSelect.setWhere(newExpr);
            // }
            // return plainSelect.toString();
        }

        throw new IllegalArgumentException("Only SELECT statements are supported");
    }

    public static void main(String[] args) throws Exception {
        String originalSql = "SELECT * FROM users WHERE age > 18";
        String newCondition = "status = 'active'";

        String modifiedSql = addWhereCondition(originalSql, newCondition);
        System.out.println(modifiedSql);
        // 输出: SELECT * FROM users WHERE age > 18 AND status = 'active'
    }
}