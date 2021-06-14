package global;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Constants {

    public static final boolean stationPassante = true;

    public static final int FIELD_SIZE = 1000;
    public static final int DISPLAY_SIZE = 700;

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final List<String> listOfLinesNames = Arrays.asList("1", "2", "3", "3b", "4", "5", "6", "7", "7b", "8", "9", "10", "11", "12", "13", "14");
    public static final Map<String, Integer> listOfCapacity = new HashMap<String,Integer>(){
        {
            put("1",698);
            put("2",557);
            put("3",512);
            put("3b",340);
            put("4",686);
            put("5",557);
            put("6",572);
            put("7",562);
            put("7b",342);
            put("8",562);
            put("9",557);
            put("10",572);
            put("11",452);
            put("12",572);
            put("13",562);
            put("14",698);
        }
    };
    //Vrai chiffre
    /*public static final Map<String, Integer> listOfNumberOfRame = new HashMap<>(){
        {
            put("1",45);
            put("2",37);
            put("3",40);
            put("3b",4);
            put("4",40);
            put("5",45);
            put("6",37);
            put("7",60);
            put("7b",6);
            put("8",50);
            put("9",59);
            put("10",22);
            put("11",20);
            put("12",37);
            put("13",52);
            put("14",25);
        }
    };*/

    public static final Map<String, Integer> listOfNumberOfRame = new HashMap<String,Integer>(){
        {
            put("1",10);
            put("2",10);
            put("3",10);
            put("3b",2);
            put("4",10);
            put("5",10);
            put("6",10);
            put("7",16);
            put("7b",2);
            put("8",12);
            put("9",14);
            put("10",10);
            put("11",6);
            put("12",10);
            put("13",10);
            put("14",6);
        }
    };

    public static List<String> LINE_DEFAULTATTRIBUTES = Arrays.asList("line", "color", "sectionId","origin","destinatio");

    public static double rameMaxSpeed = 0.000001D;
    public static double rameAcceleration = 0.00000001D;
    public static double rameBraking = -0.000000008D;
    public static int generateMode = 400;
    public static int attenteRame = 50;
    public static int panneAttente = 75;

    public static final String LINES_FILESNAMES = "data/lines/lines";
    public static final String SCHEDULES_FILENAME = "data/schedule/schedule.csv";
    public static final String STATIONS_FILENAME = "data/stations/stations.csv";

    //Spec Metro :
    public static int MAX_USER_RAME =518;
    public static double DETECTION_DISTANCE = 0.0001D;
    public static final String IS_MULTIPLE_STATION_STR = "isMultipleStation";

    public static final String STATION_NAME_STR = "stationName";
    public static final int NB_PIC_MATIN = 20;
    public static final int NB_PIC_SOIR = 20;
    public static final long ATTENTE_MCT=5000;
}
