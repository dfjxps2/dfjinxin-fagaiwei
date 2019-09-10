package io.dfjinxin.modules.analyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dfjinxin.modules.analyse.entity.WpBaseIndexInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 *
 *
 * @author z.h.c
 * @email z.h.c@126.com
 * @date 2019-09-02 15:38:20
 */
@Mapper
@Repository
public interface WpBaseIndexInfoDao extends BaseMapper<WpBaseIndexInfoEntity> {

}
