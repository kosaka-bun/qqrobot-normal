package de.honoka.qqrobot.normal.service;

import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.normal.dao.WateringDao;
import de.honoka.qqrobot.normal.entity.Watering;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Random;

/* 将浇水相关逻辑转移到此类中，增加道具功能，保留上次浇水时间但不再用它判断是否可以浇水。
 * 下一次浇水时间之后因为道具的影响可能是任意时长，增加这一字段。取消Master、Hentai等Buff/Debuff，
 *  转为道具作用效果。 */
@Service
public class WateringService {

    /**
     * 浇水操作主函数
     * 注意：需要修改数据的，直接被消息处理调用的方法，需要加锁
     *
     * @param qq 指定qq
     * @return 信息
     */
    @Transactional
    public String[] watering(long qq, long group) {
        Watering w = wateringDao.findAndLock(qq);   //检查指定qq的信息
        //如果已有信息
        if(w != null) {
            //判断现在到下一次可浇水的时间的间隔时间（毫秒）
            long diff = w.getNextTimeWatering().getTime() -
                    System.currentTimeMillis();
            //若现在的时间大于下一次可浇水的时间，则执行浇水
            /*
             * 小于1000的含义是当时间间隔小于1秒，或时间间隔为负值（表示当前时间已经大于
             * 下一次可浇水时间）。
             * 当间隔大于0小于1000时，会回复“请在0秒后再浇水”，这时候应该直接执行浇水，
             * 而不是继续等待
             */
            if(diff < 1000)
                return executeWatering(w, group);
            else {
                //用于整除计算时间
                long nh = 1000 * 60 * 60;
                long nm = 1000 * 60;
                long ns = 1000;
                //剩余多长时间才可以浇水
                int ramainHour = (int) (diff / nh);
                int ramainMinute = (int) (diff % nh / nm);
                int ramainSecond = (int) (diff % nh % nm / ns);
                String time = "";
                //当数值不为0且不为负值（非法值）才显示这个数值
                if(ramainHour > 0)
                    time += ramainHour + "小时";
                if(ramainMinute > 0)
                    time += ramainMinute + "分";
                if(ramainSecond > 0)
                    time += ramainSecond + "秒";
                return new String[] {
                        "现在不能浇水，请在" + time + "后再浇水",
                        ""
                };
            }
        } else {    //如果没有该用户的浇水信息，则新建该用户的信息，执行浇水操作
            w = new Watering();
            w.setQq(qq);
            wateringDao.insert(w);
            return executeWatering(w, group);
        }
    }

    /**
     * 处理浇水信息，如增加经验值等级等
     *
     * @param w 浇水信息对象
     * @return 提示信息
     */
    private String[] executeWatering(Watering w, long group) {
        Random ra = new Random();
        StringBuilder info;
        int getExp = ra.nextInt(101) + 50;  //生成50-150的随机数
        w.plusExp(getExp);
        info = new StringBuilder("浇水成功，你本次获得的经验为：" + getExp);
        while(w.getNowExp() >= w.getLevel() * 100) {  //避免连升几级
            w.setNowExp(w.getNowExp() - w.getLevel() * 100);
            w.setLevel(w.getLevel() + 1);
            //升完级之后当前经验小于最大经验
            if(w.getNowExp() < w.getLevel() * 100) {
                info.append("\n恭喜你已升到").append(w.getLevel())
                        .append("级");
            }
        }
        info.append("\n目前等级：" + "Lv").append(w.getLevel())
                .append(" (")
                .append(w.getNowExp()).append("/")
                .append(w.getLevel() * 100)
                .append(")");
        w.setLastTimeWatering(new Date(System.currentTimeMillis()));
        //默认浇水时长后可以浇水
        w.setNextTimeWatering(new Date(w.getLastTimeWatering()
                .getTime() + PERIOD));
        //存入数据库
        wateringDao.updateById(w);
        String[] result = { info.toString(), "" };
        //确保本次浇水信息存储成功后，随机掉落道具
        String itemName = itemService.drawItem(w.getQq(), group);
        //抽到奖品
        if(itemName != null) {
            result[1] += "你获得了一个" + itemService
                    .toEmojiItemName(itemName);
        }
        return result;
    }

    /**
     * 查询浇水排名
     *
     * @return 要回复的信息
     */
    public String getRank(long group, String[] args) {
        int num;    //要获取的信息条数，最多获取20条
        StringBuilder info = new StringBuilder();
        try {
            num = Integer.parseInt(args[0]);
        } catch(IndexOutOfBoundsException e) {
            //未提供参数的情况
            num = 5;
        } catch(NumberFormatException e) {
            //提供了错误的参数
            return "错误的参数";
        }
        if(num > 20) {
            num = 20;
            info.append("你要查询的信息太多，最多只能查询20条信息\n");
        }
        List<Watering> list = wateringDao.searchWateringRank(num);
        if(list.size() <= 0) {
            return "目前还没有人浇过水";
        }
        info.append("目前等级最高的前").append(num).append("名是：");
        for(int i = 0; i < list.size(); i++) {
            Watering w = list.get(i);
            info.append("\n").append(i + 1).append(".")
                    .append(framework.getNickOrCard(group, w.getQq()))
                    .append(" Lv").append(w.getLevel()).append(" (")
                    .append(w.getNowExp()).append("/").append(w.getLevel() * 100)
                    .append(")");
        }
        return info.toString();
    }

    /**
     * 查询指定qq的等级信息
     *
     * @param qq 指定qq
     * @return 要回复的内容
     */
    public String queryLevel(long group, long qq) {
        //检查指定qq的信息
        Watering w = wateringDao.selectById(qq);
        //如果已有信息
        if(w != null) {
            return framework.getNickOrCard(group, qq) + "的等级：Lv" +
                    w.getLevel() + " (" + w.getNowExp() + "/" +
                    (w.getLevel() * 100) + ")";
        } else {
            return framework.getNickOrCard(group, qq) +
                    "还没有浇过水";
        }
    }

    /**
     * 默认浇水间隔时间
     */
    private static final long PERIOD = 3 * 60 * 60 * 1000;

    @Resource
    private WateringDao wateringDao;

    @Lazy
    @Resource
    private Framework<?> framework;

    @Resource
    private ItemService itemService;
}
