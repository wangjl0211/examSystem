package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.mapper.UserDailyLoginDurationMapper;
import cn.org.wang.exam.model.entity.UserDailyLoginDuration;
import cn.org.wang.exam.service.IUserDailyLoginDurationService;

import org.springframework.stereotype.Service;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/28 10:46 PM
 */
@Service
public class UserDailyLoginDurationServiceImpl extends ServiceImpl<UserDailyLoginDurationMapper, UserDailyLoginDuration> implements IUserDailyLoginDurationService {
}
