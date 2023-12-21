package com.hoangtien2k3.productservice.service;

import com.hoangtien2k3.productservice.dto.CategoryDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CategoryService {

//    List<CategoryDto> findAll();
    Flux<List<CategoryDto>> findAll();

    CategoryDto findById(final Integer categoryId);

//    CategoryDto save(final CategoryDto categoryDto);
    Mono<CategoryDto> save(final CategoryDto categoryDto);

    CategoryDto update(final CategoryDto categoryDto);

    CategoryDto update(final Integer categoryId, final CategoryDto categoryDto);

    void deleteById(final Integer categoryId);

}
