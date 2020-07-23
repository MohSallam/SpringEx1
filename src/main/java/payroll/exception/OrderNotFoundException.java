package payroll.exception;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(Long id) {
        super("Couldn't find order with ID : " + id);
    }
}
