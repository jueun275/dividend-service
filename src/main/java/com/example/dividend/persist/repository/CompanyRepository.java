package com.example.dividend.persist.repository;

import com.example.dividend.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByTicker(String ticker);

    void delete(CompanyEntity company);

    Optional<CompanyEntity> findByName(String companyName);

    Page<CompanyEntity> findByNameStartingWithIgnoreCase(String keyword, Pageable pageable);

    Optional<CompanyEntity> findByTicker(String ticker);
}
