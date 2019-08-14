package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<Document, Long> {
}
