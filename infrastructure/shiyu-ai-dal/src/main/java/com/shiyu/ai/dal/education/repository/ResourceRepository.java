package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ResourceBO;
import com.shiyu.ai.dal.education.dataobject.ResourceDO;
import com.shiyu.ai.dal.education.mapper.ResourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceRepository implements com.shiyu.ai.education.port.repository.ResourceRepository {

    @Resource
    private ResourceMapper resourceMapper;

    public ResourceBO selectById(Long id) {
        return MapstructUtils.convert(resourceMapper.selectOneById(id), ResourceBO.class);
    }

    public List<ResourceBO> selectBySubjectCode(String subjectCode) {
        return MapstructUtils.convert(resourceMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("subject_code", subjectCode)
                        .eq("status", 1)
        ), ResourceBO.class);
    }

    public List<ResourceBO> selectByType(String type) {
        return MapstructUtils.convert(resourceMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("type", type)
                        .eq("status", 1)
        ), ResourceBO.class);
    }

    public PageData<ResourceBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<ResourceDO> page = resourceMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .eq("status", 1)
                        .orderBy("id")
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), ResourceBO.class), page.getTotalRow());
    }

    public List<ResourceBO> selectAll() {
        return MapstructUtils.convert(resourceMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
        ), ResourceBO.class);
    }

    public int insert(ResourceBO entity) {
        ResourceDO dataObj = MapstructUtils.convert(entity, ResourceDO.class);
        int rows = resourceMapper.insert(dataObj);
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(ResourceBO entity) {
        ResourceDO dataObj = MapstructUtils.convert(entity, ResourceDO.class);
        return resourceMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return resourceMapper.deleteById(id);
    }
}
