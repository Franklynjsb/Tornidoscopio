"use client"

import { useEffect, useState } from "react"
import { useWireframe } from "@/lib/wireframe-context"
import { API_BASE_URL, apiFetch, ApiError } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
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
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Search, Plus, Minus, ChevronRight, ChevronLeft, AlertCircle, CheckCircle2, Download, FileSpreadsheet } from "lucide-react"

export interface CandidatoGestionDTO {
  nroCandidato: number
  apellido: string
  nombre: string
  tipoDocumento: "DNI" | "LC" | "LE" | "PP"
  nroDocumento: string
}

export interface CandidatoClaveDTO {
  nombreCandidato: string
  apellidoCandidato: string
  tipoDocumento: "DNI" | "LC" | "LE" | "PP"
  nroDocumento: string
  claveIngreso: string
}

export interface PuestoDTO {
  id: number
  codigo: string
  nombre: string
  descripcion: string
  idEmpresa: number
  nombreEmpresa: string
}

export interface CompetenciaRequeridaDTO {
  idCompetencia: number
  nombreCompetencia: string
  puntajeMinimoRequerido: number
}

interface ConflictoCuestionarioDTO {
  idCuestionario: number
  nroCandidato: number
  apellido: string
  nombre: string
  nombrePuesto: string
  estado: string
}

type Step = "buscar-candidatos" | "seleccionar-puesto" | "confirmar"

export function EvaluarCandidatosScreen() {
  const { setCurrentScreen } = useWireframe()
  const [step, setStep] = useState<Step>("buscar-candidatos")
  const [searchApellido, setSearchApellido] = useState("")
  const [searchNombre, setSearchNombre] = useState("")
  const [searchNroCandidato, setSearchNroCandidato] = useState("")
  const [hasSearched, setHasSearched] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const [candidatosResultados, setCandidatosResultados] = useState<CandidatoGestionDTO[]>([])
  const [candidatosSeleccionados, setCandidatosSeleccionados] = useState<CandidatoGestionDTO[]>([])
  const [conflictosActivos, setConflictosActivos] = useState<ConflictoCuestionarioDTO[]>([])

  const [puestos, setPuestos] = useState<PuestoDTO[]>([])
  const [puestoSeleccionado, setPuestoSeleccionado] = useState<PuestoDTO | null>(null)
  const [competenciasRequeridas, setCompetenciasRequeridas] = useState<CompetenciaRequeridaDTO[]>([])
  const [competenciasNoEvaluables, setCompetenciasNoEvaluables] = useState<string[]>([])

  const [candidatosConClaves, setCandidatosConClaves] = useState<CandidatoClaveDTO[]>([])

  const [showErrorCandidatosActivos, setShowErrorCandidatosActivos] = useState(false)
  const [showErrorCompetencias, setShowErrorCompetencias] = useState(false)
  const [showErrorSinPuesto, setShowErrorSinPuesto] = useState(false)
  const [showSuccess, setShowSuccess] = useState(false)
  const [showExportDialog, setShowExportDialog] = useState(false)

  // Al entrar al paso 2 carga los puestos activos desde el backend
  useEffect(() => {
    if (step === "seleccionar-puesto" && puestos.length === 0) {
      apiFetch<PuestoDTO[]>("/puestos")
        .then(setPuestos)
        .catch(() => setErrorMessage("No se pudieron cargar los puestos desde el backend"))
    }
  }, [step, puestos.length])

  // CU025 Paso 3: GestorEvaluacion.buscarCandidatos(criterios)
  const handleSearch = async () => {
    setErrorMessage(null)
    setIsLoading(true)
    try {
      const termino = [searchApellido.trim(), searchNombre.trim(), searchNroCandidato.trim()]
        .filter(Boolean)
        .join(" ")

      const lista = await apiFetch<CandidatoGestionDTO[]>(
        `/evaluaciones/candidatos?criterios=${encodeURIComponent(termino)}`
      )

      // Filtrado adicional en cliente si ingresó campos separados simultáneamente
      const filtrados = lista.filter((c) => {
        const matchAp = !searchApellido.trim() || c.apellido.toUpperCase().includes(searchApellido.trim().toUpperCase())
        const matchNom = !searchNombre.trim() || c.nombre.toUpperCase().includes(searchNombre.trim().toUpperCase())
        const matchNro = !searchNroCandidato.trim() || c.nroCandidato.toString().includes(searchNroCandidato.trim())
        return matchAp && matchNom && matchNro
      })

      setCandidatosResultados(filtrados)
      setHasSearched(true)
    } catch {
      setErrorMessage("No se pudo conectar con el servidor backend (puerto 8080)")
    } finally {
      setIsLoading(false)
    }
  }

  const handleAddCandidato = (candidato: CandidatoGestionDTO) => {
    if (!candidatosSeleccionados.some((c) => c.nroCandidato === candidato.nroCandidato)) {
      setCandidatosSeleccionados([...candidatosSeleccionados, candidato])
    }
  }

  const handleRemoveCandidato = (nroCandidato: number) => {
    setCandidatosSeleccionados(candidatosSeleccionados.filter((c) => c.nroCandidato !== nroCandidato))
  }

  // DiagramaSecuenciaCU025 Paso 9 y 9.A: seleccionarPuesto(idPuesto) -> obtenerCompetenciasRequeridas(idPuesto)
  const handleSelectPuesto = async (idStr: string) => {
    const puesto = puestos.find((p) => p.id === Number(idStr)) || null
    setPuestoSeleccionado(puesto)
    setCompetenciasRequeridas([])
    setCompetenciasNoEvaluables([])
    if (!puesto) return

    try {
      const comps = await apiFetch<CompetenciaRequeridaDTO[]>(`/puestos/${puesto.id}/competencias-requeridas`)
      setCompetenciasRequeridas(comps)
    } catch (err) {
      if (err instanceof ApiError && err.codigo === "COMPETENCIAS_NO_EVALUABLES") {
        setCompetenciasNoEvaluables(err.detalles || [err.message])
        setShowErrorCompetencias(true)
      } else if (err instanceof ApiError) {
        setErrorMessage(err.message)
      }
    }
  }

  const handleNextStep = async () => {
    setErrorMessage(null)
    if (step === "buscar-candidatos") {
      if (candidatosSeleccionados.length === 0) return

      // Flujo Alternativo 6.A: GestorEvaluacion.validarCuestionariosActivos(idsAEvaluar)
      try {
        const ids = candidatosSeleccionados.map((c) => c.nroCandidato)
        const conflictos = await apiFetch<ConflictoCuestionarioDTO[]>("/evaluaciones/validar-activos", {
          method: "POST",
          body: JSON.stringify(ids),
        })
        if (conflictos.length > 0) {
          setConflictosActivos(conflictos)
          setShowErrorCandidatosActivos(true)
          return
        }
        setStep("seleccionar-puesto")
      } catch (err) {
        setErrorMessage(err instanceof ApiError ? err.message : "Error al validar cuestionarios activos")
      }
    } else if (step === "seleccionar-puesto") {
      // Flujo Alternativo 10.A: No se seleccionó ningún puesto
      if (!puestoSeleccionado) {
        setShowErrorSinPuesto(true)
        return
      }

      if (competenciasNoEvaluables.length > 0) {
        setShowErrorCompetencias(true)
        return
      }

      // Paso 10-11: GestorEvaluacion.generarCuestionarios(puestoDTOSeleccionado, listaCandGestDTOSeleccionados)
      try {
        const generados = await apiFetch<CandidatoClaveDTO[]>("/evaluaciones/generar", {
          method: "POST",
          body: JSON.stringify({
            puesto: puestoSeleccionado,
            candidatos: candidatosSeleccionados,
          }),
        })
        setCandidatosConClaves(generados)
        setStep("confirmar")
      } catch (err) {
        setErrorMessage(err instanceof ApiError ? err.message : "Error al generar los cuestionarios")
      }
    }
  }

  const handlePrevStep = () => {
    if (step === "seleccionar-puesto") {
      setStep("buscar-candidatos")
    } else if (step === "confirmar") {
      setStep("seleccionar-puesto")
    }
  }

  // Paso 12: GestorEvaluacion.confirmarYGuardarCuestionarios()
  const handleFinalizar = async () => {
    setErrorMessage(null)
    try {
      await apiFetch("/evaluaciones/confirmar", { method: "POST" })
      setShowSuccess(true)
    } catch (err) {
      setErrorMessage(err instanceof ApiError ? err.message : "Error al confirmar y guardar los cuestionarios")
    }
  }

  // Paso 16: GestorEvaluacion.exportarCandidatosConClaves(idPuesto, listaCandidatoClaveDTO)
  const handleDescargarExcel = async () => {
    if (!puestoSeleccionado) return
    try {
      const response = await fetch(`${API_BASE_URL}/evaluaciones/exportar`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          idPuesto: puestoSeleccionado.id,
          candidatos: candidatosConClaves,
        }),
      })
      if (!response.ok) {
        throw new Error("No se pudo generar el archivo Excel")
      }
      const blob = await response.blob()
      const disposition = response.headers.get("Content-Disposition") || ""
      const match = disposition.match(/filename="(.+)"/)
      const filename = match ? match[1] : `Evaluacion-${puestoSeleccionado.nombre}.xls`

      const url = window.URL.createObjectURL(blob)
      const a = document.createElement("a")
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(url)

      setShowExportDialog(false)
      setCurrentScreen("dashboard")
    } catch {
      setErrorMessage("Error al descargar el archivo Excel")
    }
  }

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Evaluar Candidatos</h1>
        <p className="text-muted-foreground">
          Seleccione los candidatos a evaluar, asigne un puesto o función y genere sus cuestionarios con clave de ingreso
        </p>
      </div>

      {errorMessage && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{errorMessage}</AlertDescription>
        </Alert>
      )}

      {/* Stepper */}
      <div className="flex items-center justify-center gap-4 py-4">
        {["1. Buscar Candidatos", "2. Seleccionar Puesto", "3. Confirmar Cuestionarios"].map((label, i) => {
          const stepKey = ["buscar-candidatos", "seleccionar-puesto", "confirmar"][i] as Step
          const isActive = step === stepKey
          const isPast = ["buscar-candidatos", "seleccionar-puesto", "confirmar"].indexOf(step) > i

          return (
            <div key={label} className="flex items-center gap-2">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : isPast
                    ? "bg-green-500 text-white"
                    : "bg-muted text-muted-foreground"
                }`}
              >
                {isPast ? "✓" : i + 1}
              </div>
              <span className={`text-sm ${isActive ? "font-medium" : "text-muted-foreground"}`}>{label}</span>
              {i < 2 && <ChevronRight className="h-4 w-4 text-muted-foreground" />}
            </div>
          )
        })}
      </div>

      {/* Step 1: Buscar Candidatos (PantallaBusquedaCandidatos) */}
      {step === "buscar-candidatos" && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card>
            <CardHeader>
              <CardTitle>Búsqueda de Candidatos</CardTitle>
              <CardDescription>
                Busque candidatos por apellido, nombre o número de candidato
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-3 gap-2">
                <div className="space-y-2">
                  <Label>Apellido</Label>
                  <Input
                    placeholder="Apellido"
                    value={searchApellido}
                    onChange={(e) => setSearchApellido(e.target.value.toUpperCase())}
                    className="uppercase"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Nombre</Label>
                  <Input
                    placeholder="Nombre"
                    value={searchNombre}
                    onChange={(e) => setSearchNombre(e.target.value.toUpperCase())}
                    className="uppercase"
                  />
                </div>
                <div className="space-y-2">
                  <Label>Nro Candidato</Label>
                  <Input
                    placeholder="1001"
                    value={searchNroCandidato}
                    onChange={(e) => setSearchNroCandidato(e.target.value)}
                  />
                </div>
              </div>
              <Button onClick={handleSearch} className="w-full" disabled={isLoading}>
                <Search className="h-4 w-4 mr-2" />
                {isLoading ? "Buscando..." : "Buscar"}
              </Button>

              {hasSearched && (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Nro</TableHead>
                      <TableHead>Apellido</TableHead>
                      <TableHead>Nombre</TableHead>
                      <TableHead>Documento</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {candidatosResultados.map((candidato) => (
                      <TableRow key={candidato.nroCandidato}>
                        <TableCell className="font-mono">{candidato.nroCandidato}</TableCell>
                        <TableCell>{candidato.apellido}</TableCell>
                        <TableCell>{candidato.nombre}</TableCell>
                        <TableCell className="text-xs">
                          {candidato.tipoDocumento} {candidato.nroDocumento}
                        </TableCell>
                        <TableCell>
                          <Button
                            size="sm"
                            onClick={() => handleAddCandidato(candidato)}
                            disabled={candidatosSeleccionados.some(
                              (c) => c.nroCandidato === candidato.nroCandidato
                            )}
                          >
                            <Plus className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                    {candidatosResultados.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} className="text-center text-muted-foreground py-6">
                          No se encontraron candidatos
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Candidatos a Evaluar</CardTitle>
              <CardDescription>{candidatosSeleccionados.length} candidato(s) seleccionado(s)</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Nro</TableHead>
                    <TableHead>Apellido</TableHead>
                    <TableHead>Nombre</TableHead>
                    <TableHead>Doc</TableHead>
                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {candidatosSeleccionados.map((candidato) => (
                    <TableRow key={candidato.nroCandidato}>
                      <TableCell className="font-mono">{candidato.nroCandidato}</TableCell>
                      <TableCell>{candidato.apellido}</TableCell>
                      <TableCell>{candidato.nombre}</TableCell>
                      <TableCell className="text-xs">
                        {candidato.tipoDocumento} {candidato.nroDocumento}
                      </TableCell>
                      <TableCell>
                        <Button
                          size="sm"
                          variant="ghost"
                          className="text-destructive"
                          onClick={() => handleRemoveCandidato(candidato.nroCandidato)}
                        >
                          <Minus className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                  {candidatosSeleccionados.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={5} className="text-center text-muted-foreground py-8">
                        No hay candidatos seleccionados
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Step 2: Seleccionar Puesto (PantallaEvaluacionCandidatos) */}
      {step === "seleccionar-puesto" && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card>
            <CardHeader>
              <CardTitle>Seleccionar Puesto</CardTitle>
              <CardDescription>
                Seleccione el puesto o función para el cual se evaluará a los candidatos
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Select
                value={puestoSeleccionado?.id.toString() || ""}
                onValueChange={handleSelectPuesto}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione un puesto" />
                </SelectTrigger>
                <SelectContent>
                  {puestos.map((puesto) => (
                    <SelectItem key={puesto.id} value={puesto.id.toString()}>
                      {puesto.codigo} - {puesto.nombre} ({puesto.nombreEmpresa})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              {puestoSeleccionado && (
                <div className="p-4 bg-muted rounded-lg space-y-1">
                  <p className="font-medium">
                    {puestoSeleccionado.codigo} - {puestoSeleccionado.nombre}
                  </p>
                  <p className="text-sm text-muted-foreground">Empresa: {puestoSeleccionado.nombreEmpresa}</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Competencias Requeridas del Puesto</CardTitle>
              <CardDescription>
                Competencias y puntajes mínimos definidos para el puesto seleccionado
              </CardDescription>
            </CardHeader>
            <CardContent>
              {puestoSeleccionado && competenciasRequeridas.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Competencia</TableHead>
                      <TableHead className="text-right">Puntaje Mín. Requerido</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {competenciasRequeridas.map((comp) => (
                      <TableRow key={comp.idCompetencia}>
                        <TableCell>{comp.nombreCompetencia}</TableCell>
                        <TableCell className="text-right font-mono">
                          {comp.puntajeMinimoRequerido.toFixed(1)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <p className="text-center text-muted-foreground py-8">
                  Seleccione un puesto para validar y ver sus competencias requeridas
                </p>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* Step 3: Confirmar (listaCandidatoClaveDTO) */}
      {step === "confirmar" && (
        <Card>
          <CardHeader>
            <CardTitle>Confirmar Evaluación</CardTitle>
            <CardDescription>
              Lista de candidatos con sus claves de ingreso generadas. Al presionar Finalizar se guardarán los cuestionarios activos.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="mb-4 p-4 bg-muted rounded-lg">
              <p className="font-medium">
                Puesto: {puestoSeleccionado?.codigo} - {puestoSeleccionado?.nombre}
              </p>
              <p className="text-sm text-muted-foreground">Empresa: {puestoSeleccionado?.nombreEmpresa}</p>
            </div>

            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Apellido</TableHead>
                  <TableHead>Nombre</TableHead>
                  <TableHead>Tipo Doc</TableHead>
                  <TableHead>Nro Doc</TableHead>
                  <TableHead>Clave de Ingreso</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {candidatosConClaves.map((candidato, idx) => (
                  <TableRow key={`${candidato.nroDocumento}-${idx}`}>
                    <TableCell>{candidato.apellidoCandidato}</TableCell>
                    <TableCell>{candidato.nombreCandidato}</TableCell>
                    <TableCell>{candidato.tipoDocumento}</TableCell>
                    <TableCell>{candidato.nroDocumento}</TableCell>
                    <TableCell className="font-mono font-bold text-primary">{candidato.claveIngreso}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Navigation */}
      <div className="flex justify-between">
        <div>
          {step !== "buscar-candidatos" && (
            <Button variant="outline" onClick={handlePrevStep}>
              <ChevronLeft className="h-4 w-4 mr-2" />
              Anterior
            </Button>
          )}
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setCurrentScreen("dashboard")}>
            Cancelar
          </Button>
          {step !== "confirmar" ? (
            <Button
              onClick={handleNextStep}
              disabled={step === "buscar-candidatos" && candidatosSeleccionados.length === 0}
            >
              Siguiente
              <ChevronRight className="h-4 w-4 ml-2" />
            </Button>
          ) : (
            <Button onClick={handleFinalizar}>Finalizar</Button>
          )}
        </div>
      </div>

      {/* Flujo Alternativo 6.A: Candidatos con cuestionarios activos o en proceso */}
      <Dialog open={showErrorCandidatosActivos} onOpenChange={setShowErrorCandidatosActivos}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Candidatos con Cuestionarios Activos o En Proceso
            </DialogTitle>
            <DialogDescription>
              Los siguientes candidatos poseen cuestionarios en estado ACTIVO o EN_PROCESO y no pueden ser evaluados
              nuevamente:
              <ul className="list-disc pl-4 mt-2">
                {conflictosActivos.map((c) => (
                  <li key={c.idCuestionario}>
                    {c.apellido}, {c.nombre} (Nro {c.nroCandidato} - {c.estado})
                  </li>
                ))}
              </ul>
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setShowErrorCandidatosActivos(false)}>Aceptar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Flujo Alternativo 9.A: Competencias sin factores evaluables (finaliza el CU) */}
      <Dialog open={showErrorCompetencias} onOpenChange={setShowErrorCompetencias}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Competencias No Evaluables
            </DialogTitle>
            <DialogDescription>
              Las siguientes competencias del puesto NO poseen al menos un factor con 2 o más preguntas activas para
              ser evaluado:
              <ul className="list-disc pl-4 mt-2">
                {competenciasNoEvaluables.map((nombreComp, i) => (
                  <li key={i}>{nombreComp}</li>
                ))}
              </ul>
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              onClick={() => {
                setShowErrorCompetencias(false)
                setCurrentScreen("dashboard")
              }}
            >
              Aceptar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Flujo Alternativo 10.A: No seleccionó puesto */}
      <Dialog open={showErrorSinPuesto} onOpenChange={setShowErrorSinPuesto}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Selección Requerida
            </DialogTitle>
            <DialogDescription>Debe seleccionar un puesto para continuar.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setShowErrorSinPuesto(false)}>Aceptar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Paso 13 y 14: Éxito + ¿Desea exportar la lista de candidatos con sus claves a Excel? */}
      <Dialog open={showSuccess} onOpenChange={setShowSuccess}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Evaluación Creada
            </DialogTitle>
            <DialogDescription>
              Los cuestionarios han sido creados en estado ACTIVO para cada candidato. ¿Desea exportar la lista de
              candidatos con sus claves a un archivo Excel?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setShowSuccess(false)
                setCurrentScreen("dashboard")
              }}
            >
              NO
            </Button>
            <Button
              onClick={() => {
                setShowSuccess(false)
                setShowExportDialog(true)
              }}
            >
              <FileSpreadsheet className="h-4 w-4 mr-2" />
              SI
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Paso 15, 16 y 16.A: Diálogo Guardar Archivo Excel */}
      <Dialog open={showExportDialog} onOpenChange={setShowExportDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Guardar Archivo Excel</DialogTitle>
            <DialogDescription>
              Se descargará el archivo Excel con la lista de candidatos evaluados y sus claves de ingreso
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <div className="flex items-center gap-2 p-3 bg-muted rounded-lg">
              <FileSpreadsheet className="h-5 w-5 text-green-600" />
              <span className="text-sm font-mono">
                aaaa-mm-dd-hh-mm-{puestoSeleccionado?.nombre.replace(/ /g, "_")}.xls
              </span>
            </div>
            <p className="text-xs text-muted-foreground mt-2">Formato: aaaa-mm-dd-hh-mm-NombrePuesto.xls</p>
          </div>
          <DialogFooter>
            {/* Flujo Alternativo 16.A: Cancelar vuelve a preguntar "¿Desea exportar...?" */}
            <Button
              variant="outline"
              onClick={() => {
                setShowExportDialog(false)
                setShowSuccess(true)
              }}
            >
              Cancelar
            </Button>
            <Button onClick={handleDescargarExcel}>
              <Download className="h-4 w-4 mr-2" />
              Guardar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
