"use client"

import { useState } from "react"
import { useWireframe } from "@/lib/wireframe-context"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { AlertCircle, User, ClipboardList } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"

// Alineado con enum TipoDocumento en DiagramaDeClases.puml
export type TipoDocumento = "DNI" | "LC" | "LE" | "PP"

export function AutenticarUsuarioScreen() {
  const { setCurrentScreen, setUserType, setClaveIngresoCandidato } = useWireframe()
  const [activeTab, setActiveTab] = useState("consultor")

  // GestorAutenticacion.loginConsultor(u: String, p: String): Boolean
  const [usuario, setUsuario] = useState("")
  const [password, setPassword] = useState("")

  // GestorAutenticacion.loginCandidato(t: TipoDocumento, d: String, c: String): Boolean
  const [tipoDocumento, setTipoDocumento] = useState<TipoDocumento | "">("")
  const [nroDocumento, setNroDocumento] = useState("")
  const [claveIngreso, setClaveIngreso] = useState("")

  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const handleConsultorLogin = () => {
    setErrorMessage(null)
    if (!usuario.trim() || !password.trim()) {
      setErrorMessage("Debe ingresar su usuario y contraseña")
      return
    }
    setUserType("consultor")
    setCurrentScreen("dashboard")
  }

  const handleCandidatoLogin = () => {
    setErrorMessage(null)
    if (!claveIngreso.trim()) {
      setErrorMessage("Debe ingresar la Clave de Ingreso de 8 caracteres para acceder al cuestionario")
      return
    }
    setClaveIngresoCandidato(claveIngreso.trim().toUpperCase())
    setUserType("candidato")
    setCurrentScreen("cu26-completar-cuestionario")
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-muted to-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-2">
          <div className="mx-auto w-16 h-16 bg-primary rounded-xl flex items-center justify-center mb-2">
            <span className="text-2xl font-bold text-primary-foreground">CH</span>
          </div>
          <CardTitle className="text-2xl">Capit@l Humano</CardTitle>
          <CardDescription>
            Sistema de Gestión de Evaluaciones de Perfiles
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs
            value={activeTab}
            onValueChange={(v) => {
              setActiveTab(v)
              setErrorMessage(null)
            }}
            className="w-full"
          >
            <TabsList className="grid w-full grid-cols-2 mb-6">
              <TabsTrigger value="consultor" className="flex items-center gap-2">
                <User className="h-4 w-4" />
                Usuario Consultor
              </TabsTrigger>
              <TabsTrigger value="candidato" className="flex items-center gap-2">
                <ClipboardList className="h-4 w-4" />
                Realizar Cuestionario
              </TabsTrigger>
            </TabsList>

            <TabsContent value="consultor" className="space-y-4">
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  handleConsultorLogin()
                }}
                className="space-y-4"
              >
                <div className="space-y-2">
                  <Label htmlFor="usuario">Usuario *</Label>
                  <Input
                    id="usuario"
                    placeholder="Ingrese su usuario"
                    value={usuario}
                    onChange={(e) => setUsuario(e.target.value.toUpperCase())}
                    maxLength={15}
                    className="uppercase"
                  />
                  <p className="text-xs text-muted-foreground">Máximo 15 caracteres</p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">Contraseña *</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="********"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    maxLength={20}
                  />
                  <p className="text-xs text-muted-foreground">Máximo 20 caracteres</p>
                </div>

                {errorMessage && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{errorMessage}</AlertDescription>
                  </Alert>
                )}

                <div className="flex gap-2 pt-2">
                  <Button type="submit" className="w-full">
                    Ingresar como Consultor
                  </Button>
                </div>
              </form>
            </TabsContent>

            <TabsContent value="candidato" className="space-y-4">
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  handleCandidatoLogin()
                }}
                className="space-y-4"
              >
                <div className="space-y-2">
                  <Label htmlFor="tipo-doc">Tipo de Documento</Label>
                  <Select value={tipoDocumento} onValueChange={(v) => setTipoDocumento(v as TipoDocumento)}>
                    <SelectTrigger id="tipo-doc">
                      <SelectValue placeholder="Seleccione tipo" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="DNI">DNI</SelectItem>
                      <SelectItem value="LE">LE</SelectItem>
                      <SelectItem value="LC">LC</SelectItem>
                      <SelectItem value="PP">PP (Pasaporte)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="nro-doc">Número de Documento</Label>
                  <Input
                    id="nro-doc"
                    placeholder="Ej: 40123456"
                    value={nroDocumento}
                    onChange={(e) => setNroDocumento(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="clave">Clave de Ingreso *</Label>
                  <Input
                    id="clave"
                    placeholder="Ej: A1B2C3D4"
                    value={claveIngreso}
                    onChange={(e) => setClaveIngreso(e.target.value.toUpperCase())}
                    maxLength={8}
                    className="uppercase font-mono"
                  />
                  <p className="text-xs text-muted-foreground">
                    Ingrese la clave de 8 caracteres asignada para su evaluación
                  </p>
                </div>

                {errorMessage && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{errorMessage}</AlertDescription>
                  </Alert>
                )}

                <div className="flex gap-2 pt-2">
                  <Button type="submit" className="w-full">
                    Ingresar al Cuestionario
                  </Button>
                </div>
              </form>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}
