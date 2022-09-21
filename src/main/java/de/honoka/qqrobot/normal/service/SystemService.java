package de.honoka.qqrobot.normal.service;

import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.starter.component.RobotAttributes;
import de.honoka.sdk.util.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;

/**
 * 系统服务，通过系统获取参数，以及系统运行相关的逻辑
 */
@Slf4j
@Service
public class SystemService {

    //机器人全局初始化方法，在Spring框架加载完成后，机器人启动前执行
    public void init() {
        //不重要的初始化操作可以放入新线程中处理，以免减缓程序加载
        new Thread(() -> {
            //none
        }).start();
        framework.boot();
        //开启消息处理开关
        attributes.isEnabled = true;
        log.info("classpath: "  + FileUtils.getClasspath());
        log.info("file.encoding: " + System.getProperty("file.encoding") +
                "\nsun.jnu.encoding: " + System.getProperty("sun.jnu.encoding"));
    }

    //机器人全局关闭与保存方法，软件关闭前执行
    public void shutdown() {
        framework.stop();
        entityManagerFactory.close();
    }

    @Resource
    private EntityManagerFactory entityManagerFactory;

    @Resource
    private RobotAttributes attributes;

    @Resource
    private Framework<?> framework;
}
