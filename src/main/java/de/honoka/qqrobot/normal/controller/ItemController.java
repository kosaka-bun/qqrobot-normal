package de.honoka.qqrobot.normal.controller;

import de.honoka.qqrobot.normal.service.ItemService;
import de.honoka.qqrobot.starter.command.CommandMethodArgs;
import de.honoka.qqrobot.starter.common.annotation.Command;
import de.honoka.qqrobot.starter.common.annotation.RobotController;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Resource;

/**
 * 道具类命令控制器
 */
@SuppressWarnings("unused")
@RobotController
public class ItemController {

    //获取道具 [道具名] [数量]
    @Command(value = "获取道具", argsNum = 2, admin = true)
    public String getItem(CommandMethodArgs mArgs) {
        return itemService.makeItems(ArrayUtils.toStringArray(
                mArgs.getArgs()));
    }

    //赠送道具 [道具名] [数量] [@]
    @Command(value = "赠送道具", argsNum = 3)
    public String giveItem(CommandMethodArgs mArgs) {
        long atQQ = mArgs.getAt(2).getContent();
        return itemService.prepareGiveItems(mArgs.getQq(), atQQ,
                ArrayUtils.toStringArray(mArgs.getArgs()));
    }

    //使用道具 [道具名] [@]
    @Command(value = "使用道具", argsNum = 2)
    public String useItem(CommandMethodArgs mArgs) {
        long atQQ = mArgs.getAt(1).getContent();
        return itemService.prepareUseItem(
                atQQ, mArgs.getQq(),
                mArgs.getGroup(),
                ArrayUtils.toStringArray(mArgs.getArgs())
        );
    }

    @Command("我的道具")
    public String myItems(CommandMethodArgs mArgs) {
        return itemService.getItemRecords(mArgs.getQq());
    }

    @Resource
    private ItemService itemService;
}
