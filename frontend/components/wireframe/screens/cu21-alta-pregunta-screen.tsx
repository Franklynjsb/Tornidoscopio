"use client"

import { useEffect, useState } from "react"
import { useWireframe } from "@/lib/wireframe-context"
import { apiFetch, ApiError } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { AlertCircle, CheckCircle2, Sparkles } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"

// Alineado con DiagramaSecuenciaCU021.puml y DiagramaDeClases.puml:
// - GestorCompetencias.buscar -> List<CompetenciaDTO>
// - GestorOpcionesRespuesta.buscar -> List<OpcionRespuestaDTO> (con List<ItemOpcionDTO>)
// - GestorFactores.buscarPorCompetencia(idCompetencia) -> List<FactorDTO>
// - GestorPreguntas.generarPreguntaIA(idCompetencia, idFactor, descripcion, idOpcionRespuesta) -> PreguntaSugeridaDTO
// - GestorPreguntas.validarOmisiones(p: PreguntaDTO) -> List<String>
// - GestorPreguntas.validarLongitudMaxima(p: PreguntaDTO) -> List<String>
// - GestorPreguntas.validarPonderacion(opc: List<PreguntaOpcionDTO>) -> Boolean
// - GestorPreguntas.altaPregunta(p: PreguntaDTO) -> PreguntaDTO
// - GestorPreguntas.asignarOpciones(idPregunta: Long, opc: List<PreguntaOpcionDTO>) -> Void

interface CompetenciaDTO {
  id: number
  codigo: string
  nombre: string
  descripcion: string
}

interface FactorDTO {
  id: number
  nombre: string
  descripcion: string
  nroOrden: number
  idCompetencia: number
  nombreCompetencia: string
}

interface ItemOpcionDTO {
  id: number
  ordenVisualizacion: number
  descripcion: string
}

interface OpcionRespuestaDTO {
  id: number
  nombre: string
  descripcion: string
  items: ItemOpcionDTO[]
}

interface PreguntaSugeridaDTO {
  nombrePregunta: string
  enunciado: string
  descripcion: string
}

const MAX_LEN_NOMBRE = 150

export function AltaPreguntaScreen() {
  const { setCurrentScreen } = useWireframe()
  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([])
  const [factores, setFactores] = useState<FactorDTO[]>([])
  const [opcionesRespuesta, setOpcionesRespuesta] = useState<OpcionRespuestaDTO[]>([])

  const [idCompetencia, setIdCompetencia] = useState("")
  const [idFactor, setIdFactor] = useState("")
  const [nombre, setNombre] = useState("")
  const [enunciado, setEnunciado] = useState("")
  const [descripcion, setDescripcion] = useState("")
  const [idOpcionRespuesta, setIdOpcionRespuesta] = useState("")
  const [ponderaciones, setPonderaciones] = useState<Record<number, string>>({})

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [validationMessages, setValidationMessages] = useState<string[]>([])
  const [iaBanner, setIaBanner] = useState<string | null>(null)
  const [showSuccess, setShowSuccess] = useState(false)
  const [duplicateMessage, setDuplicateMessage] = useState<string | null>(null)
  const [showPonderacionError, setShowPonderacionError] = useState(false)

  // DiagramaSecuenciaCU021: Carga inicial de Competencias y Opciones de Respuesta
  useEffect(() => {
    apiFetch<CompetenciaDTO[]>("/competencias")
      .then(setCompetencias)
      .catch(() => setValidationMessages(["No se pudo conectar con el backend para cargar las competencias"]))

    apiFetch<OpcionRespuestaDTO[]>("/opciones-respuesta")
      .then(setOpcionesRespuesta)
      .catch(() => {})
  }, [])

  // DiagramaSecuenciaCU021: seleccionarCompetencia(idCompetencia) -> gestorFac.buscarPorCompetencia(idCompetencia)
  useEffect(() => {
    if (!idCompetencia) {
      setFactores([])
      return
    }
    apiFetch<FactorDTO[]>(`/competencias/${idCompetencia}/factores`)
      .then(setFactores)
      .catch(() => setFactores([]))
  }, [idCompetencia])

  const selectedOpcionRespuesta = opcionesRespuesta.find((o) => o.id === Number(idOpcionRespuesta))

  const handlePonderacionChange = (idItemOpcion: number, value: string) => {
    setPonderaciones({ ...ponderaciones, [idItemOpcion]: value })
  }

  const calculateTotalPonderacion = () => {
    return Object.values(ponderaciones).reduce((sum, val) => sum + (parseFloat(val) || 0), 0)
  }

  // Flujo Alternativo 1.A.1 (CU033): GestorPreguntas.generarPreguntaIA(idCompetencia, idFactor, descripcion, idOpcionRespuesta)
  const handleGenerarConIA = async () => {
    setValidationMessages([])
    setIaBanner(null)
    if (!idCompetencia || !idFactor || !idOpcionRespuesta) {
      setValidationMessages([
        "Para generar una pregunta con IA debe seleccionar primero Competencia, Factor y Opción de Respuesta.",
      ])
      return
    }

    try {
      const sugerida = await apiFetch<PreguntaSugeridaDTO | null>("/preguntas/generar-ia", {
        method: "POST",
        body: JSON.stringify({
          idCompetencia: Number(idCompetencia),
          idFactor: Number(idFactor),
          descripcion,
          idOpcionRespuesta: Number(idOpcionRespuesta),
        }),
      })

      if (sugerida && sugerida.nombrePregunta) {
        setNombre(sugerida.nombrePregunta)
        setEnunciado(sugerida.enunciado || "")
        setDescripcion(sugerida.descripcion || "")
      } else {
        const compObj = competencias.find((c) => c.id === Number(idCompetencia))
        const facObj = factores.find((f) => f.id === Number(idFactor))
        setNombre(`EVALUACION DE ${facObj?.nombre || "FACTOR"}`)
        setEnunciado(
          `EN UN ESCENARIO DE ${compObj?.nombre || "TRABAJO"}, ¿LA APLICACION DE BUENAS PRACTICAS DE ${
            facObj?.nombre || "GESTION"
          } MEJORA EL CUMPLIMIENTO DE LOS OBJETIVOS DEL PROYECTO?`
        )
        setDescripcion(
          descripcion.trim() ||
            `PREGUNTA SUGERIDA POR IA PARA EVALUAR ${facObj?.nombre} DENTRO DE ${compObj?.nombre}.`
        )
      }
      setIaBanner(
        "Sugerencia generada con IA. Puede revisar y editar los campos antes de presionar Aceptar."
      )
    } catch {
      setValidationMessages(["No se pudo generar la sugerencia con IA."])
    }
  }

  // Paso 2: Validaciones 2.A (omisiones), 2.B (longitud máxima), ponderaciones (= 10 y >= 0), 2.C (duplicada) y Paso 3 (alta + asignarOpciones)
  const handleSubmit = async () => {
    setIaBanner(null)
    setValidationMessages([])
    setDuplicateMessage(null)

    const listaPreguntaOpcionDTO = (selectedOpcionRespuesta?.items || []).map((item) => ({
      idItemOpcion: item.id,
      ponderacion: ponderaciones[item.id] !== undefined && ponderaciones[item.id] !== ""
        ? parseFloat(ponderaciones[item.id])
        : 0.0,
    }))

    setIsSubmitting(true)
    try {
      await apiFetch("/preguntas", {
        method: "POST",
        body: JSON.stringify({
          pregunta: {
            nombre: nombre.trim(),
            enunciado: enunciado.trim(),
            descripcion: descripcion.trim(),
            idFactor: idFactor ? Number(idFactor) : null,
            idOpcionRespuesta: idOpcionRespuesta ? Number(idOpcionRespuesta) : null,
          },
          opciones: listaPreguntaOpcionDTO,
        }),
      })
      setShowSuccess(true)
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.codigo === "PREGUNTA_DUPLICADA") {
          setDuplicateMessage(err.message)
        } else if (err.codigo === "PONDERACION_INVALIDA") {
          setShowPonderacionError(true)
        } else if (err.detalles && err.detalles.length > 0) {
          setValidationMessages(err.detalles)
        } else {
          setValidationMessages([err.message])
        }
      } else {
        setValidationMessages(["No se pudo conectar con el servidor backend (verifique que esté corriendo en el puerto 8080)"])
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleSuccessResponse = (continueAdding: boolean) => {
    setShowSuccess(false)
    if (continueAdding) {
      setNombre("")
      setEnunciado("")
      setDescripcion("")
      setPonderaciones({})
      setValidationMessages([])
      setIaBanner(null)
    } else {
      setCurrentScreen("cu20-gestionar-preguntas")
    }
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">
            Dar de Alta Pregunta
          </h1>
          <p className="text-muted-foreground">
            Complete los datos de la pregunta y las ponderaciones de sus opciones de respuesta
          </p>
        </div>
        <Button variant="outline" className="gap-2" onClick={handleGenerarConIA}>
          <Sparkles className="h-4 w-4 text-primary" />
          Generar con IA
        </Button>
      </div>

      {iaBanner && (
        <Alert className="border-blue-200 bg-blue-50 text-blue-950">
          <Sparkles className="h-4 w-4 text-blue-700" />
          <AlertDescription>{iaBanner}</AlertDescription>
        </Alert>
      )}

      {validationMessages.length > 0 && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            <ul className="list-disc pl-4">
              {validationMessages.map((msg, i) => (
                <li key={i}>{msg}</li>
              ))}
            </ul>
          </AlertDescription>
        </Alert>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Datos de la Pregunta</CardTitle>
            <CardDescription>Todos los campos son obligatorios excepto Descripción</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>Competencia *</Label>
              <Select
                value={idCompetencia}
                onValueChange={(v) => {
                  setIdCompetencia(v)
                  setIdFactor("")
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione competencia" />
                </SelectTrigger>
                <SelectContent>
                  {competencias.map((comp) => (
                    <SelectItem key={comp.id} value={comp.id.toString()}>
                      {comp.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Factor *</Label>
              <Select value={idFactor} onValueChange={setIdFactor} disabled={!idCompetencia}>
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione factor" />
                </SelectTrigger>
                <SelectContent>
                  {factores.map((fac) => (
                    <SelectItem key={fac.id} value={fac.id.toString()}>
                      {fac.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="nombre-pregunta">Nombre de Pregunta * (máx. {MAX_LEN_NOMBRE} car.)</Label>
              <Input
                id="nombre-pregunta"
                placeholder="Ej: METODOLOGIA VALOR GANADO"
                value={nombre}
                onChange={(e) => setNombre(e.target.value.toUpperCase())}
                className="uppercase"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="enunciado">Enunciado (Pregunta) *</Label>
              <Textarea
                id="enunciado"
                placeholder="Ingrese el enunciado completo de la pregunta..."
                value={enunciado}
                onChange={(e) => setEnunciado(e.target.value.toUpperCase())}
                className="uppercase min-h-[100px]"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="descripcion">Descripción (Opcional)</Label>
              <Textarea
                id="descripcion"
                placeholder="Descripción o contexto adicional..."
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value.toUpperCase())}
                className="uppercase"
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Opción de Respuesta y Ponderaciones</CardTitle>
            <CardDescription>
              Seleccione la opción de respuesta y asigne la ponderación a cada ítem (deben sumar 10 puntos)
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>Opción de Respuesta *</Label>
              <Select
                value={idOpcionRespuesta}
                onValueChange={(v) => {
                  setIdOpcionRespuesta(v)
                  setPonderaciones({})
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione opción de respuesta" />
                </SelectTrigger>
                <SelectContent>
                  {opcionesRespuesta.map((opc) => (
                    <SelectItem key={opc.id} value={opc.id.toString()}>
                      {opc.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {selectedOpcionRespuesta && (
              <>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-16">Orden</TableHead>
                      <TableHead>Descripción del Ítem</TableHead>
                      <TableHead className="w-28">Ponderación</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {selectedOpcionRespuesta.items.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className="font-mono text-center">{item.ordenVisualizacion}</TableCell>
                        <TableCell>{item.descripcion}</TableCell>
                        <TableCell>
                          <Input
                            type="number"
                            min="0"
                            max="10"
                            step="0.5"
                            placeholder="0"
                            value={ponderaciones[item.id] || ""}
                            onChange={(e) => handlePonderacionChange(item.id, e.target.value)}
                            className="w-full"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>

                <div className="flex justify-between items-center p-3 bg-muted rounded-md">
                  <span className="font-medium">Total Ponderaciones:</span>
                  <span
                    className={`font-bold text-lg ${
                      Math.abs(calculateTotalPonderacion() - 10) < 0.001 ? "text-green-600" : "text-destructive"
                    }`}
                  >
                    {calculateTotalPonderacion().toFixed(1)} / 10
                  </span>
                </div>

                <p className="text-xs text-muted-foreground">
                  * Las ponderaciones deben sumar exactamente 10 puntos y ser valores mayores o iguales a 0
                </p>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={() => setCurrentScreen("cu20-gestionar-preguntas")}>
          Cancelar
        </Button>
        <Button onClick={handleSubmit} disabled={isSubmitting}>
          {isSubmitting ? "Guardando..." : "Aceptar"}
        </Button>
      </div>

      {/* Paso 3 y 4: Dialog de éxito con botones SI y NO */}
      <Dialog open={showSuccess} onOpenChange={setShowSuccess}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Pregunta Creada
            </DialogTitle>
            <DialogDescription>
              La Pregunta <strong>{nombre}</strong> se ha creado correctamente. ¿Desea cargar otra?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => handleSuccessResponse(false)}>
              NO
            </Button>
            <Button onClick={() => handleSuccessResponse(true)}>SI</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Flujo Alternativo 2.C: Dialog de pregunta duplicada */}
      <Dialog open={!!duplicateMessage} onOpenChange={() => setDuplicateMessage(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Pregunta Duplicada
            </DialogTitle>
            <DialogDescription>{duplicateMessage}</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setDuplicateMessage(null)}>Aceptar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog de error - ponderación */}
      <Dialog open={showPonderacionError} onOpenChange={setShowPonderacionError}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Error de Ponderación
            </DialogTitle>
            <DialogDescription>
              Las ponderaciones deben sumar 10 puntos y ser mayores o iguales a 0. Actualmente suman:{" "}
              {calculateTotalPonderacion().toFixed(1)}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setShowPonderacionError(false)}>Aceptar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
