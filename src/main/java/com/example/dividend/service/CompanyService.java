package com.example.dividend.service;

import com.example.dividend.model.Company;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.persist.repository.CompanyRepository;
import com.example.dividend.persist.repository.DividendRepository;
import com.example.dividend.scraper.Scraper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);
        }

        return this.storeCompanyAndDividend(ticker);
    }

    public Page<Company> getAllCompany(Pageable pageable) {
        Page<CompanyEntity> companyEntities = companyRepository.findAll(pageable);
        return companyEntities.map(CompanyEntity::toDomain);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // 1. ticker 를 기준으로 회사를 스크래핑
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 2. 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);

        // 3. 스크래핑 결과 반환
        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
            .map(dividend -> DividendEntity.from(dividend, companyEntity.getId()))
            .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }


    // 자동완성 기능 1 - LIKE 연산자를 활용하여 구현
    public Object getCompanyNamesByKeyword(String keyword) {
        PageRequest limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
            .map(CompanyEntity::getName)
            .collect(Collectors.toList());
    }



    /*
    // 자동완성 기능 2 - Trie 를 이용하여 자동완성 기능 구현 코드
    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
            .stream()
            .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);   // (key, value)
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }
    */

}
