package de.honoka.qqrobot.normal;

import de.honoka.qqrobot.normal.service.SystemService;
import de.honoka.qqrobot.starter.component.RobotConsoleWindow;
import de.honoka.sdk.util.file.FileUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QqRobotNormal {

    public static void main(String[] args) {
        RobotConsoleWindow console = RobotConsoleWindow.of("QQ Robot Normal",
                1.25, QqRobotNormal.class);
        console.setBeforeRunApplication(QqRobotNormal::checkAndOutputFiles);
        console.setOnExit(context -> {
            context.getBean(SystemService.class).shutdown();
        }).create();
        console.getContext().getBean(SystemService.class).init();
    }

    public static void checkAndOutputFiles() {
        Class<?> thisClass = QqRobotNormal.class;
        FileUtils.copyResourceIfNotExists(thisClass, "/qqrobot/mirai/" +
                "deviceInfo.json");
    }
}
