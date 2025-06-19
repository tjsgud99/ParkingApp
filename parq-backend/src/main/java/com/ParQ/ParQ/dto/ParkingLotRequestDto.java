package com.ParQ.ParQ.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingLotRequestDto {
	private String name;
	private String address;
	private String runtime;

	@NotNull(message = "총 주차공간 수는 필수입니다.")
	private Integer total_space;
	private Integer fee;
}
