package com.example.dividend.scheduler;

import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.model.constants.CacheKey;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.persist.repository.CompanyRepository;
import com.example.dividend.persist.repository.DividendRepository;
import com.example.dividend.scraper.Scraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@EnableCaching
@RequiredArgsConstructor
public class ScraperScheduler {

    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;


    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        List<CompanyEntity> companies = companyRepository.findAll();

        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = yahooFinanceScraper.scrap(
                Company.builder()
                    .ticker(company.getTicker())
                    .name(company.getName())
                    .build()
            );

            saveNewDividends(scrapedResult.getDividends(), company.getId());

            try {
                Thread.sleep(3000); // 서버 부하 방지
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void saveNewDividends(List<Dividend> dividends, Long companyId) {
        for (Dividend dividend : dividends) {
            DividendEntity entity = DividendEntity.from(dividend, companyId);

            boolean exists = dividendRepository.existsByCompanyIdAndDate(entity.getCompanyId(), entity.getDate());
            if (!exists) {
                dividendRepository.save(entity);
            }
        }
    }

//스케줄링 테스트
    /*
    @Scheduled(fixedRate = 1000)
    public void test() throws InterruptedException {
        Thread.sleep(10000); // 10초간 정지
        System.out.println("테스트 1 : " + LocalDate.now());
    }

    @Scheduled(fixedRate = 1000)
    public void tes2t() throws InterruptedException {
        // 정지X
        System.out.println("테스트 2 : " + LocalDate.now());
    }
 */

}
