package de.honoka.qqrobot.normal.service;

import de.honoka.qqrobot.normal.dao.ItemRecordDao;
import de.honoka.qqrobot.normal.dao.WateringDao;
import de.honoka.qqrobot.normal.entity.ItemRecord;
import de.honoka.qqrobot.normal.entity.Watering;
import de.honoka.qqrobot.normal.util.EmojiUtils;
import de.honoka.qqrobot.starter.RobotBasicProperties;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class ItemService {

    /**
     * 不判断道具是否充足，道具名是否合法，不扣除道具，直接使用一个道具
     */
    @SneakyThrows
    public String useItemForFree(long targetQQ, long fromQQ, long group,
                                 String itemName) {
        //查询目标的浇水信息
        Watering w = wateringDao.selectById(targetQQ);
        if(w == null) {
            return "使用失败，可能是该用户没有浇水信息，或没有查询成功";
        }
        Method itemMethod = itemMethods.get(itemName);
        return (String) itemMethod.invoke(itemUsingService, fromQQ,
                targetQQ, group);
    }

    /**
     * 对指定QQ使用一个道具
     *
     * @param targetQQ 目标
     * @param fromQQ   来源
     * @param itemName 道具名
     * @return 状态
     */
    private String useItem(long targetQQ, long fromQQ, long group,
                           String itemName) {
        //检查道具名
        itemName = toWordItemName(itemName);    //转为文字道具名
        if(itemName == null) return "输入的道具名不正确";
        //获得发起者的此道具的记录信息，检查道具数量
        ItemRecord ir = itemRecordDao.findAndLock(fromQQ, itemName);
        if(ir == null || ir.getCount() < 1) {
            return "你没有足够的" + toEmojiItemName(itemName);
        }
        //使用道具
        String info = useItemForFree(targetQQ, fromQQ, group, itemName);
        //使用成功，扣除道具数，由于使用道具以后，
        //被使用的道具数量可能发生了变化，必须先更新
        ir = itemRecordDao.findAndLock(fromQQ, itemName);
        ir.setCount(ir.getCount() - 1);
        itemRecordDao.update(ir);
        info += "\n你还剩余" + ir.getCount() + "个" +
                toEmojiItemName(ir.getItemName());
        return info;
    }

    //为使用道具命令提取和准备参数
    @Transactional
    public String prepareUseItem(long targetQQ, long fromQQ, long group,
                                 String[] args) {
        //0号位是道具名，1号位是at了指定QQ的CQ码
        String itemName = args[0];
        return useItem(targetQQ, fromQQ, group, itemName);
    }

    /**
     * 指定QQ进行一次道具抽奖，抽到返回道具名，未抽到返回null
     *
     * @param qq qq
     * @return 道具名
     */
    @Transactional
    public String drawItem(long qq, long group) {
        Random ra = new Random();
        int value = ra.nextInt(100) + 1;    //1~100
        //在开发群抽奖一定抽到
        if(group == basicProperties.getDevelopingGroup()) value = 100;
        if(value <= 85) return null;    //没抽到，此处可以调整中奖概率
        //抽到以后，获取道具名称
        List<String> wordNameList = new ArrayList<>(emojiWordNameMap.values());
        String itemName = wordNameList.get(ra.nextInt(wordNameList.size()));
        //存入数据库，首先查询有没有相应的道具记录
        ItemRecord ir = itemRecordDao.findAndLock(qq, itemName);
        if(ir == null) {    //没有相应的记录
            ir = new ItemRecord();
            ir.setQq(qq);
            ir.setItemName(itemName);
            ir.setCount(0);
            itemRecordDao.insert(ir);
        }
        //获取到的道具记录信息对象，数量+1
        ir.setCount(ir.getCount() + 1);
        itemRecordDao.update(ir);
        return itemName;
    }

    /**
     * 获取指定qq的道具信息
     *
     * @param qq qq
     * @return 信息
     */
    public String getItemRecords(long qq) {
        List<ItemRecord> list = itemRecordDao.getAvaliableItemsOfUser(
                qq, false);
        if(list.size() <= 0) return "你还没有道具";
        StringBuilder info = new StringBuilder("你目前拥有如下道具：");
        int i = 1;
        for(ItemRecord ir : list) {
            info.append("\n").append(i).append(".")
                    .append(toEmojiItemName(ir.getItemName()))
                    .append(" ").append(ir.getCount()).append("个");
            i++;
        }
        return info.toString();
    }

    @Transactional
    public String prepareGiveItems(long fromQQ, long targetQQ,
                                   String[] args) {
        try {
            if(args.length < 3) throw new IndexOutOfBoundsException();
            //0号位是道具名，1号位是数量，2号位是at了指定QQ的CQ码
            String itemName = args[0];
            int num = Integer.parseInt(args[1]);
            return giveItems(itemName, num, fromQQ, targetQQ);
        } catch(IndexOutOfBoundsException e) {
            return "命令格式有误，提供的参数不足";
        } catch(NumberFormatException e2) {
            return "参数有误";
        }
    }

    /**
     * 向指定QQ赠送道具
     *
     * @param itemName 道具名
     * @param num      数量
     * @param fromQQ   来源
     * @param targetQQ 目标
     * @return 提示信息
     */
    private String giveItems(String itemName, int num, long fromQQ,
                             long targetQQ) {
        //检查QQ
        if(fromQQ == targetQQ) return "你不能给自己赠送道具";
        //检查道具名
        itemName = toWordItemName(itemName);
        if(itemName == null) return "输入的道具名不正确";
        //获得发起者的此道具的记录信息，检查道具数量
        ItemRecord ir = itemRecordDao.findAndLock(fromQQ, itemName);
        if(ir == null || ir.getCount() < num) {
            return "你没有足够的" + toEmojiItemName(itemName);
        }
        //赠送道具，获取接受者的此道具信息
        ItemRecord getterIr = itemRecordDao.findAndLock(targetQQ, itemName);
        //修正错误信息
        if(getterIr == null) {
            getterIr = new ItemRecord();
            getterIr.setQq(targetQQ);
            getterIr.setItemName(itemName);
            getterIr.setCount(0);
            itemRecordDao.insert(getterIr);
        }
        if(getterIr.getCount() <= 0) getterIr.setCount(0);
        //计算道具数量
        ir.setCount(ir.getCount() - num);    //发起者扣除道具
        getterIr.setCount(getterIr.getCount() + num);    //接受者增加道具
        //存入数据库
        itemRecordDao.update(ir);
        itemRecordDao.update(getterIr);
        return "赠送成功，你还剩余" + ir.getCount() + "个" +
                toEmojiItemName(ir.getItemName());
    }

    //开发者直接获取道具
    @Transactional
    public String makeItems(String[] args) {
        long qq = basicProperties.getAdminQq();
        try {
            String itemName = toWordItemName(args[0]);
            if(itemName == null) return "输入的道具名不正确";
            int num = Integer.parseInt(args[1]);
            ItemRecord ir = itemRecordDao.findAndLock(qq, itemName);
            if(ir == null) {
                ir = new ItemRecord();
                ir.setQq(qq);
                ir.setItemName(itemName);
                ir.setCount(0);
                itemRecordDao.insert(ir);
            }
            ir.setCount(ir.getCount() + num);
            itemRecordDao.update(ir);
            return "获取成功，你还剩余" + ir.getCount() + "个" +
                    toEmojiItemName(ir.getItemName());
        } catch(IndexOutOfBoundsException e) {
            return "命令格式有误，提供的参数不足";
        } catch(NumberFormatException e2) {
            return "参数有误";
        }
    }

    public String toWordItemName(String name) {
        for(Map.Entry<String, String> entry : emojiWordNameMap.entrySet()) {
            String emojiName = entry.getKey();
            String wordName = entry.getValue();
            if(name.equals(emojiName) || name.equals(wordName))
                return wordName;
        }
        return null;
    }

    public String toEmojiItemName(String name) {
        for(Map.Entry<String, String> entry : emojiWordNameMap.entrySet()) {
            String emojiName = entry.getKey();
            String wordName = entry.getValue();
            if(name.equals(emojiName) || name.equals(wordName))
                return emojiName;
        }
        return null;
    }

    private void initMethodList() {
        Method[] declaredMethods = ItemUsingService.class.getDeclaredMethods();
        for(Method method : declaredMethods) {
            if(method.isAnnotationPresent(ItemUsingService.Item.class)) {
                ItemUsingService.Item anno = method.getAnnotation(
                        ItemUsingService.Item.class);
                itemMethods.put(anno.name(), method);
                String emojiName = EmojiUtils.unicodeToEmoji(
                        anno.emojiNameUnicode());
                itemMethods.put(emojiName, method);
                emojiWordNameMap.put(emojiName, anno.name());
            }
        }
    }

    public Map<String, String> getEmojiWordNameMap() {
        return emojiWordNameMap;
    }

    public ItemService() {
        initMethodList();
    }

    private final Map<String, String> emojiWordNameMap = new HashMap<>();

    private final Map<String, Method> itemMethods = new HashMap<>();

    @Resource
    private ItemUsingService itemUsingService;

    @Resource
    private WateringDao wateringDao;

    @Resource
    private ItemRecordDao itemRecordDao;

    @Resource
    private RobotBasicProperties basicProperties;
}
