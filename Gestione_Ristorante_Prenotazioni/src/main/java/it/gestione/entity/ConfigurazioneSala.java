package it.gestione.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConfigurazioneSala {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    private Turno turno;
    private Sala sala;              // <-- nuova relazione
    private List<Tavolo> tavoli;

    public ConfigurazioneSala() {
        this.tavoli = new ArrayList<>();
    }

    public ConfigurazioneSala(LocalDate data, Turno turno, Sala sala) {
        this.data = data;
        this.turno = turno;
        this.sala = sala;
        this.tavoli = new ArrayList<>();
    }

    // --- getter / setter ---

    public LocalDate getData() {
        return this.data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Turno getTurno() {
        return this.turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public List<Tavolo> getTavoli() {
        return tavoli;
    }

    public void setTavoli(List<Tavolo> tavoli) {
        this.tavoli = tavoli;
    }
}
