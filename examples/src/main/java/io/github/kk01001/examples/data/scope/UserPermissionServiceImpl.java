package io.github.kk01001.examples.data.scope;

import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import io.github.kk01001.mybatis.permission.service.UserPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2025-04-16 15:49
 * @description
 */
@Service
public class UserPermissionServiceImpl implements UserPermissionService<Long> {

    @Override
    public Long getCurrentSelfId(DataColumn dataColumn) {
        return 3L;
    }

    @Override
    public List<Long> getSelfAndSubIds(DataColumn dataColumn) {
        return List.of(1L, 2L);
    }

    @Override
    public List<Long> getCustomDataScope(DataColumn dataColumn) {
        return List.of();
    }
}
