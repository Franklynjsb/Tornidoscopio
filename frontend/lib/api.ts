export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api"

export interface ApiErrorResponse {
  codigo?: string
  mensaje?: string
  detalles?: string[]
}

export class ApiError extends Error {
  status: number
  codigo?: string
  detalles?: string[]

  constructor(status: number, message: string, codigo?: string, detalles?: string[]) {
    super(message)
    this.status = status
    this.codigo = codigo
    this.detalles = detalles
  }
}

export async function apiFetch<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint.startsWith("/") ? endpoint : `/${endpoint}`}`
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers || {}),
    },
  })

  if (!response.ok) {
    let errorBody: ApiErrorResponse | null = null
    try {
      errorBody = await response.json()
    } catch {
      // ignore if body is not json
    }
    const mensaje = errorBody?.mensaje || `Error HTTP ${response.status}`
    throw new ApiError(response.status, mensaje, errorBody?.codigo, errorBody?.detalles)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) {
    return undefined as T
  }

  return JSON.parse(text) as T
}
