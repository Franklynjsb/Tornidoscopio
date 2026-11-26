"use client"

import { createContext, useContext, useState, type ReactNode } from "react"

export type Screen =
  | "cu01-autenticar-usuario"
  | "dashboard"
  | "cu20-gestionar-preguntas"
  | "cu21-alta-pregunta"
  | "cu25-evaluar-candidatos"
  | "cu26-completar-cuestionario"
  | "cu28-orden-merito"

interface WireframeContextType {
  currentScreen: Screen
  setCurrentScreen: (screen: Screen) => void
  userType: "consultor" | "candidato" | null
  setUserType: (type: "consultor" | "candidato" | null) => void
  selectedPreguntaId: number | null
  setSelectedPreguntaId: (id: number | null) => void
  claveIngresoCandidato: string | null
  setClaveIngresoCandidato: (clave: string | null) => void
}

const WireframeContext = createContext<WireframeContextType | undefined>(undefined)

export function WireframeProvider({ children }: { children: ReactNode }) {
  const [currentScreen, setCurrentScreen] = useState<Screen>("cu01-autenticar-usuario")
  const [userType, setUserType] = useState<"consultor" | "candidato" | null>(null)
  const [selectedPreguntaId, setSelectedPreguntaId] = useState<number | null>(null)
  const [claveIngresoCandidato, setClaveIngresoCandidato] = useState<string | null>(null)

  return (
    <WireframeContext.Provider
      value={{
        currentScreen,
        setCurrentScreen,
        userType,
        setUserType,
        selectedPreguntaId,
        setSelectedPreguntaId,
        claveIngresoCandidato,
        setClaveIngresoCandidato,
      }}
    >
      {children}
    </WireframeContext.Provider>
  )
}

export function useWireframe() {
  const context = useContext(WireframeContext)
  if (context === undefined) {
    throw new Error("useWireframe must be used within a WireframeProvider")
  }
  return context
}
