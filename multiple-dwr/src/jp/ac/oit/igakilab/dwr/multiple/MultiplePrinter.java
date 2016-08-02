package jp.ac.oit.igakilab.dwr.multiple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * DWRでJSから呼ばれるメソッドはすべてpublicでなければならない．また，必要なクラスはすべてdwr.xmlに定義されている必要がある．
 * @author Hiroshi
 *
 */
public class MultiplePrinter {

//    public static void main(String[] args) throws InvalidValueException {
//        CloudSpiralPrinter csp = new CloudSpiralPrinter();
//        CSPInput input = new CSPInput();
//        List<String> list = csp.execute(input);
//        System.out.println(list);
//    }


    /**
     * 参考:忘れた
     * @return
     * @throws Exception
     */

    public  String executeGet() {
    	StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL("https://api.github.com/users/masumiueyama");

            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(),
                                                                       StandardCharsets.UTF_8);
                         BufferedReader reader = new BufferedReader(isr)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }


    /**
     * 参考:http://gootara.org/library/2014/04/javaapijsonjdk1618.html
     * @return
     * @throws Exception
     */

    public String executeParseJson() throws Exception{
    	String data = executeGet();

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        // ScriptEngine の eval に JSON を渡す時は、括弧で囲まないと例外が発生します。eval はセキュリティ的には好ましくないので、安全であることが不明なデータを扱うことは想定していません。
        // 外部ネットワークと連携するプログラムで使用しないでください。
        Object obj = engine.eval(String.format("(%s)", data));
        // Rhino は、jdk1.6,7までの JavaScript エンジン。jdk1.8は「jdk.nashorn.api.scripting.NashornScriptEngine」
        Map<String, Object> map = JsonSample.jsonToMap(obj, engine.getClass().getName().equals("com.sun.script.javascript.RhinoScriptEngine"));
        return map.toString();
    }


}
