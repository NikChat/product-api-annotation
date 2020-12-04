package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiAnnotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiAnnotationApplication.class, args);
	}
	//I want to execute some code right after Spring Boot app finishes starting up
	//we want to insert some initial products to our embedded db
	@Bean //Spring can process the returned CommandLineRunner object automatically
	CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository repository) { //CommandLineRunner = functional inteface
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null, "Big Latte", 2.99),
					new Product(null, "Big Decaf", 2.49),
					new Product(null, "Green Tea", 1.99))
					.flatMap(repository::save); //transform the flux of products into a flux of save products
					//.flatMap(p -> repository.save(p));

			//then: allows the first Publisher to complete, and then executes the publisher it receives as an argument
			//then: takes no parameters or a Mono, thenMany: takes any Publisher (Flux or Mono)
			productFlux 
					.thenMany(repository.findAll()) //i want to be sure the save operation is completely finished 
					.subscribe(System.out::println); //I subscribe to the flux returned by findAll(). This will trigger the whole process.

			//in case we want to work with a real mongodb db
            /*operations.collectionExists(Product.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(exists))
					.thenMany(v -> operations.createCollection(Product.class))
					.thenMany(productFlux)
					.thenMany(repository.findAll())
					.subscribe(System.out::println);*/
		};
	}
}
