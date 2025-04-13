package com.raffleease.raffleease.Domains.Carts.Jobs;

public class ReservationsCleanupScheduler {
    // TODO: Fix release scheduled
    /*
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void releaseScheduled() {
        LocalDateTime lastModified = LocalDateTime.now().minusMinutes(30);
        List<Cart> expiredCarts = customCartRepository.findExpiredCarts(lastModified);

        Map<Raffle, List<Ticket>> ticketsByRaffle = expiredCarts.stream()
                .flatMap(cart -> cart.getTickets().stream()
                        .map(ticket -> new AbstractMap.SimpleEntry<>(cart.getRaffle(), ticket)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        for (Map.Entry<Raffle, List<Ticket>> entry : ticketsByRaffle.entrySet()) {
            Raffle raffle = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            ticketsService.edit(tickets, AVAILABLE);
            raffleTicketsAvailabilityService.increaseAvailableTickets(raffle, tickets.size());
        }

        customCartRepository.updateExpiredCart(lastModified);
    }
     */
}
