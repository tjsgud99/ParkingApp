package com.ParQ.ParQ.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.DeleteExchange;

import com.ParQ.ParQ.dto.FavoriteRequestDto;
import com.ParQ.ParQ.entity.Favorite;
import com.ParQ.ParQ.service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
	
	private final FavoriteService favoriteService;
	
	public FavoriteController(FavoriteService favoriteService) {
		this.favoriteService = favoriteService;
	}
	
	@PostMapping
	public ResponseEntity<String> addFavorite(@RequestBody FavoriteRequestDto dto){
		String result = favoriteService.addFavorite(dto);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/{userId}")
	public ResponseEntity<List<Favorite>> getFavorites(@PathVariable Long userId) {
		return ResponseEntity.ok(favoriteService.getFavorites(userId));
	}
	
	@DeleteMapping
	public ResponseEntity<String> deleteFavorite(
			@RequestBody FavoriteRequestDto dto) {
		String result = favoriteService.removeFavorite(dto.getUserId(),
				dto.getParkingLotId());
		return ResponseEntity.ok(result);
		
	}
}
