package com.rapidmotos.rapidmotos.Users;

import com.rapidmotos.rapidmotos.Users.Dto.Request.AuthLoginRequest;
import com.rapidmotos.rapidmotos.Users.Dto.Response.AuthResponse;
import com.rapidmotos.rapidmotos.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    //@Autowired
    //private CloudinaryServiceImpl cloudinaryService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Buscar usuario en la base de datos
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username+" not found"));

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        //Devolver Usuario que entiende spring
        return new User(user.getUsername(),
                user.getPassword(),
                grantedAuthorities);
    }

    public AuthResponse loginUser(AuthLoginRequest authLoginRequest) {
        String username = authLoginRequest.username();
        String password = authLoginRequest.password();

        Authentication authentication = this.authentication(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        String accessToken = jwtUtils.createToken(authentication);

        return new AuthResponse("success", "User logged succes", username, accessToken);
    }

    public Authentication authentication(String username, String password) {
        //Buscar el usuario en base de datos
        UserDetails userDetails = loadUserByUsername(username);

        if(userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        //Comprobar contraseña
        if(!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        //Si las credenciales son correctas
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }
}
