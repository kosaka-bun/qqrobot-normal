package de.honoka.qqrobot.normal.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.normal.entity.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (UserStatus)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-13 13:21:12
 */
@Mapper
public interface UserStatusDao extends BaseMapper<UserStatus> {

    Integer getLastInsertId();

    UserStatus findLatest(@Param("qq") long qq);

    void clear();
}
