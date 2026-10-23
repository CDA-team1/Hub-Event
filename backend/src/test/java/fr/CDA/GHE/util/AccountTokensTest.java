package fr.CDA.GHE.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de {@link AccountTokens} : génération aléatoire et empreinte stockée en base (SEC-10).
 */
class AccountTokensTest {

    @Test
    void generate_shouldProduce256BitUrlSafeTokens() {
        String token = AccountTokens.generate();

        // 32 octets en base64 URL sans remplissage = 43 caractères, utilisables tels quels dans un lien
        assertThat(token).hasSize(43).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void generate_shouldNeverRepeat() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            tokens.add(AccountTokens.generate());
        }

        assertThat(tokens).hasSize(2000);
    }

    @Test
    void hash_shouldBeTheSha256InHexadecimal() {
        // Vecteur de test officiel de SHA-256 : "abc"
        assertThat(AccountTokens.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void hash_shouldBeStableAndDifferentFromTheToken() {
        String token = AccountTokens.generate();

        assertThat(AccountTokens.hash(token)).isEqualTo(AccountTokens.hash(token)).hasSize(64).isNotEqualTo(token);
        assertThat(AccountTokens.hash(token)).isNotEqualTo(AccountTokens.hash(AccountTokens.generate()));
    }
}
