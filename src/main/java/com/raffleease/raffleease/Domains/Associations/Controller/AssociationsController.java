package com.raffleease.raffleease.Domains.Associations.Controller;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Services.OrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/associations")
public class AssociationsController {
    private final OrchestratorService orchestrator;

    @GetMapping("/{id}")
    public ResponseEntity<AssociationDTO> findById(
            @PathVariable("id") long id
    ) {
        return ResponseEntity.ok(orchestrator.findById(id));
    }

    @PostMapping("/")
    public ResponseEntity<AssociationDTO> create(
            @Valid @RequestBody AssociationCreate request
    ) {
        AssociationDTO association = orchestrator.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(association);
    }
}
