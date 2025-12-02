package it.gestione.entity;

import it.gestione.database.WorkingDayDAO;

import java.util.ArrayList;
import java.util.List;

public class Negozio {
    private String nome;
    private String indirizzo;
    private List<WorkingDay> turni;
    private List<Prenotazione> prenotazioni;
    private List<ConfigurazioneSala> sala;


    public Negozio() {
    }

    public Negozio(String n, String ind) {
        this.nome = n;
        this.indirizzo = ind;
        this.turni = new ArrayList<WorkingDay>();
        this.prenotazioni = new ArrayList<Prenotazione>();
        this.sala = new ArrayList<ConfigurazioneSala>();
    }

    public String getNome() {
        return this.nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getIndirizzo() {
        return this.indirizzo;
    }
    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }
    public void setTurni(List<WorkingDay> turni) {
        this.turni = turni;
    }
    public void setPrenotazioni(List<Prenotazione> prenotazioni) {
        this.prenotazioni = prenotazioni;
    }
    public void setSala(List<ConfigurazioneSala> sala) {
        this.sala = sala;
    }
    public List<Prenotazione> getPrenotazioni() {
        return this.prenotazioni;
    }
    public List<ConfigurazioneSala> getSala() {
        return this.sala;
    }
    public List<WorkingDay> getTurni() {
        return turni;
    }

}
