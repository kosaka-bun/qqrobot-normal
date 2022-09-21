package de.honoka.qqrobot.normal.controller;

import de.honoka.qqrobot.normal.service.WateringService;
import de.honoka.qqrobot.starter.command.CommandMethodArgs;
import de.honoka.qqrobot.starter.common.annotation.Command;
import de.honoka.qqrobot.starter.common.annotation.RobotController;

import javax.annotation.Resource;

/**
 * 浇水类命令控制器
 */
@SuppressWarnings("unused")
@RobotController
public class WateringController {

    @Command("浇水")
    public String watering(CommandMethodArgs mArgs) {
        String[] result = wateringService.watering(mArgs.getQq(),
                mArgs.getGroup());
        String reply = result[0];
        if(result[1] != null && !result[1].equals("")) {
            reply += "\n" + result[1];
        }
        return reply;
    }

    @Resource
    private WateringService wateringService;
}
