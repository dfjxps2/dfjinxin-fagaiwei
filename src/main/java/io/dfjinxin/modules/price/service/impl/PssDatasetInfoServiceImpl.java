package io.dfjinxin.modules.price.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dfjinxin.common.utils.PageUtils;
import io.dfjinxin.common.utils.Query;

import io.dfjinxin.modules.price.dao.PssDatasetInfoDao;
import io.dfjinxin.modules.price.entity.PssDatasetInfoEntity;
import io.dfjinxin.modules.price.service.PssDatasetInfoService;


@Service("pssDatasetInfoService")
public class PssDatasetInfoServiceImpl extends ServiceImpl<PssDatasetInfoDao, PssDatasetInfoEntity> implements PssDatasetInfoService {

    @Override
    public List<PssDatasetInfoEntity> listAll() {
        return super.list();
    }
}
