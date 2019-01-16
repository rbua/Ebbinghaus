package ServiceJava.Parser;

public class FullTranslation {
    private String wordToTranslate;
    private String  translatedWord;
    private Synonyms[] synonyms;
    private String[][] sentencesInEnglishRussian=null;
    private String[] sentencesToVoiceENtext=null;
    private String[] sentencesToVoiceRUtext=null;
    private String[] sentencesToVoiceENsoundURL=null;
    private String[] sentencesToVoiceENsoundFILENAME=null;

    public String[] getSentencesToVoiceRUtext() {
        return sentencesToVoiceRUtext;
    }

    public String[] getSentencesToVoiceENsoundFILENAME() {
        return sentencesToVoiceENsoundFILENAME;
    }

    public void setSentencesToVoiceENsoundFILENAME(String[] sentencesToVoiceENsoundFILENAME) {
        this.sentencesToVoiceENsoundFILENAME = sentencesToVoiceENsoundFILENAME;
    }

    public void setSentencesToVoiceRUtext(String[] sentencesToVoiceRUtext) {
        this.sentencesToVoiceRUtext = sentencesToVoiceRUtext;
    }

    FullTranslation(String wordToTranslate, String  translatedWord){
        this.wordToTranslate=wordToTranslate;
        this.translatedWord=translatedWord;
    }

    public void setWordToTranslate(String wordToTranslate) {
        this.wordToTranslate = wordToTranslate;
    }

    public void setTranslatedWord(String translatedWord) {
        this.translatedWord = translatedWord;
    }

    public String[] getSentencesToVoiceENtext() {
        return sentencesToVoiceENtext;
    }

    public void setSentencesToVoiceENtext(String[] sentencesToVoiceENtext) {
        this.sentencesToVoiceENtext = sentencesToVoiceENtext;
    }

    public String[] getSentencesToVoiceENsoundURL() {
        return sentencesToVoiceENsoundURL;
    }

    public void setSentencesToVoiceENsoundURL(String[] sentencesToVoiceENsoundURL) {
        this.sentencesToVoiceENsoundURL = sentencesToVoiceENsoundURL;
    }

    public Synonyms[] getSynonyms() {
        return synonyms;
    }

    public String[][] getSentencesInEnglishRussian() {
        return sentencesInEnglishRussian;
    }

    public void setSentencesInEnglishRussian(String[][] sentencesInEnglishRussian) {
        this.sentencesInEnglishRussian = sentencesInEnglishRussian;
    }

    public void setSynonyms(Synonyms[] synonyms) {
        this.synonyms = synonyms;
    }

    public String getWordToTranslate() {
        return wordToTranslate;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }
}