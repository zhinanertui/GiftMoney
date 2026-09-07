package com.example.demo.gift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.gift.dto.ContactStat;
import com.example.demo.gift.dto.DirectionStat;
import com.example.demo.gift.dto.MonthlyStat;
import com.example.demo.gift.dto.OccasionStat;
import com.example.demo.gift.entity.GiftRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GiftRecordMapper extends BaseMapper<GiftRecord> {

    /**
     * 按姓名+事由分组：我送出 vs 对方还礼 汇总
     */
    @Select("SELECT name, occasion, " +
            "SUM(CASE WHEN direction = 1 THEN amount ELSE 0 END) AS sent, " +
            "SUM(CASE WHEN direction = 2 THEN amount ELSE 0 END) AS received, " +
            "SUM(CASE WHEN direction = 2 THEN amount ELSE 0 END) - SUM(CASE WHEN direction = 1 THEN amount ELSE 0 END) AS balance " +
            "FROM tb_gift_record WHERE user_id = #{userId} AND del_yn = 0 " +
            "GROUP BY name, occasion ORDER BY name, occasion")
    List<ContactStat> contactsStat(@Param("userId") Long userId);

    /**
     * 按姓名+事由分组往来汇总（分页 + 关键字搜索）
     * 第一个参数为 IPage 时，MyBatis-Plus 自动套用分页（COUNT + LIMIT）
     */
    @Select("<script>" +
            "SELECT name, occasion, " +
            "SUM(CASE WHEN direction = 1 THEN amount ELSE 0 END) AS sent, " +
            "SUM(CASE WHEN direction = 2 THEN amount ELSE 0 END) AS received, " +
            "SUM(CASE WHEN direction = 2 THEN amount ELSE 0 END) - SUM(CASE WHEN direction = 1 THEN amount ELSE 0 END) AS balance " +
            "FROM tb_gift_record WHERE user_id = #{userId} AND del_yn = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (name LIKE CONCAT('%', #{keyword}, '%') OR occasion LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "GROUP BY name, occasion ORDER BY name, occasion" +
            "</script>")
    IPage<ContactStat> contactsStatPage(IPage<ContactStat> page,
                                        @Param("userId") Long userId,
                                        @Param("keyword") String keyword);

    /**
     * 按 年/月/方向 汇总
     */
    @Select("SELECT YEAR(gift_date) AS year, MONTH(gift_date) AS month, direction, SUM(amount) AS total " +
            "FROM tb_gift_record WHERE user_id = #{userId} AND del_yn = 0 " +
            "GROUP BY YEAR(gift_date), MONTH(gift_date), direction " +
            "ORDER BY year, month, direction")
    List<MonthlyStat> monthlyStat(@Param("userId") Long userId);

    /**
     * 按方向汇总（送出 / 收到）
     */
    @Select("SELECT direction, SUM(amount) AS total FROM tb_gift_record " +
            "WHERE user_id = #{userId} AND del_yn = 0 GROUP BY direction")
    List<DirectionStat> directionStat(@Param("userId") Long userId);

    /**
     * 按事由汇总
     */
    @Select("SELECT occasion, SUM(amount) AS total FROM tb_gift_record " +
            "WHERE user_id = #{userId} AND del_yn = 0 AND occasion IS NOT NULL AND occasion != '' " +
            "GROUP BY occasion ORDER BY total DESC")
    List<OccasionStat> occasionStat(@Param("userId") Long userId);
}
