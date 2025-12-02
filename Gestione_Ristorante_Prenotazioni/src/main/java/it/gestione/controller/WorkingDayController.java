package it.gestione.controller;

import it.gestione.entity.WorkingDay;
import it.gestione.entity.WorkingDayType;
import it.gestione.service.GestioneWorkingDay;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController  // questo pezzo dice che sto è una classe che espone api rest
@RequestMapping("/api/working-days")  //prefisso comune a tutti gli endpoint di questo controller es: GET /api/working-days   o    POST /api/working-days    o   GET /api/working-days/type/WEEKDAY
@CrossOrigin(origins = "http://localhost:3000")
public class WorkingDayController {

    private final GestioneWorkingDay gestioneWorkingDay;

    public WorkingDayController(GestioneWorkingDay gestioneWorkingDay) {
        this.gestioneWorkingDay = gestioneWorkingDay;
    }

    // ---------- CREATE ---------- //

    @PostMapping // con il RequestMapping sopra e PostMapping il metodo risponde a POST /api/working-days
    public ResponseEntity<?> create(@RequestBody WorkingDay body) { //ResponseEntity oggetto di spring che contiene tutta la risposta http con stato headers body e <?> indica che i parametri che consegnamo possono essere di qualunque tipo (nel caso modificare con <WorkingDay> o <String> ecc
// @RequestBody WorkingDay body  ----> qua dice perndi il body del json e traforma in working day È Jackson che fa la deserializzazione JSON → oggetto Java.
        int res = gestioneWorkingDay.creaWorkingDay(body);

        if (res == 1) {
            // inserito
            return ResponseEntity.status(HttpStatus.CREATED).body(body); //CREATED risorsa creata e ritorna anche il working day oggeto creato
        } else if (res == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("WorkingDay già esistente");
        }  else if (res == -2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Data giorno speciale non valida per la creazione del WorkingDay");
        } else { // -1 o altri errori dal DAO
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) //qua posso gestire se è già presentde o meno l'oggetto
                    .body("Errore durante l'inserimento del WorkingDay");
        }
    }

    // ---------- READ ---------- //

    @GetMapping // risponde a GET /api/working-days
    public List<WorkingDay> getAll() {
        return gestioneWorkingDay.getAllWorkingDays();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getByType(@PathVariable String type) { //PathVariable prende il pezzo {type} e lo associa alla variabile type
        try {
            WorkingDayType wdType = WorkingDayType.valueOf(type.toUpperCase()); //converte la striga in enum
            List<WorkingDay> lista = gestioneWorkingDay.getWorkingDaysByType(wdType);
            return ResponseEntity.ok(lista); //status 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Tipo non valido. Valori ammessi: WEEKDAY, SATURDAY, SUNDAY, SPECIAL"); //status 400
        }
    }

    // ---------- DELETE ---------- //

    @DeleteMapping("/template/{type}") // risponde a DeleteMapping
    public ResponseEntity<?> deleteTemplate(@PathVariable String type) {
        try {
            WorkingDayType wdType = WorkingDayType.valueOf(type.toUpperCase());
            int deleted = gestioneWorkingDay.eliminaTemplate(wdType);
            return ResponseEntity.ok("Template eliminati: " + deleted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Tipo non valido. Valori ammessi: WEEKDAY, SATURDAY, SUNDAY, SPECIAL");
        }
    }

    @DeleteMapping("/special/{type}/{date}") // DELETE /api/working-days/special/SPECIAL/2025-11-17
    public ResponseEntity<?> deleteSpecial(@PathVariable String type,
                                           @PathVariable
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // dice che il formato atteso è YYYY-MM-DD e quindi trasforma in LocalDate
                                           LocalDate date) {
        try {
            WorkingDayType wdType = WorkingDayType.valueOf(type.toUpperCase());
            int deleted = gestioneWorkingDay.eliminaSpecial(wdType, date);
            return ResponseEntity.ok("Special eliminati: " + deleted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Tipo non valido. Valori ammessi: WEEKDAY, SATURDAY, SUNDAY, SPECIAL");
        }
    }
}
