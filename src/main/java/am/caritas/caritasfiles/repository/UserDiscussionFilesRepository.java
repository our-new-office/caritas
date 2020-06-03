package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.UserDiscussionFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDiscussionFilesRepository extends JpaRepository<UserDiscussionFiles, Long> {

    List<UserDiscussionFiles> findAllByDiscussionIdOrderByIdDesc(Long id);

}
