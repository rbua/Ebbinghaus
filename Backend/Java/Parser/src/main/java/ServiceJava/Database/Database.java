package ServiceJava.Database;

import ServiceJava.Parser.FullTranslation;
import ServiceJava.Parser.Synonyms;

import javax.xml.crypto.Data;
import java.sql.*;
/** MAKE TABLE FOR WORD CATEGORIES ESPECIALLY*/
public class Database {
    /**put get *
     *
     */
    Connection connectionToDatabase;
    public Database() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/dictionary"+
                    "?verifyServerCertificate=false"+
                    "&useSSL=true"+
                    "&requireSSL=true"+
                    "&useLegacyDatetimeCode=false"+
                    "&amp"+
                    "&serverTimezone=UTC";
            connectionToDatabase= DriverManager.getConnection(url,"root","q1w2e3r4t5y6");
    }

    synchronized public boolean putAllFullTranslation(FullTranslation fullTranslation){
        if(fullTranslation==null)return false;
        try(PreparedStatement preparedStatementForEN_RU_word_translationSELECT=connectionToDatabase.prepareStatement("SELECT translation_ID FROM EN_RU_word_translation WHERE word=?;");
            PreparedStatement preparedStatementForWord_categories_translationSELECTForCheck=connectionToDatabase.prepareStatement("SELECT categories_ID FROM word_categories_translation WHERE word=? AND translation=?");
            PreparedStatement preparedStatementForSentencesSELECTForCheck=connectionToDatabase.prepareStatement("SELECT sentences_ID FROM sentences WHERE sentence_EN=? AND sentence_RU=?");
            PreparedStatement preparedStatementForAudioSentencesSELECTForCheck=connectionToDatabase.prepareStatement("SELECT audio_sentences_ID FROM audio_sentences WHERE sentence_EN=?;");
            PreparedStatement preparedStatementForListOfWordCategoriesSELECT=connectionToDatabase.prepareStatement("SELECT word_category_id FROM list_of_word_categories WHERE word_category=?;");
            PreparedStatement preparedStatementForEN_RU_word_translationINSERT=connectionToDatabase.prepareStatement("INSERT INTO EN_RU_word_translation(word,translated)VALUES(?,?);");
            PreparedStatement preparedStatementForWord_categories_translationINSERT=connectionToDatabase.prepareStatement("INSERT INTO word_categories_translation(word,word_category_id,translation)VALUES(?,?,?);");
            PreparedStatement preparedStatementForSentencesINSERT=connectionToDatabase.prepareStatement("INSERT INTO sentences(sentence_EN,sentence_RU) VALUES(?,?);");
            PreparedStatement preparedStatementForAudioSentencesINSERT=connectionToDatabase.prepareStatement("INSERT INTO audio_sentences(sentence_EN,sentence_RU,URL,file_name) VALUES(?,?,?,?);");
            PreparedStatement preparedStatementForTranslation_ID_ALL_keyINSERT=connectionToDatabase.prepareStatement("INSERT INTO translation_ID_ALL_key(translation_ID,categories_ID,sentences_ID,audio_sentences_ID) VALUES(?,?,?,?);");
            PreparedStatement preparedStatementForListOfWordCategoriesINSERT=connectionToDatabase.prepareStatement("INSERT INTO list_of_word_categories(word_category) VALUES(?);")) {
            String wordToTranslate=fullTranslation.getWordToTranslate();
            String translated = fullTranslation.getTranslatedWord();
            Synonyms[] synonyms=null;
            String[] sentencesToVoiceENtext=fullTranslation.getSentencesToVoiceENtext();
            String[] sentencesToVoiceRUtext=fullTranslation.getSentencesToVoiceRUtext();
            String[] sentencesToVoiceENsoundURL=fullTranslation.getSentencesToVoiceENsoundURL();
            String[] sentencesToVoiceENsoundFILENAME=fullTranslation.getSentencesToVoiceENsoundFILENAME();
            String[][] sentencesInEnglishRussian=fullTranslation.getSentencesInEnglishRussian();
            final int SIZE_OF_TOKENS=40;
            int[] tokensForWord_categories_translation =new int[SIZE_OF_TOKENS];
            int[] tokensForSentences = new int[SIZE_OF_TOKENS];
            int[] tokensForAudioSentences=new int[SIZE_OF_TOKENS];
            /**  CHECK IF THE VALUE ALREADY EXISTS*/
            if (fullTranslation.getSynonyms() != null) synonyms=fullTranslation.getSynonyms();
            preparedStatementForEN_RU_word_translationSELECT.setString(1,wordToTranslate);
            ResultSet resultSetEN_RU_word_translation=preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            if(!resultSetEN_RU_word_translation.first()){//if already in database>EXIT
                /** INSERT EN_RU_words*/
                preparedStatementForEN_RU_word_translationINSERT.setString(1,wordToTranslate);
                preparedStatementForEN_RU_word_translationINSERT.setString(2,translated);
                preparedStatementForEN_RU_word_translationINSERT.executeUpdate();
            }
           else{
               return true;
            }
            int counter=0;
            resultSetEN_RU_word_translation=preparedStatementForEN_RU_word_translationSELECT.executeQuery();
            resultSetEN_RU_word_translation.first();
            int keyValueForInsert=resultSetEN_RU_word_translation.getInt(1);
            if(keyValueForInsert<=0)return true;
            /**CHECK OTHER TABLE AND TRY TO INSERT OR IF ALREADY IN DATABASE >SELECT TOKEN*/

            /** INSERT SYNONYMS*/
            if(synonyms!=null) {
                int word_category_id=0;
                for (counter = 0; counter < synonyms.length; counter++) {
                    String wordInEnglish=synonyms[counter].getWordInEnglish();
                    String wordCategory=synonyms[counter].getWordCategory();
                    String[] translations=synonyms[counter].getTranslations();
                    if(wordInEnglish.equals("")||wordCategory.equals(""))continue;
                    for(int innerCounter=0;innerCounter<translations.length;innerCounter++){
                        if(translations[innerCounter].equals(""))continue;
                        preparedStatementForWord_categories_translationSELECTForCheck.setString(1,wordInEnglish);
                        preparedStatementForWord_categories_translationSELECTForCheck.setString(2,translations[innerCounter]);
                        if(preparedStatementForWord_categories_translationSELECTForCheck.executeQuery().first()) {
                            ResultSet resultSet = preparedStatementForWord_categories_translationSELECTForCheck.executeQuery();
                            resultSet.first();
                            for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                                if (tokensForWord_categories_translation[i] == resultSet.getInt(1)) break;
                                if (tokensForWord_categories_translation[i] == 0) {
                                    tokensForWord_categories_translation[i] = resultSet.getInt(1);
                                    break;
                                }
                            }
                            continue;
                        }else{
                            preparedStatementForWord_categories_translationINSERT.setString(1,wordInEnglish);
                            preparedStatementForListOfWordCategoriesSELECT.setString(1,wordCategory);
                            ResultSet resultSetForWordCategoryId=preparedStatementForListOfWordCategoriesSELECT.executeQuery();
                            if(!resultSetForWordCategoryId.first()){
                                preparedStatementForListOfWordCategoriesINSERT.setString(1,wordCategory);
                                preparedStatementForListOfWordCategoriesINSERT.executeUpdate();
                                resultSetForWordCategoryId = preparedStatementForListOfWordCategoriesSELECT.executeQuery();
                                resultSetForWordCategoryId.first();

                            }
                            word_category_id=resultSetForWordCategoryId.getInt(1);
                            preparedStatementForWord_categories_translationINSERT.setInt(2,word_category_id);
                            preparedStatementForWord_categories_translationINSERT.setString(3,translations[innerCounter]);
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
                        }

                    }
                }
            }
            /** INSERT SENTENCES*/
            if(sentencesInEnglishRussian[0]!=null&&sentencesInEnglishRussian[1]!=null) {
                for (counter = 0; counter < sentencesInEnglishRussian[0].length;counter++){
                    preparedStatementForSentencesSELECTForCheck.setString(1,sentencesInEnglishRussian[0][counter]);
                    preparedStatementForSentencesSELECTForCheck.setString(2,sentencesInEnglishRussian[1][counter]);
                if(preparedStatementForSentencesSELECTForCheck.executeQuery().first()){//CHECK IF EXISTS
                    ResultSet resultSet = preparedStatementForSentencesSELECTForCheck.executeQuery();
                    resultSet.first();
                    for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                        if (tokensForSentences[i] == resultSet.getInt(1)) break;
                        if (tokensForSentences[i] == 0) {
                            tokensForSentences[i] = resultSet.getInt(1);
                            break;
                        }
                    }
                    continue;
                }else {
                    preparedStatementForSentencesINSERT.setString(1,sentencesInEnglishRussian[0][counter]);
                    preparedStatementForSentencesINSERT.setString(2,sentencesInEnglishRussian[1][counter]);
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

                }

                }
            }
            /** INSERT AUDIO SENTENCES*/
            if(sentencesToVoiceENtext!=null&&(sentencesToVoiceENsoundURL!=null||sentencesToVoiceENsoundFILENAME!=null)){
            for (counter=0;counter<sentencesToVoiceENtext.length;counter++){

                preparedStatementForAudioSentencesSELECTForCheck.setString(1,sentencesToVoiceENtext[counter]);
                if (preparedStatementForAudioSentencesSELECTForCheck.executeQuery().first()){//CHECK IF EXISTS

                        ResultSet resultSet = preparedStatementForAudioSentencesSELECTForCheck.executeQuery();
                        resultSet.first();
                        for (int i = 0; i < SIZE_OF_TOKENS; i++) {
                            if (tokensForAudioSentences[i] == resultSet.getInt(1)) break;
                            if (tokensForAudioSentences[i] == 0) {
                                tokensForAudioSentences[i] = resultSet.getInt(1);
                                break;
                            }
                        }
                        continue;

                }
                preparedStatementForAudioSentencesINSERT.setString(1,sentencesToVoiceENtext[counter]);
                if(sentencesToVoiceRUtext!=null)
                if(counter<sentencesToVoiceRUtext.length){
                    preparedStatementForAudioSentencesINSERT.setString(2,sentencesToVoiceRUtext[counter]);
                }
                else preparedStatementForAudioSentencesINSERT.setNull(2,Types.VARCHAR);
                if(sentencesToVoiceENsoundURL!=null)
                if(counter<sentencesToVoiceENsoundURL.length){
                    preparedStatementForAudioSentencesINSERT.setString(3,sentencesToVoiceENsoundURL[counter]);
                }
                else preparedStatementForAudioSentencesINSERT.setNull(3,Types.VARCHAR);
                if(sentencesToVoiceENsoundFILENAME!=null)
                if(counter<sentencesToVoiceENsoundFILENAME.length){
                    preparedStatementForAudioSentencesINSERT.setString(4,sentencesToVoiceENsoundFILENAME[counter]);
                }
                else preparedStatementForAudioSentencesINSERT.setNull(4,Types.VARCHAR);
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
            }
            }
            /** INSERT INTO TOKENS TABLE*/
            for(counter=0;counter<SIZE_OF_TOKENS;counter++){
                if(tokensForWord_categories_translation[counter]==0&&tokensForSentences[counter]==0&&tokensForAudioSentences[counter]==0)break;
                preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(1,keyValueForInsert);
                if (tokensForWord_categories_translation[counter]!=0){
                    preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(2,tokensForWord_categories_translation[counter]);
                }else preparedStatementForTranslation_ID_ALL_keyINSERT.setNull(2,Types.INTEGER);
                if(tokensForSentences[counter]!=0){
                    preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(3,tokensForSentences[counter]);
                }else preparedStatementForTranslation_ID_ALL_keyINSERT.setNull(3,Types.INTEGER);
                if(tokensForAudioSentences[counter]!=0){
                    preparedStatementForTranslation_ID_ALL_keyINSERT.setInt(4,tokensForAudioSentences[counter]);
                }else preparedStatementForTranslation_ID_ALL_keyINSERT.setNull(4,Types.INTEGER);
                preparedStatementForTranslation_ID_ALL_keyINSERT.executeUpdate();
            }
        } catch (DataTruncation e){
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }

    return true;
    }

}
