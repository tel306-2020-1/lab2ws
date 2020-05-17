package apps.moviles.lab2ws.repository;

import apps.moviles.lab2ws.entity.Department;
import apps.moviles.lab2ws.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query(value = "select * from departments where department_short_name = ?1", nativeQuery = true)
    List<Department> obtenerDepartmentoPorDSN(String depaShortName);

}
