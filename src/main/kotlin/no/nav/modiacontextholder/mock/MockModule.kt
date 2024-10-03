package no.nav.modiacontextholder.mock

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.nom.NomClient
import no.nav.modiacontextholder.consumers.norg2.Norg2Client
import no.nav.modiacontextholder.service.AzureADService
import no.nav.modiacontextholder.service.PdlService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val mockModule =
    module {
        singleOf(::MockAzureADService) { bind<AzureADService>() }
        singleOf(::MockNomClient) { bind<NomClient>() }
        singleOf(::MockAxsysClient) { bind<AxsysClient>() }
        singleOf(::MockPdlService) { bind<PdlService>() }
        singleOf(::MockNorg2Client) { bind<Norg2Client>() }
        singleOf(::FakeUnleash) { bind<Unleash>() }
    }
