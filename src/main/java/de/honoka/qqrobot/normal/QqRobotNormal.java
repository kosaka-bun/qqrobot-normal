package de.honoka.qqrobot.normal;

import de.honoka.qqrobot.normal.service.SystemService;
import de.honoka.qqrobot.starter.component.RobotConsoleWindow;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QqRobotNormal {

    public static void main(String[] args) {
        RobotConsoleWindow console = RobotConsoleWindow.of("QQ Robot Normal",
                1.25, QqRobotNormal.class);
        console.setOnExit(context -> {
            context.getBean(SystemService.class).shutdown();
        }).create();
        console.getContext().getBean(SystemService.class).init();
    }
}
