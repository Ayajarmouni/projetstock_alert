package com.stockalert.approvisionnement.mapper;

import com.stockalert.approvisionnement.dto.PurchaseOrderDto;
import com.stockalert.approvisionnement.dto.PurchaseOrderRequestDto;
import com.stockalert.approvisionnement.entite.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PurchaseOrderMapper {
    PurchaseOrder toEntity(PurchaseOrderRequestDto dto);

    PurchaseOrderDto toDto(PurchaseOrder entity);
}
