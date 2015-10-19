package de.unisaarland.UniApp.staff;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import de.unisaarland.UniApp.utils.ContentExtractor;

public class StaffInfoParser implements ContentExtractor<StaffInfo> {

    private final String baseUrl;

    public StaffInfoParser(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public StaffInfo extract(InputStream data) throws ParseException, IOException {
        Document doc = Jsoup.parse(data, null, baseUrl);

        String name = null;
        String gender = null;
        String academicDegree = null;
        String building = null;
        String room = null;
        String phone = null;
        String fax = null;
        String email = null;

        Elements tableElements = doc.getElementsByTag("table");
        for (Element tableElement : tableElements) {
            if (tableElement.attr("summary").equals("Grunddaten zur Veranstaltung")) {
                Elements tdElements = tableElement.child(1).getElementsByTag("td");
                String lastName = tdElements.get(0).text();
                gender = tdElements.get(1).text();
                gender = gender.substring(0, 1).toUpperCase() + gender.substring(1);
                String firstName = tdElements.get(2).text();
                name = String.format("%s %s", firstName, lastName);
                academicDegree = tdElements.get(8).text();
            }
            if (tableElement.attr("summary").equals("Angaben zur Dienstadresse")) {
                Elements tdElements = tableElement.child(0).getElementsByTag("td");
                phone = tdElements.get(1).text();
                fax = tdElements.get(3).text();
                email = tdElements.get(5).tagName("a").text();
                room = tdElements.get(6).tagName("a").text();
                String a = tdElements.get(7).text();
                building = tdElements.get(8).tagName("a").text();
                String[] tempArray = building.split(" ");
                if (tempArray.length > 1) {
                    if (tempArray.length == 3) {
                        building = tempArray[1] + tempArray[2];
                    } else {
                        building = tempArray[1];
                    }
                }
            }
        }

        return new StaffInfo(name, gender, academicDegree, building, room, phone, fax, email);

    }
}
