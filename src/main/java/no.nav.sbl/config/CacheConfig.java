package no.nav.sbl.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static int cacheSecondsInt = Integer.parseInt(System.getProperty("cache.config.seconds", "3600"));

    @Bean
    public net.sf.ehcache.CacheManager ehCacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(lagCacheConfig("enheterCache"));
        config.addCache(lagCacheConfig("veilederCache"));

        return net.sf.ehcache.CacheManager.newInstance(config);
    }


    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return null;
    }

    static class CustomKeyGenerator extends SimpleKeyGenerator {
        @Override
        public Object generate(Object target, Method method, Object... params) {
            return String.format("%s.%s(%s)", target.getClass().getName(), method.getName(), super.generate(target, method, params));
        }
    }

    static CacheConfiguration lagCacheConfig(String name) {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName(name);
        cacheConfiguration.setMaxEntriesLocalHeap(10000);
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
        cacheConfiguration.setTimeToIdleSeconds(6000);
        cacheConfiguration.setTimeToLiveSeconds(cacheSecondsInt);

        return cacheConfiguration;
    }
}
