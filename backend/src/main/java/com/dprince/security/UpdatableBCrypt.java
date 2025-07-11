package com.dprince.security;

import org.jboss.logging.Logger;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.function.Function;

/**
 * * @author Chris Ndayishimiye
 * * @created 8/6/2020
 */

public class UpdatableBCrypt {
    private static final Logger log = Logger.getLogger(UpdatableBCrypt.class);

    private static final int LOG_ROUNDS = 11;

    public UpdatableBCrypt(){}

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean verifyHash(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static boolean verifyAndUpdateHash(String password, String hash, Function<String, Boolean> updateFunc) {
        if (BCrypt.checkpw(password, hash)) {
            int rounds = getRounds(hash);
            if (rounds != LOG_ROUNDS) {
                log.debug("Updating password from "+rounds+" rounds to "+ LOG_ROUNDS);
                String newHash = hash(password);
                return updateFunc.apply(newHash);
            }
            return true;
        }
        return false;
    }

    private static int getRounds(String salt) {
        char minor = (char)0;
        int off = 0;

        if (salt.charAt(0) != '$' || salt.charAt(1) != '2')
            throw new IllegalArgumentException ("Invalid salt version");
        if (salt.charAt(2) == '$')
            off = 3;
        else {
            minor = salt.charAt(2);
            if (minor != 'a' || salt.charAt(3) != '$')
                throw new IllegalArgumentException ("Invalid salt revision");
            off = 4;
        }
        if (salt.charAt(off + 2) > '$')
            throw new IllegalArgumentException ("Missing salt rounds");
        return Integer.parseInt(salt.substring(off, off + 2));
    }
}