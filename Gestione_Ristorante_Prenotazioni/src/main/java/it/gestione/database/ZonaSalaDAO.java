package it.gestione.database;

import it.gestione.entity.TipoZona;
import it.gestione.entity.ZonaSala;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ZonaSalaDAO {

    // ---------- SQL ---------- //

    private static final String INSERT_SQL =
            "INSERT INTO zona_sala (nome_sala, x, y, base, altezza, tipo) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT DO NOTHING";

    private static final String DELETE_SQL =
            "DELETE FROM zona_sala " +
                    "WHERE nome_sala = ? AND x = ? AND y = ?";

    private static final String DELETE_ALL_SQL =
            "DELETE FROM zona_sala " +
                    "WHERE nome_sala = ?";

    private static final String SELECT_BY_SALA_SQL =
            "SELECT nome_sala, x, y, base, altezza, tipo " +
                    "FROM zona_sala " +
                    "WHERE nome_sala = ? " +
                    "ORDER BY x, y";


    // ---------- COSTRUTTORE ---------- //

    public ZonaSalaDAO() {}


    // ---------- CRUD ---------- //

    /**
     * Aggiunge una zona ad una sala.
     */
    public int aggiungiZona(String nomeSala, ZonaSala z) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, nomeSala);
            ps.setInt(2, z.getX());
            ps.setInt(3, z.getY());
            ps.setInt(4, z.getBase());
            ps.setInt(5, z.getAltezza());
            ps.setString(6, z.getTipo().name());

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiungiZona: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Elimina UNA singola zona della sala.
     */
    public int eliminaZona(String nomeSala, int x, int y) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setString(1, nomeSala);
            ps.setInt(2, x);
            ps.setInt(3, y);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaZona: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Elimina TUTTE le zone della sala.
     */
    public int eliminaTutteLeZone(String nomeSala) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ALL_SQL)) {

            ps.setString(1, nomeSala);

            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaTutteLeZone: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Restituisce TUTTE le zone di una sala.
     */
    public List<ZonaSala> getZoneForSala(String nomeSala) {
        List<ZonaSala> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_SALA_SQL)) {

            ps.setString(1, nomeSala);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToZonaSala(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getZoneForSala: " + e.getMessage());
        }

        return lista;
    }


    // ---------- helper ---------- //

    private ZonaSala mapRowToZonaSala(ResultSet rs) throws SQLException {

        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int base = rs.getInt("base");
        int altezza = rs.getInt("altezza");

        TipoZona tipo = TipoZona.valueOf(rs.getString("tipo"));

        return new ZonaSala(x, y, tipo, base, altezza);
    }
}
/*CREATE TABLE zona_sala (
    nome_sala VARCHAR(64) NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    base INTEGER NOT NULL,
    altezza INTEGER NOT NULL,
    tipo VARCHAR(32) NOT NULL,

    PRIMARY KEY (nome_sala, x, y),
    FOREIGN KEY (nome_sala) REFERENCES sala(nome)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
*/
