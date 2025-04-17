package io.github.kk01001.examples.sql;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

public class SqlConditionAdder {
    public static String addWhereCondition(String originalSql, String newCondition) throws Exception {
        // 解析原始SQL
        Statement statement = CCJSqlParserUtil.parse(originalSql);

        if (statement instanceof Select select) {
            net.sf.jsqlparser.statement.select.PlainSelect plainSelect = (net.sf.jsqlparser.statement.select.PlainSelect) select.getSelectBody();
            net.sf.jsqlparser.expression.Expression where = plainSelect.getWhere();
            net.sf.jsqlparser.expression.Expression newExpr = CCJSqlParserUtil.parseCondExpression(newCondition);
            if (where != null) {
                plainSelect.setWhere(new net.sf.jsqlparser.expression.operators.conditional.AndExpression(where, newExpr));
            } else {
                plainSelect.setWhere(newExpr);
            }
            return statement.toString();
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