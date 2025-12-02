package it.gestione.controller;

import it.gestione.entity.Prenotazione;
import it.gestione.service.GestionePrenotazione;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDate;
import java.util.List;

@RestController  // classe che espone API REST
@RequestMapping("/api/prenotazioni")  // prefisso comune: es. /api/prenotazioni
@CrossOrigin(origins = "http://localhost:3000")
public class PrenotazioneController {

    private final GestionePrenotazione gestionePrenotazione;

    public PrenotazioneController(GestionePrenotazione gestionePrenotazione) {
        this.gestionePrenotazione = gestionePrenotazione;
    }

    // ---------- CREATE ---------- //

    @PostMapping  // POST /api/prenotazioni
    public ResponseEntity<?> create(@RequestBody Prenotazione body) {
        // Jackson deserializza il JSON in Prenotazione
        int res = gestionePrenotazione.creaPrenotazione(body);

        if (res == 1) {
            // inserita
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else if (res == 0) {
            // già esistente (data + nome)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Prenotazione già esistente per quella data e quel nome");
        } else { // -1 o altri errori
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'inserimento della prenotazione");
        }
    }

    // ---------- READ ---------- //

    // GET /api/prenotazioni
    @GetMapping
    public List<Prenotazione> getAll() {
        return gestionePrenotazione.getAllPrenotazioni();
    }

    // GET /api/prenotazioni/2025-11-18
    @GetMapping("/{date}")
    public ResponseEntity<?> getByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Prenotazione> lista = gestionePrenotazione.getPrenotazioniByData(date);
        return ResponseEntity.ok(lista); // anche se lista vuota → 200
    }

    // GET /api/prenotazioni/2025-11-18/Mario
    @GetMapping("/{date}/{nome}")
    public ResponseEntity<?> getByDateAndNome(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String nome) {

        Prenotazione p = gestionePrenotazione.getPrenotazione(date, nome);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Prenotazione non trovata");
        }
        return ResponseEntity.ok(p);
    }

    // ---------- DELETE ---------- //

    // DELETE /api/prenotazioni/2025-11-18/Mario
    @DeleteMapping("/{date}/{nome}")
    public ResponseEntity<?> delete(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String nome) {

        int deleted = gestionePrenotazione.eliminaPrenotazione(date, nome);

        if (deleted == 1) {
            return ResponseEntity.ok("Prenotazione eliminata");
        } else if (deleted == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Prenotazione non trovata");
        } else { // -1 o errore generico
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'eliminazione della prenotazione");
        }
    }
}
