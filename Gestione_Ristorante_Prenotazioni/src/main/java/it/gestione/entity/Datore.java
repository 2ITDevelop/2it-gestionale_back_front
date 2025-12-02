package it.gestione.entity;

import java.util.ArrayList;

public class Datore {
    private String nome;
    private String cognome;
    private String email;
    private ArrayList<Negozio> negozi;

    public Datore(String n, String c, String e) {
        this.nome = n;
        this.cognome = c;
        this.email = e;
        this.negozi = new ArrayList<>();
    }

}
