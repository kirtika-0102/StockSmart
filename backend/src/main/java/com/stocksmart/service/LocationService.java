package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreateLocationRequest;
import com.stocksmart.dto.LocationResponse;
import com.stocksmart.dto.UpdateLocationRequest;
import com.stocksmart.entity.Location;
import com.stocksmart.exception.ConflictException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.LocationRepository;

@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationResponse> list() {
        return locationRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public LocationResponse get(Long id) {
        return toResponse(findLocation(id));
    }

    @Transactional
    public LocationResponse create(CreateLocationRequest request) {
        String name = request.name().trim();
        if (locationRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Location name is already used");
        }
        Location location = new Location();
        location.setName(name);
        location.setType(request.type());
        location.setAddress(trimOrNull(request.address()));
        locationRepository.save(location);
        return toResponse(location);
    }

    @Transactional
    public LocationResponse update(Long id, UpdateLocationRequest request) {
        Location location = findLocation(id);
        String name = request.name().trim();
        if (locationRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Location name is already used");
        }
        location.setName(name);
        location.setType(request.type());
        location.setAddress(trimOrNull(request.address()));
        return toResponse(location);
    }

    @Transactional
    public void delete(Long id) {
        Location location = findLocation(id);
        locationRepository.delete(location);
    }

    private Location findLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getType(),
                location.getAddress());
    }
}
