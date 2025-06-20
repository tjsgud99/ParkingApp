package com.ParQ.ParQ.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.ParQ.ParQ.dto.UserLoginDto;
import com.ParQ.ParQ.dto.UserRegisterDto;
import com.ParQ.ParQ.dto.UserResponseDto;
import com.ParQ.ParQ.service.UserService;
import com.ParQ.ParQ.repository.UserRepository;
import com.ParQ.ParQ.entity.User;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

	private final UserService userService;
	private final UserRepository userRepository;

	public UserController(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterDto dto) {
		System.out.println("컨트롤러 진입");
		String result = userService.register(dto);
		return ResponseEntity.ok(new UserResponseDto(true, result));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserLoginDto dto) {
		UserResponseDto userResponse = userService.login(dto);
		if (userResponse != null) {
			return ResponseEntity.ok(userResponse);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 올바르지 않습니다.");
		}
	}
}
