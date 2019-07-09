package am.caritas.caritasfiles.security;


import am.caritas.caritasfiles.model.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/")
                .permitAll()
                .antMatchers("/admin/**")
                .hasAnyAuthority(Role.ADMIN.name())
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/login?error")
                .and()
                .rememberMe()
                .rememberMeParameter("REMEMBER_ME")
                .rememberMeCookieName("RM")
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(3600 * 1000 * 24 * 7)
                .and()
                .logout()
                .logoutSuccessUrl("/login")
                .deleteCookies("RM");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}