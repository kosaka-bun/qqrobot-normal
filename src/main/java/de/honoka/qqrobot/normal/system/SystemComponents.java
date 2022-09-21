package de.honoka.qqrobot.normal.system;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 用于存放系统运行时需要的一些组件，如工厂对象等
 */
@Component
public class SystemComponents implements ApplicationContextAware {

    /**
     * 当前系统所使用的applicationContext
     */
    public static ApplicationContext applicationContext;

    //系统启动时，实例化此类，调用此方法，注入applicationContext成员
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext)
            throws BeansException {
        SystemComponents.applicationContext = applicationContext;
    }

    public static <T> T get(Class<T> type) {
        return applicationContext.getBean(type);
    }
}
