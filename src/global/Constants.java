package global;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final int FIELD_SIZE = 1000;
    public static final int DISPLAY_SIZE = 700;

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final List<String> listOfLinesNames = Arrays.asList("1", "2", "3", "3b", "4", "5", "6", "7", "7b", "8", "9", "10", "11", "12", "13", "14");
    public static List<String> LINE_DEFAULTATTRIBUTES = Arrays.asList("line", "color", "sectionId","origin","destinatio");


    public static final String LINES_FILESNAMES = "data/lines/lines";
    public static final String SCHEDULES_FILENAME = "data/schedule/schedule.csv";
    public static final String STATIONS_FILENAME = "data/stations/stations.csv";

    //Spec Metro :
    public static int MAX_USER_RAME =518;
    public static final String IS_MULTIPLE_STATION_STR = "isMultipleStation";

    public static final String STATION_NAME_STR = "stationName";
    public static int MAX_USER_RAME =518;
    public static final int NB_PIC_MATIN = 20;
    public static final int NB_PIC_SOIR = 20;

}
