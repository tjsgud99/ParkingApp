package com.ParQ.ParQ.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ParQ.ParQ.dto.FavoriteRequestDto;
import com.ParQ.ParQ.entity.Favorite;
import com.ParQ.ParQ.entity.ParkingLot;
import com.ParQ.ParQ.entity.User;
import com.ParQ.ParQ.repository.FavoriteRepository;
import com.ParQ.ParQ.repository.ParkingLotRepository;
import com.ParQ.ParQ.repository.UserRepository;

@Service
public class FavoriteService {

	private final FavoriteRepository favoriteRepo;
	private final UserRepository userRepo;
	private final ParkingLotRepository parkingRepo;
	
	
	
	public FavoriteService(FavoriteRepository favoriteRepo, UserRepository userRepo, ParkingLotRepository parkingRepo) {
		this.favoriteRepo = favoriteRepo;
		this.userRepo = userRepo;
		this.parkingRepo = parkingRepo;
	}
	
	public String addFavorite(FavoriteRequestDto dto) {
		User user = userRepo.findById(dto.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
		ParkingLot parkingLot = parkingRepo.findById(dto.getParkingLotId())
				.orElseThrow(() ->new IllegalArgumentException("주차장 없음")); 
		
		boolean exists = favoriteRepo
				.findByUserIdAndParkingLotId(user.getId(),
				parkingLot.getId()).isPresent();
		
		if (exists) {
			return "이미 즐겨찾기에 추가된 주차장입니다.";
		}
		
		Favorite favorite = new Favorite(null, user, parkingLot);
		favoriteRepo.save(favorite);
				
		return "즐겨찾기 추가 완료";
	}
	
	public List<Favorite> getFavorites(long userId) {
		return favoriteRepo.findByUserId(userId);
		
	}
	
	public String removeFavorite(Long userId, Long parkingLotId) {
		Optional<Favorite> favoriteOpt = favoriteRepo.findByUserIdAndParkingLotId(
				userId, parkingLotId);
		if (userId == null || parkingLotId == null) {
			throw new IllegalArgumentException("userId와 parkingLotId는 null이 될 수 없습니다.");
		}
		
		if (favoriteOpt.isPresent()) {
			favoriteRepo.delete(favoriteOpt.get());
			return "즐겨찾기 삭제 완료!";
		} else {
			return "즐겨찾기가 존재하지 않습니다.";
		}
		
	}
}
