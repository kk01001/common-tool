package io.github.kk01001.xxljob.config;

import cn.hutool.core.collection.CollUtil;
import io.github.kk01001.xxljob.annotations.XxlJobRegister;
import io.github.kk01001.xxljob.model.XxlJobGroup;
import io.github.kk01001.xxljob.model.XxlJobInfo;
import io.github.kk01001.xxljob.service.XxlJobGroupService;
import io.github.kk01001.xxljob.service.XxlJobInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * date 2023-01-23 16:01:00
 * 自动注册执行器和添加@Xxljob任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "xxl-job", name = {"adminAddresses", "title", "userName", "password"})
public class XxlJobAutoRegister implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final XxlJobGroupService xxlJobGroupService;

    private final XxlJobInfoService xxlJobInfoService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // 注册执行器
            addJobGroup();
            // 注册任务
            addJobInfo();
        } catch (Exception e) {
            log.error("xxl-job auto register error: ", e);
        }
    }

    /**
     * 自动注册group
     */
    private void addJobGroup() {
        if (xxlJobGroupService.preciselyCheck()) {
            return;
        }

        if (xxlJobGroupService.autoRegisterGroup()) {
            log.info("auto register xxl-job group success!");
        }
    }

    private void addJobInfo() {
        List<XxlJobGroup> jobGroups = xxlJobGroupService.getJobGroup();
        if (CollUtil.isEmpty(jobGroups)) {
            return;
        }
        XxlJobGroup xxlJobGroup = jobGroups.get(0);
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanDefinitionName);
            } catch (BeansException e) {
                continue;
            }
            Map<Method, XxlJob> annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                    (MethodIntrospector.MetadataLookup<XxlJob>) method ->
                            AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class));
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();

                // 自动注册
                if (executeMethod.isAnnotationPresent(XxlJobRegister.class)) {
                    XxlJobRegister xxlJobRegister = executeMethod.getAnnotation(XxlJobRegister.class);
                    List<XxlJobInfo> jobInfo = xxlJobInfoService.getJobInfo(xxlJobGroup.getId(), xxlJob.value());
                    if (!jobInfo.isEmpty()) {
                        // executor_handler 条件为模糊查询, 需再过滤
                        Optional<XxlJobInfo> first = jobInfo.stream()
                                .filter(xxlJobInfo -> xxlJobInfo.getExecutorHandler().equals(xxlJob.value()))
                                .findFirst();
                        if (first.isPresent()) {
                            log.info("xxl-job executor_handler: {}, already add", xxlJob.value());
                            continue;
                        }
                    }

                    XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, xxlJob, xxlJobRegister);
                    Integer jobInfoId = xxlJobInfoService.addJobInfo(xxlJobInfo);
                    log.info("xxl-job executor_handler: {}, add success, jobInfoId: {}", xxlJob.value(), jobInfoId);
                }
            }
        }
    }

    private XxlJobInfo createXxlJobInfo(XxlJobGroup xxlJobGroup, XxlJob xxlJob, XxlJobRegister xxlJobRegister) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobGroup.getId());
        xxlJobInfo.setJobDesc(xxlJobRegister.jobDesc());
        xxlJobInfo.setAuthor(xxlJobRegister.author());
        xxlJobInfo.setScheduleType("CRON");
        xxlJobInfo.setScheduleConf(xxlJobRegister.cron());
        xxlJobInfo.setGlueType("BEAN");
        xxlJobInfo.setExecutorHandler(xxlJob.value());
        xxlJobInfo.setExecutorRouteStrategy(xxlJobRegister.executorRouteStrategy().name());
        xxlJobInfo.setMisfireStrategy(xxlJobRegister.misfireStrategy().name());
        xxlJobInfo.setExecutorBlockStrategy(xxlJobRegister.executorBlockStrategy().name());
        xxlJobInfo.setExecutorTimeout(xxlJobRegister.executorTimeout());
        xxlJobInfo.setExecutorFailRetryCount(xxlJobRegister.executorFailRetryCount());
        xxlJobInfo.setGlueRemark("GLUE代码初始化");
        xxlJobInfo.setExecutorParam(xxlJobRegister.executorParam());
        xxlJobInfo.setTriggerStatus(xxlJobRegister.triggerStatus());
        return xxlJobInfo;
    }
}
