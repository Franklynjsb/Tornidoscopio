"use client"

import { useWireframe, type Screen } from "@/lib/wireframe-context"
import {
  Users,
  FileQuestion,
  Award,
  LogOut,
  Home,
  PlusCircle,
  User,
} from "lucide-react"
import { cn } from "@/lib/utils"

const menuItems: { label: string; icon: React.ElementType; screen: Screen }[] = [
  { label: "Inicio", icon: Home, screen: "dashboard" },
  { label: "Gestionar preguntas", icon: FileQuestion, screen: "cu20-gestionar-preguntas" },
  { label: "Alta de pregunta", icon: PlusCircle, screen: "cu21-alta-pregunta" },
  { label: "Evaluar candidatos", icon: Users, screen: "cu25-evaluar-candidatos" },
  { label: "Orden de mérito", icon: Award, screen: "cu28-orden-merito" },
]

export function Sidebar() {
  const { currentScreen, setCurrentScreen, setUserType } = useWireframe()

  const handleLogout = () => {
    setUserType(null)
    setCurrentScreen("cu01-autenticar-usuario")
  }

  return (
    <aside className="flex h-screen w-64 flex-col border-r border-sidebar-border bg-sidebar text-sidebar-foreground">
      <div className="flex items-center gap-3 border-b border-sidebar-border p-4">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground font-bold text-sm">
          CH
        </div>
        <div className="min-w-0">
          <h1 className="text-lg font-bold leading-tight text-sidebar-primary truncate">Capit@l Humano</h1>
          <p className="text-xs text-sidebar-foreground/70 truncate">Sistema de Evaluaciones</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto p-3">
        {menuItems.map((item) => (
          <button
            key={item.screen}
            onClick={() => setCurrentScreen(item.screen)}
            className={cn(
              "flex w-full items-center gap-3 rounded-md px-3 py-2 text-left text-sm font-medium transition-colors",
              currentScreen === item.screen
                ? "bg-sidebar-primary text-sidebar-primary-foreground"
                : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
            )}
          >
            <item.icon className="h-4 w-4 shrink-0" />
            <span className="truncate">{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="border-t border-sidebar-border p-3">
        <div className="mb-2 flex items-center gap-3 px-3 py-2">
          <User className="h-4 w-4 shrink-0 text-sidebar-foreground/80" />
          <div className="min-w-0">
            <p className="text-sm font-medium truncate">Juan Pérez</p>
            <p className="text-xs text-sidebar-foreground/70 truncate">Consultor</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium text-destructive hover:bg-destructive/10 transition-colors"
        >
          <LogOut className="h-4 w-4" />
          Cerrar Sesión
        </button>
      </div>
    </aside>
  )
}
