package no.nav.modiacontextholder.naudit

class AuditResources {
    companion object {
        @JvmField
        val NullstillKontekst = Audit.AuditResource("kontekst.nullstill")

        @JvmField
        val OppdaterKontekst = Audit.AuditResource("kontekst.oppdater")

        @JvmField
        val NullstillBrukerIKontekst = Audit.AuditResource("kontekst.bruker.nullstill")
    }
}
