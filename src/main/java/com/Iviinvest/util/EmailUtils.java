package com.Iviinvest.util;

public class EmailUtils {
    /**
     * Mascarar e-mail, mantendo o primeiro caractere e o domínio.
     * Ex.: usuario@exemplo.com → u***@exemplo.com
     */
    public static String mask(String email) {
        if (email == null) return null;
        return email.replaceAll("(^.).*(@.*$)", "$1***$2");
    }
}
