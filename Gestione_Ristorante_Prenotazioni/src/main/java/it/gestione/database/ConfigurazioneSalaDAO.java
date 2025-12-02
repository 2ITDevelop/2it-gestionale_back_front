package it.gestione.database;

import it.gestione.entity.ConfigurazioneSala;
import it.gestione.entity.Sala;
import it.gestione.entity.Turno;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ConfigurazioneSalaDAO {

    // ---------- SQL ---------- //

    private static final String INSERT_SQL =
            "INSERT INTO configurazione_sala (data, turno, nome_sala) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT DO NOTHING";

    private static final String DELETE_SQL =
            "DELETE FROM configurazione_sala " +
                    "WHERE data = ? AND turno = ? AND nome_sala = ?";

    private static final String SELECT_BY_KEY_SQL =
            "SELECT data, turno, nome_sala " +
                    "FROM configurazione_sala " +
                    "WHERE data = ? AND turno = ? AND nome_sala = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT data, turno, nome_sala " +
                    "FROM configurazione_sala " +
                    "ORDER BY data, turno, nome_sala";


    // ---------- COSTRUTTORE ---------- //

    public ConfigurazioneSalaDAO() {}


    // ---------- CRUD ---------- //

    /**
     * Inserisce una nuova configurazione sala.
     *  1 = inserito
     *  0 = già presente
     * -1 = errore SQL
     */
    public int aggiungiConfigurazione(ConfigurazioneSala c) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setDate(1, Date.valueOf(c.getData()));
            ps.setString(2, c.getTurno().name());
            ps.setString(3, c.getSala().getNome());

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiungiConfigurazione: " + e.getMessage());
            return -1;
        }
    }


    /**
     * Elimina una configurazione sala specifica.
     */
    public int eliminaConfigurazione(LocalDate data, Turno turno, String nomeSala) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaConfigurazione: " + e.getMessage());
            return -1;
        }
    }


    /**
     * Restituisce una configurazione (data, turno, sala).
     * Se non trovata → null.
     */
    public ConfigurazioneSala getConfigurazione(LocalDate data, Turno turno, String nomeSala) {

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_KEY_SQL)) {

            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, turno.name());
            ps.setString(3, nomeSala);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToConfigurazione(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getConfigurazione: " + e.getMessage());
        }

        return null;
    }


    /**
     * Restituisce tutte le configurazioni sala.
     */
    public List<ConfigurazioneSala> getAllConfigurazioni() {
        List<ConfigurazioneSala> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToConfigurazione(rs));
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getAllConfigurazioni: " + e.getMessage());
        }

        return lista;
    }


    // ---------- helper ---------- //

    private ConfigurazioneSala mapRowToConfigurazione(ResultSet rs) throws SQLException {

        LocalDate data = rs.getDate("data").toLocalDate();
        Turno turno = Turno.valueOf(rs.getString("turno"));
        String nomeSala = rs.getString("nome_sala");

        Sala sala = new Sala();
        sala.setNome(nomeSala);

        return new ConfigurazioneSala(data, turno, sala);
    }
}


/*CREATE TABLE configurazione_sala (
    data DATE NOT NULL,
    turno VARCHAR(16) NOT NULL,
    nome_sala VARCHAR(64) NOT NULL,

    PRIMARY KEY (data, turno, nome_sala),

    FOREIGN KEY (nome_sala)
        REFERENCES sala(nome)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);*/
