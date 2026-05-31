package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Repo;
import cn.org.wang.exam.model.vo.repo.RepoVO;

import java.util.List;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/28 20:21
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface RepoConverter {

    List<RepoVO> listEntityToVo(List<Repo> list);

}
