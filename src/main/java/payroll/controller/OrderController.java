package payroll.controller;


import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payroll.exception.OrderNotFoundException;
import payroll.mapper.OrderModelAssembler;
import payroll.model.CustomerOrder;
import payroll.model.Status;
import payroll.repo.OrderRepo;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class OrderController {

    private final OrderRepo orderRepo;
    private final OrderModelAssembler orderModelAssembler;

    public OrderController(OrderRepo orderRepo, OrderModelAssembler orderModelAssembler) {
        this.orderRepo = orderRepo;
        this.orderModelAssembler = orderModelAssembler;
    }

    @GetMapping("/orders")
    public CollectionModel<EntityModel<CustomerOrder>> all(){
        List<EntityModel<CustomerOrder>> orders = orderRepo.findAll().stream()
                .map(orderModelAssembler::toModel).collect(Collectors.toList());
        return CollectionModel.of(orders,
                linkTo(methodOn(OrderController.class).all()).withSelfRel());
    }

    @GetMapping("/orders/{id}")
    public EntityModel<CustomerOrder> one(@PathVariable long id) {
        CustomerOrder order = orderRepo.findById(id).orElseThrow(()->new OrderNotFoundException(id));
        return orderModelAssembler.toModel(order);
    }

    @PostMapping("/orders")
    ResponseEntity<?> newOrder(@RequestBody CustomerOrder order){
        order.setStatus(Status.IN_PROGRESS);
        CustomerOrder newOrder = orderRepo.save(order);
        return ResponseEntity.created(linkTo(methodOn(OrderController.class).one(newOrder.getId())).toUri())
                .body(orderModelAssembler.toModel(newOrder));
    }

    @DeleteMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable long id) {
        CustomerOrder order = orderRepo.findById(id).orElseThrow(()->new OrderNotFoundException(id));
        if(order.getStatus() == Status.IN_PROGRESS){
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(orderModelAssembler.toModel(orderRepo.save(order)));
        }
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create().withTitle("Method not allowed")
                        .withDetail("You can't cancel order in status " + order.getStatus() + " status" ));
    }

    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable long id) {
        CustomerOrder order = orderRepo.findById(id).orElseThrow(()-> new OrderNotFoundException(id));
        if (order.getStatus() == Status.IN_PROGRESS){
            order.setStatus(Status.COMPLETED);
            CustomerOrder updatedOrder = orderRepo.save(order);
            return  ResponseEntity.ok(orderModelAssembler.toModel(updatedOrder));
        }
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                .withTitle("Method not allowed")
                .withDetail("You can't complete order that is in status " + order.getStatus()));
    }
}
