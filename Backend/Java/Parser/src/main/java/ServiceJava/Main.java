package ServiceJava;

import ServiceJava.Database.Database;
import ServiceJava.Parser.FullTranslation;
import ServiceJava.Parser.Parser;

import javax.xml.crypto.Data;
import java.io.*;
import java.sql.SQLException;
import java.util.HashSet;


public class Main {
   public static void main(String[] args) throws IOException, InterruptedException, SQLException {
ConnectByHttp.createConn();
        BufferedReader fileReader = new BufferedReader(new FileReader("C:/20k.txt"));
        PrintWriter writeToFile = new PrintWriter("C:/test.txt", "UTF-8");
        Parser parser = new Parser();
        Database database=new Database();

        String toTranslate = "Check";
        HashSet<String> categories = new HashSet<>();
        int temp = 0;
        while ((toTranslate = fileReader.readLine()) != null) {
            System.out.println(temp++);
            FullTranslation fullTranslation = parser.getFullTranslation(toTranslate);
            database.putAllFullTranslation(fullTranslation);
            if (fullTranslation == null) continue;
            if (fullTranslation.getSynonyms() != null)
                for (int i = 0; i < fullTranslation.getSynonyms().length; i++)
                    categories.add(fullTranslation.getSynonyms()[i].getWordCategory());

        }
        writeToFile.write(categories.toString());
        fileReader.close();
        writeToFile.flush();
        writeToFile.close();
    }
}
