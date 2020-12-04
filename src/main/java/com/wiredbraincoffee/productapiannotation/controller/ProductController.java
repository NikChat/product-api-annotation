package com.wiredbraincoffee.productapiannotation.controller;

import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductRepository repository;

    public ProductController(ProductRepository repository) { //constructor, so spring can inject the repository automatically
        this.repository = repository;
    }

    @GetMapping
    public Flux<Product> getAllProducts() {
        return repository.findAll(); //there is no subscribe. spring will call it.
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
        return repository.findById(id) //findById returns a mono of type Product
                .map(product -> ResponseEntity.ok(product)) //convert product in a responseEntity with ok status.
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //always returns HttpStatus.CREATED
    public Mono<Product> saveProduct(@RequestBody Product product) {
        return repository.save(product); //we return a Publisher, because this method is executed asynchronously
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable(value = "id") String id,
                                                       @RequestBody Product product) {
        return repository.findById(id) //returns Mono<Product>
                .flatMap(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    return repository.save(existingProduct); //return the mono from the save operation
                })
                .map(updateProduct -> ResponseEntity.ok(updateProduct))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable(value = "id") String id) {
        return repository.findById(id)
                .flatMap(existingProduct ->
                        repository.delete(existingProduct) //returns empty mono that doesn't publish a value, mono completion signal
                                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build()); //if an empty Mono is returned
    }

    @DeleteMapping
    public Mono<Void> deleteAllProducts() {
        return repository.deleteAll();
    }

    // we will simulate a stream of events that will be pushed to the clients of the API at a regular interval (using server-side events)
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> getProductEvents() {
        return Flux.interval(Duration.ofSeconds(1)) //interval operator emits values starting with 0, and increment every second
                .map(val ->
                        new ProductEvent(val, "Product Event")
                );
    }
}
