package no.nav.sbl.util;

import static java.lang.System.getProperty;

public class Utils {
    public static boolean isMasterNode() {
        return "true".equals(getProperty("cluster.ismasternode"));
    }
}
