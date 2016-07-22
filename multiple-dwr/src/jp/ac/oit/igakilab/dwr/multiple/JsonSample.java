package jp.ac.oit.igakilab.dwr.multiple;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JsonSample {

    public static void main(String[] args) throws Exception {
        // 起動時にオプションを指定しなかった場合は、このサンプルデータを使用する。
        String script = "{ \"key1\" : \"val1\", \"key2\" : \"val2\", \"key3\" : { \"ckey1\" : \"cval1\", \"ckey2\" : [ \"cval2-1\", \"cval2-2\" ] } }";
        if (args.length > 0) {
            java.io.File f = new java.io.File(args[0]);
            if (f.exists() && f.isFile()) {
                // 起動時の第１引数がファイルならファイルから読み込み（java 6 対応版なので、try-with-resources すら使えません。実際は、こんな書き方せずにちゃんとエラー処理してください）
                byte[] buf = new byte[new Long(f.length()).intValue()];
                java.io.FileInputStream fin = null; try { fin = new java.io.FileInputStream(f); fin.read(buf); } catch (Exception ex) { throw ex; } finally { if (fin != null) { fin.close(); }}
                script = args.length > 1 ? new String(buf, args[1]) : new String(buf); // ファイルの文字コードがシステムの文字コードと異なる場合は、第２引数で指定。例えば「UTF-8」や「JISAutoDetect」など。
            } else {
                script = args[0]; // ファイルでなければ、第１引数の文字列をそのまま JSON として扱う
            }
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        // ScriptEngine の eval に JSON を渡す時は、括弧で囲まないと例外が発生します。eval はセキュリティ的には好ましくないので、安全であることが不明なデータを扱うことは想定していません。
        // 外部ネットワークと連携するプログラムで使用しないでください。
        Object obj = engine.eval(String.format("(%s)", script));
        // Rhino は、jdk1.6,7までの JavaScript エンジン。jdk1.8は「jdk.nashorn.api.scripting.NashornScriptEngine」
        Map<String, Object> map = jsonToMap(obj, engine.getClass().getName().equals("com.sun.script.javascript.RhinoScriptEngine"));
        System.out.println(map.toString());
    }

    static Map<String, Object> jsonToMap(Object obj, boolean rhino) throws Exception {
        // Nashorn の場合は isArray で obj が配列かどうか判断できますが、特に何もしなくても配列番号をキーにして値を取得し Map に格納できるので、ここでは無視しています。
        // Rhino だとインデックスを文字列として指定した場合に値が返ってこないようなので、仕方なく処理を切り分けました。
        // 実際は HashMap なんか使わずに自分で定義したクラス（配列はそのオブジェクトの List プロパティ）にマップすることになると思うので、動作サンプルとしてはこんなもんでよろしいかと。
        boolean array = rhino ? Class.forName("sun.org.mozilla.javascript.internal.NativeArray").isInstance(obj) : false;
        Class scriptObjectClass = Class.forName(rhino ? "sun.org.mozilla.javascript.internal.Scriptable" : "jdk.nashorn.api.scripting.ScriptObjectMirror");
        // キーセットを取得
        Object[] keys = rhino ? (Object[])obj.getClass().getMethod("getIds").invoke(obj) : ((java.util.Set)obj.getClass().getMethod("keySet").invoke(obj)).toArray();
        // get メソッドを取得
        Method method_get = array ? obj.getClass().getMethod("get", int.class, scriptObjectClass) : (rhino ? obj.getClass().getMethod("get", Class.forName("java.lang.String"), scriptObjectClass) : obj.getClass().getMethod("get", Class.forName("java.lang.Object")));
        Map<String, Object> map = new HashMap<String, Object>();

        System.out.println("view:" + map);

        for (Object key : keys) {
            Object val = array ? method_get.invoke(obj, (Integer)key, null) : (rhino ? method_get.invoke(obj, key.toString(), null) : method_get.invoke(obj, key));
            System.out.println("view: " + val);
            if (scriptObjectClass.isInstance(val)) {
                map.put(key.toString(), jsonToMap(val, rhino));
            } else {
            	map.put(key.toString(), (val != null ? val.toString() : "null")); // サンプルなので、ここでは単純に toString() してますが、実際は val の型を有効に活用した方が良いでしょう。
            }
        }
        return map;
    }
}
