package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Long> {
}
