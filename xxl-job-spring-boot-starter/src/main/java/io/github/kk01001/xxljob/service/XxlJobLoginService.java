package io.github.kk01001.xxljob.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.github.kk01001.xxljob.constants.XxlJobConstants;
import io.github.kk01001.xxljob.core.XxlJobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author kk01001
 * date 2023-02-02 14:03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XxlJobLoginService {

    private final XxlJobProperties xxlJobProperties;

    private final Map<String, String> LOGIN_COOKIE = new HashMap<>();

    /**
     * 登录获取cookie
     */
    public void login() {

        if (StrUtil.isEmpty(xxlJobProperties.getUserName())) {
            log.error("xxl-job properties userName not set!");
            return;
        }
        if (StrUtil.isEmpty(xxlJobProperties.getPassword())) {
            log.error("xxl-job properties password not set!");
            return;
        }

        String loginUrl = xxlJobProperties.getAdminAddresses() + XxlJobConstants.LOGIN_URI;
        try (HttpResponse response = HttpRequest.post(loginUrl)
                .form("userName", xxlJobProperties.getUserName())
                .form("password", xxlJobProperties.getPassword())
                .execute()) {
            List<HttpCookie> cookies = response.getCookies();
            Optional<HttpCookie> cookieOpt = cookies.stream()
                    .filter(cookie -> XxlJobConstants.XXL_JOB_LOGIN_IDENTITY.equals(cookie.getName()))
                    .findFirst();
            if (!cookieOpt.isPresent()) {
                log.error("get xxl-job cookie error!");
                return;
            }

            LOGIN_COOKIE.put(XxlJobConstants.XXL_JOB_LOGIN_IDENTITY, cookieOpt.get().getValue());
        } catch (Exception e) {
            log.error("xxl-job loginUrl: {}, request error: ", loginUrl, e);
        }
    }

    /**
     * 从map中获取cookie
     */
    public String getCookie() {
        for (int i = 0; i < XxlJobConstants.RETRY_GET_COOKIE; i++) {
            String cookieStr = LOGIN_COOKIE.get(XxlJobConstants.XXL_JOB_LOGIN_IDENTITY);
            if (cookieStr != null) {
                return XxlJobConstants.XXL_JOB_LOGIN_IDENTITY + "=" + cookieStr;
            }
            login();
        }
        log.error("get xxl-job cookie error!");
        return "";
    }

}
