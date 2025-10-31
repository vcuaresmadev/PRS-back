package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.msdistribution.domain.models.Fare;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@Slf4j
public class FareSchedulerService {

    @Autowired
    private FareRepository fareRepository;
    
    @Autowired
    private FareServiceImpl fareService;

    /**
     * Scheduled task that runs every hour to check for fare transitions
     * This will activate/deactivate fares based on their effective dates
     */
    @Scheduled(cron = "0 0 * * * ?") // cada hora
    public void processFareTransitions() {
        log.info("Processing fare transitions...");
        
        Date now = new Date();
        
        // Process automatic fare transitions (deactivate old fares after Nov 1st)
        fareService.processAutomaticFareTransitions();
        
        // Activar tarifas cuya vigencia comenzó
        activateScheduledFares(now)
                .then(deactivateExpiredFares(now))
                .subscribe(
                    unused -> log.info("Fare transition processing completed."),
                    error -> log.error("Error processing fare transitions: ", error)
                );
    }

    /**
     * Manual trigger for fare transitions processing
     * This allows immediate activation/deactivation of fares based on their effective dates
     */
    public void triggerFareTransitions() {
        log.info("Manually triggering fare transitions...");
        
        Date now = new Date();
        
        // Process automatic fare transitions (deactivate old fares after Nov 1st)
        fareService.processAutomaticFareTransitions();
        
        // Activar tarifas cuya vigencia comenzó
        activateScheduledFares(now)
                .then(deactivateExpiredFares(now))
                .subscribe(
                    unused -> log.info("Manual fare transition processing completed."),
                    error -> log.error("Error processing manual fare transitions: ", error)
                );
    }

    private Mono<Void> activateScheduledFares(Date now) {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name())
                .filter(fare -> fare.getEffectiveDate() != null &&
                            (fare.getEffectiveDate().before(now) || fare.getEffectiveDate().equals(now)))
                .flatMap(fare -> {
                    fare.setStatus(Constants.ACTIVE.name());
                    return fareRepository.save(fare)
                            .flatMap(this::handleFareActivation);
                })
                .then();
    }

    private Mono<Void> deactivateExpiredFares(Date now) {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getEffectiveDate() != null && 
                       fare.getEffectiveDate().before(now) && 
                       !fare.getEffectiveDate().equals(now))
                .flatMap(fare -> {
                    fare.setStatus(Constants.INACTIVE.name());
                    return fareRepository.save(fare);
                })
                .then();
    }

    private Mono<Fare> handleFareActivation(Fare activatedFare) {
        return fareRepository.findByOrganizationIdAndStatusOrderByEffectiveDateDesc(
                        activatedFare.getOrganizationId(), Constants.ACTIVE.name())
                .filter(fare -> !fare.getId().equals(activatedFare.getId()))
                .flatMap(fare -> {
                    fare.setStatus(Constants.INACTIVE.name());
                    return fareRepository.save(fare);
                })
                .then(Mono.just(activatedFare));
    }
}