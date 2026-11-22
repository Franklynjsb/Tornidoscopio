package com.tornidoscopio.tp.dominio;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "opcion_respuesta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion_resp")
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo;

    // OpcionRespuesta "1" *-- "2..*" ItemOpcion : contiene >
    @OneToMany(mappedBy = "opcionRespuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOpcion> items;
}
