package it.gestione.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class DBConnectionManager {

    // ======= CONFIG =======
    // Imposta queste variabili d'ambiente in esecuzione:
    // DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
    private static final String HOST = System.getenv("DB_HOST");
    private static final String PORT = System.getenv("DB_PORT");
    private static final String DB   = System.getenv("DB_NAME");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASS = System.getenv("DB_PASSWORD");

    // URL JDBC per Supabase/Postgres
    // - sslmode=require: obbligatorio
    // - preferQueryMode=simple + prepareThreshold=0: compatibili con PgBouncer (transaction pooling)
    // - reWriteBatchedInserts=true: migliora i batch insert
    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB + "?sslmode=require&preferQueryMode=simple";

    private static final HikariDataSource DS;

    static {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(URL);
        cfg.setUsername(USER);
        cfg.setPassword(PASS);

        // ------- Pool tuning (valori tipici, aggiusta in base al carico) -------
        cfg.setMaximumPoolSize(20);
        cfg.setMinimumIdle(2);
        cfg.setAutoCommit(true);
        cfg.setConnectionTimeout(10_000); // ms
        cfg.setIdleTimeout(300_000);      // 5 min
        cfg.setMaxLifetime(1_800_000);    // 30 min (tenere < timeout server)

        // Compatibilità PgBouncer (transaction pooling)
        cfg.addDataSourceProperty("prepareThreshold", "0");

        DS = new HikariDataSource(cfg);
    }

    /** Restituisce una Connection dal pool (ricordati di chiuderla con try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }

    /**
     * Esegue una SELECT con Statement semplice e restituisce un ResultSet APERTO.
     * Chi deve chiudere: ResultSet, Statement e Connection (usa closeResources).
     * Consigliato usare PreparedStatement nei DAO.
     */
    public static ResultSet selectQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        System.out.println("Eseguo SELECT: " + query);
        return stmt.executeQuery(query);
    }

    /** Esegue UPDATE/INSERT/DELETE con Statement semplice (risorse chiuse automaticamente). */
    public static int updateQuery(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("Eseguo UPDATE: " + query);
            int result = stmt.executeUpdate(query);
            System.out.println("Righe modificate: " + result);
            return result;
        }
    }

    /** Utility per chiudere in sicurezza rs/stmt/conn quando usi selectQuery(String). */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null && !rs.isClosed()) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null && !stmt.isClosed()) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
