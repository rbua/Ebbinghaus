package ServiceJava;

import ServiceJava.Database.Database;
import ServiceJava.Parser.FullTranslation;
import ServiceJava.Parser.Parser;

import java.io.*;
import java.sql.SQLException;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException {

        ConnectByHttp.start(8080);
        Thread.currentThread().setPriority(2);
        //BufferedReader fileReader = new BufferedReader(new FileReader("C:/20k.txt"));
        //PrintWriter writeToFile = new PrintWriter("C:/test.txt", "UTF-8");
        Parser parser = new Parser();
        Map<String, List<String>> query_pairs = null;
        Database database = new Database();
        //   database.putAllFullTranslation(parser.getFullTranslation("go"));
        int request = 1;
        while (true) {
            query_pairs = ConnectByHttp.getBlockingQueueWithQueryPairs().take();
            String toTranslate = query_pairs.get("word").get(0);
            String type = query_pairs.get("reqtype").get(0);
            System.out.println(toTranslate + "  request№  " + request++);
            FullTranslation fullTranslation = null;
            switch (type) {
                case "getFullTranslation":
                    if (database.isConnected())
                        fullTranslation = database.getFullTranslation(toTranslate);
                    if (fullTranslation != null) {
                        ConnectByHttp.setBlockingQueueWithResponse_FullTranslation(fullTranslation);
                    } else {
                        fullTranslation = parser.getFullTranslation(toTranslate);
                        ConnectByHttp.setBlockingQueueWithResponse_FullTranslation(fullTranslation);
                        if (fullTranslation.isSuccessful() && database.isConnected() && !fullTranslation.getWordToTranslate().matches(".* .*"))
                            database.putAllFullTranslation(fullTranslation);
                    }
                    break;
                case "wordById":
                    if (database.isConnected())
                        try {
                            fullTranslation = database.getSimpleWordTranslationById(Integer.parseInt(toTranslate));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            fullTranslation = new FullTranslation("", "");
                            fullTranslation.setSuccessful(false);
                            fullTranslation.setFromCache(false);
                            ConnectByHttp.setBlockingQueueWithResponse_FullTranslation(fullTranslation);

                        }
                    else fullTranslation = new FullTranslation("", "");

                    if (!fullTranslation.isSuccessful()) {
                        fullTranslation.setSuccessful(false);
                        fullTranslation.setFromCache(false);
                    }
                    ConnectByHttp.setBlockingQueueWithResponse_FullTranslation(fullTranslation);
                    break;
                case "simpleTranslation":
                    fullTranslation = parser.getSimpleTranslationOfEverything(toTranslate);
                    ConnectByHttp.setBlockingQueueWithResponse_FullTranslation(fullTranslation);
                    break;
                case "parametrizedTranslation":
                    break;
                default:
                    fullTranslation = new FullTranslation("", "");
                    fullTranslation.setSuccessful(false);
                    fullTranslation.setFromCache(false);
                    ConnectByHttp.setBlockingQueueWithResponse_FullTranslation(fullTranslation);

            }
        }

/*        int temp = 0;
        while ((toTranslate = fileReader.readLine()) != null) {
            System.out.println(temp++);
            FullTranslation fullTranslation = parser.getFullTranslation(toTranslate);
            database.putAllFullTranslation(fullTranslation);
            if (fullTranslation == null) continue;
            if (fullTranslation.getSynonyms() != null)
                for (int i = 0; i < fullTranslation.getSynonyms().length; i++)


        }
        writeToFile.write(categories.toString());
        fileReader.close();
        writeToFile.flush();
        writeToFile.close();*/
    }

}
