package atm.server.db.scraper_tools;

import java.util.ArrayList;
import java.util.TreeMap;

import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

public class ExchangeRateScraper {

    private TreeMap<String, Double> exchangeRatesMap = new TreeMap<>(); // All rates are set against CAD
    private ArrayList<Double> exchangeRates = new ArrayList<>();

    public TreeMap<String, Double> updateExchangeRates() throws Exception {

        exchangeRatesMap.clear();

        Document doc = Jsoup.connect("https://www.exchange-rates.org/MajorRates.aspx").get();
        double rate;

        for (Element row : doc.select("table.table.table-hover.table-exchangeX.table-major-rates.table-striped.table-fixedX.allow-stacktable")) {

            String base_currency_row = row.select("td:eq(6)").text();
            String[] base_currency_row_array = base_currency_row.split(" ");

            for (String s : base_currency_row_array) {
                this.exchangeRates.add(Double.valueOf(s));
            }

            int i = 0;

            for (Element e : row.select("td")) {
                String country_code = e.select("a").attr("title");
                if (!country_code.trim().isEmpty()) {
                    exchangeRatesMap.put(country_code, exchangeRates.get(i));
                    i++;
                }
            }
        }
        return exchangeRatesMap;
    }

}


