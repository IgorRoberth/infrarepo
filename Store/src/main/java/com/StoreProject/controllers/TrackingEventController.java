package com.StoreProject.controllers;

import com.StoreProject.tracking.*;
import com.StoreProject.enums.OrderStatus;

import com.StoreProject.model.TrackingEvent;
import com.StoreProject.services.TrackingEventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tracking-events")
public class TrackingEventController {

    @Autowired
    private TrackingEventService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TrackingEventRequestDTO eventDTO) {
        try {
            TrackingEvent event = service.create(eventDTO);
            TrackingEventResponseDTO responseDTO = convertToResponseDTO(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<List<TrackingEventResponseDTO>> getByTracking(@PathVariable Long trackingId) {
        List<TrackingEvent> events = service.findByTracking(trackingId);
        List<TrackingEventResponseDTO> responseDTOs = events.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<List<TrackingEventResponseDTO>> getByCodigoRastreamento(@PathVariable String codigo) {
        List<TrackingEvent> events = service.findByCodigoRastreamento(codigo);
        List<TrackingEventResponseDTO> responseDTOs = events.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            TrackingEvent event = service.findById(id);
            TrackingEventResponseDTO responseDTO = convertToResponseDTO(event);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TrackingEventResponseDTO>> getByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataEvento").descending());
        Page<TrackingEvent> events = service.findByStatus(status, pageable);
        Page<TrackingEventResponseDTO> responseDTOs = events.map(this::convertToResponseDTO);

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/tracking/{trackingId}/ultimo")
    public ResponseEntity<?> getLastEventByTracking(@PathVariable Long trackingId) {
        try {
            TrackingEvent event = service.findLastEventByTracking(trackingId);
            TrackingEventResponseDTO responseDTO = convertToResponseDTO(event);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<TrackingEventResponseDTO>> getByPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {

        List<TrackingEvent> events = service.findByPeriodo(inicio, fim);
        List<TrackingEventResponseDTO> responseDTOs = events.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/localizacao/{localizacao}")
    public ResponseEntity<List<TrackingEventResponseDTO>> getByLocalizacao(@PathVariable String localizacao) {
        List<TrackingEvent> events = service.findByLocalizacao(localizacao);
        List<TrackingEventResponseDTO> responseDTOs = events.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody TrackingEventUpdateDTO updateDTO) {
        try {
            TrackingEvent updatedEvent = service.update(id, updateDTO);
            TrackingEventResponseDTO responseDTO = convertToResponseDTO(updatedEvent);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("mensagem", "Evento excluído com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TrackingEventResponseDTO>> getRecentEvents() {
        List<TrackingEvent> events = service.findRecentEvents();
        List<TrackingEventResponseDTO> responseDTOs = events.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TrackingEventResponseDTO>> getBySeller(@PathVariable Long sellerId) {
        List<TrackingEvent> events = service.findBySeller(sellerId);
        List<TrackingEventResponseDTO> responseDTOs = events.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    private TrackingEventResponseDTO convertToResponseDTO(TrackingEvent event) {
        TrackingEventResponseDTO dto = new TrackingEventResponseDTO();
        dto.setId(event.getId());
        dto.setDataEvento(event.getDataEvento());
        dto.setDescricao(event.getDescricao());
        dto.setLocalizacao(event.getLocalizacao());
        dto.setStatus(event.getStatus());

        if (event.getTracking() != null) {
            dto.setTrackingId(event.getTracking().getId());
            dto.setCodigoRastreamento(event.getTracking().getCodigoRastreamento());

            if (event.getTracking().getOrder() != null) {
                dto.setOrderId(event.getTracking().getOrder().getId());
                dto.setOrderNumber(event.getTracking().getOrder().getOrderNumber());
                
                // Dados do cliente
                if (event.getTracking().getOrder().getCustomer() != null) {
                    dto.setCustomerId(event.getTracking().getOrder().getCustomer().getId());
                    dto.setCustomerName(event.getTracking().getOrder().getCustomer().getName());
                }
            }
        }

        return dto;
    }
}