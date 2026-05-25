package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.dto.teacher.AttachmentVO;
import com.scnu.springbootjdk17demo.entity.AttachmentLink;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AttachmentLinkMapper extends BaseMapper<AttachmentLink> {

    @Select("""
        SELECT a.id, a.file_name, a.file_size, a.mime_type
        FROM attachment_link l
        JOIN attachment a ON a.id = l.attachment_id
        WHERE l.biz_type = #{bizType} AND l.biz_id = #{bizId}
        ORDER BY l.sort, l.id
    """)
    List<AttachmentVO> selectByBiz(
            @Param("bizType") String bizType,
            @Param("bizId")   Long bizId);
}