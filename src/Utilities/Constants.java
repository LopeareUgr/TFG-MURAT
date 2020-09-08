/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

/**
 *
 * @author Raúl López Arévalo
 */
public class Constants {    // LIMPIAR
    
    public static final String ROAD_SECTIONS_PATH = "DB\\Tramos.csv";
    public static final String CROSSROAD_STATES_PATH = "DB\\Cruces.csv";
    public static final String INFORMS_PATH = "Informes\\000 - FINAL L - 5 MIN";
    public static final String EXTENSION = ".xlsx";
    
    public static final String SIMULATOR_NAME = "Simulator";
    public static final String SWITCHBOARD_NAME = "Switchboard";
    
    public static final String EMPTY = "";

    // Communication content
    public static final String CROSSROAD_STATE = "Crossroad semaphore state";
    public static final String GO_ON = "Go on";
    public static final String INFORM = "Inform";
    public static final String STATE = "Semaphore state";
    public static final String NOT_NOW = "not now but yes";
    public static final String TRAFFIC = "traffic";
    
    // DataBase and build data
    public static final String CROSSROAD_START = "CrossroadStart"; //check
    public static final String CROSSROAD_END = "CrossroadEnd"; //check
    public static final String ROOT = "root";
    public static final String LEAF = "leaf";
    public static final String ID = "ID";
    public static final String NUM_ROADS = "NumRoads";
    public static final String ROADS_IN = "Roads in";
    public static final String ROADS_OUT = "Roads out";
    public static final String CURRENT_CAP = "current capacity";
    public static final String CAPACITY = "Capacity";
    public static final String OC_IN = "Occupancy in";
    public static final String OC_OUT = "Occupancy out";
    public static final String V_IN = "Vehicle in";
    public static final String V_OUT = "Vehicle out";
    public static final String VEHICLES = "Vehicles";
    public static final String OCCUPATION = "Occupation";   
    
    public static final String ZERO = "0";
    public static final String DIR_END = "DirEnd";
    public static final String DIR_START = "DirStart";
    public static final String TIME = "Time";
    
    // Traffic simulation
    public static final String POSISITION = "position";
    public static final String LENGHT = "lenght";
    public static final String DEFAULT_LENGTH = "4";
    public static final String TYPE = "type";
    public static final String DEFAULT_TYPE = "car";
    public static final String START_DRIVING = "Start driving";
    public static final String TOTAL_TIME_DRIV = "Total time driving";
    public static final String MAX_TIME_DRIV = "Max time driving";
    public static final String MIN_TIME_DRIV = "Min time driving";
    public static final String VEHICLES_DATA = "Vehicles data";
    public static final int DEFAULT_SPEED = 10; // m/s
    public static final int DEFAULT_SEPARATION = 1;
    public static final int INF = 1000000;
    public static final String NOT_ALLOWED = "R";
    public static final String ALLOWED = "V";
    public static final String ALLOWED_CAREFULL = "A";
    
    // Objects names
    
    public static final String TRAFFIC_INFORM = "Traffic Inform"; //check
    
    public static final String ENDS = "Finishing";
    public static final String THINK = "think again";
    
    public static final String THINK_ENDS = "send ends of logic";
    
    
    public static final String START = "Start";
    public static final String END = "End";
    
    
    public static final String NORTH = "N";
    public static final String SOUTH = "S";
    public static final String EAST = "E";
    public static final String WEAST = "W";
    
    public static final String TOTAL_CARS = "Total cars";
    public static final String INTENSITY = "Intensity";
    public static final String PER_OCCUPATION = "Percentage Occupation";
    public static final String SCORE = "Score";
    public static final String V_RELATION = "V in and V out relation";
    public static final String ROAD_NAME = "Road Name";
    
    
    // Tiempos simulación
    public static final boolean LOGIC = true;
    public static final boolean USE_SENSORS = false;
    public static final boolean WORKABLE_DAY = true;
    public static final int CONGESTION = 90;
    public static final int LIMIT_CARS = 20000;
    
    public static final int TIME_START = 0*3600;
    public static final int TIME_END = 24*3600;
    public static final int TIME_INFORM = 5*60;
    
    public static final int VERY_LITTLE = 12;//12
    public static final int LITTLE = 8;//8
    public static final int NORMAL = 4;//4
    public static final int SEVERAL = 1;
}
