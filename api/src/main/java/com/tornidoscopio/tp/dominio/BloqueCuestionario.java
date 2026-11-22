package com.tornidoscopio.tp.dominio;

import com.tornidoscopio.tp.dto.RespuestaCandidatoDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "bloque_cuestionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloque")
    private Long id;

    @Column(name = "nro_bloque", nullable = false)
    private Integer nroBloque;

    @Column(nullable = false)
    private Boolean completado;

    // Cuestionario "1" *-- "1..*" BloqueCuestionario : estructurado en >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false)
    private Cuestionario cuestionario;

    // BloqueCuestionario "1" *-- "1..*" RespuestaCandidato : contiene >
    @OneToMany(mappedBy = "bloqueCuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespuestaCandidato> respuestas;

    public Boolean validarRespuestasCompletas(List<RespuestaCandidatoDTO> r) {
        return null;
    }

    public void registrarRespuestas(List<RespuestaCandidatoDTO> r) {
    }

    public void marcarCompletado() {
    }
}
