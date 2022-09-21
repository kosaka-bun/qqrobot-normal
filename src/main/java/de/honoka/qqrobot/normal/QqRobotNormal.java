package de.honoka.qqrobot.normal;

import de.honoka.qqrobot.framework.Framework;
import de.honoka.qqrobot.normal.service.SystemService;
import de.honoka.qqrobot.normal.system.ExtendRobotAttributes;
import de.honoka.qqrobot.normal.system.SystemComponents;
import de.honoka.qqrobot.starter.component.RobotAttributes;
import de.honoka.sdk.util.file.FileUtils;
import de.honoka.sdk.util.system.gui.ConsoleWindow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class QqRobotNormal {

    private static SystemService systemService;

    public static void main(String[] args) {
        //region 初始化窗口与托盘图标
        ConsoleWindow console = new ConsoleWindow("QQ Robot Normal",
                null, QqRobotNormal::exit);
        console.setAutoScroll(true);
        console.setScreenZoomScale(1.25);
        ExtendRobotAttributes.consoleWindow = console;
        console.show();
        //endregion
        //region 构建应用、加载配置、启动应用
        checkAndOutputFiles();
        SpringApplication app = new SpringApplication(QqRobotNormal.class);
        app.run(args);
        //endregion
        //region 装配组件
        ApplicationContext context = SystemComponents.applicationContext;
        RobotAttributes attributes = context.getBean(RobotAttributes.class);
        attributes.consoleWindow = console;
        systemService = context.getBean(SystemService.class);
        systemService.init();
        //添加托盘图标菜单项
        console.addTrayIconMenuItem("Relogin", true,
                context.getBean(Framework.class)::reboot);
        //endregion
    }

    public static void exit() {
        systemService.shutdown();
    }

    public static void checkAndOutputFiles() {
        Class<?> thisClass = QqRobotNormal.class;
        FileUtils.checkResources(thisClass, "/qqrobot/mirai/" +
                "deviceInfo.json");
    }
}
