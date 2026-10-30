package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreateSupplierRequest;
import com.stocksmart.dto.SupplierResponse;
import com.stocksmart.dto.UpdateSupplierRequest;
import com.stocksmart.entity.Supplier;
import com.stocksmart.exception.ConflictException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierResponse> list() {
        return supplierRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponse get(Long id) {
        return toResponse(findSupplier(id));
    }

    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
        String name = request.name().trim();
        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Supplier name is already used");
        }
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactName(trimOrNull(request.contactName()));
        supplier.setEmail(trimOrNull(request.email()));
        supplier.setPhone(trimOrNull(request.phone()));
        supplier.setAddress(trimOrNull(request.address()));
        supplierRepository.save(supplier);
        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Long id, UpdateSupplierRequest request) {
        Supplier supplier = findSupplier(id);
        String name = request.name().trim();
        if (supplierRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Supplier name is already used");
        }
        supplier.setName(name);
        supplier.setContactName(trimOrNull(request.contactName()));
        supplier.setEmail(trimOrNull(request.email()));
        supplier.setPhone(trimOrNull(request.phone()));
        supplier.setAddress(trimOrNull(request.address()));
        return toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = findSupplier(id);
        supplierRepository.delete(supplier);
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress());
    }
}
