package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_opcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOpcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_opcion")
    private Long id;

    @Column(name = "orden_visualizacion", nullable = false)
    private Integer ordenVisualizacion;

    @Column(nullable = false)
    private String descripcion;

    // OpcionRespuesta "1" *-- "2..*" ItemOpcion : contiene >
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_resp", nullable = false)
    private OpcionRespuesta opcionRespuesta;
}
