package io.github.kk01001.xxljob.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.xxljob.constants.XxlJobConstants;
import io.github.kk01001.xxljob.core.XxlJobProperties;
import io.github.kk01001.xxljob.model.XxlJobInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kk01001
 * date 2023-02-02 14:03
 * 任务管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XxlJobInfoService {

    private final XxlJobProperties xxlJobProperties;

    private final XxlJobLoginService xxlJobLoginService;

    /**
     * 获取当前服务所属执行器下的任务列表
     *
     * @param jobGroupId      执行器id
     * @param executorHandler 任务jobHandler名称
     */
    public List<XxlJobInfo> getJobInfo(Integer jobGroupId, String executorHandler) {
        String url = xxlJobProperties.getAdminAddresses() + XxlJobConstants.JOB_INFO_PAGE_LIST;
        try (HttpResponse response = HttpRequest.post(url)
                .form("jobGroup", jobGroupId)
                .form("executorHandler", executorHandler)
                .form("triggerStatus", -1)
                .cookie(xxlJobLoginService.getCookie())
                .execute()) {
            String body = response.body();
            JSONArray array = JSONUtil.parse(body).getByPath("data", JSONArray.class);
            return array.stream()
                    .map(o -> JSONUtil.toBean((JSONObject) o, XxlJobInfo.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("xxl-job job info page list: {} request error: ", url, e);
        }
        return ListUtil.empty();
    }

    /**
     * 添加任务
     *
     * @param xxlJobInfo 任务信息
     */
    public Integer addJobInfo(XxlJobInfo xxlJobInfo) {
        String url = xxlJobProperties.getAdminAddresses() + XxlJobConstants.JOB_INFO_ADD;
        Map<String, Object> paramMap = BeanUtil.beanToMap(xxlJobInfo);
        try (HttpResponse response = HttpRequest.post(url)
                .form(paramMap)
                .cookie(xxlJobLoginService.getCookie())
                .execute()) {
            JSON json = JSONUtil.parse(response.body());
            Object code = json.getByPath("code");
            if (code.equals(HttpStatus.HTTP_OK)) {
                return Convert.toInt(json.getByPath("content"));
            }
        } catch (Exception e) {
            log.error("xxl job info add request fail: {}, url: {}", paramMap, url, e);
        }
        return -1;
    }

}
