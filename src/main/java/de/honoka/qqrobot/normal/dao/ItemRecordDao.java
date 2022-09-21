package de.honoka.qqrobot.normal.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import de.honoka.qqrobot.normal.entity.ItemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (ItemRecords)表数据库访问层
 *
 * @author makejava
 * @since 2021-12-13 13:19:05
 */
@Mapper
public interface ItemRecordDao extends BaseMapper<ItemRecord> {

    void update(ItemRecord itemRecord);

    ItemRecord findAndLock(@Param("qq") long qq,
                           @Param("itemName") String itemName);

    List<ItemRecord> getAvaliableItemsOfUser(@Param("qq") long qq,
                                             @Param("lock") boolean lock);
}
