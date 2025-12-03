package it.gestione.entity;

public class PrenotazioneTavolo {

    private Prenotazione prenotazione;
    private Tavolo tavolo;


    public PrenotazioneTavolo(Prenotazione prenotazione, Tavolo tavolo) {
        this.prenotazione = prenotazione;
        this.tavolo = tavolo;
    }

    public PrenotazioneTavolo() {}

    public Prenotazione getPrenotazione() {
        return this.prenotazione;
    }
    public void setPrenotazione(Prenotazione prenotazione) {
        this.prenotazione = prenotazione;
    }
    public Tavolo getTavolo() {
        return this.tavolo;
    }
    public void setTavolo(Tavolo tavolo) {
        this.tavolo = tavolo;
    }

}
