package it.gestione.entity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class WorkingDay {
    private WorkingDayType type;
    private boolean g1;
    private boolean g2;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime a1;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime c1;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime a2;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime c2;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    public WorkingDay() {
    }

    public WorkingDay (WorkingDayType t, boolean g1, boolean g2, LocalTime a1, LocalTime c1, LocalTime a2, LocalTime c2, LocalDate d) {
        this.type = t;
        this.g1 = g1;
        this.g2 = g2;
        this.a1 = a1;
        this.c1 = c1;
        this.a2 = a2;
        this.c2 = c2;
        this.data = d;
    }

    public WorkingDayType getType() {
        return type;
    }
    public boolean isG1() {
        return g1;
    }
    public boolean isG2() {
        return g2;
    }
    public LocalTime getA1() {
        return a1;
    }
    public LocalTime getC1() {
        return c1;
    }
    public LocalTime getA2() {
        return a2;
    }
    public LocalTime getC2() {
        return c2;
    }
    public LocalDate getData() {
        return data;
    }

    public void setType(WorkingDayType type) {
            this.type = type;
    }
    public void setG1(boolean g1) {
        this.g1 = g1;
    }
    public void setG2(boolean g2) {
        this.g2 = g2;
    }
    public void setA1(LocalTime a1) {
        this.a1 = a1;
    }
    public void setC1(LocalTime c1) {
        this.c1 = c1;
    }
    public void setA2(LocalTime a2) {
        this.a2 = a2;
    }
    public void setC2(LocalTime c2) {
        this.c2 = c2;
    }

}
