package com.rapidmotos.rapidmotos.Users;

import com.rapidmotos.rapidmotos.Users.Dto.Request.AuthLoginRequest;
import com.rapidmotos.rapidmotos.Users.Dto.Response.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    UserDetailServiceImpl userDetailService;

    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthLoginRequest userRequest) {
        return  new ResponseEntity<>(userDetailService.loginUser(userRequest), HttpStatus.OK);
    }
}
