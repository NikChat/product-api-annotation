package com.wiredbraincoffee.productapiannotation.repository;

import com.wiredbraincoffee.productapiannotation.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//it has methods like save, findAll but with Mono and Flux return types -> reactive repository
public interface ProductRepository
        extends ReactiveMongoRepository<Product, String> {
}
