package it.gestione.entity;

import java.util.ArrayList;
import java.util.List;

public class Sala {
    private String nome;
    private List<ZonaSala> zone;

    public Sala() {}

    public Sala(String nome, List<ZonaSala> zone) {
        this.nome = nome;
        this.zone = zone;
    }

    public Sala(String nome) {
        this.nome = nome;
        this.zone = new ArrayList<ZonaSala>();
    }

    public String getNome() {
        return this.nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public List<ZonaSala> getZone() {
        return this.zone;
    }
    public void setZone(List<ZonaSala> zone) {
        this.zone = zone;
    }

}
