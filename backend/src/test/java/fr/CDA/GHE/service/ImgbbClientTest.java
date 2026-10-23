package fr.CDA.GHE.service;

import fr.CDA.GHE.LogCapture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le lien de suppression d'une image est un secret (quiconque l'a peut supprimer l'image sur
 * l'hébergeur) : un échec de suppression ne doit pas l'écrire dans les logs (SEC-14).
 */
class ImgbbClientTest {

    @Test
    void delete_shouldNotLogTheDeletionLink_whenTheHostIsUnreachable() {
        ImgbbClient client = new ImgbbClient("cle-factice");

        try (LogCapture logs = new LogCapture(ImgbbClient.class.getName())) {
            client.delete("http://localhost:1/delete/LIEN-SECRET-DE-SUPPRESSION");

            assertThat(logs.lines()).hasSize(1);
            assertThat(logs.all())
                    .contains("Impossible de supprimer une image sur imgbb")
                    .doesNotContain("LIEN-SECRET-DE-SUPPRESSION");
        }
    }
}
