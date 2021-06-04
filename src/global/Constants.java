package global;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final int FIELD_SIZE = 1000;
    public static final int DISPLAY_SIZE = 700;

    public static final List<String> listOfLinesNames = Arrays.asList("1", "2", "3", "3b", "4", "5", "6", "7", "7b", "8", "9", "10", "11", "12", "13", "14");
    public static List<String> LINE_DEFAULTATTRIBUTES = Arrays.asList("line", "color", "sectionId","origin","destinatio");


    public static final String LINES_FILESNAMES = "data/lines/lines";
    public static final String SCHEDULES_FILENAME = "data/schedule/schedule.csv";
    public static final String STATIONS_FILENAME = "data/stations/stations.csv";

    // argument of Gare geometries
    public static final String STATION_NAME_STR = "stationName";
}
