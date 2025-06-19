package com.ParQ.ParQ.controller;

import java.util.List;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ParQ.ParQ.dto.ParkingLotRequestDto;
import com.ParQ.ParQ.entity.ParkingLot;
import com.ParQ.ParQ.service.ParkingLotService;

@RestController
@RequestMapping("/api/parkinglots")
public class ParkingLotController {
	
	private final ParkingLotService parkingLotService;
	
	public ParkingLotController(ParkingLotService parkingLotService) {
		this.parkingLotService = parkingLotService;
		
	}
	
	@PostMapping
	public ResponseEntity<String> registerParkingLot(
			@RequestBody ParkingLotRequestDto dto) {
		String result = parkingLotService.registerParkingLot(dto);
		return ResponseEntity.ok(result);
		
	}
	
	@GetMapping
	public ResponseEntity<List<ParkingLot>> getAllParkingLots(){
		return ResponseEntity.ok(parkingLotService.getAllParkingLots());
	}
	
	@GetMapping("/{name}")
	public ResponseEntity<ParkingLot> getParkingLotByName(
			@PathVariable String name) {
		ParkingLot lot = parkingLotService.getParkingLotByName(name);
		return ResponseEntity.ok(lot);
	}
}
