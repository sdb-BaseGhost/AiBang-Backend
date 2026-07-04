package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.sdb.aiban.entity.UserSkillProgress;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserSkillProgressMapper extends BaseMapper<UserSkillProgress> {

    /**
     * 批量统计技能的学习人数和完成人数
     * @param skillIds 当前页的技能ID列表
     * @return 每个技能的 user_count 和 completed_count
     */
    @Select("<script>" +
        "SELECT skill_id AS skillId, " +
        "       COUNT(*) AS userCount, " +
        "       SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount " +
        "FROM user_skill_progress " +
        "WHERE skill_id IN " +
        "<foreach item='id' collection='skillIds' open='(' separator=',' close=')'>" +
        "#{id}" +
        "</foreach> " +
        "GROUP BY skill_id" +
        "</script>")
    List<Map<String, Object>> countProgressBySkillIds(@Param("skillIds") List<Long> skillIds);
}
