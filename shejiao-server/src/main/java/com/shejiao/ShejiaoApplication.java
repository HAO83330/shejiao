package com.shejiao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @Author shejiao
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ShejiaoApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(ShejiaoApplication.class, args);
        System.out.println("(๑˃̵ᴗ˂̵)و  社交平台启动成功   (•̀ᴗ•́)و ̑̑   \n" +
                "   _____ _                 _            \n" +
                "  / ____| |               | |           \n" +
                " | |    | |__   __ _ _ __ | |_ ___ _ __ \n" +
                " | |    | '_ \\ / _` | '_ \\| __/ _ \\ '__|\n" +
                " | |____| | | | (_| | | | | ||  __/ |   \n" +
                "  \\_____|_| |_|\\__,_|_| |_|\\__\\___|_|   "
        );
    }
}
