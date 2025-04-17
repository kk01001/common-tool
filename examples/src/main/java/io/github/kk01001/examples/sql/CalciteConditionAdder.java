package io.github.kk01001.examples.sql;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;

public class CalciteConditionAdder {
    public static String addCondition(String sql, String condition) throws Exception {
        SqlParser parser = SqlParser.create(sql);
        SqlNode sqlNode = parser.parseStmt();

        // if (sqlNode instanceof SqlSelect) {
        //     SqlSelect select = (SqlSelect) sqlNode;
        //     SqlNode where = select.getWhere();
        //
        //     SqlParser condParser = SqlParser.create(condition);
        //     SqlNode newCondition = condParser.parseExpression();
        //
        //     if (where != null) {
        //         select.setWhere(SqlStdOperatorTable.AND.createCall(where, newCondition));
        //     } else {
        //         select.setWhere(newCondition);
        //     }
        //
        //     return select.toString();
        // }

        throw new IllegalArgumentException("Only SELECT statements are supported");
    }
}