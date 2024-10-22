package com.rapidmotos.rapidmotos.Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("key") //${jwt_key_private}
    private String privateKey;

    @Value("hola") //${jwt_user_generator}
    private String userGenerator;

    public String createToken(Authentication authentication) {

        String username = authentication.getName();

        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority) //Lo miso que grantedAuthority -> grantedAuthority.getAuthority()
                .collect(Collectors.joining(","));

        //Generando JWT
        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(username) //a quien se le genera el token
                .withClaim("authorities",authorities)
                .withIssuedAt(new Date()) //Fecha en la que se genera el token
                .withNotBefore(new Date(System.currentTimeMillis())) //A partir de cuando va a ser valido el token
                .sign(Algorithm.HMAC256(privateKey));
    }

    public DecodedJWT verifyToken(String token) {

        try{
            Algorithm algorithm = Algorithm.HMAC256(privateKey);

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();

            //Obtener jwt decodificado
            return verifier.verify(token);

        }catch (JWTVerificationException e){
            throw new JWTVerificationException("Token invalid, not authorized");
        }
    }

    public String extractUser(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject().toString();
    }

    public Claim getClaim(DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }

    public Map<String, Claim> returnAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }
}
