package com.example.dividend.scraper;

import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class YahooFinanceScraper implements Scraper {
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?frequency=1mo&period1=%d&period2=%d";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400;   // 60 * 60 * 24

    @Override
    public ScrapedResult scrap(Company company) {
        ScrapedResult scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;   // ms를 초단위로 변경

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            System.out.println("URL: " + url);
            String url2 = String.format("https://finance.yahoo.com/quote/COKE/history/?frequency=1mo&period1=86400&period2=1744123317");
            Connection connection = Jsoup.connect(url2.toString());
            Document document = connection.get();

            Elements parsingDivs = document.select("table.table.yf-1jecxey.noDl.hideOnPrint");
            Element tableEle = parsingDivs.get(0);  // table 전체
            System.out.println(tableEle);

            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(Dividend.builder()
                    .date(LocalDateTime.of(year, month, day, 0, 0))
                    .dividend(dividend)
                    .build());
            }

            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();

            Element titleElement = document.getElementsByTag("h1").get(1);
            String title = titleElement.text().split("\\(")[0].trim();

            return Company.builder()
                .ticker(ticker)
                .name(title)
                .build();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
