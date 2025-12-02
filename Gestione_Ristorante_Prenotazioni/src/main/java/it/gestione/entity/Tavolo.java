package it.gestione.entity;

public class Tavolo {

    private int x;                 // coordinata X nella sala
    private int y;                 // coordinata Y nella sala
    private StatoTavolo stato;     // LIBERO / RISERVATO / OCCUPATO


    public Tavolo(){}

    public Tavolo(int x, int y, StatoTavolo s) {
        this.x = x;
        this.y = y;
        this.stato = s;
    }

    public Tavolo(int x, int y) {
        this.x = x;
        this.y = y;
        this.stato = StatoTavolo.LIBERO; // default
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public StatoTavolo getStato() {
        return this.stato;
    }

    public void setStato(StatoTavolo stato) {
        this.stato = stato;
    }

    public void libera() {
        this.stato = StatoTavolo.LIBERO;
    }

    public void riserva() {
        this.stato = StatoTavolo.RISERVATO;
    }

    public void occupa() {
        this.stato = StatoTavolo.OCCUPATO;
    }
}
