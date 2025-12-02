package it.gestione.database;

import it.gestione.entity.Prenotazione;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PrenotazioneDAO {

    // ---------- SQL ---------- //

    private static final String INSERT_SQL =
            "INSERT INTO prenotazioni " +
                    " (nome, data, num_persone, orario, numero_telefono) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (data, nome) DO NOTHING";

    private static final String DELETE_SQL =
            "DELETE FROM prenotazioni WHERE data = ? AND nome = ?";

    private static final String SELECT_BY_DATA_SQL =
            "SELECT * FROM prenotazioni WHERE data = ? ORDER BY orario, nome";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM prenotazioni ORDER BY data, orario, nome";

    private static final String SELECT_BY_PK_SQL =
            "SELECT * FROM prenotazioni WHERE data = ? AND nome = ?";



    // Costruttore di default
    public PrenotazioneDAO() {}


    // ---------- CRUD ---------- //

    /**
     * Inserisce una Prenotazione.
     *  1 = inserita
     *  0 = già presente (conflitto su PK data+nome)
     * -1 = errore
     */
    public int aggiungiPrenotazione(Prenotazione p) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, p.getNome());
            ps.setDate(2, Date.valueOf(p.getDate()));
            ps.setInt(3, p.getNumPersone());

            LocalTime orario = p.getOrario();
            if (orario != null)
                ps.setTime(4, Time.valueOf(orario));
            else
                ps.setNull(4, Types.TIME);

            String tel = p.getNumeroTelefono();
            if (tel != null && !tel.isBlank())
                ps.setString(5, tel);
            else
                ps.setNull(5, Types.VARCHAR);

            return ps.executeUpdate(); // 1 o 0

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiungiPrenotazione: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Elimina una prenotazione identificata da data + nome.
     * Ritorna:
     *  numero righe eliminate (0 o 1),
     * -1 in caso di errore.
     */
    public int eliminaPrenotazione(LocalDate data, String nome) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, nome);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaPrenotazione: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Ritorna tutte le prenotazioni per una certa data.
     * In caso di errore: ritorna lista vuota.
     */
    public List<Prenotazione> getPrenotazioniByData(LocalDate data) {
        List<Prenotazione> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_DATA_SQL)) {

            ps.setDate(1, Date.valueOf(data));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToPrenotazione(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getPrenotazioniByData: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Ritorna tutte le prenotazioni.
     * In caso di errore: ritorna lista vuota.
     */
    public List<Prenotazione> getAllPrenotazioni() {
        List<Prenotazione> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToPrenotazione(rs));
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getAllPrenotazioni: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Ritorna una singola prenotazione identificata da data + nome.
     * Ritorna:
     *  - Prenotazione se trovata
     *  - null se non esiste o in caso di errore
     */
    public Prenotazione getPrenotazione(LocalDate data, String nome) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PK_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, nome);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPrenotazione(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getPrenotazione: " + e.getMessage());
        }

        return null;
    }



    // ---------- helper privati ---------- //

    private Prenotazione mapRowToPrenotazione(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();

        p.setNome(rs.getString("nome"));
        p.setNumPersone(rs.getInt("num_persone"));

        Date d = rs.getDate("data");
        if (d != null)
            p.setDate(d.toLocalDate());

        Time t = rs.getTime("orario");
        if (t != null)
            p.setOrario(t.toLocalTime());

        p.setNumeroTelefono(rs.getString("numero_telefono"));

        return p;
    }

}


/*
CREATE TABLE prenotazioni (
    nome            VARCHAR(100) NOT NULL,
    data            DATE         NOT NULL,
    num_persone     INTEGER      NOT NULL,
    orario          TIME         NOT NULL,
    numero_telefono VARCHAR(20),

    PRIMARY KEY (data, nome)
);
*/
