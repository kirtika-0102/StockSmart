package com.stocksmart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreateTransferRequest;
import com.stocksmart.dto.StockMovementRequest;
import com.stocksmart.dto.StockTransferItemResponse;
import com.stocksmart.dto.StockTransferResponse;
import com.stocksmart.dto.TransferItemRequest;
import com.stocksmart.entity.Location;
import com.stocksmart.entity.Product;
import com.stocksmart.entity.StockTransfer;
import com.stocksmart.entity.StockTransferItem;
import com.stocksmart.entity.TransferStatus;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.LocationRepository;
import com.stocksmart.repository.ProductRepository;
import com.stocksmart.repository.StockTransferRepository;

@Service
@Transactional(readOnly = true)
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public StockTransferService(
            StockTransferRepository stockTransferRepository,
            LocationRepository locationRepository,
            ProductRepository productRepository,
            InventoryService inventoryService) {
        this.stockTransferRepository = stockTransferRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    public List<StockTransferResponse> listAll() {
        return stockTransferRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public StockTransferResponse get(Long id) {
        return toResponse(findTransfer(id));
    }

    @Transactional
    public StockTransferResponse create(CreateTransferRequest request) {
        if (request.sourceLocationId().equals(request.destLocationId())) {
            throw new BusinessException("Source and destination locations cannot be the same");
        }

        Location source = locationRepository.findById(request.sourceLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Source location not found"));
        Location dest = locationRepository.findById(request.destLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination location not found"));

        StockTransfer transfer = new StockTransfer();
        transfer.setSourceLocation(source);
        transfer.setDestLocation(dest);
        transfer.setStatus(TransferStatus.PENDING);

        for (TransferItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            StockTransferItem item = new StockTransferItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            transfer.addItem(item);
        }

        stockTransferRepository.save(transfer);
        return toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse complete(Long id) {
        StockTransfer transfer = findTransfer(id);
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new BusinessException("Stock transfer is already completed");
        }
        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new BusinessException("Cannot complete a cancelled transfer");
        }

        Location source = transfer.getSourceLocation();
        Location dest = transfer.getDestLocation();

        for (StockTransferItem item : transfer.getItems()) {
            // Deduct from source (validates sufficient stock)
            inventoryService.stockOut(new StockMovementRequest(
                    source.getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "TRANSFER",
                    "TR-" + transfer.getId(),
                    "Transfer to " + dest.getName()));

            // Add to destination
            inventoryService.stockIn(new StockMovementRequest(
                    dest.getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "TRANSFER",
                    "TR-" + transfer.getId(),
                    "Transfer from " + source.getName()));
        }

        transfer.setStatus(TransferStatus.COMPLETED);
        return toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse cancel(Long id) {
        StockTransfer transfer = findTransfer(id);
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel an already completed transfer");
        }
        transfer.setStatus(TransferStatus.CANCELLED);
        return toResponse(transfer);
    }

    private StockTransfer findTransfer(Long id) {
        return stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transfer not found"));
    }

    private StockTransferResponse toResponse(StockTransfer transfer) {
        List<StockTransferItemResponse> itemResponses = transfer.getItems().stream()
                .map(i -> new StockTransferItemResponse(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getProduct().getSku(),
                        i.getQuantity()))
                .toList();

        return new StockTransferResponse(
                transfer.getId(),
                transfer.getSourceLocation().getId(),
                transfer.getSourceLocation().getName(),
                transfer.getDestLocation().getId(),
                transfer.getDestLocation().getName(),
                transfer.getStatus(),
                itemResponses,
                transfer.getCreatedAt());
    }
}
