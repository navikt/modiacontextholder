package no.nav.sbl.redis

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.sbl.db.VeilederContextDatabase
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.redis.model.RedisEventType
import no.nav.sbl.redis.model.RedisPEvent
import no.nav.sbl.redis.model.RedisPEventKey

class RedisVeilederContextDatabase(
    private val authJedisPool: AuthJedisPool,
    private val objectMapper: ObjectMapper,
) : VeilederContextDatabase {
    override fun save(pEvent: PEvent): Unit =
        runBlocking {
            val redisPEvent = RedisPEvent.from(pEvent)
            val json = objectMapper.writeValueAsString(redisPEvent)

            authJedisPool
                .useResource {
                    it.set(redisPEvent.key.toString(), json)
                }.getOrThrow()
        }

    override fun sistAktiveBrukerEvent(veilederIdent: String): PEvent? =
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

    override fun sistAktiveEnhetEvent(veilederIdent: String): PEvent? =
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

    override fun slettAllEventer(veilederIdent: String): Unit =
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
        eventType: String,
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
