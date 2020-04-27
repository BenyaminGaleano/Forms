import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import csv.CSVWatcher;
import display.Display;
import config.ConfigParser;

public class CSV {

    public static void main(String[] args) {

        /* Observador para archivos de entrada */
        CSVWatcher wIn = new CSVWatcher(
            new ConfigParser("input.config"), 
            new File("./input").listFiles((file, fname) -> fname.matches(".*\\.csv"))
        );

        /* Observador para archivos de salida */
        CSVWatcher wOut = new CSVWatcher(
            new ConfigParser("output.config"), 
            new File("./output").listFiles((file, fname) -> fname.matches(".*\\.csv"))
        );

        /* Texto con formato y titulos personalizados */
        Display display = new Display(new ConfigParser("display.config"));

        TreeMap<String,Double> ids = new TreeMap<>();

        final int idColIn = wIn.settings.get("idCol", Integer.class) - 1;
        final int gradeColIn = wIn.settings.get("gradeCol", Integer.class) - 1;
        wIn.consumeCSVs((row)->{
            final String[] aux = row.get(gradeColIn).replaceAll("\"", "").split("/");
            final double gradeAux = Math.round(Double.parseDouble(aux[0].trim()) / Double.parseDouble(aux[1]) * wOut.settings.get("grade", Integer.class));
            
            if(!ids.containsKey(row.get(idColIn))){
                ids.put(row.get(idColIn), gradeAux);
            } else if(ids.get(row.get(idColIn)) < gradeAux){
                ids.replace(row.get(idColIn), gradeAux);
            } 

        });

        StringBuffer buffer = new StringBuffer("No encontrados:\n");

        ids.forEach((key,value)->{
            String result = wOut.writeAndGet(new TreeMap<String,String>(){
                private static final long serialVersionUID = 1L;
                {
                    put("gradeCol", value.toString());
                }
            }, "idCol", key, "gradeCol");
            if(result != null) return;
            LinkedList<String> inf = wIn.search("idCol", key);
            buffer.append("sección: "+inf.get(wIn.settings.get("classCol", Integer.class)-1)+" usuario: "+inf.get(wIn.settings.get("nameCol", Integer.class)-1)+" carnet: "+inf.get(wIn.settings.get("idCol", Integer.class)-1)+" nota: "+value);
            buffer.append("\n");
        });

        wOut.rewrite();
        File log = new File("./wanted.log");
        if(log.exists()) log.delete();
        try {
            FileOutputStream stream = new FileOutputStream(log);
            stream.write(buffer.toString().getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        display.inf("Ver wanted.log");
        display.msg("Terminado..!!");
    }
}