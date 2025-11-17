package io.github.kk01001.mqtt.examples.service.impl;

import io.github.kk01001.mqtt.examples.config.EmqxAuthProperties;
import io.github.kk01001.mqtt.examples.dto.EmqxAuthRequestDTO;
import io.github.kk01001.mqtt.examples.service.EmqxAuthService;
import io.github.kk01001.mqtt.examples.vo.EmqxAuthResponseVO;
import io.github.kk01001.mqtt.examples.vo.EmqxAclRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author kk01001
 * @date 2025-11-17 15:45:00
 * @description EMQX HTTP 鉴权服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmqxAuthServiceImpl implements EmqxAuthService {

    private final EmqxAuthProperties authProperties;

    @Override
    public EmqxAuthResponseVO authenticate(EmqxAuthRequestDTO request) {
        // TODO 待验证
        log.info(" authenticate request: {}", request);
        EmqxAuthResponseVO resp = new EmqxAuthResponseVO();

        EmqxAuthProperties.User matched = null;
        if (authProperties.getUsers() != null) {
            for (EmqxAuthProperties.User u : authProperties.getUsers()) {
                if (u != null && Objects.equals(u.getUsername(), request.getUsername())) {
                    matched = u;
                    break;
                }
            }
        }

        if (matched != null && Objects.equals(matched.getPassword(), request.getPassword())) {
            resp.setResult("allow");
            resp.setIs_superuser(Boolean.TRUE.equals(matched.getSuperuser()));

            // 默认 ACL：订阅自己的 chat/{username}；允许发布到 chat/+（演示用途）
            String uname = StrUtil.blankToDefault(request.getUsername(), "");
            List<EmqxAclRuleVO> acl = new ArrayList<>();

            EmqxAclRuleVO subRule = new EmqxAclRuleVO();
            subRule.setPermission("allow");
            subRule.setAction("subscribe");
            subRule.setTopics(List.of("chat/" + uname));
            acl.add(subRule);

            EmqxAclRuleVO pubRule = new EmqxAclRuleVO();
            pubRule.setPermission("allow");
            pubRule.setAction("publish");
            pubRule.setTopics(List.of("chat/+"));
            acl.add(pubRule);

            // resp.setAcl(acl);
        } else {
            resp.setResult("deny");
            resp.setIs_superuser(false);
        }
        
        log.info(" authenticate response: {}", resp);
        return resp;
    }
}