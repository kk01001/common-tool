package io.github.kk01001.postgres.examples.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.postgres.examples.dto.CreateUserDTO;
import io.github.kk01001.postgres.examples.dto.IdDTO;
import io.github.kk01001.postgres.examples.dto.PageQueryDTO;
import io.github.kk01001.postgres.examples.dto.UpdateUserDTO;
import io.github.kk01001.postgres.examples.entity.DemoUser;
import io.github.kk01001.postgres.examples.mapper.DemoUserMapper;
import io.github.kk01001.postgres.examples.service.PostgresDemoService;
import io.github.kk01001.postgres.examples.vo.PageResultVO;
import io.github.kk01001.postgres.examples.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostgresDemoServiceImpl implements PostgresDemoService {

    private final DemoUserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void initTable() {
        try {
            ClassPathResource res = new ClassPathResource("db/demo_user.sql");
            String sql = StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long createUser(CreateUserDTO dto) {
        DemoUser entity = new DemoUser();
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        userMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public UserVO getUser(IdDTO dto) {
        DemoUser e = userMapper.selectById(dto.getId());
        if (e == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(e.getId());
        vo.setUsername(e.getUsername());
        vo.setEmail(e.getEmail());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }

    @Override
    public Boolean updateUser(UpdateUserDTO dto) {
        DemoUser entity = new DemoUser();
        entity.setId(dto.getId());
        if (StrUtil.isNotBlank(dto.getUsername())) {
            entity.setUsername(dto.getUsername());
        }
        if (StrUtil.isNotBlank(dto.getEmail())) {
            entity.setEmail(dto.getEmail());
        }
        return userMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteUser(IdDTO dto) {
        return userMapper.deleteById(dto.getId()) > 0;
    }

    @Override
    public PageResultVO<UserVO> pageUsers(PageQueryDTO dto) {
        int limit = Objects.requireNonNullElse(dto.getPageSize(), 10);
        int page = Objects.requireNonNullElse(dto.getPageNumber(), 1);
        int offset = (page - 1) * limit;
        long total = userMapper.count(dto.getKeyword());
        List<DemoUser> list = userMapper.page(dto.getKeyword(), limit, offset);
        List<UserVO> items = list.stream().map(e -> {
            UserVO vo = new UserVO();
            vo.setId(e.getId());
            vo.setUsername(e.getUsername());
            vo.setEmail(e.getEmail());
            vo.setCreatedAt(e.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
        PageResultVO<UserVO> vo = new PageResultVO<>();
        vo.setTotal(total);
        vo.setItems(items);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transactionalRollbackDemo() {
        DemoUser a = new DemoUser();
        a.setUsername("tx_user_1");
        a.setEmail("tx1@example.com");
        userMapper.insert(a);
        DemoUser b = new DemoUser();
        b.setUsername("tx_user_2");
        b.setEmail("tx2@example.com");
        userMapper.insert(b);
        throw new RuntimeException("rollback");
    }
}