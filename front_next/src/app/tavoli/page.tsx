"use client";

import { useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api-client";
import type { Sala, Tavolo, Turno, StatoTavolo } from "@/lib/types";

const TURNI: Turno[] = ["PRANZO", "CENA"];

// 🔹 helper per ciclio stato tavolo
function nextStato(stato: StatoTavolo): StatoTavolo {
  if (stato === "LIBERO") return "RISERVATO";
  if (stato === "RISERVATO") return "OCCUPATO";
  return "LIBERO";
}

// 🔹 crea sala + configurazione + primo tavolo usando SOLO API esistenti
async function creaSetupDemo(date: string, turno: Turno): Promise<string> {
  const nomeSala = "Sala Principale";

  // 1) crea/aggiorna sala base (solo nome, senza zone per semplificare)
  await api.post<Sala>("/sala", {
    nome: nomeSala,
  });

  // 2) crea configurazione per data+turno
  await api.post("/sala/configurazioni", {
    data: date,
    turno,
    sala: { nome: nomeSala },
  });

  // 3) aggiungi un tavolo iniziale in (0,0)
  await api.post<Tavolo>(
    `/sala/tavoli/${encodeURIComponent(nomeSala)}/${date}/${turno}`,
    {
      x: 0,
      y: 0,
      stato: "LIBERO",
    }
  );

  return nomeSala;
}

export default function TavoliPage() {
  const [sale, setSale] = useState<Sala[]>([]);
  const [selectedSala, setSelectedSala] = useState<string>("");

  const [date, setDate] = useState<string>(
    new Date().toISOString().slice(0, 10)
  );
  const [turno, setTurno] = useState<Turno>("CENA");

  const [tavoli, setTavoli] = useState<Tavolo[]>([]);
  const [loadingTavoli, setLoadingTavoli] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [seeding, setSeeding] = useState(false);

  // 🔹 carica sale all'avvio
  useEffect(() => {
    api
      .get<Sala[]>("/sala")
      .then((data) => {
        setSale(data);
        setError(null);
        // se non hai ancora selezionato nulla e c'è una sala, seleziona la prima
        if (!selectedSala && data.length > 0) {
          setSelectedSala(data[0].nome);
        }
      })
      .catch((err: any) => {
        console.error(err);
        setError(err.message ?? "Errore nel caricamento delle sale");
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 🔹 carica tavoli quando cambiano sala/data/turno
  useEffect(() => {
    if (!selectedSala) return;

    setLoadingTavoli(true);
    api
      .get<Tavolo[]>(
        `/sala/tavoli/${encodeURIComponent(selectedSala)}/${date}/${turno}`
      )
      .then((data) => {
        setTavoli(data);
        setError(null);
      })
      .catch((err: any) => {
        console.error(err);
        setError(err.message ?? "Errore nel caricamento dei tavoli");
        setTavoli([]);
      })
      .finally(() => setLoadingTavoli(false));
  }, [selectedSala, date, turno]);

  // 🔹 mappa rapida per trovare tavolo da (x,y)
  const tavoliMap = useMemo(() => {
    const map = new Map<string, Tavolo>();
    for (const t of tavoli) {
      map.set(`${t.x},${t.y}`, t);
    }
    return map;
  }, [tavoli]);

  // 🔹 dimensioni griglia in base ai tavoli presenti
  const { cols, rows } = useMemo(() => {
    if (tavoli.length === 0) {
      return { cols: 10, rows: 6 };
    }
    const maxX = Math.max(...tavoli.map((t) => t.x));
    const maxY = Math.max(...tavoli.map((t) => t.y));
    // un minimo per non avere griglie minuscole
    return { cols: Math.max(maxX + 3, 10), rows: Math.max(maxY + 3, 6) };
  }, [tavoli]);

  // 🔹 azione: crea tavolo in una cella vuota
  async function handleCreateTable(x: number, y: number) {
    if (!selectedSala) return;
    try {
      setError(null);
      await api.post<Tavolo>(
        `/sala/tavoli/${encodeURIComponent(selectedSala)}/${date}/${turno}`,
        {
          x,
          y,
          stato: "LIBERO",
        }
      );
      // ricarica tavoli
      const data = await api.get<Tavolo[]>(
        `/sala/tavoli/${encodeURIComponent(selectedSala)}/${date}/${turno}`
      );
      setTavoli(data);
    } catch (err: any) {
      console.error(err);
      setError(err.message ?? "Errore nella creazione del tavolo");
    }
  }

  // 🔹 azione: cambia stato tavolo (click)
  async function handleToggleState(t: Tavolo) {
    if (!selectedSala) return;
    const nuovoStato = nextStato(t.stato as StatoTavolo);
    try {
      setError(null);
      await api.patch(
        `/sala/tavoli/${encodeURIComponent(
          selectedSala
        )}/${date}/${turno}/${t.x}/${t.y}/stato/${nuovoStato}`
      );
      // aggiorna localmente per essere più reattivo
      setTavoli((prev) =>
        prev.map((old) =>
          old.x === t.x && old.y === t.y
            ? { ...old, stato: nuovoStato }
            : old
        )
      );
    } catch (err: any) {
      console.error(err);
      setError(err.message ?? "Errore nel cambio stato tavolo");
    }
  }

  // 🔹 azione: elimina tavolo (tasto destro)
  async function handleDeleteTable(t: Tavolo) {
    if (!selectedSala) return;
    try {
      setError(null);
      await api.delete(
        `/sala/tavoli/${encodeURIComponent(
          selectedSala
        )}/${date}/${turno}/${t.x}/${t.y}`
      );
      setTavoli((prev) =>
        prev.filter((old) => !(old.x === t.x && old.y === t.y))
      );
    } catch (err: any) {
      console.error(err);
      setError(err.message ?? "Errore nell'eliminazione del tavolo");
    }
  }

  return (
    <main className="p-4 md:p-8">
      <h1 className="text-2xl md:text-3xl font-semibold mb-4">
        Planimetria Tavoli
      </h1>

      {error && (
        <p className="text-sm text-red-400 mb-3">Errore: {error}</p>
      )}

      {/* FILTRI + crea demo */}
      <div className="flex flex-wrap items-center gap-3 mb-6">
        <select
          className="bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm"
          value={selectedSala}
          onChange={(e) => setSelectedSala(e.target.value)}
        >
          <option value="">Seleziona sala</option>
          {sale.map((s) => (
            <option key={s.id ?? s.nome} value={s.nome}>
              {s.nome}
            </option>
          ))}
        </select>

        <input
          type="date"
          className="bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />

        <select
          className="bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm"
          value={turno}
          onChange={(e) => setTurno(e.target.value as Turno)}
        >
          {TURNI.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>

        <button
          type="button"
          onClick={async () => {
            try {
              setSeeding(true);
              setError(null);
              const nomeSala = await creaSetupDemo(date, turno);
              // ricarica sale
              const saleAgg = await api.get<Sala[]>("/sala");
              setSale(saleAgg);
              setSelectedSala(nomeSala);
              // ricarica tavoli
              const tav = await api.get<Tavolo[]>(
                `/sala/tavoli/${encodeURIComponent(
                  nomeSala
                )}/${date}/${turno}`
              );
              setTavoli(tav);
            } catch (err: any) {
              console.error(err);
              setError(
                err.message ?? "Errore nella creazione della demo"
              );
            } finally {
              setSeeding(false);
            }
          }}
          className="ml-auto bg-emerald-600 hover:bg-emerald-500 text-xs md:text-sm px-4 py-2 rounded-lg"
        >
          {seeding ? "Creazione dati demo..." : "Crea sala/tavoli demo"}
        </button>
      </div>

      <div className="grid gap-6 md:grid-cols-[2fr,1fr]">
        {/* AREA PLANIMETRIA */}
        <div className="rounded-xl border border-slate-700 bg-slate-900/60 p-4 min-h-[400px]">
          {!selectedSala && (
            <p className="text-sm text-slate-400">
              Seleziona una sala o crea una demo per iniziare.
            </p>
          )}

          {selectedSala && loadingTavoli && (
            <p className="text-sm text-slate-400">
              Caricamento tavoli...
            </p>
          )}

          {selectedSala && !loadingTavoli && (
            <div className="w-full h-full">
              <div
                className="border border-slate-700 rounded-lg bg-slate-950/80 p-2 overflow-auto"
                style={{ maxHeight: "600px" }}
              >
                <div
                  className="grid gap-1"
                  style={{
                    gridTemplateColumns: `repeat(${cols}, minmax(24px, 1fr))`,
                  }}
                >
                  {Array.from({ length: rows }).map((_, row) =>
                    Array.from({ length: cols }).map((__, col) => {
                      const key = `${col},${row}`;
                      const tavolo = tavoliMap.get(key);

                      if (tavolo) {
                        const colorClass =
                          tavolo.stato === "LIBERO"
                            ? "bg-emerald-500/80"
                            : tavolo.stato === "RISERVATO"
                            ? "bg-amber-500/80"
                            : "bg-rose-500/80";

                        return (
                          <button
                            key={key}
                            onClick={() => handleToggleState(tavolo)}
                            onContextMenu={(e) => {
                              e.preventDefault();
                              handleDeleteTable(tavolo);
                            }}
                            className={`flex items-center justify-center text-[10px] md:text-xs font-semibold rounded ${colorClass} hover:opacity-80 transition`}
                            title={`Tavolo (${tavolo.x},${tavolo.y}) - ${tavolo.stato}`}
                          >
                            {tavolo.x},{tavolo.y}
                          </button>
                        );
                      }

                      // cella vuota → crea tavolo al click
                      return (
                        <button
                          key={key}
                          onClick={() => handleCreateTable(col, row)}
                          className="h-7 md:h-8 rounded border border-slate-800/60 bg-slate-900/40 hover:bg-slate-800/80 transition"
                          title={`Crea tavolo in (${col},${row})`}
                        />
                      );
                    })
                  )}
                </div>
              </div>
              <p className="text-[11px] text-slate-500 mt-2">
                Suggerimento: clic su cella vuota = crea tavolo. Clic su
                tavolo = cambia stato. Tasto destro su tavolo = elimina.
              </p>
            </div>
          )}
        </div>

        {/* RIEPILOGO / DEBUG */}
        <aside className="rounded-xl border border-slate-700 bg-slate-900/60 p-4 text-sm">
          <h2 className="font-medium mb-2">Riepilogo configurazione</h2>
          <p className="text-slate-300 mb-2">
            Sala:{" "}
            <span className="font-semibold">
              {selectedSala || "—"}
            </span>
          </p>
          <p className="text-slate-300 mb-2">
            Data: <span className="font-mono">{date}</span>
          </p>
          <p className="text-slate-300 mb-4">
            Turno: <span className="font-semibold">{turno}</span>
          </p>

          <p className="text-slate-300 mb-2">
            Tavoli totali:{" "}
            <span className="font-semibold">{tavoli.length}</span>
          </p>

          <details className="mt-3">
            <summary className="cursor-pointer text-slate-400 hover:text-slate-200">
              Mostra JSON tavoli (debug)
            </summary>
            <pre className="mt-2 text-[10px] max-h-64 overflow-auto bg-slate-950/80 border border-slate-800 rounded p-2 text-slate-300">
              {JSON.stringify(tavoli, null, 2)}
            </pre>
          </details>
        </aside>
      </div>
    </main>
  );
}
