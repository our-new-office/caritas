package am.caritas.caritasfiles.repository;

import am.caritas.caritasfiles.model.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    Page<Log> findAll(Pageable pageable);

    List<Log> findAllByDateBetween(Date startDate, Date date);

    List<Log> findAllByUserIgnoreCaseContainingOrderByIdDesc(String user);

    List<Log> findAllByActionContainingOrderByIdDesc(String action);




}
