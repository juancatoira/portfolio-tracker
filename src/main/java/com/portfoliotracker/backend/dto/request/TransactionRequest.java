package com.portfoliotracker.backend.dto.request;

import com.portfoliotracker.backend.entity.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequest {

    @NotBlank(message = "El id de la moneda es obligatorio")
    private String coinId;

    @NotBlank(message = "El nombre de la moneda es obligatorio")
    private String coinName;

    @NotBlank(message = "El símbolo de la moneda es obligatorio")
    private String coinSymbol;

    @NotNull(message = "El tipo de transacción es obligatorio")
    private Transaction.TransactionType type;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.00000001", message = "La cantidad debe ser mayor que 0")
    private BigDecimal quantity;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private BigDecimal priceUsd;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDateTime date;

    private String notes;

    private BigDecimal feeUsd;

}