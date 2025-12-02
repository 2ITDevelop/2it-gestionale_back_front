package it.gestione.service;

import it.gestione.database.WorkingDayDAO;
import it.gestione.entity.WorkingDay;
import it.gestione.entity.WorkingDayType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class GestioneWorkingDay {


    private final WorkingDayDAO workingDayDAO;

    public GestioneWorkingDay(WorkingDayDAO workingDayDAO) {
        this.workingDayDAO = workingDayDAO;
    }

    public int creaWorkingDay(WorkingDayType t,boolean g1,boolean g2,LocalTime a1,LocalTime c1,LocalTime a2,LocalTime c2,LocalDate d) {

        if (t == WorkingDayType.SPECIAL && d == null) {
            return -2;
        }

        LocalDate data = (t == WorkingDayType.SPECIAL) ? d : null;

        WorkingDay wd;

        if (g1 && g2) {
            wd = new WorkingDay(t, true, true, null, null, null, null, data);
        } else if (!g1 && !g2) {
            wd = new WorkingDay(t, false, false, a1, c1, a2, c2, data);
        } else if (g1 && !g2) {
            wd = new WorkingDay(t, true, false, null, null, a2, c2, data);
        } else if (!g1 && g2) {
            wd = new WorkingDay(t, false, true, a1, c1, null, null, data);
        } else {
            return 0;
        }


        int res = workingDayDAO.aggiungiWorkingDay(wd);
        // QUI dovresti fare qualcosa con wd:
        // salvarla su DB, aggiungerla a una lista, ecc.
        // es: workingDays.add(wd);
        return res;
    }

    public int creaWorkingDay(WorkingDay input) {
        return creaWorkingDay(
                input.getType(),
                input.isG1(),
                input.isG2(),
                input.getA1(),
                input.getC1(),
                input.getA2(),
                input.getC2(),
                input.getData()
        );
    }


    public List<WorkingDay> getAllWorkingDays() {
        return workingDayDAO.getAllWorkingDays();
    }

    public List<WorkingDay> getWorkingDaysByType(WorkingDayType type) {
        return workingDayDAO.getWorkingDaysByType(type);
    }

    public int eliminaTemplate(WorkingDayType type) {
        return workingDayDAO.eliminaWorkingDayTemplate(type);
    }

    public int eliminaSpecial(WorkingDayType type, LocalDate data) {
        return workingDayDAO.eliminaWorkingDay(type, data);
    }

}
