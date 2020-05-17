package apps.moviles.lab2ws.controllers;

import apps.moviles.lab2ws.entity.*;
import apps.moviles.lab2ws.repository.DepartmentRepository;
import apps.moviles.lab2ws.repository.EmployeeRepository;
import apps.moviles.lab2ws.repository.JobRepository;
import apps.moviles.lab2ws.repository.UserKeysRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
public class MainController {


    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    JobRepository jobRepository;
    @Autowired
    UserKeysRepository userKeysRepository;

    @GetMapping(value = "")
    public ResponseEntity index() {
        return new ResponseEntity("GGWP :V", HttpStatus.OK);
    }

    @PostMapping(value = "/getApiKey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getApiKey(@RequestParam(name = "groupKey", required = false) String groupKey) {

        HashMap<String, Object> responseMap = new HashMap<>();

        if (groupKey != null) {

            List<UserKeys> userKeysList = userKeysRepository.findByUserKey(groupKey);
            if (!userKeysList.isEmpty()) {
                UserKeys userKey = userKeysList.get(0);
                responseMap.put("estado", "ok");
                responseMap.put("api-key", userKey.getApiKey());
                responseMap.put("cuota", userKey.getCuota());
                return new ResponseEntity(responseMap, HttpStatus.OK);
            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "no se encontró un api-key para el group key enviado");
                return new ResponseEntity(responseMap, HttpStatus.OK);
            }
        } else {
            responseMap.put("estado", "error");
            responseMap.put("msg", "debe enviar un group key");
            return new ResponseEntity(responseMap, HttpStatus.OK);
        }
    }

    private HashMap<String, Object> validarApiKey(String apiKey, Boolean getuserKeyId) {
        HashMap<String, Object> responseMap = new HashMap<>();

        if (apiKey != null) {
            Optional<UserKeys> opt = userKeysRepository.findByApiKey(apiKey);
            if (opt.isPresent()) {
                UserKeys userKeys = opt.get();

                if (userKeys.getCuota() > 0) {
                    userKeys.setCuota(userKeys.getCuota() - 1);
                    userKeysRepository.save(userKeys);
                    responseMap.put("estado", "ok");
                    responseMap.put("cuota", userKeys.getCuota());
                    if (getuserKeyId) {
                        responseMap.put("userKeyId", userKeys.getId());
                    }
                    return responseMap;
                } else {
                    responseMap.put("estado", "error");
                    responseMap.put("msg", "Su cuota se ha agotado :( y su lab también...");
                    return responseMap;
                }
            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "El api-key enviado no existe");
                return responseMap;
            }
        } else {
            responseMap.put("estado", "error");
            responseMap.put("msg", "Debe enviar un api-key");
            return responseMap;
        }
    }


    @GetMapping(value = "/listar/empleados", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listarEmpleados(@RequestHeader(value = "api-key", required = false) String apiKey) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey,false);

        if (responseMap.get("estado").equals("ok")) {
            responseMap.put("empleados", employeeRepository.findAll());
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);

    }

    @GetMapping(value = "/listar/trabajos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listarTrabajos(@RequestHeader(value = "api-key", required = false) String apiKey) {
        HashMap<String, Object> responseMap = validarApiKey(apiKey,false);

        if (responseMap.get("estado").equals("ok")) {
            responseMap.put("trabajos", jobRepository.findAll());
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);

    }

    @GetMapping(value = "/listar/departamentos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listarDepartamentos(@RequestHeader(value = "api-key", required = false) String apiKey) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey,false);

        if (responseMap.get("estado").equals("ok")) {
            responseMap.put("departamentos", departmentRepository.findAll());
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    @GetMapping(value = "/existe/departamento/nombre_corto", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity buscarDepaPorNombreCorto(@RequestHeader(value = "api-key", required = false) String apiKey,
                                                   @RequestParam(value = "nombre_corto", required = false) String nombreCorto) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey,false);

        if (responseMap.get("estado").equals("ok")) {
            if (nombreCorto != null) {
                List<Department> departments = departmentRepository.obtenerDepartmentoPorDSN(nombreCorto);
                responseMap.put("existe", !departments.isEmpty());
            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "Debe enviar el nombre corto");
            }
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    @GetMapping(value = "/buscar/departamento", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity buscarDepartamentosPorId(@RequestHeader(value = "api-key", required = false) String apiKey,
                                                   @RequestParam(value = "id", required = false) String idStr) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey,false);

        if (responseMap.get("estado").equals("ok")) {
            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Optional<Department> opt = departmentRepository.findById(id);
                    if (opt.isPresent()) {
                        responseMap.put("departamento", opt.get());
                    } else {
                        responseMap.put("estado", "error");
                        responseMap.put("msg", "no se encontró el departamento con id: " + id);
                    }
                } catch (NumberFormatException ex) {
                    responseMap.put("estado", "error");
                    responseMap.put("msg", "El ID debe ser un número");
                }
            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "Debe enviar un ID");
            }
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    @DeleteMapping(value = "/borrar/empleado", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity borrarEmpleado(@RequestHeader(value = "api-key", required = false) String apiKey,
                                         @RequestParam(value = "id", required = false) String idStr) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey, true);
        String grupo = "grupo_" + responseMap.get("userKeyId");
        responseMap.remove("userKeyId");

        if (responseMap.get("estado").equals("ok")) {
            if (idStr != null) {

                Optional<Employee> empOpt = employeeRepository.findById(idStr);

                if (empOpt.isPresent()) {
                    Employee employeeTmp = empOpt.get();
                    if (employeeTmp.getCreatedBy().equals(grupo)) {
                        employeeRepository.deleteById(idStr);
                        responseMap.put("estado", "borrado exitoso");
                    } else {
                        responseMap.put("estado", "error");
                        responseMap.put("msg", "No tiene permisos para borrar este empleado");
                    }
                } else {
                    responseMap.put("estado", "error");
                    responseMap.put("msg", "no se encontró el empleado con id: " + idStr);
                }

            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "Debe enviar un ID");
            }
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }


    @DeleteMapping(value = "/borrar/trabajo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity borrarTrabajo(@RequestHeader(value = "api-key", required = false) String apiKey,
                                        @RequestParam(value = "id", required = false) String idStr) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey, true);
        String grupo = "grupo_" + responseMap.get("userKeyId");
        responseMap.remove("userKeyId");

        if (responseMap.get("estado").equals("ok")) {
            if (idStr != null) {
                Optional<Job> jobOpt = jobRepository.findById(idStr);

                if (jobOpt.isPresent()) {
                    Job jobTmp = jobOpt.get();
                    if (jobTmp.getCreatedBy().equals(grupo)) {
                        jobRepository.deleteById(idStr);
                        responseMap.put("estado", "borrado exitoso");
                    } else {
                        responseMap.put("estado", "error");
                        responseMap.put("msg", "No tiene permisos para borrar este trabajo");
                    }
                } else {
                    responseMap.put("estado", "error");
                    responseMap.put("msg", "no se encontró el trabajo con id: " + idStr);
                }
            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "Debe enviar un ID");
            }
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    @PostMapping(value = "/trabajo",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity guardarTrabajo(@RequestHeader(value = "api-key", required = false) String apiKey,
                                         @RequestParam(value = "update", required = false) String update,
                                         @RequestParam(value = "jobId", required = false) String jobId,
                                         @RequestParam(value = "jobTitle", required = false) String jobTitle,
                                         @RequestParam(value = "minSalary", required = false) String minSalaryStr,
                                         @RequestParam(value = "maxSalary", required = false) String maxSalaryStr) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey, true);
        String grupo = "grupo_" + responseMap.get("userKeyId");
        responseMap.remove("userKeyId");

        if (responseMap.get("estado").equals("ok")) {
            if (jobId != null && jobTitle != null && minSalaryStr != null && maxSalaryStr != null
                    && !jobId.trim().isEmpty() && !jobTitle.trim().isEmpty()) {

                Optional<Job> jobOpt = jobRepository.findById(jobId);

                if (!jobOpt.isPresent()) {
                    llenarTrabajoYguardar(jobId, jobTitle, minSalaryStr, maxSalaryStr, grupo, responseMap, "creado");
                } else {
                    if (update != null && update.equalsIgnoreCase("true")) {
                        Job jobTmp = jobOpt.get();
                        if (jobTmp.getCreatedBy().equals(grupo)) {
                            llenarTrabajoYguardar(jobId, jobTitle, minSalaryStr, maxSalaryStr, grupo, responseMap, "actualizado");
                        } else {
                            responseMap.put("estado", "error");
                            responseMap.put("msg", "No tiene permisos para actualizar este trabajo");
                        }
                    } else {
                        responseMap.put("estado", "error");
                        responseMap.put("msg", "Ya existe un trabajo con ese job_id");
                    }
                }


            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "Debe enviar todos los parámetros");
            }
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    private void llenarTrabajoYguardar(String jobId, String jobTitle, String minSalaryStr, String maxSalaryStr, String grupo,
                                       HashMap<String, Object> responseMap, String update) {
        try {
            int minSalary = Integer.parseInt(minSalaryStr);
            int maxSalary = Integer.parseInt(maxSalaryStr);
            Job job = new Job();
            job.setJobId(jobId);
            job.setJobTitle(jobTitle);
            job.setMinSalary(minSalary);
            job.setMaxSalary(maxSalary);
            job.setCreatedBy(grupo);
            try {
                jobRepository.save(job);
                responseMap.put("estado", "ok");
                responseMap.put("msg", "Trabajo " + update + " exitosamente");
            } catch (DataIntegrityViolationException ex) {
                responseMap.put("estado", "error");
                responseMap.put("msg", "consulte a su profesor");
                responseMap.put("msg_exception_cause", ex.getMostSpecificCause().getLocalizedMessage());
            }
        } catch (NumberFormatException ex) {
            responseMap.put("estado", "error");
            responseMap.put("msg", "Los salarios deben ser números enteros");
        }
    }


    @PostMapping(value = "/empleado",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity guardarEmpleado(@RequestHeader(value = "api-key", required = false) String apiKey,
                                          @RequestParam(value = "update", required = false) String update,
                                          @RequestParam(value = "employeeId", required = false) String employeeId,
                                          @RequestParam(value = "firstName", required = false) String firstName,
                                          @RequestParam(value = "lastName", required = false) String lastName,
                                          @RequestParam(value = "email", required = false) String email,
                                          @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                          @RequestParam(value = "jobId", required = false) String jobId,
                                          @RequestParam(value = "salary", required = false) String salaryStr,
                                          @RequestParam(value = "commissionPct", required = false) String commissionPctStr,
                                          @RequestParam(value = "managerId", required = false) String managerId,
                                          @RequestParam(value = "departmentId", required = false) String departmentIdStr) {

        HashMap<String, Object> responseMap = validarApiKey(apiKey, true);
        String grupo = "grupo_" + responseMap.get("userKeyId");
        responseMap.remove("userKeyId");

        if (responseMap.get("estado").equals("ok")) {
            if (employeeId != null && lastName != null && email != null && jobId != null && managerId != null && departmentIdStr != null
                    && !employeeId.trim().isEmpty() && !lastName.trim().isEmpty()
                    && !email.trim().isEmpty() && !jobId.trim().isEmpty()
                    && !managerId.trim().isEmpty() && !departmentIdStr.trim().isEmpty()) {

                Optional<Employee> empOpt = employeeRepository.findById(employeeId);
                if (!empOpt.isPresent()) {
                    llenarEmpleado(employeeId, firstName, lastName, email, phoneNumber, jobId,
                            salaryStr, commissionPctStr, managerId, departmentIdStr, grupo, responseMap, "creado");
                } else {
                    if (update != null && update.equalsIgnoreCase("true")) {
                        Employee employeeTmp = empOpt.get();
                        if (employeeTmp.getCreatedBy().equals(grupo)) {
                            llenarEmpleado(employeeId, firstName, lastName, email, phoneNumber, jobId,
                                    salaryStr, commissionPctStr, managerId, departmentIdStr, grupo, responseMap, "actualizado");
                        } else {
                            responseMap.put("estado", "error");
                            responseMap.put("msg", "No tiene permisos para actualizar este empleado");
                        }
                    } else {
                        responseMap.put("estado", "error");
                        responseMap.put("msg", "Ya existe un empleado con ese employeeId");
                    }
                }
            } else {
                responseMap.put("estado", "error");
                responseMap.put("msg", "Debe enviar todos los parámetros");
            }
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    private void llenarEmpleado(String employeeId, String firstName, String lastName, String email,
                                String phoneNumber, String jobId, String salaryStr, String commissionPctStr,
                                String managerId, String departmentIdStr, String grupo,
                                HashMap<String, Object> responseMap, String update) {

        try {
            Employee e = new Employee();
            e.setEmployeeId(employeeId);
            if (firstName != null) e.setFirstName(firstName);
            e.setLastName(lastName);
            e.setEmail(email);
            if (phoneNumber != null) e.setPhoneNumber(phoneNumber);

            Job j = new Job();
            j.setJobId(jobId);
            e.setJobId(j);

            if (salaryStr != null) {
                double salary = Double.parseDouble(salaryStr);
                e.setSalary(salary);
            }
            if (commissionPctStr != null) {
                double commissionPct = Double.parseDouble(commissionPctStr);
                e.setCommissionPct(commissionPct);
            }
            ManagerDto manager = new ManagerDto();
            manager.setEmployeeId(managerId);
            e.setManagerId(manager);

            Department d = new Department();
            int departmentId = Integer.parseInt(departmentIdStr);
            d.setDepartmentId(departmentId);
            e.setDepartmentId(d);

            e.setCreatedBy(grupo);

            responseMap.remove("userKeyId");
            try {
                employeeRepository.save(e);
                responseMap.put("estado", "ok");
                responseMap.put("msg", "Empleado " + update + " exitosamente");
            } catch (DataIntegrityViolationException | JpaObjectRetrievalFailureException ex) {
                responseMap.put("estado", "error");
                responseMap.put("msg", "consulte a su profesor");
                responseMap.put("msg_exception_cause", ex.getMostSpecificCause().getLocalizedMessage());
            }
        } catch (NumberFormatException ex) {
            responseMap.put("estado", "error");
            responseMap.put("msg", "Los números no están en formato adecuado");
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity gestionExcepcion(HttpServletRequest request) {

        HashMap<String, Object> responseMap = new HashMap<>();
        if (request.getMethod().equals("POST")) {
            responseMap.put("estado", "error");
            responseMap.put("msg", "Contacte al profesor");
        }
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }


}
