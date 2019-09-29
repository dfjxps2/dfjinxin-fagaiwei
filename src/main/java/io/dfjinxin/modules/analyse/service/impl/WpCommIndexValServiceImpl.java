package io.dfjinxin.modules.analyse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dfjinxin.common.dto.KpiInfoDto;
import io.dfjinxin.common.utils.KpiTypeEnum;
import io.dfjinxin.common.utils.PageUtils;
import io.dfjinxin.common.utils.Query;
import io.dfjinxin.modules.analyse.dao.WpBaseIndexInfoDao;
import io.dfjinxin.modules.analyse.dao.WpCommIndexValDao;
import io.dfjinxin.modules.analyse.entity.WpBaseIndexInfoEntity;
import io.dfjinxin.modules.analyse.entity.WpCommIndexValEntity;
import io.dfjinxin.modules.analyse.service.WpCommIndexValService;
import io.dfjinxin.modules.price.dao.PssCommTotalDao;
import io.dfjinxin.modules.price.entity.PssCommTotalEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service("wpCommIndexValService")
public class WpCommIndexValServiceImpl extends ServiceImpl<WpCommIndexValDao, WpCommIndexValEntity> implements WpCommIndexValService {

    @Autowired
    private PssCommTotalDao pssCommTotalDao;
    @Autowired
    private WpCommIndexValDao wpCommIndexValDao;
    @Autowired
    private WpBaseIndexInfoDao wpBaseIndexInfoDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WpCommIndexValEntity> page = this.page(
                new Query<WpCommIndexValEntity>().getPage(params),
                new QueryWrapper<WpCommIndexValEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<Map<String, PssCommTotalEntity>> queryList() {

        QueryWrapper where1 = new QueryWrapper();
        where1.eq("level_code", "0");
        where1.eq("data_flag", "0");
        //查询0级商品
        List<PssCommTotalEntity> commType0 = pssCommTotalDao.selectList(where1);

        List<Map<String, PssCommTotalEntity>> resultList = new ArrayList();
        Map<String, PssCommTotalEntity> tempMap = new HashMap<>();
        for (PssCommTotalEntity entity : commType0) {
            Map<String, Object> temp = queryCommByLevelCode0(entity);
            PssCommTotalEntity result = (PssCommTotalEntity) temp.get("result");
            if (1 == entity.getCommId()) {
                tempMap.put("dazong", result);
            } else {
                tempMap.put("minsheng", result);
            }
        }
        resultList.add(tempMap);
        return resultList;
    }

    @Override
    public List<Map<String, Object>> queryDetailByCommId(Map<String, Object> condition) {
        if (condition.get("indexType").equals("价格")) {
            return wpCommIndexValDao.queryIndexTypePrice(condition);
        }
        return wpCommIndexValDao.queryIndexTypeByCondition(condition);
    }

    @Override
    public List<Map<String, Object>> queryIndexTypeByCommId(Integer commId) {
        return wpCommIndexValDao.queryIndexTypeByCommId(commId);
    }

    @Override
    public List<Map<String, Object>> queryLevel4CommInfo(Integer commId) {

        QueryWrapper where = new QueryWrapper();
        where.eq("data_flag", 0);
        where.eq("comm_id", commId);
        PssCommTotalEntity commLevel2 = pssCommTotalDao.selectOne(where);
        if (commLevel2 == null) {
            return null;
        }
        QueryWrapper where1 = new QueryWrapper();
        where1.eq("data_flag", 0);
        where1.eq("parent_code", commId);
        List<PssCommTotalEntity> commLevel3 = pssCommTotalDao.selectList(where1);
        if (commLevel3 == null) {
            return null;
        }
        List resultList = new ArrayList();
        //查询4类商品所有指标类型
        String sql = "select pss_comm_total.comm_id from pss_comm_total where data_flag=0 and parent_code=" + commId;
        QueryWrapper where2 = new QueryWrapper();
        where2.eq("index_flag", 0);
        where2.inSql("comm_id", sql);
        where2.groupBy("index_type");
        List<WpBaseIndexInfoEntity> baseIndexInfoEntityList = wpBaseIndexInfoDao.selectList(where2);

        Set<String> indexTypeList = new HashSet<>();
        for (WpBaseIndexInfoEntity entity : baseIndexInfoEntityList) {
            indexTypeList.add(entity.getIndexType());
        }

        for (String type : indexTypeList) {
            QueryWrapper where3 = new QueryWrapper();
            where3.eq("index_flag", 0);
            where3.inSql("comm_id", sql);
            where3.eq("index_type", type);
            List<WpBaseIndexInfoEntity> baseIndexInfoEntities = wpBaseIndexInfoDao.selectList(where3);
            KpiTypeEnum typeEnum = KpiTypeEnum.getbyType(type);
            switch (typeEnum) {
                //价格指标
                case Pri:
                    List<WpCommIndexValEntity> priList = null;
                    for (PssCommTotalEntity entity : commLevel3) {
                        if (commLevel2.getCommName().equals(entity.getCommName())) {
                            priList = doIndexPriceInfo(baseIndexInfoEntities, entity.getCommId(), type);

                        }
                    }
                    Map priMap = new HashMap();
                    priMap.put(type, priList);
                    resultList.add(priMap);
                    continue;
                case Ccl:
                    List<KpiInfoDto> cclList = doIndexInfo(baseIndexInfoEntities);
                    Map cclMap = new HashMap();
                    cclMap.put(type, cclList);
                    resultList.add(cclMap);
                    continue;
                case Csp:
                    List<KpiInfoDto> cspList = doIndexInfo(baseIndexInfoEntities);
                    Map cspMap = new HashMap();
                    cspMap.put(type, cspList);
                    resultList.add(cspMap);
                    continue;
                case Cst:
                    List<KpiInfoDto> cstList = doIndexInfo(baseIndexInfoEntities);
                    Map cstMap = new HashMap();
                    cstMap.put(type, cstList);
                    resultList.add(cstMap);
                    continue;
                case Prd:
                    List<KpiInfoDto> prdList = doIndexInfo(baseIndexInfoEntities);
                    Map<String, List<KpiInfoDto>> prdMap = new HashMap();
                    prdMap.put(type, prdList);
                    resultList.add(prdMap);
                    continue;
                default:
            }
        }
        return resultList;
    }

    private List doIndexPriceInfo(List<WpBaseIndexInfoEntity> list, int commId, String indexType) {

        for (WpBaseIndexInfoEntity indexInfoEntity : list) {
            if (indexInfoEntity.getCommId() == commId) {
                List valList = wpCommIndexValDao.selectListBystatAreaId(commId, indexType, indexInfoEntity.getIndexId());
                return valList;
            }
        }
        return null;
    }

    private List doIndexInfo(List<WpBaseIndexInfoEntity> indexInfolist) {

        List<KpiInfoDto> list = new ArrayList<>();
        for (WpBaseIndexInfoEntity indexInfoEntity : indexInfolist) {
            QueryWrapper where2 = new QueryWrapper();
            where2.eq("comm_id", indexInfoEntity.getCommId());
            where2.eq("index_i", indexInfoEntity.getIndexId());
            where2.isNotNull("data_time");
            where2.orderByDesc("data_time");
            List<WpCommIndexValEntity> indexValEntityList = wpCommIndexValDao.selectList(where2);
            if (null != indexValEntityList && indexValEntityList.size() > 0) {
                WpCommIndexValEntity wpCommIndexValEntity = indexValEntityList.get(0);
                PssCommTotalEntity commTotalEntity = pssCommTotalDao.selectById(indexInfoEntity.getCommId());
                StringBuffer sb = new StringBuffer(commTotalEntity.getCommName());
                sb.append("-");
                sb.append(wpCommIndexValEntity.getIndexName());
                sb.append("-");
                sb.append(wpCommIndexValEntity.getIndexType());
                KpiInfoDto dto = new KpiInfoDto();
                dto.setIndexName(sb.toString());
                dto.setIndexVal(wpCommIndexValEntity.getIndexVal().toString());
                dto.setIndexUnit(wpCommIndexValEntity.getIndexUnit());
                dto.setDataTime(wpCommIndexValEntity.getDataTime().toString());
                dto.setCommId(wpCommIndexValEntity.getCommId());
                list.add(dto);
            }
        }
        return list;
    }

    private List<WpBaseIndexInfoEntity> getWpBaseIndexInfo(int commId) {
        QueryWrapper where2 = new QueryWrapper();
        where2.eq("comm_id", commId);
        where2.eq("index_flag", 0);
        return wpBaseIndexInfoDao.selectList(where2);
    }

    private Map<String, Object> queryCommByLevelCode0(PssCommTotalEntity levelCode0) {
        if (levelCode0 == null || levelCode0.getCommId() == null) {
            return null;
        }
        //根据0类查询1类
        QueryWrapper where1 = new QueryWrapper();
        where1.in("parent_code", levelCode0.getCommId());
        where1.eq("data_flag", "0");
        where1.eq("level_code", "1");
        // 获取一类商品
        List<PssCommTotalEntity> commLevelCode1 = pssCommTotalDao.selectList(where1);

        for (PssCommTotalEntity entity1 : commLevelCode1) {
            QueryWrapper where2 = new QueryWrapper();
            where2.eq("level_code", "2");
            where2.eq("data_flag", "0");
            where2.eq("parent_code", entity1.getCommId());
            List<PssCommTotalEntity> commLevelCode2 = pssCommTotalDao.selectList(where2);
            entity1.setSubCommList(commLevelCode2);
        }
        levelCode0.setSubCommList(commLevelCode1);
        Map<String, Object> map = new HashMap<>();
        map.put("result", levelCode0);
        return map;
    }

}
