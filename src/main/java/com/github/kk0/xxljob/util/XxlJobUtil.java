package com.github.kk0.xxljob.util;

import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-02-27 9:23
 */
@Slf4j
public class XxlJobUtil<T> {

    /**
     * 分片
     */
    public static <T> List<T> getShardList(List<T> list) {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("分片总数: {}, 当前位置: {}", shardTotal, shardIndex);
        List<T> shardList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i % shardTotal == shardIndex) {
                shardList.add(list.get(i));
            }
        }
        if (shardIndex == -1) {
            shardList = list;
        }
        return shardList;
    }
}
