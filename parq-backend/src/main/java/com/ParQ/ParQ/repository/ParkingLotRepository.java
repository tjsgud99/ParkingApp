package com.ParQ.ParQ.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ParQ.ParQ.entity.ParkingLot;
import java.util.List;


public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
	Optional<ParkingLot> findByName(String name);
	Optional<ParkingLot> findByAddress(String address);
}
