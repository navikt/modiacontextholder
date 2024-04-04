package no.nav.sbl.config
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.jsr107.Eh107Configuration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import javax.cache.CacheManager
import javax.cache.Caching

@Configuration
@EnableCaching
open class CacheConfig {
    private val cacheSecondsInt = System.getProperty("cache.config.seconds", "3600").toLong()

    @Bean
    open fun createCacheWithinManager(): CacheManager {
        val cacheConfiguration =
            CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                    String::class.java,
                    String::class.java,
                    ResourcePoolsBuilder.heap(1000).build(),
                )
                .withExpiry(
                    ExpiryPolicyBuilder
                        .timeToIdleExpiration(Duration.ofSeconds(cacheSecondsInt)),
                ).build()

        val cachingProvider = Caching.getCachingProvider()
        val cacheManager = cachingProvider.cacheManager

        // parsing ehcache configuration to something SpringBoot will understand
        val configuration = Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration)

        cacheManager.createCache("enheterCache", configuration)
        cacheManager.createCache("veilederCache", configuration)
        cacheManager.createCache("veilederRolleCache", configuration)
        cacheManager.createCache("ldapCache", configuration)

        // add shutdown hook to close cacheManager
        Runtime.getRuntime().addShutdownHook(Thread(cacheManager::close))

        return cacheManager
    }
}
