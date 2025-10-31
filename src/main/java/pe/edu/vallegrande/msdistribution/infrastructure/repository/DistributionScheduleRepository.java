package pe.edu.vallegrande.msdistribution.infrastructure.repository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DistributionScheduleRepository extends ReactiveMongoRepository<DistributionSchedule, String> {

    Flux<DistributionSchedule> findAllByStatus(String status);

    Mono<Boolean> existsByScheduleCode(String scheduleCode);

    Mono<DistributionSchedule> findTopByOrderByScheduleCodeDesc();
}
