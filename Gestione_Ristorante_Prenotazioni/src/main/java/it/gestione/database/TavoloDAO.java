package it.gestione.database;

import it.gestione.entity.StatoTavolo;
import it.gestione.entity.Tavolo;
import it.gestione.entity.Turno;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TavoloDAO {

    // ---------- SQL ---------- //

    private static final String INSERT_SQL =
            "INSERT INTO tavolo_sala (data, turno, nome_sala, x, y, stato) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT DO NOTHING";

    private static final String UPDATE_STATO_SQL =
            "UPDATE tavolo_sala SET stato = ? " +
                    "WHERE data = ? AND turno = ? AND nome_sala = ? AND x = ? AND y = ?";

    private static final String DELETE_SQL =
            "DELETE FROM tavolo_sala " +
                    "WHERE data = ? AND turno = ? AND nome_sala = ? AND x = ? AND y = ?";

    private static final String SELECT_BY_CONFIG_SQL =
            "SELECT data, turno, nome_sala, x, y, stato " +
                    "FROM tavolo_sala " +
                    "WHERE data = ? AND turno = ? AND nome_sala = ? " +
                    "ORDER BY y, x";

    private static final String SELECT_ONE_SQL =
            "SELECT data, turno, nome_sala, x, y, stato " +
                    "FROM tavolo_sala " +
                    "WHERE data = ? AND turno = ? AND nome_sala = ? AND x = ? AND y = ?";


    // ---------- COSTRUTTORE ---------- //

    public TavoloDAO() {}


    // ---------- CRUD ---------- //

    /**
     * Aggiunge un tavolo in una configurazione sala (data, turno, nome_sala).
     *  1 = inserito
     *  0 = già presente
     * -1 = errore SQL
     */
    public int aggiungiTavolo(LocalDate data, Turno turno, String nomeSala, Tavolo t) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);
            ps.setInt(4, t.getX());
            ps.setInt(5, t.getY());
            ps.setString(6, t.getStato().name());

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiungiTavolo: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Modifica lo stato di un tavolo in una configurazione sala.
     */
    public int aggiornaStato(LocalDate data, Turno turno,
                             String nomeSala, int x, int y, StatoTavolo nuovoStato) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATO_SQL)) {

            ps.setString(1, nuovoStato.name());
            ps.setDate(2, Date.valueOf(data));
            ps.setString(3, turno.name());
            ps.setString(4, nomeSala);
            ps.setInt(5, x);
            ps.setInt(6, y);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiornaStato: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Rimuove un tavolo da una configurazione sala.
     */
    public int eliminaTavolo(LocalDate data, Turno turno,
                             String nomeSala, int x, int y) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);
            ps.setInt(4, x);
            ps.setInt(5, y);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaTavolo: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Ottiene tutti i tavoli di una configurazione sala (data, turno, nome_sala).
     */
    public List<Tavolo> getTavoli(LocalDate data, Turno turno, String nomeSala) {

        List<Tavolo> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CONFIG_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToTavolo(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getTavoli: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Ottiene un singolo tavolo in una configurazione sala.
     */
    public Tavolo getTavolo(LocalDate data, Turno turno,
                            String nomeSala, int x, int y) {

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ONE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);
            ps.setInt(4, x);
            ps.setInt(5, y);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTavolo(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getTavolo: " + e.getMessage());
        }

        return null;
    }


    // ---------- helper ---------- //

    private Tavolo mapRowToTavolo(ResultSet rs) throws SQLException {

        int x = rs.getInt("x");
        int y = rs.getInt("y");
        StatoTavolo stato = StatoTavolo.valueOf(rs.getString("stato"));

        return new Tavolo(x, y, stato);
    }
}

/*
CREATE TABLE tavolo_sala (
    data DATE NOT NULL,
    turno VARCHAR(16) NOT NULL,
    nome_sala VARCHAR(64) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    stato VARCHAR(16) NOT NULL,

    PRIMARY KEY (data, turno, nome_sala, x, y),

    FOREIGN KEY (data, turno, nome_sala)
        REFERENCES configurazione_sala(data, turno, nome_sala)
        ON DELETE CASCADE
);
*/
