package cn.aisino.misstudybackend.config;
/*
 * 注册 MyBatis-Plus 拦截器
 * 添加分页内部拦截器，使 MyBatis-Plus 在执行分页查询时自动拼接对应数据库的分页 SQL。
 */
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInnerInterceptor =
                new PaginationInnerInterceptor(DbType.MYSQL);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}