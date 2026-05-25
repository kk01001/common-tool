package io.github.archer099.postgres.examples.service;

import io.github.archer099.postgres.examples.dto.CreateUserDTO;
import io.github.archer099.postgres.examples.dto.IdDTO;
import io.github.archer099.postgres.examples.dto.PageQueryDTO;
import io.github.archer099.postgres.examples.dto.UpdateUserDTO;
import io.github.archer099.postgres.examples.vo.PageResultVO;
import io.github.archer099.postgres.examples.vo.UserVO;

public interface PostgresDemoService {

    void initTable();

    Long createUser(CreateUserDTO dto);

    UserVO getUser(IdDTO dto);

    Boolean updateUser(UpdateUserDTO dto);

    Boolean deleteUser(IdDTO dto);

    PageResultVO<UserVO> pageUsers(PageQueryDTO dto);

    Boolean transactionalRollbackDemo();
}