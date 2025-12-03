package it.gestione.service;

import it.gestione.database.ConfigurazioneSalaDAO;
import it.gestione.database.PrenotazioneTavoloDAO;
import it.gestione.database.SalaDAO;
import it.gestione.database.TavoloDAO;
import it.gestione.database.ZonaSalaDAO;
import it.gestione.entity.ConfigurazioneSala;
import it.gestione.entity.Prenotazione;
import it.gestione.entity.Sala;
import it.gestione.entity.StatoTavolo;
import it.gestione.entity.Tavolo;
import it.gestione.entity.Turno;
import it.gestione.entity.ZonaSala;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class GestioneSala {

    private final ConfigurazioneSalaDAO configurazioneSalaDAO;
    private final TavoloDAO tavoloDAO;
    private final SalaDAO salaDAO;
    private final ZonaSalaDAO zonaSalaDAO;

    // ---------- NUOVI CAMPI PER ASSOCIAZIONE PRENOTAZIONE <-> TAVOLI ---------- //
    private final PrenotazioneTavoloDAO prenotazioneTavoloDAO;
    private final GestionePrenotazione gestionePrenotazione;

    public GestioneSala(ConfigurazioneSalaDAO configurazioneSalaDAO,
                        TavoloDAO tavoloDAO,
                        SalaDAO salaDAO,
                        ZonaSalaDAO zonaSalaDAO,
                        PrenotazioneTavoloDAO prenotazioneTavoloDAO,
                        GestionePrenotazione gestionePrenotazione) {
        this.configurazioneSalaDAO = configurazioneSalaDAO;
        this.tavoloDAO = tavoloDAO;
        this.salaDAO = salaDAO;
        this.zonaSalaDAO = zonaSalaDAO;
        this.prenotazioneTavoloDAO = prenotazioneTavoloDAO;
        this.gestionePrenotazione = gestionePrenotazione;
    }

    // ===================== GESTIONE SALA FISICA (Sala + ZonaSala) ===================== //

    /**
     * Crea una sala e, se presenti, inserisce anche le sue zone (ZonaSala).
     *
     * Convenzione ritorno:
     *  1 = sala inserita (almeno a livello di riga in SALA;
     *      le zone vengono inserite best-effort, se una fallisce ritorna -1)
     *  0 = sala già presente (in questo caso tenta comunque di inserire le zone,
     *      grazie a ON CONFLICT DO NOTHING su zona_sala è idempotente)
     * -1 = errore SQL (o nella sala o in una delle zone)
     */
    public int creaSalaConZone(Sala sala) {
        if (sala == null || sala.getNome() == null || sala.getNome().isBlank()) {
            return -1;
        }

        int resSala = salaDAO.aggiungiSala(sala);
        if (resSala == -1) {
            return -1;
        }

        // Inserimento zone (se presenti)
        if (sala.getZone() != null) {
            for (ZonaSala z : sala.getZone()) {
                int rz = zonaSalaDAO.aggiungiZona(sala.getNome(), z);
                if (rz == -1) {
                    return -1;
                }
            }
        }

        return resSala; // 1 se nuova sala, 0 se già esisteva
    }

    /**
     * Aggiorna completamente le zone di una sala:
     * - cancella tutte le zone esistenti
     * - inserisce quelle presenti nell'oggetto Sala
     *
     * Utile se il front ricostruisce da zero la mappa e la rimanda.
     */
    public int aggiornaZoneSala(Sala sala) {
        if (sala == null || sala.getNome() == null || sala.getNome().isBlank()) {
            return -1;
        }

        // pulisco tutte le zone esistenti per quella sala
        int del = zonaSalaDAO.eliminaTutteLeZone(sala.getNome());
        if (del == -1) {
            return -1;
        }

        // reinserisco le nuove zone
        if (sala.getZone() != null) {
            for (ZonaSala z : sala.getZone()) {
                int rz = zonaSalaDAO.aggiungiZona(sala.getNome(), z);
                if (rz == -1) {
                    return -1;
                }
            }
        }

        return 1;
    }

    /**
     * Restituisce una sala dal DB, con tutte le sue ZoneSala caricate.
     */
    public Sala getSala(String nomeSala) {
        return salaDAO.getSala(nomeSala);
    }

    /**
     * Restituisce tutte le sale presenti nel DB (ognuna con le sue zone).
     */
    public List<Sala> getAllSale() {
        return salaDAO.getAllSale();
    }

    /**
     * Elimina una sala.
     * Grazie ai vincoli ON DELETE CASCADE su zona_sala, configurazione_sala e tavolo_sala,
     * verranno eliminate anche:
     *  - le zone di quella sala
     *  - tutte le configurazioni di quella sala
     *  - tutti i tavoli delle configurazioni di quella sala
     */
    public int eliminaSala(String nomeSala) {
        return salaDAO.eliminaSala(nomeSala);
    }

    // ================= CONFIGURAZIONE SALA ================= //

    /**
     * Crea una configurazione per una certa sala, data e turno.
     * Presuppone che la sala esista già in DB.
     */
    public int aggiungiConfigurazione(LocalDate data, Turno turno, Sala sala) {
        ConfigurazioneSala c = new ConfigurazioneSala(data, turno, sala);
        return configurazioneSalaDAO.aggiungiConfigurazione(c);
    }

    /**
     * Elimina una configurazione specifica per (data, turno, sala).
     */
    public int eliminaConfigurazione(LocalDate data, Turno turno, Sala sala) {
        return configurazioneSalaDAO.eliminaConfigurazione(data, turno, sala.getNome());
    }

    /**
     * Restituisce una configurazione specifica per (data, turno, sala).
     * Se non trovata -> null.
     */
    public ConfigurazioneSala getConfigurazione(LocalDate data, Turno turno, Sala sala) {
        return configurazioneSalaDAO.getConfigurazione(data, turno, sala.getNome());
    }

    /**
     * Restituisce tutte le configurazioni sala presenti a DB
     * (per tutte le sale, date e turni).
     */
    public List<ConfigurazioneSala> getAllConfigurazioni() {
        return configurazioneSalaDAO.getAllConfigurazioni();
    }

    // ===================== TAVOLI (CRUD base) ===================== //

    /**
     * Aggiunge un tavolo in una configurazione sala (data, turno, sala).
     */
    public int aggiungiTavolo(LocalDate data, Turno turno, Sala sala, Tavolo t) {
        return tavoloDAO.aggiungiTavolo(data, turno, sala.getNome(), t);
    }

    public int aggiungiTavolo(LocalDate data, Turno turno, Sala sala,
                              int x, int y, StatoTavolo stato) {
        Tavolo t = new Tavolo(x, y, stato);
        return tavoloDAO.aggiungiTavolo(data, turno, sala.getNome(), t);
    }

    /**
     * Ottiene tutti i tavoli di una configurazione (data, turno, sala).
     */
    public List<Tavolo> getTavoli(LocalDate data, Turno turno, Sala sala) {
        return tavoloDAO.getTavoli(data, turno, sala.getNome());
    }

    /**
     * Ottiene un singolo tavolo di una configurazione (data, turno, sala).
     */
    public Tavolo getTavolo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return tavoloDAO.getTavolo(data, turno, sala.getNome(), x, y);
    }

    /**
     * Modifica lo stato di un tavolo in una configurazione (data, turno, sala).
     */
    public int aggiornaStatoTavolo(LocalDate data, Turno turno, Sala sala,
                                   int x, int y, StatoTavolo nuovoStato) {
        return tavoloDAO.aggiornaStato(data, turno, sala.getNome(), x, y, nuovoStato);
    }

    /**
     * Rimuove un tavolo da una configurazione (data, turno, sala).
     */
    public int eliminaTavolo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return tavoloDAO.eliminaTavolo(data, turno, sala.getNome(), x, y);
    }

    // Convenience methods per stato singolo tavolo

    public int liberaTavolo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return aggiornaStatoTavolo(data, turno, sala, x, y, StatoTavolo.LIBERO);
    }

    public int riservaTavolo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return aggiornaStatoTavolo(data, turno, sala, x, y, StatoTavolo.RISERVATO);
    }

    public int occupaTavolo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return aggiornaStatoTavolo(data, turno, sala, x, y, StatoTavolo.OCCUPATO);
    }

    // ===================== POSTI PER GRUPPO ===================== //

    /**
     * Calcola il numero di posti per ogni gruppo di tavoli adiacenti
     * in una specifica configurazione (data, turno, sala).
     *
     * Gruppo = componente connessa su 4-direzioni (su/giù/sinistra/destra).
     *
     * Regole:
     *  - 1 tavolo da solo -> 2 posti
     *  - gruppo con N tavoli (N >= 2):
     *        posti = 4 * N - 2 * U
     *    dove U = numero di unioni (lati condivisi) tra tavoli del gruppo.
     */
    public List<Integer> calcolaPostiPerGruppo(LocalDate data, Turno turno, Sala sala) {
        List<Tavolo> tavoli = tavoloDAO.getTavoli(data, turno, sala.getNome());
        if (tavoli.isEmpty()) {
            return new ArrayList<>();
        }

        // Mappa (x,y) -> Tavolo
        Map<String, Tavolo> tavoliByKey = new HashMap<>();
        for (Tavolo t : tavoli) {
            tavoliByKey.put(key(t.getX(), t.getY()), t);
        }

        Set<String> visitati = new HashSet<>();
        List<Integer> postiPerGruppo = new ArrayList<>();

        for (Tavolo t : tavoli) {
            String k = key(t.getX(), t.getY());
            if (visitati.contains(k)) {
                continue; // già processato in un gruppo precedente
            }

            // Trovo il gruppo a partire da questo tavolo e segno i visitati globali
            List<Tavolo> gruppo = trovaGruppoTavoli(t, tavoliByKey, visitati);

            int nTavoli = gruppo.size();
            int posti;

            if (nTavoli == 1) {
                posti = 2; // tavolo singolo
            } else {
                // Calcolo quante unioni ci sono nel gruppo (solo destra/giù per non contarle doppie)
                Set<String> groupKeys = new HashSet<>();
                for (Tavolo g : gruppo) {
                    groupKeys.add(key(g.getX(), g.getY()));
                }

                int unioni = 0;
                for (Tavolo g : gruppo) {
                    int x = g.getX();
                    int y = g.getY();

                    String right = key(x + 1, y);
                    if (groupKeys.contains(right)) {
                        unioni++;
                    }

                    String down = key(x, y + 1);
                    if (groupKeys.contains(down)) {
                        unioni++;
                    }
                }

                posti = 4 * nTavoli - 2 * unioni;
            }

            postiPerGruppo.add(posti);
        }

        return postiPerGruppo;
    }

    /**
     * Somma di tutti i posti disponibili nella configurazione (data, turno, sala),
     * secondo le regole di unione tavoli.
     */
    public int calcolaPostiTotali(LocalDate data, Turno turno, Sala sala) {
        int tot = 0;
        for (int p : calcolaPostiPerGruppo(data, turno, sala)) {
            tot += p;
        }
        return tot;
    }

    // ===================== CAMBIO STATO DI GRUPPO ===================== //

    /**
     * Cambia lo stato di TUTTO il gruppo a cui appartiene il tavolo (x,y)
     * in una specifica configurazione (data, turno, sala).
     *
     * Gruppo = tavoli connessi per adiacenza 4-direzioni (su/giù/sx/dx).
     *
     * Ritorna quanti tavoli sono stati aggiornati.
     * Se il tavolo (x,y) non esiste nella configurazione -> 0.
     * Se c'è un errore SQL in uno degli update -> -1.
     */
    public int aggiornaStatoGruppo(LocalDate data, Turno turno, Sala sala,
                                   int x, int y, StatoTavolo nuovoStato) {

        List<Tavolo> tavoli = tavoloDAO.getTavoli(data, turno, sala.getNome());
        if (tavoli.isEmpty()) {
            return 0;
        }

        Map<String, Tavolo> tavoliByKey = new HashMap<>();
        for (Tavolo t : tavoli) {
            tavoliByKey.put(key(t.getX(), t.getY()), t);
        }

        String startKey = key(x, y);
        Tavolo start = tavoliByKey.get(startKey);
        if (start == null) {
            return 0; // nessun tavolo a quelle coordinate
        }

        // Per il cambio stato non mi serve il set dei visitati globali,
        // quindi passo un Set nuovo
        List<Tavolo> gruppo = trovaGruppoTavoli(start, tavoliByKey, new HashSet<>());

        int updatedCount = 0;
        for (Tavolo t : gruppo) {
            int res = tavoloDAO.aggiornaStato(data, turno, sala.getNome(), t.getX(), t.getY(), nuovoStato);
            if (res == -1) {
                return -1; // errore SQL in uno degli update
            }
            updatedCount += res; // 0 o 1
        }

        return updatedCount;
    }

    // Convenience methods per stato di gruppo

    public int liberaGruppo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return aggiornaStatoGruppo(data, turno, sala, x, y, StatoTavolo.LIBERO);
    }

    public int riservaGruppo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return aggiornaStatoGruppo(data, turno, sala, x, y, StatoTavolo.RISERVATO);
    }

    public int occupaGruppo(LocalDate data, Turno turno, Sala sala, int x, int y) {
        return aggiornaStatoGruppo(data, turno, sala, x, y, StatoTavolo.OCCUPATO);
    }

    // ===================== NUOVA PARTE: ASSOCIAZIONE PRENOTAZIONE <-> GRUPPO ===================== //

    /**
     * Assegna una prenotazione all'intero gruppo di tavoli
     * a cui appartiene il tavolo (x,y) in una configurazione (data, turno, sala).
     *
     * Flusso:
     *  - verifica parametri
     *  - recupera la prenotazione (data + nome)
     *  - recupera tutti i tavoli della configurazione
     *  - trova il gruppo partendo da (x,y)
     *  - inserisce in prenotazione_tavolo una riga per ogni tavolo del gruppo
     *
     * Ritorna:
     *   >0 = numero di associazioni inserite
     *   0  = nessuna associazione (es. tavolo inesistente, gruppo vuoto)
     *  -1  = errore SQL o parametri non validi
     */
    public int assegnaPrenotazioneAGruppo(LocalDate data,
                                          Turno turno,
                                          Sala sala,
                                          int x,
                                          int y,
                                          String nomePrenotazione) {

        // validazione base
        if (data == null || turno == null ||
                sala == null || sala.getNome() == null || sala.getNome().isBlank() ||
                nomePrenotazione == null || nomePrenotazione.isBlank()) {
            return -1;
        }

        // recupero prenotazione (PK = data + nome, come da PrenotazioneDAO)
        Prenotazione prenotazione = gestionePrenotazione.getPrenotazione(data, nomePrenotazione);
        if (prenotazione == null) {
            // prenotazione non esiste
            return -1;
        }

        LocalTime orarioPrenotazione = prenotazione.getOrario();
        if (orarioPrenotazione == null) {
            // senza orario non posso applicare il vincolo delle 2 ore
            return -1;
        }

        // recupero tutti i tavoli della configurazione
        List<Tavolo> tavoliConfig = tavoloDAO.getTavoli(data, turno, sala.getNome());
        if (tavoliConfig.isEmpty()) {
            return 0;
        }

        // mappa (x;y) -> Tavolo
        Map<String, Tavolo> tavoliByKey = new HashMap<>();
        for (Tavolo t : tavoliConfig) {
            tavoliByKey.put(key(t.getX(), t.getY()), t);
        }

        // tavolo di partenza (x,y)
        Tavolo start = tavoliByKey.get(key(x, y));
        if (start == null) {
            // nessun tavolo a quelle coordinate
            return 0;
        }

        // trovo tutto il gruppo a cui appartiene (riuso la BFS già presente)
        List<Tavolo> gruppo = trovaGruppoTavoli(start, tavoliByKey, new HashSet<>());
        if (gruppo.isEmpty()) {
            return 0;
        }

        // 1) controllo vincolo delle 2 ore su TUTTI i tavoli del gruppo
        final int SOGLIA_ORE = 2;
        for (Tavolo t : gruppo) {
            boolean conflitto = prenotazioneTavoloDAO.esisteConflittoOrarioPerTavolo(
                    data,
                    turno,
                    sala.getNome(),
                    t.getX(),
                    t.getY(),
                    orarioPrenotazione,
                    SOGLIA_ORE
            );

            if (conflitto) {
                // c'è già una prenotazione su questo tavolo in +/- 2 ore
                return -1;
            }
        }

        // 2) se nessun conflitto, salvo tutte le associazioni
        int inserted = 0;

        for (Tavolo t : gruppo) {
            int res = prenotazioneTavoloDAO.associaTavoloAPrenotazione(
                    data,
                    nomePrenotazione,
                    turno,
                    sala.getNome(),
                    t.getX(),
                    t.getY()
            );

            if (res == -1) {
                return -1; // qualche errore SQL
            }
            inserted += res; // 0 se già presente, 1 se nuova associazione
        }

        return inserted;
    }


    /**
     * Variante comoda: oltre ad assegnare la prenotazione al gruppo,
     * segna tutto il gruppo come RISERVATO.
     */
    public int assegnaPrenotazioneAGruppoERiserva(LocalDate data,
                                                  Turno turno,
                                                  Sala sala,
                                                  int x,
                                                  int y,
                                                  String nomePrenotazione) {

        int res = assegnaPrenotazioneAGruppo(data, turno, sala, x, y, nomePrenotazione);
        if (res <= 0) {
            return res; // 0 o -1: non tocco lo stato
        }

        int upd = riservaGruppo(data, turno, sala, x, y);
        if (upd == -1) {
            return -1; // errore sugli update
        }

        return res;
    }

    /**
     * Restituisce le prenotazioni DISTINTE associate al gruppo di tavoli
     * a cui appartiene il tavolo (x,y) in una data configurazione
     * (data, turno, sala).
     *
     * La distintività è sulla PK logica della prenotazione: (data, nome).
     */
    public List<Prenotazione> getPrenotazioniGruppo(LocalDate data,
                                                    Turno turno,
                                                    Sala sala,
                                                    int x,
                                                    int y) {

        List<Prenotazione> risultato = new ArrayList<>();

        if (data == null || turno == null ||
                sala == null || sala.getNome() == null || sala.getNome().isBlank()) {
            return risultato;
        }

        // 1) prendo tutti i tavoli della configurazione (data, turno, sala)
        List<Tavolo> tavoliConfig = tavoloDAO.getTavoli(data, turno, sala.getNome());
        if (tavoliConfig.isEmpty()) {
            return risultato;
        }

        // 2) mappa (x;y) -> Tavolo
        Map<String, Tavolo> tavoliByKey = new HashMap<>();
        for (Tavolo t : tavoliConfig) {
            tavoliByKey.put(key(t.getX(), t.getY()), t);
        }

        // 3) tavolo di partenza (x,y)
        Tavolo start = tavoliByKey.get(key(x, y));
        if (start == null) {
            return risultato; // nessun tavolo a quelle coordinate
        }

        // 4) trovo il gruppo a cui appartiene (x,y)
        List<Tavolo> gruppo = trovaGruppoTavoli(start, tavoliByKey, new HashSet<>());
        if (gruppo.isEmpty()) {
            return risultato;
        }

        // 5) per evitare duplicati (prenotazione collegata a più tavoli del gruppo)
        //    uso una mappa pk -> Prenotazione
        Map<String, Prenotazione> prenByPk = new LinkedHashMap<>();

        for (Tavolo t : gruppo) {
            List<Prenotazione> prenotazioniTavolo =
                    prenotazioneTavoloDAO.getPrenotazioniPerTavolo(
                            data,              // stessa data della configurazione
                            turno,             // stesso turno
                            sala.getNome(),    // stessa sala
                            t.getX(),
                            t.getY()
                    );

            for (Prenotazione p : prenotazioniTavolo) {
                // PK logica: data + nome, come nella tabella prenotazioni
                String pk = p.getDate() + ";" + p.getNome();
                prenByPk.putIfAbsent(pk, p);
            }
        }

        risultato.addAll(prenByPk.values());
        return risultato;
    }




    // ===================== helper interni ===================== //

    private String key(int x, int y) {
        return x + ";" + y;
    }

    /**
     * BFS per trovare tutti i tavoli nel gruppo del tavolo di partenza.
     * Usa adiacenza 4-direzioni (su/giù/sx/dx).
     *
     * Il set visitati è condiviso tra chiamate diverse
     * quando usato da calcolaPostiPerGruppo, per evitare di rifare gli stessi gruppi.
     */
    private List<Tavolo> trovaGruppoTavoli(Tavolo start,
                                           Map<String, Tavolo> tavoliByKey,
                                           Set<String> visitati) {

        List<Tavolo> gruppo = new ArrayList<>();
        Deque<Tavolo> queue = new ArrayDeque<>();

        String startKey = key(start.getX(), start.getY());
        if (!visitati.add(startKey)) {
            // era già visitato, ritorno gruppo vuoto (per sicurezza)
            return gruppo;
        }

        queue.add(start);

        while (!queue.isEmpty()) {
            Tavolo current = queue.poll();
            gruppo.add(current);

            int x = current.getX();
            int y = current.getY();

            int[][] delta = {
                    {1, 0}, {-1, 0},
                    {0, 1}, {0, -1}
            };

            for (int[] d : delta) {
                int nx = x + d[0];
                int ny = y + d[1];
                String nk = key(nx, ny);

                Tavolo vicino = tavoliByKey.get(nk);
                if (vicino != null && visitati.add(nk)) {
                    queue.add(vicino);
                }
            }
        }

        return gruppo;
    }
}
