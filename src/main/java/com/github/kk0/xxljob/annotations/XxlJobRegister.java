package com.github.kk0.xxljob.annotations;

import com.github.kk0.xxljob.enums.ExecutorBlockStrategyEnum;
import com.github.kk0.xxljob.enums.ExecutorRouteStrategyEnum;
import com.github.kk0.xxljob.enums.MisfireStrategyEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linshiqiang
 * date:  2023-02-02 14:03
 * xx-job任务配置信息
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XxlJobRegister {

    String cron() default "0 0 0 * * ? *";

    /**
     * 任务描述
     */
    String jobDesc() default "xxl job desc";

    /**
     * 作者
     */
    String author() default "admin";

    /**
     * 调度状态：0-停止，1-运行
     */
    int triggerStatus() default 0;

    /**
     * 执行器，任务参数
     */
    String executorParam() default "";

    /**
     * 路由策略
     */
    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.ROUND;

    /**
     * 阻塞处理策略
     */
    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.DISCARD_LATER;

    /**
     * 调度过期策略
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * 任务执行超时时间，单位秒
     */
    int executorTimeout() default 0;

    /**
     * 失败重试次数
     */
    int executorFailRetryCount() default 0;
}
