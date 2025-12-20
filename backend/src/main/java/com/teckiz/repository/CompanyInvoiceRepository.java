package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyInvoiceRepository extends JpaRepository<CompanyInvoice, Long> {

    Optional<CompanyInvoice> findByInvoiceKey(String invoiceKey);

    Optional<CompanyInvoice> findByInvoiceNumber(String invoiceNumber);

    List<CompanyInvoice> findByCompany(Company company);

    Page<CompanyInvoice> findByCompany(Company company, Pageable pageable);

    List<CompanyInvoice> findByCompanyAndStatus(Company company, String status);

    Page<CompanyInvoice> findByCompanyAndStatus(Company company, String status, Pageable pageable);

    List<CompanyInvoice> findByCompanyAndDueDateBeforeAndStatusNot(Company company, LocalDateTime date, String status);

    List<CompanyInvoice> findByCompanyAndStatusAndDueDateBetween(
            Company company, String status, LocalDateTime start, LocalDateTime end);
}

