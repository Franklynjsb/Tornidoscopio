"use client"

import { WireframeProvider, useWireframe } from "@/lib/wireframe-context"
import { Sidebar } from "@/components/wireframe/sidebar"
import { DashboardScreen } from "@/components/wireframe/screens/dashboard-screen"
import { AutenticarUsuarioScreen } from "@/components/wireframe/screens/cu01-autenticar-usuario-screen"
import { GestionarPreguntasScreen } from "@/components/wireframe/screens/cu20-gestionar-preguntas-screen"
import { AltaPreguntaScreen } from "@/components/wireframe/screens/cu21-alta-pregunta-screen"
import { EvaluarCandidatosScreen } from "@/components/wireframe/screens/cu25-evaluar-candidatos-screen"
import { CompletarCuestionarioScreen } from "@/components/wireframe/screens/cu26-completar-cuestionario-screen"
import { OrdenMeritoScreen } from "@/components/wireframe/screens/cu28-orden-merito-screen"

function WireframeContent() {
  const { currentScreen, userType } = useWireframe()

  // CU01 - Autenticar Usuario
  if (currentScreen === "cu01-autenticar-usuario") {
    return <AutenticarUsuarioScreen />
  }

  // Actor Candidato -> CU26 - Completar Cuestionario (sin sidebar)
  if (userType === "candidato" || currentScreen === "cu26-completar-cuestionario") {
    return <CompletarCuestionarioScreen />
  }

  // Actor Consultor -> Flujo con sidebar
  const renderScreen = () => {
    switch (currentScreen) {
      case "dashboard":
        return <DashboardScreen />
      case "cu20-gestionar-preguntas":
        return <GestionarPreguntasScreen />
      case "cu21-alta-pregunta":
        return <AltaPreguntaScreen />
      case "cu25-evaluar-candidatos":
        return <EvaluarCandidatosScreen />
      case "cu28-orden-merito":
        return <OrdenMeritoScreen />
      default:
        return <DashboardScreen />
    }
  }

  return (
    <div className="flex h-screen">
      <Sidebar />
      <main className="flex-1 overflow-auto bg-background">
        {renderScreen()}
      </main>
    </div>
  )
}

export default function Home() {
  return (
    <WireframeProvider>
      <WireframeContent />
    </WireframeProvider>
  )
}
