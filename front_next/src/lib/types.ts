export type Turno = "PRANZO" | "CENA";

export interface ZonaSala {
  id?: number;
  x: number;
  y: number;
  base: number;
  altezza: number;
  tipoZona: "SPAZIO_VIVIBILE" | "SPAZIO_NON_VIVIBILE";
}

export interface Sala {
  id?: number;
  nome: string;
  zone?: ZonaSala[];
}

export type StatoTavolo = "LIBERO" | "RISERVATO" | "OCCUPATO";

export interface Tavolo {
  id?: number;
  x: number;
  y: number;
  stato: StatoTavolo;
  // se nel JSON arrivano altri campi (idConfigurazione ecc.), li aggiungiamo dopo
}

export interface ConfigurazioneSala {
  id?: number;
  data: string; // ISO date (yyyy-mm-dd)
  turno: Turno;
  sala: Sala;
}

export interface Prenotazione {
  id?: number;
  data: string; // ISO date
  nome: string;
  numPersone: number;
  orario: string; // tipo "20:30"
  telefono: string;
}
