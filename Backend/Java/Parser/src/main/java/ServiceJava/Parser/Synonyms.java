package ServiceJava.Parser;

public class Synonyms {
    private String wordInEnglish=null;

    public String getWordInEnglish() {
        return wordInEnglish;
    }

    public void setWordInEnglish(String wordInEnglish) {
        this.wordInEnglish = wordInEnglish;
    }

    public String[] getTranslations() {
        return translations;
    }

    public void setTranslations(String[] translations) {
        this.translations = translations;
    }

    public String getWordCategory() {
        return wordCategory;
    }

    public void setWordCategory(String wordCategory) {
        this.wordCategory = wordCategory;
    }

    private String[] translations=null;
    private String wordCategory=null;
    Synonyms(String wordInEnglish, String[] translations,String wordCategory){
        this.wordInEnglish=wordInEnglish;
        this.translations=translations;
        this.wordCategory=wordCategory;
    }
}
