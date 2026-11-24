package com.canchas.reservas.infraestructure.driver_adapters.jpa_repository;

import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "reservas", indexes = {
        @Index(name = "idx_reservas_cancha_fecha_estado", columnList = "cancha_id, fecha, estado"),
        @Index(name = "idx_reservas_usuario", columnList = "usuario_id"),
        @Index(name = "idx_reservas_fecha", columnList = "fecha")
})
public class ReservaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long canchaId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Long precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Enumerated(EnumType.STRING)
    private MotivoCancelacion motivoCancelacion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}
