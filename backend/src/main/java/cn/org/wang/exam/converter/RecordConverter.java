package cn.org.wang.exam.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Repo;
import cn.org.wang.exam.model.vo.record.ExerciseRecordVO;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/30 11:39 AM
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface RecordConverter {

    Page<ExerciseRecordVO> pageRepoEntityToVo(Page<Repo> page);

}
