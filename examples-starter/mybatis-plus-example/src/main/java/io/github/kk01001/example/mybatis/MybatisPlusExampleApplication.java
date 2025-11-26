package io.github.kk01001.example.mybatis;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyBatis Plus 示例应用启动类
 *
 * @author kk01001
 */
@SpringBootApplication(exclude = DynamicDataSourceAutoConfiguration.class)
@MapperScan("io.github.kk01001.example.mybatis.mapper")
public class MybatisPlusExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusExampleApplication.class, args);
    }
}
