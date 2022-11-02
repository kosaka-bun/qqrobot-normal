package de.honoka.qqrobot.normal.service;

import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.normal.dao.ItemRecordDao;
import de.honoka.qqrobot.normal.dao.UserStatusDao;
import de.honoka.qqrobot.normal.dao.WateringDao;
import de.honoka.qqrobot.normal.entity.ItemRecord;
import de.honoka.qqrobot.normal.entity.UserStatus;
import de.honoka.qqrobot.normal.entity.Watering;
import de.honoka.qqrobot.normal.util.EmojiUtils;
import de.honoka.sdk.util.code.ActionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

@SuppressWarnings("unused")
@Service
public class ItemUsingService {

    @Transactional
    public void onGiftEffect(UserStatus userStatus) {
        Random ra = new Random();
        List<ItemRecord> itemList = itemRecordDao.getAvaliableItemsOfUser(
                userStatus.getQq(), true);
        List<ItemRecord> itemListCopy = new ArrayList<>(itemList);
        int removedCount = 0;
        StringBuilder removedItemNames = new StringBuilder();
        while(removedCount < 5 && itemListCopy.size() > 0) {
            ItemRecord item = itemListCopy.get(ra.nextInt(itemListCopy.size()));
            item.setCount(item.getCount() - 1);
            removedCount++;
            removedItemNames.append(itemService.toEmojiItemName(item.getItemName()));
            if(item.getCount() <= 0) itemListCopy.remove(item);
        }
        if(removedCount > 0) {
            //更新道具记录
            for(ItemRecord itemRecord : itemList) {
                itemRecordDao.update(itemRecord);
            }
            //通知
            framework.sendGroupMsg(userStatus.getFromGroup(), EmojiUtils
                    .unicodeToEmoji(127873) + "使用成功，" + framework.getNickOrCard(
                            userStatus.getFromGroup(), userStatus.getQq()
                    ) + "丢失了以下道具：\n" + removedItemNames
            );
        } else {
            framework.sendGroupMsg(userStatus.getFromGroup(), EmojiUtils
                    .unicodeToEmoji(127873) + "使用成功，" + framework.getNickOrCard(
                            userStatus.getFromGroup(), userStatus.getQq()
                    ) + "没有道具，未受到任何影响"
            );
        }
    }

    @Item(name = "礼物", emojiNameUnicode = 127873)
    public String useGift(long fromQQ, long targetQQ, long group) {
        UserStatus userStatus = new UserStatus()
                .setFromQq(fromQQ)
                .setQq(targetQQ)
                .setFromGroup(group)
                .setStatus("礼物")
                .setTime(new Date());
        userStatusDao.insert(userStatus);
        userStatus.setId(userStatusDao.getLastInsertId());
        //timer task
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                ActionUtils.doIgnoreException(() -> {
                    itemUsingService.onGiftEffect(userStatus);
                });
                statusTaskMap.remove(userStatus.getId());
                userStatusDao.deleteById(userStatus.getId());
            }
        };
        timer.schedule(task, new Date(System.currentTimeMillis() +
                5L * 60 * 1000));
        statusTaskMap.put(userStatus.getId(), task);
        return "使用成功，5分钟后生效";
    }

    //使用柠檬
    @Item(name = "柠檬", emojiNameUnicode = 127819)
    public String useLemon(long fromQQ, long targetQQ, long group) {
        String username = framework.getNickOrCard(group, targetQQ);
        UserStatus latestStatus = userStatusDao.findLatest(targetQQ);
        if(latestStatus != null) {
            TimerTask task = statusTaskMap.get(latestStatus.getId());
            task.cancel();
            statusTaskMap.remove(latestStatus.getId());
            userStatusDao.deleteById(latestStatus.getId());
            return "使用成功，" + username + "的一个“" +
                    latestStatus.getStatus() + "”状态已被移除";
        } else {
            return "使用成功，" + username + "没有被施加任何状态";
        }
    }

    //使用放大镜
    @Item(name = "放大镜", emojiNameUnicode = 128269)
    public String useMagnifier(long fromQQ, long targetQQ, long group) {
        String username = framework.getNickOrCard(group, targetQQ);
        List<ItemRecord> list = itemRecordDao.getAvaliableItemsOfUser(
                targetQQ, false);
        if(list.size() <= 0) return username + "还没有道具";
        StringBuilder info = new StringBuilder("使用成功，" + username +
                "目前拥有如下道具：");
        int i = 1;
        for (ItemRecord ir : list) {
            info.append("\n").append(i).append(".")
                    .append(itemService.toEmojiItemName(ir.getItemName()))
                    .append(" ").append(ir.getCount()).append("个");
            i++;
        }
        return info.toString();
    }

    //使用粉色果实
    @Item(name = "粉色果实", emojiNameUnicode = 127825)
    public String usePinkFruit(long fromQQ, long targetQQ, long group) {
        Random ra = new Random();
        if(fromQQ == targetQQ) {
            int exp = ra.nextInt(100) + 1;	//1~100
            //查询浇水信息
            Watering target = wateringDao.findAndLock(targetQQ);
            //增加经验
            target.plusExp(exp);
            //升级
            while(target.getNowExp() >= target.getLevel() * 100) {  //避免连升几级
                target.setNowExp(target.getNowExp() - target.getLevel() * 100);
                target.setLevel(target.getLevel() + 1);
            }
            //存入数据库
            wateringDao.updateById(target);
            return "使用成功，你获得了" + exp + "点经验\n你目前的等级和经验为：\n" +
                    "Lv" + target.getLevel() + " (" + target.getNowExp() + "/" +
                    (target.getLevel() * 100) + ")";
        }
        int exp = ra.nextInt(100) + 1;	//1~100
        Watering from, target;
        //查询浇水信息
        from = wateringDao.findAndLock(fromQQ);
        target = wateringDao.findAndLock(targetQQ);
        if(from == null) {	//此处不能确保发起者一定有浇水信息
            from = new Watering();
            from.setQq(fromQQ);
            from.setLevel(1);
            from.setNowExp(0);
            wateringDao.insert(from);
        }
        //增加经验
        from.plusExp(exp);
        target.plusExp(exp * 2);
        //升级
        while(from.getNowExp() >= from.getLevel() * 100) {  //避免连升几级
            from.setNowExp(from.getNowExp() - from.getLevel() * 100);
            from.setLevel(from.getLevel() + 1);
        }
        while(target.getNowExp() >= target.getLevel() * 100) {  //避免连升几级
            target.setNowExp(target.getNowExp() - target.getLevel() * 100);
            target.setLevel(target.getLevel() + 1);
        }
        //存入数据库
        wateringDao.updateById(from);
        wateringDao.updateById(target);
        return "使用成功，你获得了" + exp + "点经验，" +
                framework.getNickOrCard(group, targetQQ) +
                "获得了" + (exp*2) + "点经验\n" +
                "你目前的等级和经验为：\n" + "Lv" + from.getLevel() + " (" +
                from.getNowExp() + "/" + (from.getLevel() * 100) + ")\n" +
                framework.getNickOrCard(group, targetQQ) +
                "目前的等级和经验为：\n" + "Lv" + target.getLevel() + " (" +
                target.getNowExp() + "/" + (target.getLevel() * 100) + ")";
    }

    //使用柚子
    @Item(name = "柚子", emojiNameUnicode = 127816)
    public String useShaddock(long fromQQ, long targetQQ, long group) {
        //抽取两个不是柚子，且互不相同的道具
        Random ra = new Random();
        String itemName1, itemName2;
        List<String> itemNames = new ArrayList<>(itemService
                .getEmojiWordNameMap().values());
        do {
            itemName1 = itemNames.get(ra.nextInt(itemNames.size()));
        } while(itemName1.equals("柚子"));
        do {
            itemName2 = itemNames.get(ra.nextInt(itemNames.size()));
        } while(itemName2.equals("柚子") || itemName2.equals(itemName1));
        //获取两个道具的信息
        ItemRecord ir1, ir2;
        ir1 = itemRecordDao.findAndLock(targetQQ, itemName1);
        if(ir1 == null) {
            ir1 = new ItemRecord();
            ir1.setQq(targetQQ);
            ir1.setItemName(itemName1);
            ir1.setCount(0);
            itemRecordDao.insert(ir1);
        }
        ir2 = itemRecordDao.findAndLock(targetQQ, itemName2);
        if(ir2 == null) {
            ir2 = new ItemRecord();
            ir2.setQq(targetQQ);
            //这里是复制的ir1的代码，出现过没有把itemName1改成2的低级错误
            ir2.setItemName(itemName2);
            ir2.setCount(0);
            itemRecordDao.insert(ir2);
        }
        ir1.setCount(ir1.getCount() + 1);
        ir2.setCount(ir2.getCount() + 1);
        //保存数据
        itemRecordDao.update(ir1);
        itemRecordDao.update(ir2);
        return "使用成功，" + framework.getNickOrCard(group, targetQQ) +
                "获得了一个" + itemService.toEmojiItemName(itemName1) +
                "和一个" + itemService.toEmojiItemName(itemName2);
    }

    //使用白色果实
    @Item(name = "白色果实", emojiNameUnicode = 127834)
    public String useWhiteFruit(long fromQQ, long targetQQ, long group) {
        Random ra = new Random();
        //选择一个不是白色果实的道具
        String itemName;
        List<String> itemNames = new ArrayList<>(itemService
                .getEmojiWordNameMap().values());
        do {
            itemName = itemNames.get(ra.nextInt(itemNames.size()));
        } while(itemName.equals("白色果实"));
        String info = "这个道具为：" + itemService.toEmojiItemName(itemName) + "\n";
        //对目标使用此道具
        info += itemService.useItemForFree(targetQQ, fromQQ, group, itemName);
        return info;
    }

    //使用紫色果实
    @Item(name = "紫色果实", emojiNameUnicode = 127815)
    public String usePurpleFruit(long fromQQ, long targetQQ, long group) {
        //如果目标是自己
        if(fromQQ == targetQQ) return "使用成功，你获得了自己的一个道具（左手换右手）";
        //获得目标的道具记录
        List<ItemRecord> targets = itemRecordDao.getAvaliableItemsOfUser(
                targetQQ, false);
        if(targets.size() <= 0) {
            return framework.getNickOrCard(group, targetQQ) +
                    "没有道具，你没有获得任何道具";
        }
        Random ra = new Random();
        //随机抽取一条有效记录
        ItemRecord target = targets.get(ra.nextInt(targets.size()));
        target = itemRecordDao.findAndLock(target.getQq(), target.getItemName());
        //查询发起者关于此道具的信息
        ItemRecord from = itemRecordDao.findAndLock(fromQQ, target.getItemName());
        if(from == null) {
            from = new ItemRecord();
            from.setQq(fromQQ);
            from.setItemName(target.getItemName());
            from.setCount(0);
            itemRecordDao.insert(from);
        }
        //执行交换
        target.setCount(target.getCount() - 1);
        from.setCount(from.getCount() + 1);
        //存入数据库
        itemRecordDao.update(target);
        itemRecordDao.update(from);
        return "使用成功，你获得了" + framework.getNickOrCard(group, targetQQ) +
                "的一个" + itemService.toEmojiItemName(target.getItemName());
    }

    //使用红色果实
    @Item(name = "红色果实", emojiNameUnicode = 127822)
    public String useRedFruit(long fromQQ, long targetQQ, long group) {
        Watering w = wateringDao.findAndLock(targetQQ);
        long now = System.currentTimeMillis();
        long timen = w.getNextTimeWatering().getTime() - now;	//剩余等待时间
        w.setNextTimeWatering(new Date(now + timen * 2));
        //计算文本化时间
        long diff = timen * 2;
        String time = "";
        if(diff >= 1000) {
            //用于整除计算时间
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            long ns = 1000;
            //剩余多长时间才可以浇水
            int ramainHour = (int)(diff / nh);
            int ramainMinute = (int)(diff % nh / nm);
            int ramainSecond = (int)(diff % nh % nm / ns);
            if(ramainHour > 0)	//当数值不为0且不为负值（非法值）才显示这个数值
                time += ramainHour + "小时";
            if(ramainMinute > 0)
                time += ramainMinute + "分";
            if(ramainSecond > 0)
                time += ramainSecond + "秒";
        } else {
            time = "0秒";
        }
        //存入数据库
        wateringDao.updateById(w);
        return "使用成功，" + framework.getNickOrCard(group, w.getQq()) +
                "现在还需要" + time + "才能浇水";
    }

    //使用绿色果实
    @Item(name = "绿色果实", emojiNameUnicode = 127823)
    public String useGreenFruit(long fromQQ, long targetQQ, long group) {
        Watering w = wateringDao.findAndLock(targetQQ);
        Random ra = new Random();
        int minusExp = ra.nextInt(300) + 1;	//1~300
        //如果要减去的经验大于已有经验
        if(w.getNowExp() < minusExp) minusExp = w.getNowExp();
        w.setNowExp(w.getNowExp() - minusExp);
        //存入数据库
        wateringDao.updateById(w);
        return "使用成功，" + framework.getNickOrCard(group, w.getQq()) +
                "扣除了" + minusExp + "点经验，目前的等级和经验为：\nLv" +
                w.getLevel() + " (" + w.getNowExp() + "/" +
                (w.getLevel() * 100) + ")";
    }

    //使用橙色果实
    @Item(name = "橙色果实", emojiNameUnicode = 127818)
    public String useOrangeFruit(long fromQQ, long targetQQ, long group) {
        Watering w = wateringDao.findAndLock(targetQQ);
        w.setNextTimeWatering(new Date());	//现在即是下次可浇水的时间
        //存入数据库
        wateringDao.updateById(w);
        return "使用成功，" + framework.getNickOrCard(
                group, w.getQq()) + "的浇水等待时间已刷新";
    }

    @PostConstruct
    @Transactional
    public void initStatusTaskMap() {
        try {
            userStatusDao.clear();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Item {

        String name();

        int emojiNameUnicode();
    }

    /**
     * 用户状态ID与预定的定时任务相对应的map
     */
    private final Map<Integer, TimerTask> statusTaskMap = new HashMap<>();

    @Resource
    private Timer timer;

    @Resource
    private ItemUsingService itemUsingService;

    @Resource
    private UserStatusDao userStatusDao;

    @Resource
    private ItemService itemService;

    @Lazy
    @Resource
    private Framework<?> framework;

    @Resource
    private WateringDao wateringDao;

    @Resource
    private ItemRecordDao itemRecordDao;
}
