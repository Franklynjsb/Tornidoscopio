"use client"

import { useEffect, useState } from "react"
import { useWireframe } from "@/lib/wireframe-context"
import { apiFetch, ApiError } from "@/lib/api"
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
import { Search, Plus, Pencil, Trash2, AlertTriangle, CheckCircle2, AlertCircle } from "lucide-react"

// Alineado con PreguntaDTO en DiagramaDeClases.puml y DiagramaSecuenciaCU020.puml
export interface PreguntaDTO {
  id: number
  nombre: string
  enunciado?: string
  descripcion?: string
  nombreCompetencia: string
  nombreFactor: string
  idFactor?: number
  idOpcionRespuesta?: number
}

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

export function GestionarPreguntasScreen() {
  const { setCurrentScreen } = useWireframe()
  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([])
  const [factores, setFactores] = useState<FactorDTO[]>([])
  const [preguntas, setPreguntas] = useState<PreguntaDTO[]>([])

  const [searchCompetencia, setSearchCompetencia] = useState("")
  const [searchFactor, setSearchFactor] = useState("")
  const [searchNombre, setSearchNombre] = useState("")
  const [hasSearched, setHasSearched] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [criteriosError, setCriteriosError] = useState<string | null>(null)
  const [selectedPregunta, setSelectedPregunta] = useState<PreguntaDTO | null>(null)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [showDeleteSuccess, setShowDeleteSuccess] = useState(false)

  // Carga las competencias activas desde el backend para el selector de filtro
  useEffect(() => {
    apiFetch<CompetenciaDTO[]>("/competencias")
      .then(setCompetencias)
      .catch(() => {
        // Si el backend aún no está levantado, mantiene la lista vacía sin romper la vista
      })
  }, [])

  // Cuando selecciona una competencia, carga sus factores desde el backend
  useEffect(() => {
    if (!searchCompetencia) {
      setFactores([])
      return
    }
    const compObj = competencias.find((c) => c.nombre === searchCompetencia)
    if (!compObj) {
      setFactores([])
      return
    }
    apiFetch<FactorDTO[]>(`/competencias/${compObj.id}/factores`)
      .then(setFactores)
      .catch(() => setFactores([]))
  }, [searchCompetencia, competencias])

  // DiagramaSecuenciaCU020: pantalla -> gestor.buscarPreguntas(competencia, factor, nombrePregunta)
  const handleSearch = async () => {
    setCriteriosError(null)
    setIsLoading(true)
    try {
      const params = new URLSearchParams()
      if (searchCompetencia.trim()) params.set("competencia", searchCompetencia.trim())
      if (searchFactor.trim()) params.set("factor", searchFactor.trim())
      if (searchNombre.trim()) params.set("nombrePregunta", searchNombre.trim())

      const resultado = await apiFetch<PreguntaDTO[]>(`/preguntas?${params.toString()}`)
      setPreguntas(resultado)
      setHasSearched(true)
      setSelectedPregunta(null)
    } catch (err) {
      if (err instanceof ApiError) {
        setCriteriosError(err.message)
      } else {
        setCriteriosError("No se pudo conectar con el servidor backend (verifique que esté corriendo en el puerto 8080)")
      }
      setHasSearched(false)
    } finally {
      setIsLoading(false)
    }
  }

  // Flujo Alternativo 5.B: CU023 - Dar de Baja Pregunta (idPregunta)
  const handleDelete = () => {
    if (selectedPregunta) {
      setPreguntas((prev) => prev.filter((p) => p.id !== selectedPregunta.id))
    }
    setShowDeleteConfirm(false)
    setShowDeleteSuccess(true)
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Gestionar Preguntas</h1>
          <p className="text-muted-foreground">
            Busque preguntas por competencia, factor o nombre para dar de alta, modificar o dar de baja
          </p>
        </div>
        <Button onClick={() => setCurrentScreen("cu21-alta-pregunta")}>
          <Plus className="h-4 w-4 mr-2" />
          Nueva Pregunta
        </Button>
      </div>

      {criteriosError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{criteriosError}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Criterios de Búsqueda</CardTitle>
          <CardDescription>
            Ingrese al menos un criterio para buscar preguntas
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label>Competencia</Label>
              <Select
                value={searchCompetencia}
                onValueChange={(v) => {
                  setSearchCompetencia(v === "NONE" ? "" : v)
                  setSearchFactor("")
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione competencia" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="NONE">-- Sin filtro --</SelectItem>
                  {competencias.map((comp) => (
                    <SelectItem key={comp.id} value={comp.nombre}>
                      {comp.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Factor</Label>
              <Select
                value={searchFactor}
                onValueChange={(v) => setSearchFactor(v === "NONE" ? "" : v)}
                disabled={!searchCompetencia}
              >
                <SelectTrigger>
                  <SelectValue placeholder={searchCompetencia ? "Seleccione factor" : "Seleccione primero competencia"} />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="NONE">-- Sin filtro --</SelectItem>
                  {factores.map((factor) => (
                    <SelectItem key={factor.id} value={factor.nombre}>
                      {factor.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre de Pregunta</Label>
              <Input
                id="nombre"
                placeholder="Buscar por nombre..."
                value={searchNombre}
                onChange={(e) => setSearchNombre(e.target.value.toUpperCase())}
                className="uppercase"
              />
            </div>
          </div>
          <div className="flex gap-2 mt-4">
            <Button onClick={handleSearch} disabled={isLoading}>
              <Search className="h-4 w-4 mr-2" />
              {isLoading ? "Buscando..." : "Buscar"}
            </Button>
            <Button
              variant="outline"
              onClick={() => {
                setSearchCompetencia("")
                setSearchFactor("")
                setSearchNombre("")
                setPreguntas([])
                setHasSearched(false)
                setCriteriosError(null)
                setSelectedPregunta(null)
              }}
            >
              Limpiar
            </Button>
          </div>
        </CardContent>
      </Card>

      {hasSearched && (
        <Card>
          <CardHeader>
            <CardTitle>Resultados de Búsqueda</CardTitle>
            <CardDescription>{preguntas.length} pregunta(s) encontrada(s)</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-12">Sel.</TableHead>
                  <TableHead>Competencia</TableHead>
                  <TableHead>Factor</TableHead>
                  <TableHead>Nombre de Pregunta</TableHead>
                  <TableHead className="text-right">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {preguntas.map((pregunta) => (
                  <TableRow
                    key={pregunta.id}
                    className={selectedPregunta?.id === pregunta.id ? "bg-muted" : ""}
                    onClick={() => setSelectedPregunta(pregunta)}
                  >
                    <TableCell>
                      <input
                        type="radio"
                        name="pregunta"
                        checked={selectedPregunta?.id === pregunta.id}
                        onChange={() => setSelectedPregunta(pregunta)}
                        className="h-4 w-4"
                      />
                    </TableCell>
                    <TableCell className="text-sm">{pregunta.nombreCompetencia}</TableCell>
                    <TableCell className="text-sm">{pregunta.nombreFactor}</TableCell>
                    <TableCell className="text-sm max-w-md">
                      <p className="font-medium">{pregunta.nombre}</p>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          title="CU22 - Modificar Pregunta"
                          disabled
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          title="CU23 - Dar de Baja Pregunta"
                          className="text-destructive hover:bg-destructive/10"
                          onClick={(e) => {
                            e.stopPropagation()
                            setSelectedPregunta(pregunta)
                            setShowDeleteConfirm(true)
                          }}
                          disabled={selectedPregunta?.id !== pregunta.id}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
                {preguntas.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center text-muted-foreground py-8">
                      No se encontraron preguntas con los criterios ingresados
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <div className="flex justify-end">
        <Button variant="outline" onClick={() => setCurrentScreen("dashboard")}>
          Salir
        </Button>
      </div>

      {/* CU23 - Dialog de confirmación de eliminación */}
      <Dialog open={showDeleteConfirm} onOpenChange={setShowDeleteConfirm}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-destructive" />
              Confirmar Baja de Pregunta
            </DialogTitle>
            <DialogDescription>
              Los datos de la pregunta <strong>{selectedPregunta?.nombre}</strong> serán dados de baja en el sistema.
              ¿Desea confirmar la eliminación?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDeleteConfirm(false)}>
              CANCELAR
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              ELIMINAR
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Dialog de éxito de eliminación + actualización de grilla (Paso 5.B.2) */}
      <Dialog open={showDeleteSuccess} onOpenChange={setShowDeleteSuccess}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Pregunta Eliminada
            </DialogTitle>
            <DialogDescription>
              Los datos de la pregunta <strong>{selectedPregunta?.nombre}</strong> han sido eliminados del sistema y la
              grilla fue actualizada.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              onClick={() => {
                setShowDeleteSuccess(false)
                setSelectedPregunta(null)
              }}
            >
              Aceptar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
