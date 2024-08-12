package no.nav.sbl.redis

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType
import no.nav.sbl.redis.model.RedisPEvent
import no.nav.sbl.redis.model.RedisPEventKey
import no.nav.sbl.redis.model.RedisVeilederContextType
import redis.clients.jedis.JedisPool
import java.time.Duration

class RedisVeilederContextDatabase(
    private val jedisPool: JedisPool,
    private val objectMapper: ObjectMapper,
) : VeilederContextDatabase {
    private val timeToLive = Duration.ofHours(12L)

    override fun save(veilederContext: VeilederContext) {
        val redisPEvent = RedisPEvent.from(veilederContext)
        val json = objectMapper.writeValueAsString(redisPEvent)

        val redisKey = redisPEvent.key.toString()
        jedisPool.resource.use { jedis ->
            when (redisPEvent.contextType) {
                RedisVeilederContextType.AKTIV_BRUKER -> jedis.setex(redisKey, timeToLive.seconds, json)
                RedisVeilederContextType.AKTIV_ENHET -> jedis.set(redisKey, json)
            }
        }
    }

    override fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext? {
        val key = RedisPEventKey(RedisVeilederContextType.AKTIV_BRUKER, veilederIdent)

        val result = jedisPool.resource.use { jedis -> jedis.get(key.toString()) } ?: return null
        return objectMapper.readValue(result, RedisPEvent::class.java).toPEvent()
    }

    override fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext? {
        val key = RedisPEventKey(RedisVeilederContextType.AKTIV_ENHET, veilederIdent)

        val result = jedisPool.resource.use { jedis -> jedis.get(key.toString()) } ?: return null
        return objectMapper.readValue(result, RedisPEvent::class.java).toPEvent()
    }

    override fun slettAlleEventer(veilederIdent: String) {
        val keys =
            RedisVeilederContextType.entries
                .map { RedisPEventKey(it, veilederIdent).toString() }
                .toTypedArray()
        jedisPool.resource.use { jedis -> jedis.del(*keys) }
    }

    override fun slettAlleAvEventTypeForVeileder(
        contextType: VeilederContextType,
        veilederIdent: String,
    ) {
        val key = RedisPEventKey(RedisVeilederContextType.from(contextType), veilederIdent)
        jedisPool.resource.use { jedis -> jedis.del(key.toString()) }
    }
}
