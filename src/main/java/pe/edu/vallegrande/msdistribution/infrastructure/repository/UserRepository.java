package pe.edu.vallegrande.msdistribution.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.msdistribution.domain.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findAllByStatus(String status);
    Mono<User> findFirstByUserCode(String userCode);
    Mono<User> findTopByOrderByUserCodeDesc();
    Flux<User> findByOrganizationId(String organizationId);
}