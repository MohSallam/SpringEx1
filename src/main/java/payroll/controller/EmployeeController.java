package payroll.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payroll.exception.EmployeeNotFoundException;
import payroll.mapper.EmployeeModelAssembler;
import payroll.model.Employee;
import payroll.repo.EmployeeRepo;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class EmployeeController {
    private final EmployeeRepo employeeRepo;
    private final EmployeeModelAssembler employeeModelAssembler;

    public EmployeeController(EmployeeRepo employeeRepo, EmployeeModelAssembler employeeModelAssembler) {
        this.employeeRepo = employeeRepo;
        this.employeeModelAssembler = employeeModelAssembler;
    }

    @GetMapping("/employees")
    public CollectionModel<EntityModel<Employee>> all(){
        List<EntityModel<Employee>> employees = employeeRepo.findAll().stream()
                .map(employeeModelAssembler::toModel).collect(Collectors.toList());
        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }

    @GetMapping("/employees/{id}")
    public EntityModel<Employee> one(@PathVariable long id){
        Employee employee = employeeRepo.findById(id).orElseThrow(()->new EmployeeNotFoundException(id));
        return employeeModelAssembler.toModel(employee);
    }

    @PostMapping("/employees")
    ResponseEntity<?> newEmployee(@RequestBody Employee employee){
        EntityModel<Employee> entityModel = employeeModelAssembler.toModel(employeeRepo.save(employee));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }



    @PutMapping("/employees/{id}")
    ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable long id){
        Employee updatedEmployee =  employeeRepo.findById(id)
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return employeeRepo.save(employee);
                }).orElseGet(()->{
                    newEmployee.setId(id);
                    employeeRepo.save(newEmployee);
                    return employeeRepo.save(newEmployee);
                });
        EntityModel<Employee> entityModel = employeeModelAssembler.toModel(updatedEmployee);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @DeleteMapping("employees/{id}")
    ResponseEntity<?> deleteEmployee(@PathVariable long id){
        employeeRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
