package com.gym.crm.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActiveStatusRequest(
        @JsonProperty("isActive") @NotNull boolean isActive
) {
}
