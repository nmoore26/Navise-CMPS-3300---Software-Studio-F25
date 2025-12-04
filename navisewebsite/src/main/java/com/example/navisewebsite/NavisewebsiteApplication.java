/*package com.example.navisewebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.navisewebsite")
public class NavisewebsiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(NavisewebsiteApplication.class, args);
    }
}
*/


package com.example.navisewebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class NavisewebsiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(NavisewebsiteApplication.class, args);
    }
}