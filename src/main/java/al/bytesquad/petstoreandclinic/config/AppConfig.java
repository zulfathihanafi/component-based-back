package al.bytesquad.petstoreandclinic.config;


import com.zaxxer.hikari.HikariDataSource;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "al.bytesquad.petstoreandclinic")
@PropertySource("classpath:application.properties")
public class AppConfig {

    private final Environment environment;

    @Autowired
    public AppConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".jsp");
        viewResolver.setPrefix("/WEB-INF/view/");
        return viewResolver;
    }


    @Bean
    public DataSource dataSource() {

        HikariDataSource comboPooledDataSource = new HikariDataSource();

        
            comboPooledDataSource.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name"));
        

        comboPooledDataSource.setJdbcUrl(environment.getProperty("spring.datasource.url"));
        comboPooledDataSource.setUsername(environment.getProperty("spring.datasource.username"));
        comboPooledDataSource.setPassword(environment.getProperty("spring.datasource.password"));

        return comboPooledDataSource;
    }

}
