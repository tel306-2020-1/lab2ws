package apps.moviles.lab2ws.repository;

import apps.moviles.lab2ws.entity.Department;
import apps.moviles.lab2ws.entity.UserKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserKeysRepository extends JpaRepository<UserKeys,Integer> {

    List<UserKeys> findByUserKey(String userKey);

    Optional<UserKeys> findByApiKey(String apiKey);

}
