package it.gestione.database;

import it.gestione.entity.Prenotazione;
import it.gestione.entity.StatoTavolo;
import it.gestione.entity.Tavolo;
import it.gestione.entity.Turno;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PrenotazioneTavoloDAO {

    // ---------- SQL ---------- //

    private static final String INSERT_SQL =
            "INSERT INTO prenotazione_tavolo " +
                    " (data, nome_prenotazione, turno, nome_sala, x, y) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT DO NOTHING";

    private static final String DELETE_ONE_SQL =
            "DELETE FROM prenotazione_tavolo " +
                    "WHERE data = ? AND nome_prenotazione = ? " +
                    "  AND turno = ? AND nome_sala = ? AND x = ? AND y = ?";

    private static final String DELETE_BY_PRENOTAZIONE_SQL =
            "DELETE FROM prenotazione_tavolo " +
                    "WHERE data = ? AND nome_prenotazione = ?";

    private static final String SELECT_TAVOLI_BY_PRENOTAZIONE_SQL =
            "SELECT ts.data, ts.turno, ts.nome_sala, ts.x, ts.y, ts.stato " +
                    "FROM prenotazione_tavolo pt " +
                    "JOIN tavolo_sala ts " +
                    "  ON pt.data = ts.data " +
                    " AND pt.turno = ts.turno " +
                    " AND pt.nome_sala = ts.nome_sala " +
                    " AND pt.x = ts.x " +
                    " AND pt.y = ts.y " +
                    "WHERE pt.data = ? AND pt.nome_prenotazione = ? " +
                    "ORDER BY ts.y, ts.x";

    private static final String SELECT_PRENOTAZIONI_BY_TAVOLO_SQL =
            "SELECT p.nome, p.data, p.num_persone, p.orario, p.numero_telefono " +
                    "FROM prenotazione_tavolo pt " +
                    "JOIN prenotazioni p " +
                    "  ON pt.data = p.data " +
                    " AND pt.nome_prenotazione = p.nome " +
                    "WHERE pt.data = ? AND pt.turno = ? " +
                    "  AND pt.nome_sala = ? AND pt.x = ? AND pt.y = ? " +
                    "ORDER BY p.orario, p.nome";

    private static final String COUNT_CONFLITTI_ORARIO_TAVOLO_SQL =
            "SELECT COUNT(*) " +
                    "FROM prenotazione_tavolo pt " +
                    "JOIN prenotazioni p " +
                    "  ON pt.data = p.data " +
                    " AND pt.nome_prenotazione = p.nome " +
                    "WHERE pt.data = ? " +
                    "  AND pt.turno = ? " +
                    "  AND pt.nome_sala = ? " +
                    "  AND pt.x = ? " +
                    "  AND pt.y = ? " +
                    "  AND p.orario BETWEEN ? AND ?";



    // ---------- COSTRUTTORE ---------- //

    public PrenotazioneTavoloDAO() {}


    // ---------- METODI PRINCIPALI ---------- //

    /**
     * Associa un singolo tavolo ad una prenotazione.
     * Ritorna:
     *  1 = inserita associazione
     *  0 = associazione già presente (ON CONFLICT DO NOTHING)
     * -1 = errore SQL
     */
    public int associaTavoloAPrenotazione(LocalDate data,
                                          String nomePrenotazione,
                                          Turno turno,
                                          String nomeSala,
                                          int x,
                                          int y) {

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, nomePrenotazione);
            ps.setString(3, turno.name());
            ps.setString(4, nomeSala);
            ps.setInt(5, x);
            ps.setInt(6, y);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in associaTavoloAPrenotazione: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Rimuove una specifica associazione prenotazione-tavolo.
     * Ritorna:
     *  numero righe eliminate (0 o 1)
     * -1 in caso di errore.
     */
    public int rimuoviAssociazione(LocalDate data,
                                   String nomePrenotazione,
                                   Turno turno,
                                   String nomeSala,
                                   int x,
                                   int y) {

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ONE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, nomePrenotazione);
            ps.setString(3, turno.name());
            ps.setString(4, nomeSala);
            ps.setInt(5, x);
            ps.setInt(6, y);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in rimuoviAssociazione: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Rimuove tutte le associazioni per una prenotazione (es. se cambi i tavoli uniti).
     */
    public int rimuoviAssociazioniPerPrenotazione(LocalDate data,
                                                  String nomePrenotazione) {

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_PRENOTAZIONE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, nomePrenotazione);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in rimuoviAssociazioniPerPrenotazione: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Verifica se esiste almeno una prenotazione associata a quel tavolo
     * (in quella data/turno/sala/x/y) con orario compreso tra
     * [orarioNuovo - oreTolleranza, orarioNuovo + oreTolleranza].
     *
     * Se c'è almeno una riga -> true (conflitto).
     * Se non ce ne sono -> false (ok).
     * In caso di errore SQL -> true (per stare dalla parte della sicurezza).
     */
    public boolean esisteConflittoOrarioPerTavolo(LocalDate data,
                                                  Turno turno,
                                                  String nomeSala,
                                                  int x,
                                                  int y,
                                                  LocalTime orarioNuovo,
                                                  int oreTolleranza) {

        if (data == null || turno == null ||
                nomeSala == null || nomeSala.isBlank() ||
                orarioNuovo == null || oreTolleranza < 0) {
            // parametri non validi -> considero conflitto
            return true;
        }

        LocalTime from = orarioNuovo.minusHours(oreTolleranza);
        LocalTime to   = orarioNuovo.plusHours(oreTolleranza);

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_CONFLITTI_ORARIO_TAVOLO_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);
            ps.setInt(4, x);
            ps.setInt(5, y);
            ps.setTime(6, Time.valueOf(from));
            ps.setTime(7, Time.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in esisteConflittoOrarioPerTavolo: " + e.getMessage());
            return true; // in dubbio, meglio considerare che c'è conflitto
        }

        return false;
    }


    /**
     * Restituisce tutti i tavoli (fisici) associati ad una prenotazione.
     * NB: il Tavolo contiene solo x, y, stato come nel TavoloDAO.
     */
    public List<Tavolo> getTavoliPerPrenotazione(LocalDate data,
                                                 String nomePrenotazione) {

        List<Tavolo> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_TAVOLI_BY_PRENOTAZIONE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, nomePrenotazione);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToTavolo(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getTavoliPerPrenotazione: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Restituisce tutte le prenotazioni associate ad un tavolo specifico
     * (in una certa data/turno/sala/x/y).
     */
    public List<Prenotazione> getPrenotazioniPerTavolo(LocalDate data,
                                                       Turno turno,
                                                       String nomeSala,
                                                       int x,
                                                       int y) {

        List<Prenotazione> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PRENOTAZIONI_BY_TAVOLO_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);
            ps.setInt(4, x);
            ps.setInt(5, y);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToPrenotazione(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getPrenotazioniPerTavolo: " + e.getMessage());
        }

        return lista;
    }


    // ---------- helper privati ---------- //

    private Tavolo mapRowToTavolo(ResultSet rs) throws SQLException {
        int x = rs.getInt("x");
        int y = rs.getInt("y");
        StatoTavolo stato = StatoTavolo.valueOf(rs.getString("stato"));
        return new Tavolo(x, y, stato);
    }

    private Prenotazione mapRowToPrenotazione(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();

        p.setNome(rs.getString("nome"));
        p.setNumPersone(rs.getInt("num_persone"));

        Date d = rs.getDate("data");
        if (d != null) {
            p.setDate(d.toLocalDate());
        }

        Time t = rs.getTime("orario");
        if (t != null) {
            p.setOrario(t.toLocalTime());
        }

        p.setNumeroTelefono(rs.getString("numero_telefono"));

        return p;
    }
}

/*
CREATE TABLE prenotazione_tavolo (
    data              DATE         NOT NULL,
    nome_prenotazione VARCHAR(100) NOT NULL,
    turno             VARCHAR(16)  NOT NULL,
    nome_sala         VARCHAR(64)  NOT NULL,
    x                 INTEGER      NOT NULL,
    y                 INTEGER      NOT NULL,

    PRIMARY KEY (data, nome_prenotazione, turno, nome_sala, x, y),

    FOREIGN KEY (data, nome_prenotazione)
        REFERENCES prenotazioni(data, nome)
        ON DELETE CASCADE,

    FOREIGN KEY (data, turno, nome_sala, x, y)
        REFERENCES tavolo_sala(data, turno, nome_sala, x, y)
        ON DELETE CASCADE
);
*/
