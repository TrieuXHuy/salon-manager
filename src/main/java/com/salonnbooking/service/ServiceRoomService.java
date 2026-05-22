package com.salonnbooking.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ServiceRoomRequests;
import com.salonnbooking.domain.ServiceRoom;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.ServiceRoomRepository;

@Service
@Transactional
public class ServiceRoomService {
	private final ServiceRoomRepository serviceRoomRepository;

	public ServiceRoomService(ServiceRoomRepository serviceRoomRepository) {
		this.serviceRoomRepository = serviceRoomRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceRoom> findAll() {
		return serviceRoomRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
	}

	@Transactional(readOnly = true)
	public List<ServiceRoom> findActive() {
		return serviceRoomRepository.findByIsActiveTrueOrderByIdAsc();
	}

	@Transactional(readOnly = true)
	public ServiceRoom findById(Integer id) {
		return serviceRoomRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Service room not found with id: " + id));
	}

	public ServiceRoom save(ServiceRoomRequests.Create req) {
		ServiceRoom room = new ServiceRoom();
		room.setName(req.name());
		room.setDescription(req.description());
		room.setIsActive(req.isActive() == null ? true : req.isActive());
		return serviceRoomRepository.save(room);
	}

	public ServiceRoom update(Integer id, ServiceRoomRequests.Update req) {
		ServiceRoom room = findById(id);
		room.setName(req.name());
		room.setDescription(req.description());
		room.setIsActive(req.isActive() == null ? true : req.isActive());
		return serviceRoomRepository.save(room);
	}

	public void delete(Integer id) {
		if (!serviceRoomRepository.existsById(id)) {
			throw new ResourceNotFoundException("Service room not found with id: " + id);
		}
		serviceRoomRepository.deleteById(id);
	}
}
