"use client"

import { useWireframe, type Screen } from "@/lib/wireframe-context"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Building2,
  FileQuestion,
  Users,
  ClipboardCheck,
  Award,
  PlusCircle,
} from "lucide-react"

const stats = [
  { label: "Puestos Activos", value: "24", icon: Building2, color: "text-primary" },
  { label: "Preguntas Registradas", value: "156", icon: FileQuestion, color: "text-accent" },
  { label: "Candidatos en Evaluación", value: "18", icon: Users, color: "text-chart-3" },
  { label: "Cuestionarios Completos", value: "42", icon: ClipboardCheck, color: "text-chart-4" },
]

const quickActions: { label: string; screen: Screen; icon: React.ElementType }[] = [
  { label: "Gestionar Preguntas", screen: "cu20-gestionar-preguntas", icon: FileQuestion },
  { label: "Dar de Alta Pregunta", screen: "cu21-alta-pregunta", icon: PlusCircle },
  { label: "Evaluar Candidatos", screen: "cu25-evaluar-candidatos", icon: Users },
  { label: "Emitir Orden de Mérito", screen: "cu28-orden-merito", icon: Award },
]

export function DashboardScreen() {
  const { setCurrentScreen } = useWireframe()

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Panel Principal</h1>
        <p className="text-muted-foreground">
          Bienvenido al Sistema de Gestión de Evaluaciones de Perfiles (Capit@l Humano)
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardContent className="pt-6">
              <div className="flex items-center gap-4">
                <div className={`p-3 rounded-lg bg-muted ${stat.color}`}>
                  <stat.icon className="h-6 w-6" />
                </div>
                <div>
                  <p className="text-2xl font-bold">{stat.value}</p>
                  <p className="text-sm text-muted-foreground">{stat.label}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Casos de Uso Implementados</CardTitle>
          <CardDescription>Acceda directamente a cada uno de los Casos de Uso</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {quickActions.map((action) => (
              <button
                key={action.screen}
                onClick={() => setCurrentScreen(action.screen)}
                className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-muted transition-colors text-left"
              >
                <div className="p-2 rounded-md bg-primary/10 text-primary">
                  <action.icon className="h-5 w-5" />
                </div>
                <span className="font-medium text-sm">{action.label}</span>
              </button>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
