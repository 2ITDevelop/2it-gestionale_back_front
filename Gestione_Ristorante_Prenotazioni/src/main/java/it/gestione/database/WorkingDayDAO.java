package it.gestione.database;

import it.gestione.entity.WorkingDay;
import it.gestione.entity.WorkingDayType;
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
public class WorkingDayDAO {

    // ---------- SQL ---------- //

    private static final String INSERT_SQL =
            "INSERT INTO working_day " +
                    "(type, data, g1, g2, a1, c1, a2, c2) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT DO NOTHING";

    private static final String DELETE_TEMPLATE_SQL =
            "DELETE FROM working_day WHERE type = ? AND data IS NULL";

    private static final String DELETE_BY_TYPE_AND_DATE_SQL =
            "DELETE FROM working_day WHERE type = ? AND data = ?";

    private static final String SELECT_BY_TYPE_SQL =
            "SELECT type, data, g1, g2, a1, c1, a2, c2 " +
                    "FROM working_day " +
                    "WHERE type = ? " +
                    "ORDER BY data NULLS FIRST";

    private static final String SELECT_ALL_SQL =
            "SELECT type, data, g1, g2, a1, c1, a2, c2 " +
                    "FROM working_day " +
                    "ORDER BY type, data NULLS FIRST";

    // ---------- COSTRUTTORE ---------- //

    public WorkingDayDAO() {}

    // ---------- CRUD ---------- //

    /**
     * Inserisce un WorkingDay.
     *  1 = inserito
     *  0 = già presente (conflitto)
     * -1 = errore
     */
    public int aggiungiWorkingDay(WorkingDay wd) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, wd.getType().name());

            if (wd.getData() != null)
                ps.setDate(2, Date.valueOf(wd.getData()));
            else
                ps.setNull(2, Types.DATE);

            ps.setBoolean(3, wd.isG1());
            ps.setBoolean(4, wd.isG2());

            setTimeOrNull(ps, 5, wd.getA1());
            setTimeOrNull(ps, 6, wd.getC1());
            setTimeOrNull(ps, 7, wd.getA2());
            setTimeOrNull(ps, 8, wd.getC2());

            return ps.executeUpdate(); // 1 o 0

        } catch (SQLException e) {
            System.err.println("Errore SQL in aggiungiWorkingDay: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Cancella il template (data NULL).
     * ritorna numero righe eliminate, -1 se errore
     */
    public int eliminaWorkingDayTemplate(WorkingDayType type) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_TEMPLATE_SQL)) {

            ps.setString(1, type.name());
            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaWorkingDayTemplate: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Cancella uno SPECIAL (type + data).
     * ritorna numero righe eliminate, -1 se errore
     */
    public int eliminaWorkingDay(WorkingDayType type, LocalDate data) {
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_TYPE_AND_DATE_SQL)) {

            ps.setString(1, type.name());
            ps.setDate(2, Date.valueOf(data));
            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore SQL in eliminaWorkingDay: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Ritorna tutti i giorni di un tipo.
     * In caso di errore: ritorna lista vuota.
     */
    public List<WorkingDay> getWorkingDaysByType(WorkingDayType type) {
        List<WorkingDay> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TYPE_SQL)) {

            ps.setString(1, type.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToWorkingDay(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getWorkingDaysByType: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Ritorna tutti i WorkingDay.
     * In caso di errore: ritorna lista vuota.
     */
    public List<WorkingDay> getAllWorkingDays() {
        List<WorkingDay> lista = new ArrayList<>();

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToWorkingDay(rs));
            }

        } catch (SQLException e) {
            System.err.println("Errore SQL in getAllWorkingDays: " + e.getMessage());
        }

        return lista;
    }

    // ---------- helper privati ---------- //

    private void setTimeOrNull(PreparedStatement ps, int index, LocalTime time) throws SQLException {
        if (time != null)
            ps.setTime(index, Time.valueOf(time));
        else
            ps.setNull(index, Types.TIME);
    }

    private WorkingDay mapRowToWorkingDay(ResultSet rs) throws SQLException {
        WorkingDayType t = WorkingDayType.valueOf(rs.getString("type"));

        Date sqlDate = rs.getDate("data");
        LocalDate d = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        boolean g1 = rs.getBoolean("g1");
        boolean g2 = rs.getBoolean("g2");

        Time ta1 = rs.getTime("a1");
        Time tc1 = rs.getTime("c1");
        Time ta2 = rs.getTime("a2");
        Time tc2 = rs.getTime("c2");

        return new WorkingDay(
                t,
                g1,
                g2,
                (ta1 != null ? ta1.toLocalTime() : null),
                (tc1 != null ? tc1.toLocalTime() : null),
                (ta2 != null ? ta2.toLocalTime() : null),
                (tc2 != null ? tc2.toLocalTime() : null),
                d
        );
    }
}

/*CREATE TABLE working_day (
    id_working_day SERIAL PRIMARY KEY,

    type VARCHAR(16) NOT NULL,   -- WEEKDAY / SATURDAY / SUNDAY / SPECIAL
    data DATE,                   -- NULL per WEEKDAY/SATURDAY/SUNDAY, valorizzata per SPECIAL

    g1 BOOLEAN NOT NULL,
    g2 BOOLEAN NOT NULL,

    a1 TIME,
    c1 TIME,
    a2 TIME,
    c2 TIME
);

-- Un solo template per tipo (WEEKDAY/SATURDAY/SUNDAY) con data NULL
CREATE UNIQUE INDEX ux_working_day_template
    ON working_day(type)
    WHERE data IS NULL;

-- Un solo SPECIAL per ogni data
CREATE UNIQUE INDEX ux_working_day_special
    ON working_day(data)
    WHERE type = 'SPECIAL';
*/
