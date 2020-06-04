import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.function.Consumer;

import csv.CSVWatcher;
import display.Display;
import config.ConfigParser;

public class CSV {
    /* Variables estaticas */
    private static final SimpleDateFormat INPUTDATEFORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat GOOGLEDATEFORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static Date endDate = null;

    /* Campos */
    private String[] classes;
    private int idColIn;
    private int gradeColIn;
    private double gradeOut;
    private int classColIn;
    private TreeMap<String, Double> ids;

    private boolean in(String clss) {
        for (String clssTmp: classes) {
            if (clssTmp.equals(clss.toUpperCase().trim())) {
                return true;
            }
        }
        return false;
    }

    private Consumer<LinkedList<String>> gradeAllLimit = (row) -> {
        if (endDate != null) {
            try {
                if (GOOGLEDATEFORMAT.parse(row.get(0).replaceAll("\"", "")).after(endDate)){
                    return;
                }
            } catch (ParseException e) {}
        }
        
        final String[] aux = row.get(gradeColIn).replaceAll("\"", "").split("/");
        final double gradeAux = Math.round(Double.parseDouble(aux[0].trim()) / Double.parseDouble(aux[1]) * gradeOut);
        
        if(!ids.containsKey(row.get(idColIn))){
            ids.put(row.get(idColIn), gradeAux);
        } else if(ids.get(row.get(idColIn)) < gradeAux){
            ids.replace(row.get(idColIn), gradeAux);
        }
    };

    private Consumer<LinkedList<String>> gradeAll = (row) -> {
        final String[] aux = row.get(gradeColIn).replaceAll("\"", "").split("/");
        final double gradeAux = Math.round(Double.parseDouble(aux[0].trim()) / Double.parseDouble(aux[1]) * gradeOut);
        
        if(!ids.containsKey(row.get(idColIn))){
            ids.put(row.get(idColIn), gradeAux);
        } else if(ids.get(row.get(idColIn)) < gradeAux){
            ids.replace(row.get(idColIn), gradeAux);
        }
        
    };

    private Consumer<LinkedList<String>> gradeWhenLimit = (row) -> {
        if (endDate != null) {
            try {
                if (GOOGLEDATEFORMAT.parse(row.get(0)).after(endDate)){
                    return;
                }
            } catch (ParseException e) {}
        }
        
        if (!in(row.get(classColIn))) {
            return;
        }

        final String[] aux = row.get(gradeColIn).replaceAll("\"", "").split("/");
        final double gradeAux = Math.round(Double.parseDouble(aux[0].trim()) / Double.parseDouble(aux[1]) * gradeOut);
        
        if(!ids.containsKey(row.get(idColIn))){
            ids.put(row.get(idColIn), gradeAux);
        } else if(ids.get(row.get(idColIn)) < gradeAux){
            ids.replace(row.get(idColIn), gradeAux);
        }
    };

    private Consumer<LinkedList<String>> gradeWhen = (row) -> {
        if (!in(row.get(classColIn))) {
            return;
        }

        final String[] aux = row.get(gradeColIn).replaceAll("\"", "").split("/");
        final double gradeAux = Math.round(Double.parseDouble(aux[0].trim()) / Double.parseDouble(aux[1]) * gradeOut);
        
        if(!ids.containsKey(row.get(idColIn))){
            ids.put(row.get(idColIn), gradeAux);
        } else if(ids.get(row.get(idColIn)) < gradeAux){
            ids.replace(row.get(idColIn), gradeAux);
        }
    };

    private void start() throws FileNotFoundException {
        final File inputDir = new File("input");
        final File outputDir = new File("output");

        if (!inputDir.exists()) {
            inputDir.mkdir();
        }

        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        final ConfigParser inConfig = new ConfigParser("input.yaml");
        final ConfigParser outConfig = new ConfigParser("output.yaml");

        /* secciones a calificar */
        this.classes = inConfig.get("classes", String.class) == null ? new String[0] : inConfig.get("classes", String.class).split(" +");
        for (int i = 0; i < classes.length; i++) {
            classes[i] = "\""+classes[i].toUpperCase()+"\"";
        }

        /* Texto con formato y titulos personalizados */
        final Display display = new Display(new ConfigParser("display.yaml"));

        this.ids = new TreeMap<>();

        this.idColIn = inConfig.get("idCol", Integer.class) - 1;
        this.gradeColIn = inConfig.get("gradeCol", Integer.class) - 1;
        this.gradeOut = outConfig.get("grade", Double.class);
        this.classColIn = inConfig.get("classCol", Integer.class) - 1; 

        display.show("\nGoogle Forms Grader\n\n", Display.YELLOW);
        display.inf("El autograder mostrará excepciones si no configuró bien los achivos, las mismas no fueron validadas para aumentar la velocidad.");

        Consumer<LinkedList<String>> dobyline = null;

        if (classes.length == 0) {
            display.error("la configuración \"classes\" no puede estar vacía, vea \"input.yaml\".");
            return;
        }
        
        if (endDate == null) {
            if(classes[0].equals("\"ALL\"")) {
                dobyline = gradeAll;
            } else {
                dobyline = gradeWhen;
            }
        } else {
            if(classes[0].equals("\"ALL\"")) {
                dobyline = gradeAllLimit;
            } else {
                dobyline = gradeWhenLimit;
            }
        }

        /* Observador para archivos de entrada */
        CSVWatcher wIn = new CSVWatcher(
            inConfig, 
            dobyline,
            inputDir.listFiles((file, fname) -> fname.matches(".*\\.csv"))
        );

        /* Observador para archivos de salida */
        CSVWatcher wOut = new CSVWatcher(
            outConfig, 
            outputDir.listFiles((file, fname) -> fname.matches(".*\\.csv"))
        );

        final StringBuffer buffer = new StringBuffer("No encontrados:\n");

        ids.forEach((key,value)->{
            String result = wOut.writeAndGet(new TreeMap<String,String>(){
                private static final long serialVersionUID = 1L;
                {
                    put("gradeCol", value.toString());
                    put("commentCol", wOut.settings.get("comment", String.class) == null ? "" : wOut.settings.get("comment", String.class));
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

    public static void main(String[] args) throws ParseException, FileNotFoundException {
        if (args.length == 1) {
            endDate = INPUTDATEFORMAT.parse(args[0]);
        }

        final CSV csv = new CSV();
        csv.start();
    }
}