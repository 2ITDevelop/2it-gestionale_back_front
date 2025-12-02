import Link from "next/link";

const sections = [
  {
    href: "/tavoli",
    title: "Planimetria Tavoli",
    description: "Gestisci disposizione, unione e stato dei tavoli.",
  },
  {
    href: "/prenotazioni",
    title: "Prenotazioni",
    description: "Visualizza, crea e cancella le prenotazioni.",
  },
  {
    href: "/working-days",
    title: "Giorni e Orari",
    description: "Imposta giorni di apertura e turni pranzo/cena.",
  },
];

export default function HomePage() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center px-4">
      <div className="max-w-4xl w-full">
        <h1 className="text-3xl md:text-4xl font-semibold mb-6">
          Gestionale Ristorante
        </h1>
        <p className="text-slate-300 mb-10">
          Seleziona una sezione per iniziare a gestire sala, tavoli e
          prenotazioni.
        </p>

        <div className="grid gap-4 md:grid-cols-3">
          {sections.map((s) => (
            <Link
              key={s.href}
              href={s.href}
              className="block rounded-xl border border-slate-700 bg-slate-900/60 p-4 hover:border-cyan-400 hover:bg-slate-900 transition"
            >
              <h2 className="text-lg font-medium mb-2">{s.title}</h2>
              <p className="text-sm text-slate-400">{s.description}</p>
            </Link>
          ))}
        </div>
      </div>
    </main>
  );
}
