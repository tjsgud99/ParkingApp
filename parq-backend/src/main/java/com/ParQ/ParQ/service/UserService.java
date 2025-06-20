package com.ParQ.ParQ.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ParQ.ParQ.dto.UserLoginDto;
import com.ParQ.ParQ.dto.UserRegisterDto;
import com.ParQ.ParQ.dto.UserResponseDto;
import com.ParQ.ParQ.entity.User;
import com.ParQ.ParQ.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public String register(UserRegisterDto dto) {

		System.out.println("service : " + dto.getUsername());
		if (userRepository.existsByEmail(dto.getEmail())) {
			return "이미 존재하는 이메일입니다.";
		}
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		userRepository.save(user);

		return "회원가입 성공!";
	}

	public UserResponseDto login(UserLoginDto dto) {
		Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
		if (userOpt.isEmpty()) {
			return null; // 사용자가 없음
		}
		User user = userOpt.get();
		if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
			return null; // 비밀번호 불일치
		}
		// 로그인 성공 시 UserResponseDto 반환
		return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail());
	}
}
