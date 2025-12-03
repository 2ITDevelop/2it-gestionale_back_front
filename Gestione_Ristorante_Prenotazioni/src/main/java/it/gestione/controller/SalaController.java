package it.gestione.controller;

import it.gestione.entity.ConfigurazioneSala;
import it.gestione.entity.Prenotazione;
import it.gestione.entity.Sala;
import it.gestione.entity.StatoTavolo;
import it.gestione.entity.Tavolo;
import it.gestione.entity.Turno;
import it.gestione.service.GestioneSala;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sala")
@CrossOrigin(origins = "http://localhost:3000")
public class SalaController {

    private final GestioneSala gestioneSala;

    public SalaController(GestioneSala gestioneSala) {
        this.gestioneSala = gestioneSala;
    }

    // ---------- GESTIONE SALA FISICA (Sala + ZonaSala) ---------- //

    @PostMapping
    public ResponseEntity<?> creaSala(@RequestBody Sala body) {
        if (body == null || body.getNome() == null || body.getNome().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Campo obbligatorio: sala.nome");
        }

        int res = gestioneSala.creaSalaConZone(body);

        if (res == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else if (res == 0) {
            // sala già esistente, ma zonesala eventualmente inserite (idempotente)
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Sala già esistente. Zone eventualmente aggiornate/aggiunte.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione/aggiornamento della sala");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSale() {
        List<Sala> sale = gestioneSala.getAllSale();
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/{nomeSala}")
    public ResponseEntity<?> getSala(@PathVariable String nomeSala) {
        Sala sala = gestioneSala.getSala(nomeSala);
        if (sala == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sala non trovata");
        }
        return ResponseEntity.ok(sala);
    }

    @PutMapping("/{nomeSala}/zone")
    public ResponseEntity<?> aggiornaZoneSala(
            @PathVariable String nomeSala,
            @RequestBody Sala body) {

        // il body può contenere solo la lista zone, ma per sicurezza usiamo il nome del path
        Sala sala = new Sala(nomeSala);
        sala.setZone(body.getZone());

        int res = gestioneSala.aggiornaZoneSala(sala);

        if (res == 1) {
            return ResponseEntity.ok("Zone della sala aggiornate");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'aggiornamento delle zone della sala");
        }
    }

    @DeleteMapping("/{nomeSala}")
    public ResponseEntity<?> eliminaSala(@PathVariable String nomeSala) {
        int res = gestioneSala.eliminaSala(nomeSala);

        if (res == 1) {
            return ResponseEntity.ok("Sala eliminata");
        } else if (res == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sala non trovata");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'eliminazione della sala");
        }
    }

    // ---------- CONFIGURAZIONE SALA ---------- //

    @PostMapping("/configurazioni")
    public ResponseEntity<?> creaConfigurazione(@RequestBody ConfigurazioneSala body) {
        // body deve contenere: data, turno, sala.nome
        if (body.getData() == null || body.getTurno() == null || body.getSala() == null
                || body.getSala().getNome() == null || body.getSala().getNome().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Campi obbligatori: data, turno, sala.nome");
        }

        Sala sala = body.getSala();

        int res = gestioneSala.aggiungiConfigurazione(body.getData(), body.getTurno(), sala);

        if (res == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else if (res == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Configurazione già esistente per data/turno/sala");
        } else { // -1 o altri errori
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la creazione della ConfigurazioneSala");
        }
    }

    @GetMapping("/configurazioni")
    public List<ConfigurazioneSala> getAllConfigurazioni() {
        return gestioneSala.getAllConfigurazioni();
    }

    @DeleteMapping("/configurazioni/{nomeSala}/{date}/{turno}")
    public ResponseEntity<?> eliminaConfigurazione(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            int res = gestioneSala.eliminaConfigurazione(date, t, sala);

            if (res >= 0) {
                return ResponseEntity.ok("Configurazioni eliminate: " + res);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Errore durante l'eliminazione della ConfigurazioneSala");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    // ---------- TAVOLI ---------- //

    @GetMapping("/tavoli/{nomeSala}/{date}/{turno}")
    public ResponseEntity<?> getTavoli(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            List<Tavolo> tavoli = gestioneSala.getTavoli(date, t, sala);
            return ResponseEntity.ok(tavoli);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    @PostMapping("/tavoli/{nomeSala}/{date}/{turno}")
    public ResponseEntity<?> aggiungiTavolo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno,
            @RequestBody Tavolo body) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            if (body.getStato() == null) {
                return ResponseEntity.badRequest()
                        .body("Campo obbligatorio: stato tavolo");
            }

            int res = gestioneSala.aggiungiTavolo(date, t, sala, body);

            if (res == 1) {
                return ResponseEntity.status(HttpStatus.CREATED).body(body);
            } else if (res == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Tavolo già esistente in quella posizione per data/turno/sala");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Errore durante l'inserimento del Tavolo");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    @DeleteMapping("/tavoli/{nomeSala}/{date}/{turno}/{x}/{y}")
    public ResponseEntity<?> eliminaTavolo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno,
            @PathVariable int x,
            @PathVariable int y) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            int res = gestioneSala.eliminaTavolo(date, t, sala, x, y);

            if (res == 1) {
                return ResponseEntity.ok("Tavolo eliminato");
            } else if (res == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tavolo non trovato");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Errore durante l'eliminazione del Tavolo");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    @PatchMapping("/tavoli/{nomeSala}/{date}/{turno}/{x}/{y}/stato/{stato}")
    public ResponseEntity<?> aggiornaStatoTavolo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno,
            @PathVariable int x,
            @PathVariable int y,
            @PathVariable String stato) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            StatoTavolo s = StatoTavolo.valueOf(stato.toUpperCase());
            Sala sala = new Sala(nomeSala);

            int res = gestioneSala.aggiornaStatoTavolo(date, t, sala, x, y, s);

            if (res == 1) {
                return ResponseEntity.ok("Stato tavolo aggiornato");
            } else if (res == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tavolo non trovato");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Errore durante l'aggiornamento dello stato tavolo");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno o stato non valido");
        }
    }

    @PatchMapping("/tavoli/{nomeSala}/{date}/{turno}/{x}/{y}/stato-gruppo/{stato}")
    public ResponseEntity<?> aggiornaStatoGruppo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno,
            @PathVariable int x,
            @PathVariable int y,
            @PathVariable String stato) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            StatoTavolo s = StatoTavolo.valueOf(stato.toUpperCase());
            Sala sala = new Sala(nomeSala);

            int res = gestioneSala.aggiornaStatoGruppo(date, t, sala, x, y, s);

            if (res > 0) {
                return ResponseEntity.ok("Tavoli aggiornati nel gruppo: " + res);
            } else if (res == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nessun tavolo trovato alle coordinate specificate");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Errore durante l'aggiornamento dello stato del gruppo");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno o stato non valido");
        }
    }

    // ---------- PRENOTAZIONI <-> GRUPPO DI TAVOLI ---------- //

    /**
     * Assegna una prenotazione all'intero gruppo di tavoli
     * a cui appartiene il tavolo (x,y) per quella configurazione (data, turno, sala).
     *
     * Usa il service: assegnaPrenotazioneAGruppoERiserva
     * (oltre ad associare la prenotazione, segna il gruppo come RISERVATO).
     */
    @PostMapping("/tavoli/{nomeSala}/{date}/{turno}/{x}/{y}/assegna-prenotazione/{nomePrenotazione}")
    public ResponseEntity<?> assegnaPrenotazioneAGruppo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno,
            @PathVariable int x,
            @PathVariable int y,
            @PathVariable String nomePrenotazione) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            int res = gestioneSala.assegnaPrenotazioneAGruppoERiserva(
                    date, t, sala, x, y, nomePrenotazione
            );

            if (res > 0) {
                // res = numero di tavoli del gruppo associati
                return ResponseEntity.ok("Prenotazione assegnata al gruppo. Tavoli associati: " + res);
            } else if (res == 0) {
                // nessun tavolo/gruppo trovato
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nessun gruppo trovato alle coordinate specificate");
            } else { // res == -1
                // conflitto orario o errore / dati non validi
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Impossibile assegnare la prenotazione: conflitto orario o dati non validi");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    /**
     * Restituisce le prenotazioni DISTINTE associate al gruppo di tavoli
     * a cui appartiene il tavolo (x,y) per quella configurazione (data, turno, sala).
     */
    @GetMapping("/tavoli/{nomeSala}/{date}/{turno}/{x}/{y}/prenotazioni-gruppo")
    public ResponseEntity<?> getPrenotazioniGruppo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno,
            @PathVariable int x,
            @PathVariable int y) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            List<Prenotazione> prenotazioni = gestioneSala.getPrenotazioniGruppo(
                    date, t, sala, x, y
            );

            return ResponseEntity.ok(prenotazioni);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    // ---------- POSTI ---------- //

    @GetMapping("/posti/{nomeSala}/{date}/{turno}/gruppi")
    public ResponseEntity<?> getPostiPerGruppo(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            List<Integer> posti = gestioneSala.calcolaPostiPerGruppo(date, t, sala);
            return ResponseEntity.ok(posti);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }

    @GetMapping("/posti/{nomeSala}/{date}/{turno}/totale")
    public ResponseEntity<?> getPostiTotali(
            @PathVariable String nomeSala,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable String turno) {

        try {
            Turno t = Turno.valueOf(turno.toUpperCase());
            Sala sala = new Sala(nomeSala);

            int totale = gestioneSala.calcolaPostiTotali(date, t, sala);
            return ResponseEntity.ok(totale);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Turno non valido");
        }
    }
}
