package ServiceJava.Parser;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * This class is used for getting translation from English to Russian
 * by calling the method getTranslation(String totranslate)
 * and passing word to traslate into it.
 * Returning String with translation or null if failed.
 */

public class Parser {
    public FullTranslation getFullTranslation(String totranslate) {
        String url = "https://www.babla.ru/английский-русский/" + totranslate;
        FullTranslation fullTranslation;
        try {//just selector for parse
            Document document = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").maxBodySize(0).timeout(0).get();
       //deprecated     fullTranslation = new FullTranslation(totranslate, document.select("ul.sense-group-results li a").first().text());
            if(!simpleTranslationOfEverything(totranslate).matches(".*\\p{InCyrillic}.*")){
                fullTranslation=new FullTranslation("","");
                fullTranslation.setSuccessful(false);
                return fullTranslation;
            }
            fullTranslation = new FullTranslation(totranslate, simpleTranslationOfEverything(totranslate));
            fullTranslation.setSynonyms(getSynonymsFromDiv(document.getAllElements()));
            fullTranslation.setSentencesInEnglishRussian(getSentencesInEnglishRussian(document.getAllElements()));
        } catch (IOException e) {
            fullTranslation=new FullTranslation("","");
            fullTranslation.setSuccessful(false);
            return fullTranslation;
        } catch (NullPointerException e) {
            fullTranslation=new FullTranslation("","");
            fullTranslation.setSuccessful(false);
            return fullTranslation;
        }
        if (fullTranslation.getTranslatedWord() == null || fullTranslation.getTranslatedWord().equals(""))
        {
            fullTranslation=new FullTranslation("","");
            fullTranslation.setSuccessful(false);
            return fullTranslation;
        }
        else{
            fullTranslation.setSuccessful(true);
            fullTranslation.setFromCache(false);
            return fullTranslation;
        }

    }

    private String[][] getSentencesInEnglishRussian(Elements elements) {
        Elements localElements = elements.select("div.sense-group:eq(2)>div.dict-entry>div.dict-example");
        int ammountOfElements = localElements.size();
        String[][] result = new String[2][ammountOfElements];
        for (int counter = 0; counter < ammountOfElements; counter++) {
            if (localElements.get(counter).select("div.dict-source").first().children().size() > 0)
                result[0][counter] = localElements.get(counter).select("div.dict-source").first().text().substring(localElements.get(counter).select("div.dict-source>" + localElements.get(counter).select("div.dict-source").first().children().first().tag()).first().text().length()).replaceAll("[\"]","");
            else result[0][counter] = localElements.get(counter).select("div.dict-source").first().text().replaceAll("[\"]","");

            if (localElements.get(counter).select("div.dict-result").first().children().size() > 0)
                result[1][counter] = localElements.get(counter).select("div.dict-result").first().text().substring(localElements.get(counter).select("div.dict-result>" + localElements.get(counter).select("div.dict-result").first().children().first().tag()).first().text().length()).replaceAll("[\"]","");
            else result[1][counter] = localElements.get(counter).select("div.dict-result").first().text().replaceAll("[\"]","");
        }

        return result;
    }

    private Synonyms[] getSynonymsFromDiv(Elements elements) {
        Elements localElements1 = elements.select("div.quick-results").first().children();
        Elements localElements2 = localElements1.clone();
        int ammountOfElements = localElements2.size() - 2;//here is -2 because on the site first/last elements have the same parameters as the other but they have only additional information
        Synonyms[] synonyms = new Synonyms[ammountOfElements];
        String wordInEnglish;
        String category;
        for (int outerCounter = 0; outerCounter < ammountOfElements; outerCounter++) {
            wordInEnglish = localElements1.select("div.quick-result-entry > div.quick-result-option > a[href=#translationsdetails" + String.valueOf(outerCounter + 1) + "]").text();
            category = localElements1.select("div.quick-result-entry > div.quick-result-option > a[href=#translationsdetails" + String.valueOf(outerCounter + 1) + "]~span.suffix").text().replaceAll("[-+.^:,{\"}]","").replaceAll("\\[", "").replaceAll("\\]","");
            if (category.equals("")) {
                return null;
            }
            localElements2 = localElements1.get(outerCounter + 1).select("div.quick-result-overview>ul>li");
            String translations[] = new String[localElements2.size()];
            for (int innerCounter = 0; innerCounter < localElements2.size(); innerCounter++) {
                translations[innerCounter] = localElements2.select("li:eq(" + innerCounter + ")>a").first().text();

            }
            synonyms[outerCounter] = new Synonyms(wordInEnglish, translations, category);
        }
        return synonyms;
    }
public String simpleTranslationOfEverything(String toTranslate) throws IOException {
   String url="https://translate.google.sn/translate_a/t?client=dict-chrome-ex&sl=en&tl=ru&q="+toTranslate.replaceAll(" ","%20")+"&tbb=1&ie=UTF-8&oe=UTF-8";
    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
    connection.connect();

    BufferedReader r  = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = r.readLine()) != null) {
        sb.append(line);
    }
    JSONObject json=new JSONObject(sb.toString());
    HashMap hashMap= (HashMap) json.getJSONArray("sentences").toList().iterator().next();
        return (String) hashMap.get("trans");
    }
    public boolean isTranslationExists(String totranslate) {
        String url = "https://www.babla.ru/английский-русский/" + totranslate;
        String translated = null;

        try {//just selector for parse
            translated = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").maxBodySize(0).timeout(0).get().select("ul.sense-group-results li a").first().text();

        } catch (IOException e) {
            return false;
        } catch (NullPointerException e) {
            System.out.println("null");
            return false;
        }
        if (translated == null || translated == "")
            return false;
        else
            return true;//translation exists
    }

    public String oldgetTranslation(String totranslate) {
        String url = "https://dictionary.cambridge.org/ru/словарь/английский/" + totranslate;
        String translated = null;

        try {//just selector for parse
            translated = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").maxBodySize(0).timeout(0).get().select("a[title=" + totranslate + ": перевод на русский] p.flush").last().text();

        } catch (IOException e) {
            return "Слово не найдено";
        } catch (NullPointerException e) {
            System.out.println("null");
            return "Слово не найдено";
        }
        if (translated == null || translated == "")
            return "Слово не найдено";
        else
            return translated.split(",")[0];//to get back only the first word
    }

    public String getAllSynonymsInRussian(String totranslate) {
        String url = "https://dictionary.cambridge.org/ru/словарь/английский/" + totranslate;
        String translated = null;
        System.setProperty("javax.net.ssl.trustStore", "/path/to/web2.uconn.edu.jks");


        try {
            translated = Jsoup.connect(url).get().select("p.flush").text();

        } catch (IOException e) {
            return "Синонимы не найдены";
        }
        if (translated == null || translated == "")
            return "Синонимы не найдены";
        else
            return translated;
    }
}
