package de.honoka.qqrobot.normal.controller;

import de.honoka.qqrobot.normal.service.FunctionService;
import de.honoka.qqrobot.normal.service.SystemService;
import de.honoka.qqrobot.normal.service.WateringService;
import de.honoka.qqrobot.starter.command.CommandMethodArgs;
import de.honoka.qqrobot.starter.common.annotation.Command;
import de.honoka.qqrobot.starter.common.annotation.RobotController;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Resource;

/**
 * 功能类命令控制器
 */
@SuppressWarnings("unused")
@RobotController
public class FunctionController {

    @Command("百科")
    public String wiki(CommandMethodArgs mArgs) {
        return functionService.getBaike(ArrayUtils.toStringArray(
                mArgs.getArgs()));
    }

    @Command(value = "等级查询", argsNum = 1)
    public String levelQuery(CommandMethodArgs mArgs) {
        long atQQ = mArgs.getAt(0).getContent();
        return wateringService.queryLevel(mArgs.getGroup(), atQQ);
    }

    @Command("等级排名")
    public String levelRank(CommandMethodArgs mArgs) {
        return wateringService.getRank(mArgs.getGroup(), ArrayUtils
                .toStringArray(mArgs.getArgs()));
    }

    @Resource
    private WateringService wateringService;

    @Resource
    private FunctionService functionService;

    @Resource
    private SystemService systemService;
}
