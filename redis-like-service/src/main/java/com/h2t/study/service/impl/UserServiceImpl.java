package com.h2t.study.service.impl;

import com.h2t.study.mapper.UserMapper;
import com.h2t.study.po.User;
import com.h2t.study.service.UserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author hetiantian
 * @since 2019-10-08
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

}
