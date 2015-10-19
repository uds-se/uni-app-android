package de.unisaarland.UniApp.rssViews.model;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import de.unisaarland.UniApp.utils.ContentExtractor;

public class RSSArticleExtractor implements ContentExtractor<RSSArticle> {

    private final String baseUrl;

    public RSSArticleExtractor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public RSSArticle extract(InputStream data) throws ParseException, IOException {
        Document doc = Jsoup.parse(data, null, baseUrl);

        String baseHref = baseUrl;
        String date = null;
        String heading = null;
        String subTitle = null;
        String body = null;

        Elements headEls = doc.getElementsByTag("head");
        if (headEls.size() > 0) {
            Elements baseEls = headEls.get(0).getElementsByTag("base");
            if (baseEls.hasAttr("href"))
                baseHref = baseEls.attr("href");
        }

        Elements elements = doc.getElementsByTag("div");
        for (Element ele : elements) {
            if (!ele.className().equals("news-single-item"))
                continue;
            Elements e1 = ele.getElementsByAttributeValueContaining("class", "news-single-rightbox");
            if (e1.size() > 0)
                date = e1.text();
            Elements titleElements = ele.getElementsByTag("h1");
            if (titleElements.size() > 0)
                heading = titleElements.text();
            Elements subTitleElements = ele.getElementsByAttributeValueContaining("class", "newsheader2");
            if (subTitleElements.size() > 0)
                subTitle = subTitleElements.text();
            Elements textElements = ele.getElementsByAttributeValueContaining("class", "newscontent");
            if (textElements.size() > 0)
                body = textElements.html();
            Elements imgElements = ele.getElementsByAttributeValueContaining("class", "news-single-img");
            if (imgElements.size() > 0)
                body = imgElements.html() + body;
            break;
        }

        if (date == null || heading == null || body == null)
            throw new ParseException("Received document is incomplete or has unexpected format", 0);

        return new RSSArticle(baseHref, date, heading, subTitle, body);
    }

}
