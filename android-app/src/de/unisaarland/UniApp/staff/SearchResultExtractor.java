package de.unisaarland.UniApp.staff;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.UniApp.utils.ContentExtractor;

public class SearchResultExtractor implements ContentExtractor<List<SearchResult>> {

    private final String baseUrl;

    public SearchResultExtractor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public List<SearchResult> extract(InputStream data) throws ParseException, IOException {
        List<SearchResult> entries = new ArrayList<>();

        Document doc = Jsoup.parse(data, null, baseUrl);

        // first check whether we got the error that too few search terms were given.
        // in this case, return null to signal this.
        for (Element h1Elem : doc.getElementsByTag("h1"))
            if (h1Elem.text().trim().equals("Bitte geben Sie mehr Suchbegriffe ein"))
                return null;

        for (Element divElement : doc.getElementsByClass("erg_list_entry")) {
            Elements ergListLabelElements = divElement.getElementsByAttributeValueContaining("class", "erg_list_label");
            if (ergListLabelElements.isEmpty())
                continue;
            Element timeElement = ergListLabelElements.get(0);
            if (!timeElement.ownText().equals("Name:"))
                continue;
            Elements aElements = divElement.getElementsByTag("a");
            if (aElements.isEmpty())
                continue;
            Element nameElement = aElements.get(0);
            String rawName = nameElement.text();
            String[] nameArray = rawName.split(" ");
            // filter out all leading "Prof.", "Dr.", "rer." ...
            StringBuilder name = new StringBuilder();
            boolean titlePart = true;
            for (String namePart : nameArray) {
                if (!namePart.endsWith(".") &&
                        !namePart.endsWith(".-"))
                    titlePart = false;
                if (!titlePart)
                    name.append(" ").append(namePart);
            }
            String url = nameElement.attr("abs:href");

            entries.add(new SearchResult(name.toString(), url));
        }

        return entries;
    }
}
