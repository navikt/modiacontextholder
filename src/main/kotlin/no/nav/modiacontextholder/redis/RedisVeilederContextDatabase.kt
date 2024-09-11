package no.nav.modiacontextholder.redis

import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.api.sync.RedisCommands
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.redis.model.RedisPEvent
import no.nav.modiacontextholder.redis.model.RedisPEventKey
import no.nav.modiacontextholder.redis.model.RedisVeilederContextType
import java.time.Duration

class RedisVeilederContextDatabase(
    private val redis: RedisCommands<String, String>,
    private val objectMapper: ObjectMapper,
) : VeilederContextDatabase {
    private val timeToLive = Duration.ofHours(12L)

    override fun save(veilederContext: VeilederContext) {
        val redisPEvent = RedisPEvent.from(veilederContext)
        val json = objectMapper.writeValueAsString(redisPEvent)

        val redisKey = redisPEvent.key.toString()
        when (redisPEvent.contextType) {
            RedisVeilederContextType.AKTIV_BRUKER -> redis.setex(redisKey, timeToLive.seconds, json)
            RedisVeilederContextType.AKTIV_ENHET -> redis.set(redisKey, json)
        }
    }

    override fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext? {
        val key = RedisPEventKey(RedisVeilederContextType.AKTIV_BRUKER, veilederIdent)

        val result = redis.get(key.toString()) ?: return null
        return objectMapper.readValue(result, RedisPEvent::class.java).toPEvent()
    }

    override fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext? {
        val key = RedisPEventKey(RedisVeilederContextType.AKTIV_ENHET, veilederIdent)

        val result = redis.get(key.toString()) ?: return null
        return objectMapper.readValue(result, RedisPEvent::class.java).toPEvent()
    }

    override fun slettAlleEventer(veilederIdent: String) {
        val keys =
            RedisVeilederContextType.entries
                .map { RedisPEventKey(it, veilederIdent).toString() }
                .toTypedArray()
        redis.del(*keys)
    }

    override fun slettAlleAvEventTypeForVeileder(
        contextType: VeilederContextType,
        veilederIdent: String,
    ) {
        val key = RedisPEventKey(RedisVeilederContextType.from(contextType), veilederIdent)
        redis.del(key.toString())
    }
}
