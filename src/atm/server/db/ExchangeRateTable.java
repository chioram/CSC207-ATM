package atm.server.db;

import atm.server.db.scraper_tools.ExchangeRateScraper;

import java.util.TreeMap;

public class ExchangeRateTable {

    private TreeMap<String, Double> exchangeRates;
    private ExchangeRateScraper scraper = new ExchangeRateScraper();

    public void load() {

        try {
            exchangeRates = scraper.updateExchangeRates();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TreeMap<String, Double> getAllRates() {
        return exchangeRates;
    }

    public Double getSingleRate(String currency) {
        return exchangeRates.get(currency);
    }

}
