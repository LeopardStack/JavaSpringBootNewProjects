package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 查指定用户的所有权限（菜单+按钮）。Distinct 因为可能多角色 */
    @Select("""
        SELECT DISTINCT p.* FROM sys_permission p
        JOIN sys_role_permission rp ON rp.permission_id = p.id
        JOIN sys_user_role       ur ON ur.role_id       = rp.role_id
        WHERE ur.user_id = #{userId}
        ORDER BY p.sort
    """)
    List<SysPermission> selectByUserId(Long userId);
}