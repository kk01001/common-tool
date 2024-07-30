package io.github.kk01001.xxljob.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.xxljob.constants.XxlJobConstants;
import io.github.kk01001.xxljob.core.XxlJobProperties;
import io.github.kk01001.xxljob.model.XxlJobGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kk01001
 * date 2023-02-02 14:03
 * 执行器管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XxlJobGroupService {

    private final XxlJobLoginService xxlJobLoginService;

    private final XxlJobProperties xxlJobProperties;

    /**
     * 获取所有执行器
     */
    public List<XxlJobGroup> getJobGroup() {
        String url = xxlJobProperties.getAdminAddresses() + XxlJobConstants.JOB_GROUP_PAGE_LIST;
        try (HttpResponse response = HttpRequest.post(url)
                .form("appname", xxlJobProperties.getAppName())
                .form("title", xxlJobProperties.getTitle())
                .cookie(xxlJobLoginService.getCookie())
                .timeout(3000)
                .execute()) {
            String body = response.body();
            JSONArray array = JSONUtil.parse(body).getByPath("data", JSONArray.class);
            return array.stream()
                    .map(o -> JSONUtil.toBean((JSONObject) o, XxlJobGroup.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("xxl-job job group page list request error: ", e);
        }
        return ListUtil.empty();
    }

    /**
     * 检查当前服务是否已经注册
     *
     * @return 是否注册
     */
    public boolean preciselyCheck() {
        List<XxlJobGroup> jobGroup = getJobGroup();
        Optional<XxlJobGroup> has = jobGroup.stream()
                .filter(xxlJobGroup -> xxlJobGroup.getAppname().equals(xxlJobProperties.getAppName())
                        && xxlJobGroup.getTitle().equals(xxlJobProperties.getTitle()))
                .findAny();
        return has.isPresent();
    }

    /**
     * 添加当前服务为执行器
     */
    public boolean autoRegisterGroup() {
        String url = xxlJobProperties.getAdminAddresses() + XxlJobConstants.JOB_GROUP_SAVE;
        try (HttpResponse response = HttpRequest.post(url)
                .form("appname", xxlJobProperties.getAppName())
                .form("title", xxlJobProperties.getTitle())
                .cookie(xxlJobLoginService.getCookie())
                .timeout(3000)
                .execute()) {
            Object code = JSONUtil.parse(response.body()).getByPath("code");
            return code.equals(200);
        } catch (Exception e) {
            log.error("xxl-job job group save: {}, request error: ", url, e);
        }
        return false;
    }

}
