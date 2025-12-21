package com.teckiz.controller.superadmin;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyInvoice;
import com.teckiz.repository.CompanyInvoiceRepository;
import com.teckiz.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/superadmin/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyInvoiceController {

    private final CompanyInvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String companyKey,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CompanyInvoice> invoices;

        if (companyKey != null) {
            Company company = companyRepository.findByCompanyKey(companyKey)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            if (status != null) {
                invoices = invoiceRepository.findByCompanyAndStatus(company, status, pageable);
            } else {
                invoices = invoiceRepository.findByCompany(company, pageable);
            }
        } else if (status != null) {
            List<CompanyInvoice> invoiceList = invoiceRepository.findAll();
            if (startDate != null && endDate != null) {
                invoiceList = invoiceList.stream()
                        .filter(inv -> inv.getStatus().equals(status) &&
                                inv.getDueDate() != null &&
                                inv.getDueDate().isAfter(startDate) &&
                                inv.getDueDate().isBefore(endDate))
                        .toList();
            } else {
                invoiceList = invoiceList.stream()
                        .filter(inv -> inv.getStatus().equals(status))
                        .toList();
            }
            int start = page * size;
            int end = Math.min(start + size, invoiceList.size());
            List<CompanyInvoice> paginated = start < invoiceList.size() ? invoiceList.subList(start, end) : List.of();
            invoices = new org.springframework.data.domain.PageImpl<>(paginated, pageable, invoiceList.size());
        } else {
            invoices = invoiceRepository.findAll(pageable);
        }

        List<Map<String, Object>> invoiceResponses = invoices.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("invoices", invoiceResponses);
        response.put("totalPages", invoices.getTotalPages());
        response.put("totalElements", invoices.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{invoiceKey}")
    public ResponseEntity<Map<String, Object>> getInvoice(@PathVariable String invoiceKey) {
        return invoiceRepository.findByInvoiceKey(invoiceKey)
                .map(invoice -> ResponseEntity.ok(mapToResponse(invoice)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyKey}")
    public ResponseEntity<Map<String, Object>> getCompanyInvoices(
            @PathVariable String companyKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CompanyInvoice> invoices;

        if (status != null) {
            invoices = invoiceRepository.findByCompanyAndStatus(company, status, pageable);
        } else {
            invoices = invoiceRepository.findByCompany(company, pageable);
        }

        List<Map<String, Object>> invoiceResponses = invoices.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("invoices", invoiceResponses);
        response.put("totalPages", invoices.getTotalPages());
        response.put("totalElements", invoices.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody Map<String, Object> request) {
        String companyKey = (String) request.get("companyKey");
        if (companyKey == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "companyKey is required"));
        }

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Object amountObj = request.get("amount");
        if (amountObj == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "amount is required"));
        }

        BigDecimal amount;
        if (amountObj instanceof Number) {
            amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
        } else {
            amount = new BigDecimal(amountObj.toString());
        }

        CompanyInvoice invoice = CompanyInvoice.builder()
                .company(company)
                .amount(amount)
                .currency((String) request.getOrDefault("currency", "USD"))
                .status((String) request.getOrDefault("status", "pending"))
                .description((String) request.get("description"))
                .notes((String) request.get("notes"))
                .stripeInvoiceId((String) request.get("stripeInvoiceId"))
                .stripePaymentIntentId((String) request.get("stripePaymentIntentId"))
                .build();

        if (request.get("dueDate") != null) {
            invoice.setDueDate(LocalDateTime.parse(request.get("dueDate").toString()));
        }
        if (request.get("billingPeriodStart") != null) {
            invoice.setBillingPeriodStart(LocalDateTime.parse(request.get("billingPeriodStart").toString()));
        }
        if (request.get("billingPeriodEnd") != null) {
            invoice.setBillingPeriodEnd(LocalDateTime.parse(request.get("billingPeriodEnd").toString()));
        }

        invoice = invoiceRepository.save(invoice);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Invoice created successfully", "invoiceKey", invoice.getInvoiceKey()));
    }

    @PutMapping("/{invoiceKey}")
    public ResponseEntity<?> updateInvoice(
            @PathVariable String invoiceKey,
            @RequestBody Map<String, Object> request) {

        CompanyInvoice invoice = invoiceRepository.findByInvoiceKey(invoiceKey)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (request.get("amount") != null) {
            Object amountObj = request.get("amount");
            BigDecimal amount;
            if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else {
                amount = new BigDecimal(amountObj.toString());
            }
            invoice.setAmount(amount);
        }
        if (request.get("currency") != null) {
            invoice.setCurrency((String) request.get("currency"));
        }
        if (request.get("status") != null) {
            invoice.setStatus((String) request.get("status"));
            if ("paid".equals(request.get("status")) && invoice.getPaidDate() == null) {
                invoice.setPaidDate(LocalDateTime.now());
            }
        }
        if (request.get("dueDate") != null) {
            invoice.setDueDate(LocalDateTime.parse(request.get("dueDate").toString()));
        }
        if (request.get("paidDate") != null) {
            invoice.setPaidDate(LocalDateTime.parse(request.get("paidDate").toString()));
        }
        if (request.get("description") != null) {
            invoice.setDescription((String) request.get("description"));
        }
        if (request.get("notes") != null) {
            invoice.setNotes((String) request.get("notes"));
        }
        if (request.get("stripeInvoiceId") != null) {
            invoice.setStripeInvoiceId((String) request.get("stripeInvoiceId"));
        }
        if (request.get("stripePaymentIntentId") != null) {
            invoice.setStripePaymentIntentId((String) request.get("stripePaymentIntentId"));
        }
        if (request.get("billingPeriodStart") != null) {
            invoice.setBillingPeriodStart(LocalDateTime.parse(request.get("billingPeriodStart").toString()));
        }
        if (request.get("billingPeriodEnd") != null) {
            invoice.setBillingPeriodEnd(LocalDateTime.parse(request.get("billingPeriodEnd").toString()));
        }

        invoiceRepository.save(invoice);

        return ResponseEntity.ok(Map.of("message", "Invoice updated successfully"));
    }

    @DeleteMapping("/{invoiceKey}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String invoiceKey) {
        CompanyInvoice invoice = invoiceRepository.findByInvoiceKey(invoiceKey)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoiceRepository.delete(invoice);

        return ResponseEntity.ok(Map.of("message", "Invoice deleted successfully"));
    }

    private Map<String, Object> mapToResponse(CompanyInvoice invoice) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", invoice.getId());
        response.put("invoiceKey", invoice.getInvoiceKey());
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put("amount", invoice.getAmount());
        response.put("currency", invoice.getCurrency());
        response.put("status", invoice.getStatus());
        response.put("dueDate", invoice.getDueDate());
        response.put("paidDate", invoice.getPaidDate());
        response.put("description", invoice.getDescription());
        response.put("notes", invoice.getNotes());
        response.put("stripeInvoiceId", invoice.getStripeInvoiceId());
        response.put("stripePaymentIntentId", invoice.getStripePaymentIntentId());
        response.put("billingPeriodStart", invoice.getBillingPeriodStart());
        response.put("billingPeriodEnd", invoice.getBillingPeriodEnd());
        response.put("companyId", invoice.getCompany().getId());
        response.put("companyName", invoice.getCompany().getName());
        response.put("createdAt", invoice.getCreatedAt());
        response.put("updatedAt", invoice.getUpdatedAt());
        return response;
    }
}

