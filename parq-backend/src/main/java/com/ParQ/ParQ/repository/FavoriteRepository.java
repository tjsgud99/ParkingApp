package com.ParQ.ParQ.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ParQ.ParQ.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>{
	List<Favorite> findByUserId(long userID);
	
	Optional<Favorite> findByUserIdAndParkingLotId(Long userId, Long parkingLotId);
	
}
