package it.gestione.database;

import it.gestione.entity.Sala;
import it.gestione.entity.ZonaSala;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SalaDAO {

    private static final String INSERT_SQL =
            "INSERT INTO sala (nome) VALUES (?) ON CONFLICT DO NOTHING";

    private static final String DELETE_SQL =
            "DELETE FROM sala WHERE nome = ?";

    private static final String SELECT_ONE_SQL =
            "SELECT nome FROM sala WHERE nome = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT nome FROM sala ORDER BY nome";

    private final ZonaSalaDAO zonaSalaDAO;

    public SalaDAO(ZonaSalaDAO zonaSalaDAO) {
        this.zonaSalaDAO = zonaSalaDAO;
    }


    public int aggiungiSala(Sala sala) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, sala.getNome());
            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiungiSala: " + e.getMessage());
            return -1;
        }
    }

    public int eliminaSala(String nomeSala) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setString(1, nomeSala);
            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaSala: " + e.getMessage());
            return -1;
        }
    }

    public Sala getSala(String nomeSala) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ONE_SQL)) {

            ps.setString(1, nomeSala);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    Sala sala = new Sala(rs.getString("nome"));

                    // carica le zone dal DAO
                    List<ZonaSala> zone = zonaSalaDAO.getZoneForSala(nomeSala);
                    sala.setZone(zone);

                    return sala;
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getSala: " + e.getMessage());
        }

        return null;
    }

    public List<Sala> getAllSale() {
        List<Sala> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String nomeSala = rs.getString("nome");

                Sala sala = new Sala(nomeSala);
                sala.setZone(zonaSalaDAO.getZoneForSala(nomeSala));

                lista.add(sala);
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getAllSale: " + e.getMessage());
        }

        return lista;
    }
}

/*CREATE TABLE sala (
    nome VARCHAR(64) NOT NULL,
    PRIMARY KEY (nome)
);*/
