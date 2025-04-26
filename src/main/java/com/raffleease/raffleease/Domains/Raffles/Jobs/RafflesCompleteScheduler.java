package com.raffleease.raffleease.Domains.Raffles.Jobs;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason.END_DATE_REACHED;
import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.COMPLETED;

@RequiredArgsConstructor
@Service
public class RafflesCompleteScheduler {
    private final RafflesPersistenceService persistenceService;
    private final RafflesRepository repository;

    @Scheduled(cron = "${spring.application.configs.cron.images_cleanup}")
    public void completeRaffles() {
        List<Raffle> raffles = repository.findAllEligibleForCompletion(COMPLETED);

        for (Raffle raffle : raffles) {
            raffle.setStatus(COMPLETED);
            raffle.setCompletionReason(END_DATE_REACHED);
            raffle.setCompletedAt(LocalDateTime.now());
        }
        persistenceService.saveAll(raffles);
    }
}
