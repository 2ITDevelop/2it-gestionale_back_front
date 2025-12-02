package it.gestione.entity;

public class ZonaSala {
    private int x;
    private int y;
    private TipoZona tipo;
    private int base;
    private int altezza;

    public ZonaSala() {}

    public ZonaSala(int x, int y, TipoZona tipo, int base, int altezza) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.base = base;
        this.altezza = altezza;
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
    public TipoZona getTipo() {
        return this.tipo;
    }
    public void setTipo(TipoZona tipo) {
        this.tipo = tipo;
    }
    public int getBase() {
        return this.base;
    }
    public void setBase(int base) {
        this.base = base;
    }
    public int getAltezza() {
        return this.altezza;
    }
    public void setAltezza(int altezza) {
        this.altezza = altezza;
    }
}
