package payroll.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import payroll.model.CustomerOrder;

public interface OrderRepo extends JpaRepository<CustomerOrder, Long> {

}
