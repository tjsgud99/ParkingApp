package com.ParQ.ParQ.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		String result = userService.login(dto);
		if (result.contains("로그인 성공")) {
			User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
			if (user != null) {
				Map<String, String> response = new HashMap<>();
				response.put("username", user.getUsername());
				response.put("email", user.getEmail());
				return ResponseEntity.ok(response);
			}
		}
		Map<String, String> fail = new HashMap<>();
		fail.put("message", "로그인 실패");
		return ResponseEntity.status(401).body(fail);
	}
}
