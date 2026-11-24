package com.reservasapp.canchas.infraestructure.driver_adapters.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NagerHoliday(String date, String localName) {
}
