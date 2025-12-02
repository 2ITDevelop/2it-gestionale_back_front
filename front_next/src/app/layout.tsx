import type { Metadata } from "next";
import "./globals.css";
import ServiceWorkerRegister from "@/components/ServiceWorkerRegister";

export const metadata: Metadata = {
  title: "Gestionale Ristorante",
  description: "PWA per gestione sala, tavoli e prenotazioni",
  manifest: "/manifest.webmanifest",
  themeColor: "#0ea5e9",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="it">
      <body className="min-h-screen bg-slate-950 text-slate-100">
        <ServiceWorkerRegister />
        {children}
      </body>
    </html>
  );
}


