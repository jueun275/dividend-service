package com.example.dividend.web;

import com.example.dividend.model.Company;
import com.example.dividend.model.constants.CacheKey;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.repository.CompanyRepository;
import com.example.dividend.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    /**
     * 자동완성 기능을 위한 prefix 단어 검색 기능
     *
     * @param keyword 사용자가 입력한 검색어 prefix
     * @return 입력한 prefix로 시작하는 회사 이름 리스트를 포함한 응답 (HTTP 200 OK)
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        var result = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    /**
     * 회사 목록을 페이지 단위로 조회합니다.
     *
     * @param pageable 페이지 요청 정보 (페이지 번호, 크기 등)
     * @return 페이지네이션된 회사 정보 목록 (HTTP 200 OK)
     */
    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<Company> companies = companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보를 저장하고 결과를 반환합니다.
     *
     * @param request 저장할 회사 정보 (ticker 포함)
     * @return 저장된 회사 정보를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker);
        this.companyService.getCompanyNamesByKeyword(company.getName());
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFianceCompany(companyName);
        return ResponseEntity.ok(companyName);
    }

    public void clearFianceCompany(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }


}

