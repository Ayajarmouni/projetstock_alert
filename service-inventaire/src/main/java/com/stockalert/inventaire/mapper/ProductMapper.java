package com.stockalert.inventaire.mapper;

import com.stockalert.inventaire.dto.ProductDto;
import com.stockalert.inventaire.entite.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    Product toEntity(ProductDto dto);

    ProductDto toDto(Product entity);
}
