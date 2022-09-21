package de.honoka.qqrobot.normal.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.normal.entity.Watering;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Watering)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-13 13:20:44
 */
@Mapper
public interface WateringDao extends BaseMapper<Watering> {

    List<Watering> searchWateringRank(@Param("limit") int limit);

    Watering findAndLock(@Param("qq") long qq);
}
