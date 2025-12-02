package it.gestione.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class Prenotazione {
    private String nome;
    private int numPersone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime orario;

    private String numeroTelefono;

    public Prenotazione() {}

    public Prenotazione(String n, int num, LocalDate d, LocalTime o, String numT) {
        this.nome = n;
        this.numPersone = num;
        this.date = d;
        this.orario = o;
        this.numeroTelefono = numT;
    }

    public String getNome() {
        return this.nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public int getNumPersone() {
        return this.numPersone;
    }
    public void setNumPersone(int numPersone) {
        this.numPersone = numPersone;
    }
    public LocalDate getDate() {
        return this.date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public LocalTime getOrario() {
        return this.orario;
    }
    public void setOrario(LocalTime orario) {
        this.orario = orario;
    }
    public String getNumeroTelefono() {
        return this.numeroTelefono;
    }
    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

}
