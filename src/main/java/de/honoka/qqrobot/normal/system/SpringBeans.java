package de.honoka.qqrobot.normal.system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Timer;

/**
 * 装载一些不能直接通过注解来装载的实例（如多构造器类）
 */
@Configuration
public class SpringBeans {

    @Bean
    public Gson gson() {
        //生成便于查看的json文件的gson操作对象
        return new GsonBuilder()
                .setDateFormat("yyyy年MM月dd日 HH:mm:ss")
                .setPrettyPrinting().create();
    }

    //定时任务管理器
    @Bean
    public Timer timer() {
        return new Timer();
    }
}
