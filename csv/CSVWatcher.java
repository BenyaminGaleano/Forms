package csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import config.ConfigParser;

/**
 * CSVWatcher
 */
public class CSVWatcher {
    private final TreeMap<File, LinkedList<LinkedList<String>>> csvs;
    public final ConfigParser settings;

    public CSVWatcher(ConfigParser configs, File... names) {
        settings = configs;
        csvs = new TreeMap<>();
        parseAll(names);
    }

    private void parseAll(File... files) {
        for (File f : files) {
            csvs.put(f, toLists(f));
        }
    }

    private LinkedList<String> parserLine(String line, AtomicInteger max){
        final Scanner scan = new Scanner(line);
        String current;
        boolean stringEnv = false;
        final StringBuffer buff = new StringBuffer();
        final LinkedList<String> words = new LinkedList<>();
        scan.useDelimiter("");
        while (scan.hasNext()) {
            current = scan.next();
            if(current.equals("\"")){
                buff.append("\"");
                stringEnv = !stringEnv;
            } else if(stringEnv){
                //ignoramos strings
                buff.append(current);
            } else if(current.equals(",")){
                words.add(buff.toString());
                buff.delete(0, buff.length());
            } else {
                buff.append(current);
            } 
        }
        scan.close();
        if(words.size()>max.get()) {
            max.set(words.size());
        }
        return words;
    }

    private LinkedList<LinkedList<String>> toLists(File file) {
        LinkedList<LinkedList<String>> result = new LinkedList<>();
        LinkedList<Integer> cols = settings.getAllCols();
        AtomicInteger max = new AtomicInteger(cols.stream().max(Integer::compare).orElse(0));
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                result.add(parserLine(sc.nextLine(), max));
            }
            sc.close();
            for(int i = settings.get("startline", Integer.class)-1;i<result.size();i++){
                for(int ii= result.get(i).size(); ii<max.get();ii++){
                    result.get(i).add("");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private byte[] toCSV(LinkedList<LinkedList<String>> inf){
        StringBuffer buff = new StringBuffer();
        String aux;
        for(LinkedList<String> list : inf){
            aux = list.stream().reduce((a,b)->a+","+b).orElse("");
            buff.append(aux);
            if(!aux.trim().isEmpty()) {
                buff.append(",");
            }
            buff.append("\n");
        }
        if(buff.length()>0)
            buff.deleteCharAt(buff.length()-1);
        return buff.toString().getBytes();
    }

    public LinkedList<String> search(String colN, String key){
        int col = settings.get(colN, Integer.class)-1;
        int start = settings.get("startline", Integer.class)-1;
        LinkedList<String> row;
        for(LinkedList<LinkedList<String>> csv : csvs.values()){
            for(int i = start; i < csv.size(); i++){
                row = csv.get(i);
                if(row.size()>col && row.get(col).replaceAll("\"", "").replaceAll("'", "").trim().equals(key.replaceAll("\"", "").replaceAll("'", "").trim())){
                    return row;
                }
            }
        }
        return null;
    }

    public void consumeCSVs(Consumer<LinkedList<String>> foo){
        int start = settings.get("startline", Integer.class)-1;
        for(LinkedList<LinkedList<String>> csv : csvs.values()){
            for(int i = start; i < csv.size(); i++){
                foo.accept(csv.get(i));
            }
        }
    }

    public String writeAndGet(TreeMap<String,String> data, String colKeyname, String key, String colName){
        LinkedList<String> result = search(colKeyname, key);
        if(result == null) return null;
        int col;
        for(String k : data.keySet()){
            col = settings.get(k, Integer.class)-1;
            result.remove(col);
            result.add(col, data.get(k));
        }
        return result.get(settings.get(colName, Integer.class)-1);
    }

    public void rewrite() {
        csvs.forEach((file, content)->{
            try {
                if(file.exists()) file.delete();
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(toCSV(content));
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        });
    }

}