package ServiceJava;

import ServiceJava.Database.Database;
import ServiceJava.Parser.FullTranslation;
import ServiceJava.Parser.Parser;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        ConnectByHttp.start(Integer.parseInt(System.getenv("PORT")));
        //BufferedReader fileReader = new BufferedReader(new FileReader("C:/20k.txt"));
        //PrintWriter writeToFile = new PrintWriter("C:/test.txt", "UTF-8");
        //   database.putAllFullTranslation(parser.getFullTranslation("go"));


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
