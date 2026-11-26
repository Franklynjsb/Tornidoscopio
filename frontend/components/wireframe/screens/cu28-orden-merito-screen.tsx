"use client"

import { useState } from "react"
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
import { Search, Printer, AlertCircle, Medal, Clock, XCircle, AlertTriangle, CheckCircle2 } from "lucide-react"
import { Badge } from "@/components/ui/badge"

// Alineado con diagramaSecuenciaCU028.puml y DiagramaDeClases.puml:
// - PuestoDTO: id, codigo, nombre, descripcion, idEmpresa, nombreEmpresa, cantCandidatos, cantEvaluacionesCompletas
// - OrdenMeritoDTO:
//     nombreCandidato: String
//     apellidoCandidato: String
//     tipoDocumento: TipoDocumento
//     nroDocumento: String
//     puntajeTotalObtenido: Double | null
//     estadoCuestionario: EstadoCuestionario
//     fechaInicio: LocalDateTime | null
//     fechaFinOUltimoIngreso: LocalDateTime | null
//     cantAccesos: Integer
//     cumpleMinimos: Boolean
export type EstadoCuestionario = "ACTIVO" | "EN_PROCESO" | "COMPLETO" | "INCOMPLETO" | "SIN_CONTESTAR"

export interface PuestoMeritoDTO {
  id: number
  codigo: string
  nombre: string
  descripcion: string
  nombreEmpresa: string
  cantCandidatos: number
  cantEvaluacionesCompletas: number
}

export interface OrdenMeritoDTO {
  nombreCandidato: string
  apellidoCandidato: string
  tipoDocumento: "DNI" | "LC" | "LE" | "PP"
  nroDocumento: string
  puntajeTotalObtenido: number | null
  estadoCuestionario: EstadoCuestionario
  fechaInicio: string | null
  fechaFinOUltimoIngreso: string | null
  cantAccesos: number
  cumpleMinimos?: boolean
}

export function OrdenMeritoScreen() {
  const { setCurrentScreen } = useWireframe()
  const [searchCodigo, setSearchCodigo] = useState("")
  const [searchNombre, setSearchNombre] = useState("")
  const [searchEmpresa, setSearchEmpresa] = useState("")
  const [hasSearched, setHasSearched] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const [puestos, setPuestos] = useState<PuestoMeritoDTO[]>([])
  const [puestoSeleccionado, setPuestoSeleccionado] = useState<PuestoMeritoDTO | null>(null)
  const [fechasEvaluaciones, setFechasEvaluaciones] = useState<string[]>([])
  const [fechaSeleccionada, setFechaSeleccionada] = useState("")
  const [listadoOrdenMerito, setListadoOrdenMerito] = useState<OrdenMeritoDTO[]>([])

  const [showReporte, setShowReporte] = useState(false)
  const [showErrorSinEvaluaciones, setShowErrorSinEvaluaciones] = useState(false)
  const [showPrintSuccess, setShowPrintSuccess] = useState(false)

  // CU028 Paso 3 y 4: GestorPuestos.buscarPuestos(criterios)
  const handleSearch = async () => {
    setErrorMessage(null)
    setIsLoading(true)
    try {
      const termino = [searchCodigo.trim(), searchNombre.trim(), searchEmpresa.trim()]
        .filter(Boolean)
        .join(" ")

      const lista = await apiFetch<PuestoMeritoDTO[]>(
        `/puestos?criterios=${encodeURIComponent(termino)}`
      )

      const filtrados = lista.filter((p) => {
        const matchCod = !searchCodigo.trim() || p.codigo.toUpperCase().includes(searchCodigo.trim().toUpperCase())
        const matchNom = !searchNombre.trim() || p.nombre.toUpperCase().includes(searchNombre.trim().toUpperCase())
        const matchEmp = !searchEmpresa.trim() || (p.nombreEmpresa || "").toUpperCase().includes(searchEmpresa.trim().toUpperCase())
        return matchCod && matchNom && matchEmp
      })

      setPuestos(filtrados)
      setHasSearched(true)
    } catch {
      setErrorMessage("No se pudo conectar con el servidor backend (puerto 8080)")
    } finally {
      setIsLoading(false)
    }
  }

  // Paso 5 y 6.A de diagramaSecuenciaCU028.puml:
  // obtenerFechasEvaluaciones(idPuesto) -> si listaFechas está vacía muestra "El puesto no posee evaluaciones"
  const handleEmitir = async (puesto: PuestoMeritoDTO) => {
    setErrorMessage(null)
    try {
      const fechas = await apiFetch<string[]>(`/reportes/puestos/${puesto.id}/fechas`)
      if (!fechas || fechas.length === 0) {
        setShowErrorSinEvaluaciones(true)
        return
      }
      setFechasEvaluaciones(fechas)
      setPuestoSeleccionado(puesto)
      setFechaSeleccionada("")
    } catch (err) {
      setErrorMessage(err instanceof ApiError ? err.message : "Error al obtener las fechas de evaluación del puesto")
    }
  }

  // Paso 7: GestorReportes.generarOrdenMerito(idPuesto, fechas)
  const handleGenerarReporte = async () => {
    if (!puestoSeleccionado || !fechaSeleccionada) return
    setErrorMessage(null)
    try {
      const fechasAEnviar = fechaSeleccionada === "TODAS" ? fechasEvaluaciones : [fechaSeleccionada]
      const listado = await apiFetch<OrdenMeritoDTO[]>(`/reportes/puestos/${puestoSeleccionado.id}/orden-merito`, {
        method: "POST",
        body: JSON.stringify({ fechas: fechasAEnviar }),
      })
      setListadoOrdenMerito(listado)
      setShowReporte(true)
    } catch (err) {
      setErrorMessage(err instanceof ApiError ? err.message : "Error al generar el orden de mérito")
    }
  }

  // Paso 8, 9, 10: presionarImprimir() -> mostrarMensaje("Listado enviado a la impresora") y termina el CU
  const handleImprimir = () => {
    window.print()
    setShowPrintSuccess(true)
  }

  const formatearFecha = (iso: string | null) => {
    if (!iso) return "-"
    return iso.replace("T", " ").substring(0, 19)
  }

  const cumplenMinimos = listadoOrdenMerito.filter(
    (d) => d.estadoCuestionario === "COMPLETO" && d.cumpleMinimos
  )
  const noCumplenMinimos = listadoOrdenMerito.filter(
    (d) => d.estadoCuestionario === "COMPLETO" && !d.cumpleMinimos
  )
  const incompletos = listadoOrdenMerito.filter((d) => d.estadoCuestionario === "INCOMPLETO")
  const sinContestar = listadoOrdenMerito.filter((d) => d.estadoCuestionario === "SIN_CONTESTAR")
  const otrosEstados = listadoOrdenMerito.filter(
    (d) => d.estadoCuestionario === "ACTIVO" || d.estadoCuestionario === "EN_PROCESO"
  )

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Emitir Orden de Mérito</h1>
        <p className="text-muted-foreground">
          Busque un puesto, seleccione las fechas de evaluación y emita el listado de orden de mérito
        </p>
      </div>

      {errorMessage && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{errorMessage}</AlertDescription>
        </Alert>
      )}

      {!showReporte ? (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Búsqueda de Puestos</CardTitle>
              <CardDescription>
                Busque puestos activos por código, nombre o empresa
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  handleSearch()
                }}
              >
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="space-y-2">
                    <Label>Código</Label>
                    <Input
                      placeholder="Ej: PUE-01"
                      value={searchCodigo}
                      onChange={(e) => setSearchCodigo(e.target.value.toUpperCase())}
                      className="uppercase"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Nombre del Puesto</Label>
                    <Input
                      placeholder="Ej: LÍDER"
                      value={searchNombre}
                      onChange={(e) => setSearchNombre(e.target.value.toUpperCase())}
                      className="uppercase"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Empresa</Label>
                    <Input
                      placeholder="Ej: TECHCORP"
                      value={searchEmpresa}
                      onChange={(e) => setSearchEmpresa(e.target.value.toUpperCase())}
                      className="uppercase"
                    />
                  </div>
                </div>
                <div className="flex gap-2 mt-4">
                  <Button type="submit" disabled={isLoading}>
                    <Search className="h-4 w-4 mr-2" />
                    {isLoading ? "Buscando..." : "Buscar"}
                  </Button>
                  <Button type="button" variant="outline" onClick={() => setCurrentScreen("dashboard")}>
                    Cancelar
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          {hasSearched && (
            <Card>
              <CardHeader>
                <CardTitle>Resultados de Búsqueda</CardTitle>
                <CardDescription>Seleccione un puesto para emitir el orden de mérito</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Código</TableHead>
                      <TableHead>Nombre del Puesto</TableHead>
                      <TableHead>Empresa</TableHead>
                      <TableHead className="text-center">Cant. Candidatos</TableHead>
                      <TableHead className="text-center">Evaluaciones Completas</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {puestos.map((puesto) => (
                      <TableRow key={puesto.id}>
                        <TableCell className="font-mono">{puesto.codigo}</TableCell>
                        <TableCell>{puesto.nombre}</TableCell>
                        <TableCell>{puesto.nombreEmpresa}</TableCell>
                        <TableCell className="text-center">{puesto.cantCandidatos}</TableCell>
                        <TableCell className="text-center">{puesto.cantEvaluacionesCompletas}</TableCell>
                        <TableCell className="text-right">
                          <Button size="sm" onClick={() => handleEmitir(puesto)}>
                            Emitir el orden de mérito
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                    {puestos.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center text-muted-foreground py-8">
                          No se encontraron puestos con los criterios ingresados
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}

          {puestoSeleccionado && (
            <Dialog open={!!puestoSeleccionado} onOpenChange={() => setPuestoSeleccionado(null)}>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Seleccionar Fecha de Evaluación</DialogTitle>
                  <DialogDescription>
                    Seleccione una fecha de evaluación o todas las evaluaciones para{" "}
                    <strong>{puestoSeleccionado.nombre}</strong>
                  </DialogDescription>
                </DialogHeader>
                <div className="py-4">
                  <Select value={fechaSeleccionada} onValueChange={setFechaSeleccionada}>
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccione fecha o TODAS" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="TODAS">TODAS las evaluaciones</SelectItem>
                      {fechasEvaluaciones.map((fecha) => (
                        <SelectItem key={fecha} value={fecha}>
                          Fecha creación: {formatearFecha(fecha)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <DialogFooter>
                  <Button variant="outline" onClick={() => setPuestoSeleccionado(null)}>
                    Cancelar
                  </Button>
                  <Button onClick={handleGenerarReporte} disabled={!fechaSeleccionada}>
                    Generar Orden de Mérito
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          )}
        </>
      ) : (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Medal className="h-5 w-5 text-primary" />
                  Orden de Mérito - {puestoSeleccionado?.nombre}
                </CardTitle>
                <CardDescription>
                  {puestoSeleccionado?.nombreEmpresa} | Fecha Evaluación:{" "}
                  {fechaSeleccionada === "TODAS" ? "TODAS" : formatearFecha(fechaSeleccionada)}
                </CardDescription>
              </div>
              <Button variant="outline" onClick={handleImprimir}>
                <Printer className="h-4 w-4 mr-2" />
                Imprimir
              </Button>
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* 1. Cumplen mínimos: ordenados por puntajeTotalObtenido */}
            <div>
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <Medal className="h-4 w-4 text-green-600" />
                1. Candidatos con Cuestionario COMPLETO que Cumplen Puntajes Mínimos ({cumplenMinimos.length})
              </h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-16">Orden</TableHead>
                    <TableHead>Tipo Doc</TableHead>
                    <TableHead>Nro Doc</TableHead>
                    <TableHead>Apellido</TableHead>
                    <TableHead>Nombre</TableHead>
                    <TableHead className="text-right">Puntaje Total Obtenido</TableHead>
                    <TableHead>Fecha Inicio / Fin / Accesos</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {cumplenMinimos.map((dto, index) => (
                    <TableRow key={`${dto.nroDocumento}-${index}`}>
                      <TableCell>
                        <Badge variant={index === 0 ? "default" : "secondary"} className="w-8 justify-center">
                          {index + 1}
                        </Badge>
                      </TableCell>
                      <TableCell>{dto.tipoDocumento}</TableCell>
                      <TableCell className="font-mono">{dto.nroDocumento}</TableCell>
                      <TableCell>{dto.apellidoCandidato}</TableCell>
                      <TableCell>{dto.nombreCandidato}</TableCell>
                      <TableCell className="text-right font-mono font-bold text-green-600">
                        {dto.puntajeTotalObtenido?.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        Inicio: {formatearFecha(dto.fechaInicio)}
                        <br />
                        Fin: {formatearFecha(dto.fechaFinOUltimoIngreso)}
                        <br />
                        Accesos: {dto.cantAccesos}
                      </TableCell>
                    </TableRow>
                  ))}
                  {cumplenMinimos.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center text-muted-foreground py-4">
                        Ningún candidato en esta categoría
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            {/* 2. No alcanzan mínimos */}
            <div>
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <AlertTriangle className="h-4 w-4 text-amber-500" />
                2. Candidatos con Cuestionario COMPLETO que No Alcanzan Puntajes Mínimos ({noCumplenMinimos.length})
              </h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tipo Doc</TableHead>
                    <TableHead>Nro Doc</TableHead>
                    <TableHead>Apellido</TableHead>
                    <TableHead>Nombre</TableHead>
                    <TableHead className="text-right">Puntaje Total Obtenido</TableHead>
                    <TableHead>Fecha Inicio / Fin / Accesos</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {noCumplenMinimos.map((dto, index) => (
                    <TableRow key={`${dto.nroDocumento}-${index}`}>
                      <TableCell>{dto.tipoDocumento}</TableCell>
                      <TableCell className="font-mono">{dto.nroDocumento}</TableCell>
                      <TableCell>{dto.apellidoCandidato}</TableCell>
                      <TableCell>{dto.nombreCandidato}</TableCell>
                      <TableCell className="text-right font-mono text-amber-600">
                        {dto.puntajeTotalObtenido?.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        Inicio: {formatearFecha(dto.fechaInicio)}
                        <br />
                        Fin: {formatearFecha(dto.fechaFinOUltimoIngreso)}
                        <br />
                        Accesos: {dto.cantAccesos}
                      </TableCell>
                    </TableRow>
                  ))}
                  {noCumplenMinimos.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-muted-foreground py-4">
                        Ningún candidato en esta categoría
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            {/* 3. Cuestionarios INCOMPLETO */}
            <div>
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <Clock className="h-4 w-4 text-muted-foreground" />
                3. Cuestionarios en Estado INCOMPLETO ({incompletos.length})
              </h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tipo Doc</TableHead>
                    <TableHead>Nro Doc</TableHead>
                    <TableHead>Apellido</TableHead>
                    <TableHead>Nombre</TableHead>
                    <TableHead>Estado</TableHead>
                    <TableHead>Fecha Inicio / Último Ingreso / Accesos</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {incompletos.map((dto, index) => (
                    <TableRow key={`${dto.nroDocumento}-${index}`}>
                      <TableCell>{dto.tipoDocumento}</TableCell>
                      <TableCell className="font-mono">{dto.nroDocumento}</TableCell>
                      <TableCell>{dto.apellidoCandidato}</TableCell>
                      <TableCell>{dto.nombreCandidato}</TableCell>
                      <TableCell>
                        <Badge variant="secondary">{dto.estadoCuestionario}</Badge>
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        Inicio: {formatearFecha(dto.fechaInicio)}
                        <br />
                        Último ingreso: {formatearFecha(dto.fechaFinOUltimoIngreso)}
                        <br />
                        Accesos: {dto.cantAccesos}
                      </TableCell>
                    </TableRow>
                  ))}
                  {incompletos.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-muted-foreground py-4">
                        Ningún candidato en esta categoría
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            {/* 4. Cuestionarios SIN_CONTESTAR y otros estados */}
            <div>
              <h3 className="font-medium mb-3 flex items-center gap-2">
                <XCircle className="h-4 w-4 text-destructive" />
                4. Cuestionarios en Estado SIN_CONTESTAR / Otros ({sinContestar.length + otrosEstados.length})
              </h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tipo Doc</TableHead>
                    <TableHead>Nro Doc</TableHead>
                    <TableHead>Apellido</TableHead>
                    <TableHead>Nombre</TableHead>
                    <TableHead>Estado</TableHead>
                    <TableHead>Accesos</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {[...sinContestar, ...otrosEstados].map((dto, index) => (
                    <TableRow key={`${dto.nroDocumento}-${index}`}>
                      <TableCell>{dto.tipoDocumento}</TableCell>
                      <TableCell className="font-mono">{dto.nroDocumento}</TableCell>
                      <TableCell>{dto.apellidoCandidato}</TableCell>
                      <TableCell>{dto.nombreCandidato}</TableCell>
                      <TableCell>
                        <Badge variant="destructive">{dto.estadoCuestionario}</Badge>
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">{dto.cantAccesos}</TableCell>
                    </TableRow>
                  ))}
                  {sinContestar.length + otrosEstados.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-muted-foreground py-4">
                        Ningún candidato en esta categoría
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button
                variant="outline"
                onClick={() => {
                  setShowReporte(false)
                  setPuestoSeleccionado(null)
                  setFechaSeleccionada("")
                }}
              >
                Volver
              </Button>
              <Button onClick={() => setCurrentScreen("dashboard")}>Salir</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Flujo Alternativo 6.A: El puesto no posee evaluaciones */}
      <Dialog open={showErrorSinEvaluaciones} onOpenChange={setShowErrorSinEvaluaciones}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Sin Evaluaciones
            </DialogTitle>
            <DialogDescription>El puesto no posee evaluaciones.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setShowErrorSinEvaluaciones(false)}>Aceptar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Paso 10: Impresión enviada */}
      <Dialog open={showPrintSuccess} onOpenChange={setShowPrintSuccess}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              Impresión Enviada
            </DialogTitle>
            <DialogDescription>Listado enviado a la impresora.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              onClick={() => {
                setShowPrintSuccess(false)
                setCurrentScreen("dashboard")
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
