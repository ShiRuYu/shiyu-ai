package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.ResourceDO;
import com.shiyu.ai.dal.mapper.education.ResourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceRepository {

    @Resource
    private ResourceMapper resourceMapper;

    public ResourceDO selectById(Long id) {
        return resourceMapper.selectOneById(id);
    }

    public List<ResourceDO> selectBySubjectCode(String subjectCode) {
        return resourceMapper.selectListByQuery(
                QueryWrapper.create().eq("subject_code", subjectCode).eq("status", 1));
    }

    public List<ResourceDO> selectByType(String type) {
        return resourceMapper.selectListByQuery(
                QueryWrapper.create().eq("type", type).eq("status", 1));
    }

    public List<ResourceDO> selectAll() {
        return resourceMapper.selectListByQuery(QueryWrapper.create().eq("status", 1));
    }

    public int insert(ResourceDO resource) {
        return resourceMapper.insert(resource);
    }

    public int update(ResourceDO resource) {
        return resourceMapper.update(resource);
    }

    public int deleteById(Long id) {
        return resourceMapper.deleteById(id);
    }
}
