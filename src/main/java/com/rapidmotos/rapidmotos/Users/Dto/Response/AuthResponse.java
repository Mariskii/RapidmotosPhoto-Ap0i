package com.rapidmotos.rapidmotos.Users.Dto.Response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"message", "accessToken"})
public record AuthResponse(String status,
                           String message,
                           String accessToken) {
}
