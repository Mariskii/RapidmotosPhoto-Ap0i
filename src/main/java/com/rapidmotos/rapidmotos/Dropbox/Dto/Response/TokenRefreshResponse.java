package com.rapidmotos.rapidmotos.Dropbox.Dto.Response;

import lombok.Builder;

@Builder
public record TokenRefreshResponse(
        String access_token,
        String token_type
) {
}
