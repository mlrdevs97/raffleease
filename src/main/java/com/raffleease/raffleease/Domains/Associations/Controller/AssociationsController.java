package com.raffleease.raffleease.Domains.Associations.Controller;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsOrchestrator;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/associations")
public class AssociationsController {
    private final IAssociationsOrchestrator orchestrator;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(
            @PathVariable("id") long id
    ) {
        AssociationDTO association = orchestrator.findById(id);
        return ResponseEntity.ok(ResponseFactory.success(
                association,
                "Association retrieved successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @Valid @RequestBody AssociationCreate request
    ) {
        AssociationDTO createdAssociation = orchestrator.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAssociation.id())
                .toUri();

        return ResponseEntity.created(location).body(ResponseFactory.success(
                createdAssociation,
                "New association successfully created"
        ));
    }
}
