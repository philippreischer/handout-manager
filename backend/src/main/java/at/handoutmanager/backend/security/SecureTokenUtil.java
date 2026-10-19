package at.handoutmanager.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Creates and hashes opaque tokens that are stored as a hash and compared by
 * lookup - refresh tokens and password reset tokens. Not for JWTs, which are
 * signed rather than stored.
 */
public final class SecureTokenUtil {

    // SecureRandom is thread-safe, so one shared instance is fine.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private SecureTokenUtil() {
    }

    public static String createRawToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * No BCrypt here on purpose: the input is a high-entropy random value, so a
     * slow hash buys nothing - and its per-row salt would make a lookup by hash
     * impossible.
     */
    public static String hash(String rawToken) {
        try {
            // MessageDigest is not thread-safe - a new instance per call, never a field.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
