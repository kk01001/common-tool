package io.github.kk01001.postgres.examples.service;

import io.github.kk01001.postgres.examples.dto.CreateUserDTO;
import io.github.kk01001.postgres.examples.dto.IdDTO;
import io.github.kk01001.postgres.examples.dto.PageQueryDTO;
import io.github.kk01001.postgres.examples.dto.UpdateUserDTO;
import io.github.kk01001.postgres.examples.vo.PageResultVO;
import io.github.kk01001.postgres.examples.vo.UserVO;

public interface PostgresDemoService {

    void initTable();

    Long createUser(CreateUserDTO dto);

    UserVO getUser(IdDTO dto);

    Boolean updateUser(UpdateUserDTO dto);

    Boolean deleteUser(IdDTO dto);

    PageResultVO<UserVO> pageUsers(PageQueryDTO dto);

    Boolean transactionalRollbackDemo();
}