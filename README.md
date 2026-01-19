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

##### In-memory cache
For data som ikke må deles på tvers av instanser, brukes in-memory caching basert på Caffeine.

- `EnheterCache` cacher enhetsdata hentet fra NORG2.
  - Cachen fylles ved oppstart av applikasjonen og refreshes hver 6. time.

##### Distribuert cache i Redis/Valkey
For data som må deles på tvers av instanser, brukes distribuert caching i Redis. Her er det satt opp
to databaser med ulike formål:

###### DB '0' – kontekst-data
Brukes til lagring av kontekst-data for innloggede brukere.

Eksempler på keys:
- `veiledercontext:AKTIV_ENHET:<nav-ident>` - aktiv enhet for innlogget bruker
- `veiledercontext:AKTIV_BRUKER:<nav-ident>` - aktiv bruker (åpnet person)
- `veiledercontext:AKTIV_GRUPPE_ID:<nav-ident>` - aktiv gruppe_id

###### DB '1' – generell cache
Brukes til caching av oppslått data fra eksterne tjenester for å unngå gjentatte kall.

- Nøkler følger mønsteret: `<cacheNavn>:<key>` der `key` typisk er `nav-ident`.
- Verdier lagres med utløpstid, slik at de automatisk forsvinner etter en periode.
- 
Eksempler på keys:
- `enheter:<nav-ident>` - enheter innlogget bruker kan velge mellom i dropdown-meny (basert på tilganger og NORG2 data)
- `veileder:<nav-ident>` - personalinfo knyttet til innlogget bruker
- `azuread:<nav-ident>` - tilganger for innlogget bruker

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
