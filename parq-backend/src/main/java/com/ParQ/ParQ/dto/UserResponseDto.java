package com.ParQ.ParQ.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
	private boolean success;
	private String message;
	private Long id;
	private String username;
	private String email;
}
