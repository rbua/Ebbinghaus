package ServiceJava;

import ServiceJava.Database.Database;
import ServiceJava.Parser.FullTranslation;
import ServiceJava.Parser.Parser;
import ServiceJava.Parser.Synonyms;
import com.sun.net.httpserver.*;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class ConnectByHttp {
    static private HttpServer server = null;
static private volatile long trafficBytes=0;
    static private BlockingQueue<Map<String, List<String>>> blockingQueueWithQUERY_PAIRS = new ArrayBlockingQueue(10);
    static private BlockingQueue<FullTranslation> blockingQueueWithRESPONSE_FullTranslation = new ArrayBlockingQueue(10);
    static private final int VALIDATING_KEY = 348485251;

    public static long getTrafficBytes() {
        return trafficBytes;
    }

    public static void appendTrafficBytes(long trafficBytes) {
        ConnectByHttp.trafficBytes += trafficBytes;
    }

    public static BlockingQueue<Map<String, List<String>>> getBlockingQueueWithQueryPairs() {
        return blockingQueueWithQUERY_PAIRS;
    }

    public static void setBlockingQueueWithResponse_FullTranslation(FullTranslation fullTranslation) {
        try {
            blockingQueueWithRESPONSE_FullTranslation.put(fullTranslation);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.setExecutor(Executors.newFixedThreadPool(5));
        HttpContext context = server.createContext("/dictionary");
        context.setHandler(ConnectByHttp::handleRequest);
        server.start();
        System.out.println("started to listen");
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        JSONObject obj = null;
        appendTrafficBytes(exchange.getRequestURI().toString().length());
        Map<String, List<String>> query_pairs = null;
        URI requestURI = exchange.getRequestURI();
        try {
            query_pairs = splitQuery(exchange.getRequestURI());
            if(query_pairs.get("word") != null && query_pairs.get("reqtype") != null){
            int secureKey = Integer.parseInt(query_pairs.get("key").iterator().next());
            byte[] bytes = query_pairs.get("word").iterator().next().getBytes("UTF-8");
            int localCheckKey = 0;
            for (int counter = 0; counter < query_pairs.get("word").iterator().next().getBytes("UTF-8").length; counter++) {
                localCheckKey += VALIDATING_KEY - bytes[counter] ^ VALIDATING_KEY;
            }
            if (localCheckKey == secureKey ) {
                System.out.println("KEY SUCCESS");

                obj = processResponseToJSON(getFullTranslationForResponse(query_pairs), query_pairs.get("reqtype").iterator().next());
            } else {
                System.out.println("KEY FAIL");
                exchange.sendResponseHeaders(403, 0);
                DataOutputStream outputStream = new DataOutputStream(exchange.getResponseBody());
                outputStream.close();
                return;
            }}else { System.out.println("KEY FAIL");
            exchange.sendResponseHeaders(403, 0);
            DataOutputStream outputStream = new DataOutputStream(exchange.getResponseBody());
            outputStream.close();
            return;
        }

        } catch (UnsupportedEncodingException | NumberFormatException | NullPointerException e) {
            exchange.sendResponseHeaders(409, 0);
            DataOutputStream outputStream = new DataOutputStream(exchange.getResponseBody());
            outputStream.close();
            e.printStackTrace();
        }
        exchange.sendResponseHeaders(200, obj.toString().getBytes("UTF-16").length);
        DataOutputStream outputStream = new DataOutputStream(exchange.getResponseBody());
        outputStream.write(obj.toString().getBytes("UTF-16"));
        trafficBytes+=obj.toString().getBytes("UTF-16").length;
        outputStream.close();
        System.out.println(obj.toString()+"\nTOTAL TRAFFIC: "+trafficBytes/1000F+" KB");
    }

    private static FullTranslation getFullTranslationForResponse(Map<String, List<String>> query_pairs) {
        Parser parser = Parser.getInstance();
        Database database = Database.getInstance();
        String toTranslate = query_pairs.get("word").get(0);
        String type = query_pairs.get("reqtype").get(0);
        System.out.println(toTranslate);
        FullTranslation fullTranslation = null;
        try {
            switch (type) {
                case "wordById":
                    if (database.isConnected())
                        try {
                            fullTranslation = database.getSimpleWordTranslationById(Integer.parseInt(toTranslate));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            fullTranslation = new FullTranslation("", "");
                            fullTranslation.setSuccessful(false);
                            fullTranslation.setFromCache(false);
                            return fullTranslation;

                        }
                    else fullTranslation = new FullTranslation("", "");
                    break;
                case "simpleTranslation":
                    fullTranslation = parser.getSimpleTranslationOfEverything(toTranslate);
                    break;
                case "parametrizedWordTranslation":
                    if (query_pairs.get("synonyms") != null && query_pairs.get("sentencesENRU") != null && query_pairs.get("requestBy") != null && query_pairs.get("includeWordAudio") != null && toTranslate != null) {
                        if (toTranslate.matches(".* .*")) break;
                        boolean isSynonyms = Boolean.parseBoolean(query_pairs.get("synonyms").iterator().next());
                        boolean isSentencesENRU = Boolean.parseBoolean(query_pairs.get("sentencesENRU").iterator().next());
                        boolean isincludeWordAudio = Boolean.parseBoolean(query_pairs.get("includeWordAudio").iterator().next());
                        String whichwordAudio;
                        if (isincludeWordAudio && query_pairs.get("wordAudio") != null)
                            whichwordAudio = query_pairs.get("wordAudio").iterator().next();
                        else  whichwordAudio="";
                        String requestBy = query_pairs.get("requestBy").iterator().next();
                        ExecutorService executorService = Executors.newFixedThreadPool(2);
                        Future futureParser=null;
                        Future futureDatabase = executorService.submit(() -> Database.getInstance().getParametrizedTranslation(toTranslate, isSynonyms, isSentencesENRU, isincludeWordAudio, whichwordAudio, requestBy));
                       if(!requestBy.equals("ID")&&!requestBy.equals("RUword"))
                        futureParser = executorService.submit(() -> Parser.getInstance().getParametrizedTranslation(toTranslate, isSynonyms, isSentencesENRU, isincludeWordAudio, whichwordAudio));
                        if (futureParser!=null) {
                            fullTranslation = (FullTranslation) futureParser.get(10000, TimeUnit.MILLISECONDS);
                            if (isSynonyms && isSentencesENRU && isincludeWordAudio && whichwordAudio.equals("BOTH") & fullTranslation.isSuccessful()) {
                                FullTranslation newFull = fullTranslation;
                                executorService.submit(() -> Database.getInstance().putAllFullTranslation(newFull));
                            }
                        }
                        if(fullTranslation == null||!fullTranslation.isSuccessful())
                       fullTranslation = (FullTranslation) futureDatabase.get(10000, TimeUnit.MILLISECONDS);

                        executorService.shutdown();
                    }
                    break;
                case "putToCache":
                    if(toTranslate==null||toTranslate.matches(".*\\p{InCyrillic}.*")||toTranslate.matches(".* .*")||toTranslate.equals("")) break;
                    if(Database.getInstance().putAllFullTranslation(Parser.getInstance().getParametrizedTranslation(toTranslate, true, true, true, "BOTH"))) {
                        fullTranslation = new FullTranslation("", "");
                        fullTranslation.setSuccessful(true);
                        fullTranslation.setFromCache(false);
                    }
                    break;
                case"getID":
                    if(toTranslate==null||toTranslate.matches(".*\\p{InCyrillic}.*")||toTranslate.matches(".* .*")||toTranslate.equals("")) break;
                    fullTranslation=new FullTranslation(toTranslate,"");
                    fullTranslation.setWordID(Database.getInstance().getWordId(toTranslate));
                    if(fullTranslation.getWordID()!=0){
                        fullTranslation.setSuccessful(true);
                        fullTranslation.setFromCache(true);
                    }else {
                        fullTranslation.setSuccessful(false);
                        fullTranslation.setFromCache(false);
                    }
                    break;
                default:
                    fullTranslation = new FullTranslation("", "");
                    fullTranslation.setSuccessful(false);
                    fullTranslation.setFromCache(false);
            }
        } catch (InterruptedException | ExecutionException | ClassCastException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (fullTranslation == null) {
                fullTranslation = new FullTranslation("", "");
                fullTranslation.setSuccessful(false);
                fullTranslation.setFromCache(false);
            }
            return fullTranslation;
        }
    }


    private static JSONObject processResponseToJSON(FullTranslation fullTranslation, String reqtype) {
        JSONObject JSON = new JSONObject();
        System.out.println("REQTYPE: " + reqtype);
        JSON.put("Successful", fullTranslation.isSuccessful());
        JSON.put("FromCache", fullTranslation.isFromCache());
        switch (reqtype) {
            case "wordById":
                getLightJson(fullTranslation, JSON);
                break;
            case "simpleTranslation":
                getLightJson(fullTranslation, JSON);
                break;
            case "parametrizedWordTranslation":
                getparametrizedJson(fullTranslation,JSON);
                break;
            case "putToCache":
                break;
            case "getID":
                JSON.put("wordID",fullTranslation.getWordID());
                break;
            default:
                getLightJson(fullTranslation, JSON);
        }


        return JSON;
    }

    private static JSONObject getparametrizedJson(FullTranslation fullTranslation, JSONObject JSON) {
        getLightJson(fullTranslation,JSON);
        if (fullTranslation.getWordENAudioURLGB() != null)
            JSON.put("wordAudioGB", fullTranslation.getWordENAudioURLGB());
        if (fullTranslation.getWordENAudioURLUS() != null)
            JSON.put("wordAudioUS", fullTranslation.getWordENAudioURLUS());
        if (fullTranslation.getSynonyms() != null) {
            JSONObject arrayOfWordsCategories = new JSONObject();
            for (Synonyms synonyms : fullTranslation.getSynonyms()) {
                arrayOfWordsCategories.put(synonyms.getWordInEnglish() + " " + synonyms.getWordCategory(), synonyms.getTranslations());
            }
            JSON.put("synonyms", arrayOfWordsCategories);
        }
        if (fullTranslation.getSentencesInEnglishRussian() != null)
            JSON.put("sentencesENRU", fullTranslation.getSentencesInEnglishRussian());
        return JSON;

    }

    private static JSONObject getLightJson(FullTranslation fullTranslation, JSONObject JSON) {
        JSON.put("translation", fullTranslation.getTranslatedWord());
        JSON.put("wordToTranslate", fullTranslation.getWordToTranslate());
        return JSON;
    }

    // private static JSONObject getMediumJson()
    // private static JSONObject getHeavyJson()
    private static Map<String, List<String>> splitQuery(URI url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        if (url.getQuery() == null) return null;
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }
}
