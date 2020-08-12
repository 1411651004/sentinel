package com.gzf.sentinel.mapper;

import com.gzf.sentinel.entity.ResourceRoleQps;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface ResourceRoleQpsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ResourceRoleQps record);

    int insertSelective(ResourceRoleQps record);

    ResourceRoleQps selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ResourceRoleQps record);

    int updateByPrimaryKey(ResourceRoleQps record);

    List<ResourceRoleQps> selectAll();
}