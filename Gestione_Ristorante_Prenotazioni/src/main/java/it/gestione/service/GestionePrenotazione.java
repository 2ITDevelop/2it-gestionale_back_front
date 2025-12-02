package it.gestione.service;

import it.gestione.database.PrenotazioneDAO;
import it.gestione.entity.Prenotazione;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GestionePrenotazione {

    private final PrenotazioneDAO prenotazioneDAO;

    public GestionePrenotazione(PrenotazioneDAO prenotazioneDAO) {
        this.prenotazioneDAO = prenotazioneDAO;
    }

    /**
     * Crea una prenotazione a partire dai singoli parametri.
     *
     * Ritorni:
     *  1  = inserita correttamente
     *  0  = già esistente (stessa data + nome)
     * -1  = errore o parametri non validi
     */
    public int creaPrenotazione(String n, int num, LocalDate d, LocalTime o, String numT) {
        // Validazione base
        if (n == null || n.isBlank() || num <= 0 || d == null || o == null) {
            return -1;
        }

        Prenotazione p = new Prenotazione(n, num, d, o, numT);
        return prenotazioneDAO.aggiungiPrenotazione(p);
    }

    /**
     * Crea una prenotazione a partire da un oggetto Prenotazione.
     * Delega alla versione con i parametri singoli (come GestioneWorkingDay).
     */
    public int creaPrenotazione(Prenotazione p) {
        if (p == null) {
            return -1;
        }

        return creaPrenotazione(
                p.getNome(),
                p.getNumPersone(),
                p.getDate(),
                p.getOrario(),
                p.getNumeroTelefono()
        );
    }

    /**
     * Ritorna tutte le prenotazioni.
     */
    public List<Prenotazione> getAllPrenotazioni() {
        return prenotazioneDAO.getAllPrenotazioni();
    }

    /**
     * Ritorna tutte le prenotazioni per una certa data.
     * Se data è null, ritorna lista vuota.
     */
    public List<Prenotazione> getPrenotazioniByData(LocalDate data) {
        if (data == null) {
            return new ArrayList<>();
        }
        return prenotazioneDAO.getPrenotazioniByData(data);
    }

    /**
     * Ritorna una singola prenotazione identificata da data + nome.
     * Se parametri non validi → null.
     */
    public Prenotazione getPrenotazione(LocalDate data, String nome) {
        if (data == null || nome == null || nome.isBlank()) {
            return null;
        }
        return prenotazioneDAO.getPrenotazione(data, nome);
    }

    /**
     * Elimina una prenotazione (PK: data + nome).
     *
     * Ritorna:
     *  1  = eliminata
     *  0  = non trovata
     * -1  = errore o parametri non validi
     */
    public int eliminaPrenotazione(LocalDate data, String nome) {
        if (data == null || nome == null || nome.isBlank()) {
            return -1;
        }
        return prenotazioneDAO.eliminaPrenotazione(data, nome);
    }

}
