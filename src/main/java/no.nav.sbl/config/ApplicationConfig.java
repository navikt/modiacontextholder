package no.nav.sbl.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.ServletUtil;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.rest.CleanupServlet;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ComponentScan("no.nav.sbl.rest")
@Import({
        DatabaseConfig.class,
        ServiceContext.class
})
public class ApplicationConfig implements ApiApplication {

    @Override
    @SneakyThrows
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .validateAzureAdInternalUsersTokens();
    }

    @Override
    public void startup(ServletContext servletContext) {
        CleanupServlet cleanupServlet = new CleanupServlet(WebApplicationContextUtils.findWebApplicationContext(servletContext).getBean(DatabaseCleanerService.class));
        ServletUtil.leggTilServlet(servletContext, cleanupServlet, "/internal/cleanup");
    }


}
