package ServiceJava;

import ServiceJava.Parser.FullTranslation;
import com.sun.net.httpserver.*;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;


public class ConnectByHttp {
    static private HttpExchange exchange = null;
    static private HttpServer server = null;
    static private volatile Map<String, List<String>> query_pairs = null;
    static private volatile String tempResponseTranslation = null;
    static private BlockingQueue<Map<String, List<String>>> blockingQueueWithQUERY_PAIRS = new ArrayBlockingQueue(1);
    static private BlockingQueue<FullTranslation> blockingQueueWithRESPONSE_FullTranslation = new ArrayBlockingQueue(1);
    static private final int VALIDATING_KEY = 348485251;

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
            server = HttpServer.create(new InetSocketAddress(Integer.parseInt(System.getenv("PORT"))), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context = server.createContext("/dictionary");
        context.setHandler(ConnectByHttp::handleRequest);
        server.start();
        System.out.println("started to listen");
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        JSONObject obj = null;
        System.out.println("\nURL " + exchange.getRequestURI());
        ConnectByHttp.exchange = exchange;

        URI requestURI = ConnectByHttp.exchange.getRequestURI();
        try {
            ConnectByHttp.query_pairs = splitQuery(exchange.getRequestURI());
            int secureKey = Integer.parseInt(query_pairs.get("key").iterator().next());
            byte[] bytes = query_pairs.get("word").iterator().next().getBytes("UTF-8");
            int localCheckKey = 0;
            for (int counter = 0; counter < query_pairs.get("word").iterator().next().getBytes("UTF-8").length; counter++) {
                localCheckKey += VALIDATING_KEY - bytes[counter] ^ VALIDATING_KEY;
            }
            if (localCheckKey == secureKey && (query_pairs.get("word") != null && query_pairs.get("reqtype") != null)) {
                System.out.println("KEY SUCCESS");
                blockingQueueWithQUERY_PAIRS.put(query_pairs);
                obj = processResponseToJSON(blockingQueueWithRESPONSE_FullTranslation.take(), query_pairs.get("reqtype").iterator().next());
            } else {
                System.out.println("KEY FAIL");
                ConnectByHttp.exchange.sendResponseHeaders(403, 0);
                DataOutputStream outputStream = new DataOutputStream(ConnectByHttp.exchange.getResponseBody());
                outputStream.close();
                ConnectByHttp.tempResponseTranslation = null;
                return;
            }

        } catch (UnsupportedEncodingException e) {
            ConnectByHttp.exchange.sendResponseHeaders(409, 0);
            DataOutputStream outputStream = new DataOutputStream(ConnectByHttp.exchange.getResponseBody());
            outputStream.close();
            ConnectByHttp.tempResponseTranslation = null;
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            ConnectByHttp.exchange.sendResponseHeaders(409, 0);
            DataOutputStream outputStream = new DataOutputStream(ConnectByHttp.exchange.getResponseBody());
            outputStream.close();
            ConnectByHttp.tempResponseTranslation = null;
            e.printStackTrace();
            return;
        } catch (NumberFormatException e) {
            ConnectByHttp.exchange.sendResponseHeaders(409, 0);
            DataOutputStream outputStream = new DataOutputStream(ConnectByHttp.exchange.getResponseBody());
            outputStream.close();
            ConnectByHttp.tempResponseTranslation = null;
            e.printStackTrace();
            return;
        } catch (NullPointerException e) {
            ConnectByHttp.exchange.sendResponseHeaders(409, 0);
            DataOutputStream outputStream = new DataOutputStream(ConnectByHttp.exchange.getResponseBody());
            outputStream.close();
            ConnectByHttp.tempResponseTranslation = null;
            e.printStackTrace();
            return;
        }
        ConnectByHttp.exchange.sendResponseHeaders(200, obj.toString().getBytes("UTF-16").length);
        DataOutputStream outputStream = new DataOutputStream(ConnectByHttp.exchange.getResponseBody());
        outputStream.write(obj.toString().getBytes("UTF-16"));
        outputStream.close();
        System.out.println(obj.toString());
        ConnectByHttp.tempResponseTranslation = null;
    }

    private static JSONObject processResponseToJSON(FullTranslation fullTranslation, String reqtype) {
        JSONObject JSON = new JSONObject();
        System.out.println("REQTYPE: "+reqtype);
        JSON.put("Successful", fullTranslation.isSuccessful());
        JSON.put("FromCache", fullTranslation.isFromCache());
        switch (reqtype) {
            case "wordById":
                getLightJson(fullTranslation, JSON);
                break;
            case "simpleTranslation":
                getLightJson(fullTranslation, JSON);
                break;
            default:
                getLightJson(fullTranslation, JSON);
        }


        return JSON;
    }

    private static JSONObject getLightJson(FullTranslation fullTranslation, JSONObject JSON) {
        JSON.put("translation", fullTranslation.getTranslatedWord());
        JSON.put("wordToTranslate", fullTranslation.getWordToTranslate());
        System.out.println("From Cache:" + (fullTranslation.isFromCache() ? "YES" : "NO"));
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
