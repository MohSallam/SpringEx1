package payroll.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import payroll.model.CustomerOrder;
import payroll.model.Employee;
import payroll.model.Status;
import payroll.repo.EmployeeRepo;
import payroll.repo.OrderRepo;


@Configuration
public class LoadDB {
    private static final Logger log = LoggerFactory.getLogger(LoadDB.class);

    @Bean
    CommandLineRunner initDatabase(EmployeeRepo employeeRepo, OrderRepo orderRepo){
        return args -> {
            log.info("Reloading " + employeeRepo.save(new Employee("Mohamed Sallam", "Engineer")));
            log.info("Reloading " + employeeRepo.save(new Employee("Asmaa Sallam", "Business")));
            orderRepo.save(new CustomerOrder("MacBook Pro", Status.COMPLETED));
            orderRepo.save(new CustomerOrder("iPhone", Status.IN_PROGRESS));
            orderRepo.findAll().forEach(order -> {
                log.info("preloaded order : " + order);
            });
        };
    }

}
