package no.nav.sbl.redis

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType
import no.nav.sbl.redis.model.RedisEventType
import no.nav.sbl.redis.model.RedisPEvent
import no.nav.sbl.redis.model.RedisPEventKey
import java.time.Duration

class RedisVeilederContextDatabase(
    private val authJedisPool: AuthJedisPool,
    private val objectMapper: ObjectMapper,
) : VeilederContextDatabase {
    private val timeToLive = Duration.ofHours(12L)

    override fun save(veilederContext: VeilederContext): Unit =
        runBlocking {
            val redisPEvent = RedisPEvent.from(veilederContext)
            val json = objectMapper.writeValueAsString(redisPEvent)

            authJedisPool
                .useResource {
                    it.setex(redisPEvent.key.toString(), timeToLive.seconds, json)
                }.getOrThrow()
        }

    override fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext? =
        runBlocking {
            val key = RedisPEventKey(RedisEventType.AKTIV_BRUKER, veilederIdent)

            authJedisPool
                .useResource {
                    it.get(key.toString())
                }.map {
                    if (it == null) return@map null
                    objectMapper.readValue(it, RedisPEvent::class.java).toPEvent()
                }.getOrThrow()
        }

    override fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext? =
        runBlocking {
            val key = RedisPEventKey(RedisEventType.AKTIV_ENHET, veilederIdent)

            authJedisPool
                .useResource {
                    it.get(key.toString())
                }.map {
                    if (it == null) return@map null
                    objectMapper.readValue(it, RedisPEvent::class.java).toPEvent()
                }.getOrThrow()
        }

    override fun slettAlleEventer(veilederIdent: String): Unit =
        runBlocking {
            val keys =
                RedisEventType.entries
                    .map { RedisPEventKey(it, veilederIdent).toString() }
                    .toTypedArray()

            authJedisPool
                .useResource { jedis ->
                    jedis.del(*keys)
                }.getOrThrow()
        }

    override fun slettAlleAvEventTypeForVeileder(
        eventType: VeilederContextType,
        veilederIdent: String,
    ): Unit =
        runBlocking {
            val key = RedisPEventKey(RedisEventType.from(eventType), veilederIdent)

            authJedisPool
                .useResource { jedis ->
                    jedis.del(key.toString())
                }.getOrThrow()
        }
}
