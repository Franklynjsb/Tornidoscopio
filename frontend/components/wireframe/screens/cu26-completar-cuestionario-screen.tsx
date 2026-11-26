"use client"

import { useEffect, useState } from "react"
import { useWireframe } from "@/lib/wireframe-context"
import { apiFetch, ApiError } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { AlertCircle, CheckCircle2, ChevronRight, Clock, KeyRound, LogOut } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"

// Alineado con diagramaSecuenciaCU026.puml, DiagramaDeClases.puml y DTOs del backend:
// - CuestionarioDTO: id: Long, estado: EstadoCuestionario, tiempoMaxCompletar: LocalTime
// - BloqueCuestionarioDTO: nroBloque: Integer, esUltimo: Boolean, preguntas: List<PreguntaCuestionarioDTO>
// - PreguntaCuestionarioDTO: id: Long, enunciado: String, opciones: List<ItemOpcionCuestionarioDTO>
// - ItemOpcionCuestionarioDTO: id: Long, descripcion: String
// - RespuestaCandidatoDTO: preguntaId: Long, itemOpcionId: Long
export interface ItemOpcionCuestionarioDTO {
  id: number
  descripcion: string
}

export interface PreguntaCuestionarioDTO {
  id: number
  enunciado: string
  opciones: ItemOpcionCuestionarioDTO[]
}

export interface BloqueCuestionarioDTO {
  nroBloque: number
  esUltimo: boolean
  preguntas: PreguntaCuestionarioDTO[]
}

export interface CuestionarioDTO {
  id: number
  estado: "ACTIVO" | "EN_PROCESO" | "COMPLETO" | "INCOMPLETO" | "SIN_CONTESTAR"
  tiempoMaxCompletar: string
}

export interface RespuestaCandidatoDTO {
  preguntaId: number
  itemOpcionId: number
}

export function CompletarCuestionarioScreen() {
  const { setCurrentScreen, setUserType, claveIngresoCandidato, setClaveIngresoCandidato } = useWireframe()

  const [tokenInput, setTokenInput] = useState(claveIngresoCandidato || "")
  const [cuestionarioDTO, setCuestionarioDTO] = useState<CuestionarioDTO | null>(null)
  const [instrucciones, setInstrucciones] = useState<string>("")
  const [showInstrucciones, setShowInstrucciones] = useState(true)
  const [bloqueDTO, setBloqueDTO] = useState<BloqueCuestionarioDTO | null>(null)

  const [respuestas, setRespuestas] = useState<Record<number, number>>({})
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const [showErrorNoRespondidas, setShowErrorNoRespondidas] = useState(false)
  const [preguntasNoRespondidas, setPreguntasNoRespondidas] = useState<string[]>([])
  const [showSuccess, setShowSuccess] = useState(false)

  // Si ya vino con clave desde CU01, inicia automáticamente el cuestionario (Pasos 1 y 2)
  useEffect(() => {
    if (claveIngresoCandidato) {
      iniciarConClave(claveIngresoCandidato)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Pasos 1 y 2: iniciarCuestionario(token) + obtenerInstrucciones()
  const iniciarConClave = async (clave: string) => {
    setErrorMessage(null)
    setIsLoading(true)
    try {
      const dto = await apiFetch<CuestionarioDTO>("/cuestionarios/iniciar", {
        method: "POST",
        body: JSON.stringify({ claveIngreso: clave.trim().toUpperCase() }),
      })
      setCuestionarioDTO(dto)

      const instrResp = await apiFetch<{ instrucciones: string }>("/cuestionarios/instrucciones")
      setInstrucciones(instrResp.instrucciones)

      // Si el cuestionario ya estaba EN_PROCESO, según el diagrama pasa directo al bloque pendiente
      if (dto.estado === "EN_PROCESO") {
        setShowInstrucciones(false)
        await cargarSiguienteBloque(dto.id)
      } else {
        setShowInstrucciones(true)
      }
    } catch (err) {
      setErrorMessage(err instanceof ApiError ? err.message : "No se pudo conectar con el backend (puerto 8080)")
    } finally {
      setIsLoading(false)
    }
  }

  // Pasos 3 y 4: obtenerSiguienteBloque(idCuestionario)
  const cargarSiguienteBloque = async (idCuestionario: number) => {
    setErrorMessage(null)
    setIsLoading(true)
    try {
      const bloque = await apiFetch<BloqueCuestionarioDTO>(`/cuestionarios/${idCuestionario}/siguiente-bloque`, {
        method: "POST",
      })
      setBloqueDTO(bloque)
      setRespuestas({})
      setShowInstrucciones(false)
    } catch (err) {
      setErrorMessage(err instanceof ApiError ? err.message : "Error al obtener el siguiente bloque del cuestionario")
    } finally {
      setIsLoading(false)
    }
  }

  const handleRespuesta = (preguntaId: number, itemOpcionId: number) => {
    setRespuestas({ ...respuestas, [preguntaId]: itemOpcionId })
  }

  // Pasos 5 a 8: registrarBloqueRespuestas(idCuestionario, respuestas) -> siguiente bloque o finalizarCuestionario
  const handleSiguiente = async () => {
    if (!cuestionarioDTO || !bloqueDTO) return

    const listaRespuestas: RespuestaCandidatoDTO[] = Object.entries(respuestas).map(([pregId, itemId]) => ({
      preguntaId: Number(pregId),
      itemOpcionId: Number(itemId),
    }))

    setIsLoading(true)
    setErrorMessage(null)
    try {
      await apiFetch(`/cuestionarios/${cuestionarioDTO.id}/respuestas`, {
        method: "POST",
        body: JSON.stringify(listaRespuestas),
      })

      // Flujo Alternativo 8.A: existen más bloques
      if (!bloqueDTO.esUltimo) {
        await cargarSiguienteBloque(cuestionarioDTO.id)
      } else {
        // Paso 8: último bloque -> finalizarCuestionario(idCuestionario)
        await apiFetch(`/cuestionarios/${cuestionarioDTO.id}/finalizar`, {
          method: "POST",
        })
        setShowSuccess(true)
      }
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.message.includes("Preguntas sin responder")) {
          const faltantes = bloqueDTO.preguntas
            .filter((p) => !respuestas[p.id])
            .map((p) => p.enunciado)
          setPreguntasNoRespondidas(faltantes.length > 0 ? faltantes : [err.message])
          setShowErrorNoRespondidas(true)
        } else {
          setErrorMessage(err.message)
        }
      } else {
        setErrorMessage("Error de conexión al registrar las respuestas")
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleLogout = () => {
    setClaveIngresoCandidato(null)
    setUserType(null)
    setCurrentScreen("cu01-autenticar-usuario")
  }

  // Si aún no se inició un cuestionario válido (o falló la clave), permite ingresar/corregir la clave
  if (!cuestionarioDTO) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-muted to-background p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <div className="mx-auto w-14 h-14 bg-primary rounded-xl flex items-center justify-center mb-2">
              <KeyRound className="h-7 w-7 text-primary-foreground" />
            </div>
            <CardTitle>Ingresar al Cuestionario</CardTitle>
            <CardDescription>
              Ingrese su clave de 8 caracteres para acceder a la evaluación
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <form
              onSubmit={(e) => {
                e.preventDefault()
                if (tokenInput.trim() && !isLoading) {
                  iniciarConClave(tokenInput)
                }
              }}
              className="space-y-4"
            >
              {errorMessage && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{errorMessage}</AlertDescription>
                </Alert>
              )}
              <div className="space-y-2">
                <Label>Clave de Ingreso</Label>
                <Input
                  placeholder="Ej: AB12CD34"
                  value={tokenInput}
                  onChange={(e) => setTokenInput(e.target.value.toUpperCase())}
                  maxLength={8}
                  className="uppercase font-mono text-center text-lg tracking-widest"
                />
              </div>
              <div className="flex gap-2">
                <Button type="button" variant="outline" className="flex-1" onClick={handleLogout}>
                  Volver
                </Button>
                <Button
                  type="submit"
                  className="flex-1"
                  disabled={isLoading || !tokenInput.trim()}
                >
                  {isLoading ? "Validando..." : "Ingresar"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (showInstrucciones) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-muted to-background p-4">
        <Card className="w-full max-w-2xl">
          <CardHeader className="text-center">
            <div className="mx-auto w-16 h-16 bg-primary rounded-xl flex items-center justify-center mb-2">
              <span className="text-2xl font-bold text-primary-foreground">CH</span>
            </div>
            <CardTitle className="text-2xl">Instrucciones del Cuestionario</CardTitle>
            <CardDescription>
              Lea atentamente las instrucciones antes de comenzar la evaluación
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {errorMessage && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{errorMessage}</AlertDescription>
              </Alert>
            )}

            <div className="p-4 bg-muted rounded-lg space-y-3">
              <h3 className="font-medium">Instrucciones del Proceso de Evaluación</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">{instrucciones}</p>
            </div>

            <Alert>
              <Clock className="h-4 w-4" />
              <AlertDescription>
                Tiempo máximo para completar el cuestionario:{" "}
                <strong>{cuestionarioDTO.tiempoMaxCompletar} hs</strong>
              </AlertDescription>
            </Alert>

            <div className="flex gap-2 pt-4">
              {/* Flujo Alternativo 2.A: estado = ACTIVO y presiona Cancelar */}
              <Button variant="outline" className="flex-1" onClick={handleLogout}>
                Cancelar
              </Button>
              <Button
                className="flex-1"
                disabled={isLoading}
                onClick={() => cargarSiguienteBloque(cuestionarioDTO.id)}
              >
                {isLoading ? "Generando bloques..." : "Aceptar y Comenzar"}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="bg-card border-b border-border sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
              <span className="text-lg font-bold text-primary-foreground">CH</span>
            </div>
            <div>
              <h1 className="font-semibold">Completar Cuestionario</h1>
              <p className="text-xs text-muted-foreground">
                Bloque #{bloqueDTO?.nroBloque} {bloqueDTO?.esUltimo ? "(Último bloque)" : ""}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 text-sm">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <span className="font-mono">Máx: {cuestionarioDTO.tiempoMaxCompletar}</span>
            </div>
            <Button variant="ghost" size="sm" onClick={handleLogout} title="Salir">
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </header>

      {/* Questions */}
      <div className="max-w-4xl mx-auto px-4 py-6 space-y-6">
        {errorMessage && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{errorMessage}</AlertDescription>
          </Alert>
        )}

        {bloqueDTO?.preguntas.map((pregunta, index) => (
          <Card key={pregunta.id}>
            <CardHeader>
              <CardTitle className="text-base flex items-start gap-2">
                <span className="bg-primary text-primary-foreground text-xs px-2 py-1 rounded">
                  #{index + 1}
                </span>
                <span className="leading-relaxed">{pregunta.enunciado}</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <RadioGroup
                value={respuestas[pregunta.id]?.toString() || ""}
                onValueChange={(value) => handleRespuesta(pregunta.id, parseInt(value))}
              >
                <div className="space-y-3">
                  {pregunta.opciones.map((item) => (
                    <div
                      key={item.id}
                      className="flex items-center space-x-3 p-3 rounded-lg border border-border hover:bg-muted/50 cursor-pointer"
                    >
                      <RadioGroupItem value={item.id.toString()} id={`p${pregunta.id}-i${item.id}`} />
                      <Label
                        htmlFor={`p${pregunta.id}-i${item.id}`}
                        className="flex-1 cursor-pointer text-sm"
                      >
                        {item.descripcion}
                      </Label>
                    </div>
                  ))}
                </div>
              </RadioGroup>
            </CardContent>
          </Card>
        ))}

        <div className="flex justify-end gap-2 pt-4">
          <Button onClick={handleSiguiente} disabled={isLoading}>
            {!bloqueDTO?.esUltimo ? (
              <>
                {isLoading ? "Guardando..." : "Siguiente Bloque"}
                <ChevronRight className="h-4 w-4 ml-2" />
              </>
            ) : isLoading ? (
              "Calculando resultado..."
            ) : (
              "Finalizar Cuestionario"
            )}
          </Button>
        </div>
      </div>

      {/* Flujo Alternativo 6.A: Hay preguntas sin responder */}
      <Dialog open={showErrorNoRespondidas} onOpenChange={setShowErrorNoRespondidas}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Hay preguntas sin responder
            </DialogTitle>
            <DialogDescription>
              Debe responder todas las preguntas del bloque actual antes de continuar:
              <ul className="list-disc pl-4 mt-2">
                {preguntasNoRespondidas.map((enunciado, i) => (
                  <li key={i}>{enunciado}</li>
                ))}
              </ul>
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setShowErrorNoRespondidas(false)}>Aceptar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Paso 8: Cuestionario completado con éxito */}
      <Dialog open={showSuccess} onOpenChange={setShowSuccess}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Cuestionario Completado con Éxito
            </DialogTitle>
            <DialogDescription>
              Sus respuestas han sido registradas, el resultado por competencia fue calculado y el cuestionario pasó a
              estado COMPLETO. Muchas gracias por participar.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={handleLogout}>Salir</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
