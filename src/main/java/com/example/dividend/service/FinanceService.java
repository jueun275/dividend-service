package com.example.dividend.service;

import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.model.constants.CacheKey;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.persist.repository.CompanyRepository;
import com.example.dividend.persist.repository.DividendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity companyEntity = companyRepository.findByName(companyName)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 회사입니다"));

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
            .map(e -> Dividend.builder()
                .date(e.getDate())
                .dividend(e.getDividend())
                .build())
            .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
            .name(companyEntity.getName())
            .ticker(companyEntity.getTicker())
            .build(),
            dividends);

    }
}
