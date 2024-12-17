package com.raffleease.raffleease.Domains.Raffles.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RafflesOrchestrator {
    private final CreateService createService;
    private final QueryService service;
    private final StatusService statusService;
    private final DeleteService deleteService;
    private final EditService editService;

}
