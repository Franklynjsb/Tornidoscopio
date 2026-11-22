package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resultado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado")
    private Long id;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    // Cuestionario "1" *-- "0..1" Resultado : tiene asociado >
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false, unique = true)
    private Cuestionario cuestionario;

    // Resultado "1" *-- "1..*" ResultadoCompetencia : detalla >
    @OneToMany(mappedBy = "resultado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoCompetencia> resultadosCompetencia;

    public void agregarResultadoCompetencia(ResultadoCompetencia rc) {
    }
}
