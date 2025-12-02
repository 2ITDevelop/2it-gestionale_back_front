package it.gestione.service;

import it.gestione.database.ConfigurazioneSalaDAO;
import it.gestione.database.SalaDAO;
import it.gestione.database.TavoloDAO;
import it.gestione.database.ZonaSalaDAO;
import it.gestione.entity.ConfigurazioneSala;
import it.gestione.entity.Sala;
import it.gestione.entity.StatoTavolo;
import it.gestione.entity.Tavolo;
import it.gestione.entity.Turno;
import it.gestione.entity.ZonaSala;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class GestioneSala {

    private final ConfigurazioneSalaDAO configurazioneSalaDAO;
    private final TavoloDAO tavoloDAO;
    private final SalaDAO salaDAO;
    private final ZonaSalaDAO zonaSalaDAO;

    public GestioneSala(ConfigurazioneSalaDAO configurazioneSalaDAO,
                        TavoloDAO tavoloDAO,
                        SalaDAO salaDAO,
                        ZonaSalaDAO zonaSalaDAO) {
        this.configurazioneSalaDAO = configurazioneSalaDAO;
        this.tavoloDAO = tavoloDAO;
        this.salaDAO = salaDAO;
        this.zonaSalaDAO = zonaSalaDAO;
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

    // ===================== helper interni ===================== //

    private String key(int x, int y) {
        return x + ";" + y;
    }

    /**
     * BFS per trovare tutti i tavoli nel gruppo del tavolo di partenza.
     * Usa adiacenza 4-direzioni (su/giù/sx/dx).
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
