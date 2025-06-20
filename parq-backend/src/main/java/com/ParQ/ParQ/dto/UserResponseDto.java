package com.ParQ.ParQ.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
	private Long id;
	private String username;
	private String email;
}
