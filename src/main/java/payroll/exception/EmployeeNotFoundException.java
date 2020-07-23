package payroll.exception;

public class EmployeeNotFoundException extends RuntimeException{

    public EmployeeNotFoundException(Long id) {
        super("Could not found an employee with ID : " + id);
    }
}
