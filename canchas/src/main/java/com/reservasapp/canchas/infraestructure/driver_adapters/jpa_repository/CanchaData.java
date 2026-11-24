package com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "canchas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanchaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_deporte", nullable = false, length = 20)
    private TipoDeporte tipoDeporte;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(name = "precio_por_hora", nullable = false)
    private Long precioPorHora;

    @Column(nullable = false)
    private boolean techada;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCancha estado;
}
