# Modiacontextholder

Håndterer kontekst for veiledere på tvers av flater.

## Bruk

OpenAPI dokumentasjon er tilgjengelig på https://Modiacontextholder.intern.dev.nav.no/swagger

## Utvikling

Kjør `main` funksjonen i Main.kt. Lokalt miljø skal detekteres automatisk og avhengigheter vil
mockes automatisk.

## Dokumentasjon
### Caching 
Denne applikasjonen bruker både lokal in-memory caching og distribuert caching i Redis/Valkey.

#### Oversikt

##### Lokal in-memory cache
For data som ikke må deles på tvers av instanser, brukes in-memory caching basert på Caffeine.

##### Distribuert cache i Redis/Valkey
For data som må deles på tvers av instanser, brukes distribuert caching i Redis/Valkey.

- Den distribuerte cachen bruker en dedikert Redis database (`REDIS_CACHE_DB = 1`).
- Cache-keys navngis slik: `<cacheNavn>:<key>`
  - Standard cache-navn genereres som `cache-<UUID>`, hvor UUID genereres tilfeldig.
- Verdiene lagres som serialiserte JSON-strenger ved hjelp av Kotlin serialization.

#### Databaser
Applikasjonen benytter to Redis databaser:
- **DB `0`**: standarddatabase for kontekst-data.
- **DB `1`**: distribuert cache.

### Lese ut og rydde opp i verdier i cachen
For å lese ut eller rydde opp i verdier i cachen kan du benytte `redis-cli`.

#### Installasjon av redis-cli
Følg instruksjonene på https://redis.io/docs/getting-started/installation

#### Hvordan bruke redis-cli
Koble til Redis instansen ved å bruke følgende kommando (verdier må erstattes med secrets fra Kubernetes):
```bash
redis-cli -u rediss://<brukernavn>:<passord>@<host>:<port>
```

**Velg database**
- ***OBS!*** DB 0 er standard – for å jobbe med distribuert cache, bytt til DB 1:
```bash
SELECT 1
```

**Hent ut keys**
```bash
KEYS *
``` 

**Slett en key**
```bash
DEL <key-name>
```

**Tøm valgt database (valgt med SELECT)**
```bash
FLUSHDB
``` 

**Tøm alle databaser**
```bash
FLUSHALL
```

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

[Team Personoversikt](https://github.com/navikt/info-team-personoversikt)
