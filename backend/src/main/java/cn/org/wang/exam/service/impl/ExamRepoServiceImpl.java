package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.mapper.ExamRepoMapper;
import cn.org.wang.exam.model.entity.ExamRepo;
import cn.org.wang.exam.service.IExamRepoService;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class ExamRepoServiceImpl extends ServiceImpl<ExamRepoMapper, ExamRepo> implements IExamRepoService {

}
