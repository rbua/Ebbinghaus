package ServiceJava.Database;

import ServiceJava.Parser.FullTranslation;
import ServiceJava.Parser.Synonyms;
import com.mysql.cj.exceptions.CJCommunicationsException;

import java.sql.*;
import java.util.*;

public class Database {

    Connection connectionToDatabase;
    private boolean connected = false;
    private boolean freeSpace = true;

    public boolean isConnected() {
        return connected;
    }

    static Database instance = null;

    public static Database getInstance() {
        if (instance == null)
            synchronized (Database.class) {
                if (instance == null)
                    instance = new Database();

            }
        return instance;
    }

    private Database() {
        // original String url = "jdbc:mysql://remotemysql.com:3306/FQSvAFRC1n?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String url = "jdbc:mysql://localhost:3306/dictionary?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        try {
            // original connectionToDatabase = DriverManager.getConnection(url, "FQSvAFRC1n", "rxtMdHq0UO");
            connectionToDatabase = DriverManager.getConnection(url, "root", "q1w2e3r4t5y6");
            if (!connectionToDatabase.isClosed()) connected = true;
        } catch (SQLException | CJCommunicationsException e) {
            connected = false;
            e.printStackTrace();
        }

    }


    public FullTranslation getSimpleWordTranslationById(int IDofWord) {
        if(!connected){
            FullTranslation fullTranslation=new FullTranslation("","");
            fullTranslation.setSuccessful(false);
            fullTranslation.setFromCache(false);
            return fullTranslation;
        }
        FullTranslation fullTranslation = null;
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT word,translated FROM EN_RU_word_translation WHERE translation_ID=?;")) {
            preparedStatementForEN_RU_word_translationSELECT.setInt(1, IDofWord);
            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            if (resultSet.first()) {
                fullTranslation = new FullTranslation(resultSet.getString(1), resultSet.getString(2));
                fullTranslation.setSuccessful(true);
                fullTranslation.setFromCache(true);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (fullTranslation != null) return fullTranslation;
            else {
                fullTranslation = new FullTranslation("", "");
                fullTranslation.setSuccessful(false);
                fullTranslation.setFromCache(false);
                return fullTranslation;
            }

        }
    }

    public FullTranslation getFullTranslationById(int IDofWord) {
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT word FROM EN_RU_word_translation WHERE translation_ID=?;")) {
            preparedStatementForEN_RU_word_translationSELECT.setInt(1, IDofWord);
            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            if (resultSet.first()) {
                resultSet.close();
                return getFullTranslation(resultSet.getString(1));
            } else {
                resultSet.close();
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FullTranslation getParametrizedTranslation(String toTranslateValue, boolean isSynonyms, boolean isSentencesENRU,boolean includeWordAudio, String whichWordAudio, String requestBy) {
        if(!connected){
            return null;
        }
        FullTranslation fullTranslation = null;

        try {


            String[] translationPair = null;
            switch (requestBy) {
                case "ID":
                    translationPair = getTranslationPairbyId(Integer.parseInt(toTranslateValue));
                    break;
                case "ENword":
                    translationPair = getTranslationPairbyToTranslate(toTranslateValue);
                    break;
                case "RUword":
                    translationPair = getTranslationPairbyTranslated(toTranslateValue);
                    break;
            }
            if (translationPair != null) {
                fullTranslation = new FullTranslation(translationPair[0], translationPair[1]);
                fullTranslation.setFromCache(true);
                fullTranslation.setSuccessful(true);
                if (isSynonyms) {
                    fullTranslation.setSynonyms(getSynonyms(translationPair[0]));
                }
                if (isSentencesENRU) {
                    fullTranslation.setSentencesInEnglishRussian(getSentences(translationPair[0]));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            return fullTranslation;
        }
    }

private String[][] getSentences(String toTranslate){
    if(!connected){
        return null;
    }
    try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM EN_RU_word_translation WHERE word=?;");
         PreparedStatement preparedStatementForSentencesSELECT = connectionToDatabase.prepareStatement("SELECT * FROM sentences WHERE sentences_ID=?;");
         PreparedStatement preparedStatementForTranslationIdALLkeySELECT = connectionToDatabase.prepareStatement("SELECT * FROM translation_ID_ALL_key WHERE translation_ID=?");) {
        preparedStatementForEN_RU_word_translationSELECT.setString(1, toTranslate);
        ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
        /**  IF NO SUCH WORD-> EXIT*/
        if (!resultSet.first()) return null;
        int translation_ID = resultSet.getInt(1);
        resultSet.close();
        /**  SELECT ALL ID*/
        preparedStatementForTranslationIdALLkeySELECT.setInt(1, translation_ID);
        ResultSet IdToSelectSET = preparedStatementForTranslationIdALLkeySELECT.executeQuery();

        final int SIZE_OF_SENTENCES_EN_RU = 40;
        String[][] sentencesInEnglishRussian = new String[2][];
        Set sentences_IDs = new HashSet();
        while (IdToSelectSET.next()) {
            if (IdToSelectSET.getInt(4) != 0) sentences_IDs.add(IdToSelectSET.getInt(4));
        }
        IdToSelectSET.close();
        /**  SELECT ALL SENTENCES*/
        Iterator iterator = sentences_IDs.iterator();
        String[][] localSentencesInENRU = new String[2][SIZE_OF_SENTENCES_EN_RU];
        int totalAmountOfSentences = 0;
        while (iterator.hasNext()) {
            int sentenceID = (int) iterator.next();
            preparedStatementForSentencesSELECT.setInt(1, sentenceID);
            resultSet = preparedStatementForSentencesSELECT.executeQuery();
            while (resultSet.next()) {
                localSentencesInENRU[0][totalAmountOfSentences] = resultSet.getString(2);
                localSentencesInENRU[1][totalAmountOfSentences] = resultSet.getString(3);
                totalAmountOfSentences++;
            }
            resultSet.close();
        }
        sentencesInEnglishRussian = new String[2][totalAmountOfSentences];
        for (int counter = 0; counter < totalAmountOfSentences; counter++) {
            sentencesInEnglishRussian[0][counter] = localSentencesInENRU[0][counter];
            sentencesInEnglishRussian[1][counter] = localSentencesInENRU[1][counter];
        }
return sentencesInEnglishRussian;


    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    } catch (NumberFormatException e) {
        e.printStackTrace();
        return null;
    }

}

    private Synonyms[] getSynonyms(String toTranslate) {
        if(!connected){
           return null;
        }
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM EN_RU_word_translation WHERE word=?;");
             PreparedStatement preparedStatementForWord_categories_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM word_categories_translation WHERE categories_ID=? ORDER BY word_category_id;");
             PreparedStatement preparedStatementForTranslationIdALLkeySELECT = connectionToDatabase.prepareStatement("SELECT * FROM translation_ID_ALL_key WHERE translation_ID=?");
             PreparedStatement preparedStatementForListOfWordCategoriesSELECT = connectionToDatabase.prepareStatement("SELECT word_category FROM list_of_word_categories WHERE word_category_id=?;")) {
            preparedStatementForEN_RU_word_translationSELECT.setString(1, toTranslate);

            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            resultSet.first();
            int translation_ID = resultSet.getInt(1);
            resultSet.close();
            /**  SELECT ALL ID*/
            preparedStatementForTranslationIdALLkeySELECT.setInt(1, translation_ID);
            ResultSet IdToSelectSET = preparedStatementForTranslationIdALLkeySELECT.executeQuery();
            Synonyms[] synonyms = null;
            String word = null;
            String wordCategory = null;

            Set categories_IDs = new HashSet();
            while (IdToSelectSET.next()) {
                if (IdToSelectSET.getInt(3) != 0) categories_IDs.add(IdToSelectSET.getInt(3));
            }
            IdToSelectSET.close();
            IdToSelectSET = null;
            /**  SELECT ALL WORD CATEGORIES*/
            Iterator iterator = categories_IDs.iterator();
            int counter = 0;
            HashMap<String, List<String>> mapWithSynonyms = new HashMap<>();
            while (iterator.hasNext()) {
                int categoryID = (int) iterator.next();
                preparedStatementForWord_categories_translationSELECT.setInt(1, categoryID);
                resultSet = preparedStatementForWord_categories_translationSELECT.executeQuery();
                List<String> listOfWords = new ArrayList<>();
                while (resultSet.next()) {
                    if (mapWithSynonyms.containsKey(resultSet.getInt(3) + resultSet.getString(2))) {
                        listOfWords = mapWithSynonyms.get(resultSet.getInt(3) + resultSet.getString(2));
                        listOfWords.add(resultSet.getString(4));
                    } else {
                        listOfWords.add(resultSet.getString(4));
                        mapWithSynonyms.put(resultSet.getInt(3) + resultSet.getString(2), listOfWords);
                    }


                }
                resultSet.close();
            }
            Set keySet = mapWithSynonyms.keySet();
            iterator = keySet.iterator();
            counter = 0;
            synonyms = new Synonyms[keySet.size()];
            while (iterator.hasNext()) {

                String key = (String) iterator.next();
                int IDwordCategory = Integer.parseInt(key.replaceAll("[a-z]", "").replace(" ", ""));
                word = key.replaceAll("[0-9]", "");
                preparedStatementForListOfWordCategoriesSELECT.setInt(1, IDwordCategory);
                resultSet = preparedStatementForListOfWordCategoriesSELECT.executeQuery();
                resultSet.first();
                List<String> listOfWords = mapWithSynonyms.get(key);
                Iterator innerIterator = listOfWords.iterator();
                String[] translations = new String[listOfWords.size()];
                int counterForTranslationsArray = 0;
                while (innerIterator.hasNext()) {
                    translations[counterForTranslationsArray] = (String) innerIterator.next();
                    counterForTranslationsArray++;
                }
                synonyms[counter] = new Synonyms(word, translations, resultSet.getString(1));
                counter++;
                resultSet.close();
            }
            return synonyms;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String[] getTranslationPairbyToTranslate(String toTranslate) {
        if(!connected){
            return null;
        }
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM EN_RU_word_translation WHERE word=?;");) {
            preparedStatementForEN_RU_word_translationSELECT.setString(1, toTranslate);
            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            resultSet.first();
            String[] result =new String[]{resultSet.getString(2), resultSet.getString(3)};
            resultSet.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            connected=false;
            return null;
        }
    }

    private String[] getTranslationPairbyTranslated(String translated) {
        if(!connected){
            return null;
        }
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM EN_RU_word_translation WHERE translated=?;");) {
            preparedStatementForEN_RU_word_translationSELECT.setString(1, translated);
            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            resultSet.first();
            String[] result =new String[]{resultSet.getString(2), resultSet.getString(3)};
            resultSet.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            connected=false;
            return null;
        }
    }

    private String[] getTranslationPairbyId(int translationID) {
        if(!connected){
            return null;
        }
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM EN_RU_word_translation WHERE translation_ID=?;");) {
            preparedStatementForEN_RU_word_translationSELECT.setInt(1, translationID);
            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            resultSet.first();
            String[] result =new String[]{resultSet.getString(2), resultSet.getString(3)};
            resultSet.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            connected=false;
            return null;
        }
    }

    public FullTranslation getFullTranslation(String wordToTranslate) {
        if(!connected){
            return null;
        }
        FullTranslation fullTranslation = null;
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM EN_RU_word_translation WHERE word=?;");
             PreparedStatement preparedStatementForWord_categories_translationSELECT = connectionToDatabase.prepareStatement("SELECT * FROM word_categories_translation WHERE categories_ID=? ORDER BY word_category_id;");
             PreparedStatement preparedStatementForSentencesSELECT = connectionToDatabase.prepareStatement("SELECT * FROM sentences WHERE sentences_ID=?;");
             PreparedStatement preparedStatementForAudioSentencesSELECT = connectionToDatabase.prepareStatement("SELECT * FROM audio_sentences WHERE audio_sentences_ID=?;");
             PreparedStatement preparedStatementForTranslationIdALLkeySELECT = connectionToDatabase.prepareStatement("SELECT * FROM translation_ID_ALL_key WHERE translation_ID=?");
             PreparedStatement preparedStatementForListOfWordCategoriesSELECT = connectionToDatabase.prepareStatement("SELECT word_category FROM list_of_word_categories WHERE word_category_id=?;")) {
            preparedStatementForEN_RU_word_translationSELECT.setString(1, wordToTranslate);
            ResultSet resultSet = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            /**  IF NO SUCH WORD-> EXIT*/
            if (!resultSet.first()) return null;
            fullTranslation = new FullTranslation(wordToTranslate, resultSet.getString(3));
            int translation_ID = resultSet.getInt(1);
            resultSet.close();
            /**  SELECT ALL ID*/
            preparedStatementForTranslationIdALLkeySELECT.setInt(1, translation_ID);
            ResultSet IdToSelectSET = preparedStatementForTranslationIdALLkeySELECT.executeQuery();
            Synonyms[] synonyms = null;
            String word = null;
            String wordCategory = null;
            final int SIZE_OF_SENTENCES_EN_RU = 40;
            String[][] sentencesInEnglishRussian = new String[2][];
            Set categories_IDs = new HashSet();
            Set sentences_IDs = new HashSet();
            Set audio_sentences_IDs = new HashSet();
            while (IdToSelectSET.next()) {
                if (IdToSelectSET.getInt(3) != 0) categories_IDs.add(IdToSelectSET.getInt(3));
                if (IdToSelectSET.getInt(4) != 0) sentences_IDs.add(IdToSelectSET.getInt(4));
                if (IdToSelectSET.getInt(5) != 0) audio_sentences_IDs.add(IdToSelectSET.getInt(5));
            }
            IdToSelectSET.close();
            IdToSelectSET = null;
            /**  SELECT ALL WORD CATEGORIES*/
            Iterator iterator = categories_IDs.iterator();
            int counter = 0;
            HashMap<String, List<String>> mapWithSynonyms = new HashMap<>();
            while (iterator.hasNext()) {
                int categoryID = (int) iterator.next();
                preparedStatementForWord_categories_translationSELECT.setInt(1, categoryID);
                resultSet = preparedStatementForWord_categories_translationSELECT.executeQuery();
                List<String> listOfWords = new ArrayList<>();
                while (resultSet.next()) {
                    if (mapWithSynonyms.containsKey(resultSet.getInt(3) + resultSet.getString(2))) {
                        listOfWords = mapWithSynonyms.get(resultSet.getInt(3) + resultSet.getString(2));
                        listOfWords.add(resultSet.getString(4));
                    } else {
                        listOfWords.add(resultSet.getString(4));
                        mapWithSynonyms.put(resultSet.getInt(3) + resultSet.getString(2), listOfWords);
                    }


                }
                resultSet.close();
            }
            Set keySet = mapWithSynonyms.keySet();
            iterator = keySet.iterator();
            counter = 0;
            synonyms = new Synonyms[keySet.size()];
            while (iterator.hasNext()) {

                String key = (String) iterator.next();
                int IDwordCategory = Integer.parseInt(key.replaceAll("[a-z]", "").replace(" ", ""));
                word = key.replaceAll("[0-9]", "");
                preparedStatementForListOfWordCategoriesSELECT.setInt(1, IDwordCategory);
                resultSet = preparedStatementForListOfWordCategoriesSELECT.executeQuery();
                resultSet.first();
                List<String> listOfWords = mapWithSynonyms.get(key);
                Iterator innerIterator = listOfWords.iterator();
                String[] translations = new String[listOfWords.size()];
                int counterForTranslationsArray = 0;
                while (innerIterator.hasNext()) {
                    translations[counterForTranslationsArray] = (String) innerIterator.next();
                    counterForTranslationsArray++;
                }
                synonyms[counter] = new Synonyms(word, translations, resultSet.getString(1));
                counter++;
                resultSet.close();
            }
            fullTranslation.setSynonyms(synonyms);

            /**  SELECT ALL SENTENCES*/
            iterator = sentences_IDs.iterator();
            String[][] localSentencesInENRU = new String[2][SIZE_OF_SENTENCES_EN_RU];
            int totalAmountOfSentences = 0;
            while (iterator.hasNext()) {
                int sentenceID = (int) iterator.next();
                preparedStatementForSentencesSELECT.setInt(1, sentenceID);
                resultSet = preparedStatementForSentencesSELECT.executeQuery();
                while (resultSet.next()) {
                    localSentencesInENRU[0][totalAmountOfSentences] = resultSet.getString(2);
                    localSentencesInENRU[1][totalAmountOfSentences] = resultSet.getString(3);
                    totalAmountOfSentences++;
                }
                resultSet.close();
            }
            sentencesInEnglishRussian = new String[2][totalAmountOfSentences];
            for (counter = 0; counter < totalAmountOfSentences; counter++) {
                sentencesInEnglishRussian[0][counter] = localSentencesInENRU[0][counter];
                sentencesInEnglishRussian[1][counter] = localSentencesInENRU[1][counter];
            }
            fullTranslation.setSentencesInEnglishRussian(sentencesInEnglishRussian);

            /**  SELECT ALL AUDIO SENTENCES*/
            iterator = audio_sentences_IDs.iterator();
            int sentenceENCounter = 0;
            int sentenceRUCounter = 0;
            int sentenceURLCounter = 0;
            int sentenceFILENAMECounter = 0;
            String[][] arrayForAudioSentences = null;
            if (iterator.hasNext())
                arrayForAudioSentences = new String[4][40];
            while (iterator.hasNext()) {
                int sentenceID = (int) iterator.next();
                preparedStatementForAudioSentencesSELECT.setInt(1, sentenceID);
                resultSet = preparedStatementForAudioSentencesSELECT.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getString(2) != null) {
                        arrayForAudioSentences[0][sentenceENCounter] = resultSet.getString(2);
                        sentenceENCounter++;
                    }
                    if (resultSet.getString(3) != null) {
                        arrayForAudioSentences[1][sentenceRUCounter] = resultSet.getString(3);
                        sentenceRUCounter++;
                    }
                    if (resultSet.getString(4) != null) {
                        arrayForAudioSentences[2][sentenceURLCounter] = resultSet.getString(4);
                        sentenceURLCounter++;
                    }
                    if (resultSet.getString(5) != null) {
                        arrayForAudioSentences[3][sentenceFILENAMECounter] = resultSet.getString(5);
                        sentenceFILENAMECounter++;
                    }
                }
                resultSet.close();
            }
            if ((sentenceENCounter > 0) || (sentenceRUCounter > 0) || (sentenceURLCounter > 0) || (sentenceFILENAMECounter > 0)) {
                String[] sentences_EN = new String[sentenceENCounter];
                for (counter = 0; counter < sentenceENCounter; counter++) {
                    sentences_EN[counter] = arrayForAudioSentences[0][counter];
                }
                String[] sentences_RU = new String[sentenceRUCounter];
                for (counter = 0; counter < sentenceRUCounter; counter++) {
                    sentences_RU[counter] = arrayForAudioSentences[1][counter];
                }
                String[] sentences_URL = new String[sentenceURLCounter];
                for (counter = 0; counter < sentenceURLCounter; counter++) {
                    sentences_URL[counter] = arrayForAudioSentences[2][counter];
                }
                String[] sentences_FILENAME = new String[sentenceFILENAMECounter];
                for (counter = 0; counter < sentenceFILENAMECounter; counter++) {
                    sentences_FILENAME[counter] = arrayForAudioSentences[3][counter];
                }
                fullTranslation.setSentencesToVoiceENtext(sentences_EN);
                fullTranslation.setSentencesToVoiceRUtext(sentences_RU);
                fullTranslation.setSentencesToVoiceENsoundURL(sentences_URL);
                fullTranslation.setSentencesToVoiceENsoundFILENAME(sentences_FILENAME);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        fullTranslation.setFromCache(true);
        fullTranslation.setSuccessful(true);
        return fullTranslation;
    }

    synchronized public boolean putAllFullTranslation(FullTranslation fullTranslation) {
        if (fullTranslation == null) return false;
        checkDatabaseSize();
        if (!freeSpace) return false;
        try (PreparedStatement preparedStatementForEN_RU_word_translationSELECT = connectionToDatabase.prepareStatement("SELECT translation_ID FROM EN_RU_word_translation WHERE word=?;");
             PreparedStatement preparedStatementForWord_categories_translationSELECTForCheck = connectionToDatabase.prepareStatement("SELECT categories_ID FROM word_categories_translation WHERE word=? AND translation=?");
             PreparedStatement preparedStatementForSentencesSELECTForCheck = connectionToDatabase.prepareStatement("SELECT sentences_ID FROM sentences WHERE sentence_EN=? AND sentence_RU=?");
             PreparedStatement preparedStatementForAudioSentencesSELECTForCheck = connectionToDatabase.prepareStatement("SELECT audio_sentences_ID FROM audio_sentences WHERE sentence_EN=?;");
             PreparedStatement preparedStatementForListOfWordCategoriesSELECT = connectionToDatabase.prepareStatement("SELECT word_category_id FROM list_of_word_categories WHERE word_category=?;");
             PreparedStatement preparedStatementForEN_RU_word_translationINSERT = connectionToDatabase.prepareStatement("INSERT INTO EN_RU_word_translation(word,translated,audio_US,audio_GB)VALUES(?,?,?,?);");
             PreparedStatement preparedStatementForWord_categories_translationINSERT = connectionToDatabase.prepareStatement("INSERT INTO word_categories_translation(word,word_category_id,translation)VALUES(?,?,?);");
             PreparedStatement preparedStatementForSentencesINSERT = connectionToDatabase.prepareStatement("INSERT INTO sentences(sentence_EN,sentence_RU) VALUES(?,?);");
             PreparedStatement preparedStatementForAudioSentencesINSERT = connectionToDatabase.prepareStatement("INSERT INTO audio_sentences(sentence_EN,sentence_RU,URL,file_name) VALUES(?,?,?,?);");
             PreparedStatement preparedStatementForTranslation_ID_ALL_keyINSERT = connectionToDatabase.prepareStatement("INSERT INTO translation_ID_ALL_key(translation_ID,categories_ID,sentences_ID,audio_sentences_ID) VALUES(?,?,?,?);");
             PreparedStatement preparedStatementForListOfWordCategoriesINSERT = connectionToDatabase.prepareStatement("INSERT INTO list_of_word_categories(word_category) VALUES(?);")) {
            String wordToTranslate = fullTranslation.getWordToTranslate();
            String translated = fullTranslation.getTranslatedWord();
            Synonyms[] synonyms = null;
            String[] sentencesToVoiceENtext = fullTranslation.getSentencesToVoiceENtext();
            String[] sentencesToVoiceRUtext = fullTranslation.getSentencesToVoiceRUtext();
            String[] sentencesToVoiceENsoundURL = fullTranslation.getSentencesToVoiceENsoundURL();
            String[] sentencesToVoiceENsoundFILENAME = fullTranslation.getSentencesToVoiceENsoundFILENAME();
            String[][] sentencesInEnglishRussian = fullTranslation.getSentencesInEnglishRussian();
            final int SIZE_OF_TOKENS = 40;
            int[] tokensForWord_categories_translation = new int[SIZE_OF_TOKENS];
            int[] tokensForSentences = new int[SIZE_OF_TOKENS];
            int[] tokensForAudioSentences = new int[SIZE_OF_TOKENS];
            /**  CHECK IF THE VALUE ALREADY EXISTS*/
            if (fullTranslation.getSynonyms() != null) synonyms = fullTranslation.getSynonyms();
            preparedStatementForEN_RU_word_translationSELECT.setString(1, wordToTranslate);
            ResultSet resultSetEN_RU_word_translation = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            if (!resultSetEN_RU_word_translation.first()) {//if already in database>EXIT
                /** INSERT EN_RU_words*/
                preparedStatementForEN_RU_word_translationINSERT.setString(1, wordToTranslate);
                preparedStatementForEN_RU_word_translationINSERT.setString(2, translated);
                preparedStatementForEN_RU_word_translationINSERT.setString(3, fullTranslation.getWordENAudioURLUS());
                preparedStatementForEN_RU_word_translationINSERT.setString(4, fullTranslation.getWordENAudioURLGB());
                preparedStatementForEN_RU_word_translationINSERT.executeUpdate();
            } else {
                return true;
            }
            int counter = 0;
            resultSetEN_RU_word_translation.close();
            resultSetEN_RU_word_translation = preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            resultSetEN_RU_word_translation.first();
            int keyValueForInsert = resultSetEN_RU_word_translation.getInt(1);
            resultSetEN_RU_word_translation.close();
            if (keyValueForInsert <= 0) return true;
            /**CHECK OTHER TABLE AND TRY TO INSERT OR IF ALREADY IN DATABASE >SELECT TOKEN*/

            /** INSERT SYNONYMS*/
            if (synonyms != null) {
                int word_category_id = 0;
                for (counter = 0; counter < synonyms.length; counter++) {
                    String wordInEnglish = synonyms[counter].getWordInEnglish();
                    String wordCategory = synonyms[counter].getWordCategory();
                    String[] translations = synonyms[counter].getTranslations();
                    if (wordInEnglish.equals("") || wordCategory.equals("")) continue;
                    for (int innerCounter = 0; innerCounter < translations.length; innerCounter++) {
                        if (translations[innerCounter].equals("")) continue;
                        preparedStatementForWord_categories_translationSELECTForCheck.setString(1, wordInEnglish);
                        preparedStatementForWord_categories_translationSELECTForCheck.setString(2, translations[innerCounter]);
                        if (preparedStatementForWord_categories_translationSELECTForCheck.executeQuery().first()) {
                            ResultSet resultSet = preparedStatementForWord_categories_translationSELECTForCheck.executeQuery();
                            resultSet.first();
                            for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                                if (tokensForWord_categories_translation[i] == resultSet.getInt(1)) break;
                                if (tokensForWord_categories_translation[i] == 0) {
                                    tokensForWord_categories_translation[i] = resultSet.getInt(1);
                                    break;
                                }
                            }
                            resultSet.close();
                            continue;
                        } else {
                            preparedStatementForWord_categories_translationINSERT.setString(1, wordInEnglish);
                            preparedStatementForListOfWordCategoriesSELECT.setString(1, wordCategory);
                            ResultSet resultSetForWordCategoryId = preparedStatementForListOfWordCategoriesSELECT.executeQuery();
                            if (!resultSetForWordCategoryId.first()) {
                                preparedStatementForListOfWordCategoriesINSERT.setString(1, wordCategory);
                                preparedStatementForListOfWordCategoriesINSERT.executeUpdate();
                                resultSetForWordCategoryId = preparedStatementForListOfWordCategoriesSELECT.executeQuery();
                                resultSetForWordCategoryId.first();

                            }
                            word_category_id = resultSetForWordCategoryId.getInt(1);
                            preparedStatementForWord_categories_translationINSERT.setInt(2, word_category_id);
                            preparedStatementForWord_categories_translationINSERT.setString(3, translations[innerCounter]);
                            preparedStatementForWord_categories_translationINSERT.executeUpdate();
                            ResultSet resultSet = preparedStatementForWord_categories_translationSELECTForCheck.executeQuery();
                            resultSet.first();
                            for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                                if (tokensForWord_categories_translation[i] == resultSet.getInt(1)) break;
                                if (tokensForWord_categories_translation[i] == 0) {
                                    tokensForWord_categories_translation[i] = resultSet.getInt(1);
                                    break;
                                }
                            }
                            resultSet.close();
                        }

                    }
                }
            }
            /** INSERT SENTENCES*/
            if (sentencesInEnglishRussian[0] != null && sentencesInEnglishRussian[1] != null) {
                for (counter = 0; counter < sentencesInEnglishRussian[0].length; counter++) {
                    preparedStatementForSentencesSELECTForCheck.setString(1, sentencesInEnglishRussian[0][counter]);
                    preparedStatementForSentencesSELECTForCheck.setString(2, sentencesInEnglishRussian[1][counter]);
                    if (preparedStatementForSentencesSELECTForCheck.executeQuery().first()) {//CHECK IF EXISTS
                        ResultSet resultSet = preparedStatementForSentencesSELECTForCheck.executeQuery();
                        resultSet.first();
                        for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                            if (tokensForSentences[i] == resultSet.getInt(1)) break;
                            if (tokensForSentences[i] == 0) {
                                tokensForSentences[i] = resultSet.getInt(1);
                                break;
                            }
                        }
                        resultSet.close();
                        continue;
                    } else {
                        preparedStatementForSentencesINSERT.setString(1, sentencesInEnglishRussian[0][counter]);
                        preparedStatementForSentencesINSERT.setString(2, sentencesInEnglishRussian[1][counter]);
                        preparedStatementForSentencesINSERT.executeUpdate();
                        ResultSet resultSet = preparedStatementForSentencesSELECTForCheck.executeQuery();
                        resultSet.first();
                        for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                            if (tokensForSentences[i] == resultSet.getInt(1)) break;
                            if (tokensForSentences[i] == 0) {
                                tokensForSentences[i] = resultSet.getInt(1);
                                break;
                            }
                        }
                        resultSet.close();

                    }

                }
            }
            /** INSERT AUDIO SENTENCES*/
            if (sentencesToVoiceENtext != null && (sentencesToVoiceENsoundURL != null || sentencesToVoiceENsoundFILENAME != null)) {
                for (counter = 0; counter < sentencesToVoiceENtext.length; counter++) {

                    preparedStatementForAudioSentencesSELECTForCheck.setString(1, sentencesToVoiceENtext[counter]);
                    if (preparedStatementForAudioSentencesSELECTForCheck.executeQuery().first()) {//CHECK IF EXISTS

                        ResultSet resultSet = preparedStatementForAudioSentencesSELECTForCheck.executeQuery();
                        resultSet.first();
                        for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                            if (tokensForAudioSentences[i] == resultSet.getInt(1)) break;
                            if (tokensForAudioSentences[i] == 0) {
                                tokensForAudioSentences[i] = resultSet.getInt(1);
                                break;
                            }
                        }
                        resultSet.close();
                        continue;

                    }
                    preparedStatementForAudioSentencesINSERT.setString(1, sentencesToVoiceENtext[counter]);
                    if (sentencesToVoiceRUtext != null)
                        if (counter < sentencesToVoiceRUtext.length) {
                            preparedStatementForAudioSentencesINSERT.setString(2, sentencesToVoiceRUtext[counter]);
                        } else preparedStatementForAudioSentencesINSERT.setNull(2, Types.VARCHAR);
                    if (sentencesToVoiceENsoundURL != null)
                        if (counter < sentencesToVoiceENsoundURL.length) {
                            preparedStatementForAudioSentencesINSERT.setString(3, sentencesToVoiceENsoundURL[counter]);
                        } else preparedStatementForAudioSentencesINSERT.setNull(3, Types.VARCHAR);
                    if (sentencesToVoiceENsoundFILENAME != null)
                        if (counter < sentencesToVoiceENsoundFILENAME.length) {
                            preparedStatementForAudioSentencesINSERT.setString(4, sentencesToVoiceENsoundFILENAME[counter]);
                        } else preparedStatementForAudioSentencesINSERT.setNull(4, Types.VARCHAR);
                    preparedStatementForAudioSentencesINSERT.executeUpdate();

                    ResultSet resultSet = preparedStatementForAudioSentencesSELECTForCheck.executeQuery();
                    resultSet.first();
                    for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                        if (tokensForAudioSentences[i] == resultSet.getInt(1)) break;
                        if (tokensForAudioSentences[i] == 0) {
                            tokensForAudioSentences[i] = resultSet.getInt(1);
                            break;
                        }
                    }
                    resultSet.close();
                }
            }
            /** INSERT INTO TOKENS TABLE*/
            for (counter = 0; counter < SIZE_OF_TOKENS; counter++) {
                if (tokensForWord_categories_translation[counter] == 0 && tokensForSentences[counter] == 0 && tokensForAudioSentences[counter] == 0)
                    break;
                preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(1, keyValueForInsert);
                if (tokensForWord_categories_translation[counter] != 0) {
                    preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(2, tokensForWord_categories_translation[counter]);
                } else preparedStatementForTranslation_ID_ALL_keyINSERT.setNull(2, Types.INTEGER);
                if (tokensForSentences[counter] != 0) {
                    preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(3, tokensForSentences[counter]);
                } else preparedStatementForTranslation_ID_ALL_keyINSERT.setNull(3, Types.INTEGER);
                if (tokensForAudioSentences[counter] != 0) {
                    preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(4, tokensForAudioSentences[counter]);
                } else preparedStatementForTranslation_ID_ALL_keyINSERT.setNull(4, Types.INTEGER);
                preparedStatementForTranslation_ID_ALL_keyINSERT.executeUpdate();
            }
        } catch (SQLException e) {
            connected = false;
            e.printStackTrace();
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return true;
    }

    synchronized private void checkDatabaseSize() {
        try (PreparedStatement preparedStatementForCheck = connectionToDatabase.prepareStatement("SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) \"Size\" FROM information_schema.tables;")) {
            ResultSet resultSet = preparedStatementForCheck.executeQuery();
            resultSet.first();
            if (resultSet.getFloat(1) > 95.0) {
                freeSpace = false;
            }
            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
            connected = false;
        }

    }
}
