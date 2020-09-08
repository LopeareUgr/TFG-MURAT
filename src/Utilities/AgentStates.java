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
public class AgentStates {
    
    public static final String SUBSCRIBE = "subscribe";
    public static final String WAIT_SUBSCRIBE = "wait_subscribe";
    public static final String FINALIZE = "finalize";
    
    public static final String GET_DATA = "get_data";
    public static final String CREATE_AGENTS = "create_agents";
    public static final String WAIT_INFORMS = "wait_informs";
    public static final String PROCESS_INFORMS = "save_inform";
    public static final String WAIT_ALL_CROSSROADS = "wait_all_crossroads";
    public static final String REQUEST_END_CROSSROADS = "request_end_crossroads";
    public static final String MANAGE_CROSSROADS = "Manage all crossroads";
    
    public static final String SIMULATE = "simulate";
    public static final String INFORM_SENSORS = "inform_sensors";
    public static final String LISTEN_STATES = "Wait crossroads states";
    public static final String SEND_VEH_DATA = "Send vehicles data time";
    
    public static final String WAIT_TIME = "wait_TIME";
    public static final String INFORM_CROSSROAD = "inform_crossroad";
    
    public static final String WAIT_SENSORS = "wait_sensors";
    public static final String WAIT_SUBSCRIBE_SIMULATE = "wait_subscribe_simulate";
    public static final String LISTEN_SENSORS = "think";
    public static final String THINK = "ask other crossroad";
    public static final String CHANGE_SEMAPHORES = "change semaphores";
    public static final String SEND_INFORM = "inform_switchboard";
    public static final String INFORM_END = "inform I end";
    public static final String WAIT_OTHER_CROSSROADS = "wait other crossroads";
    public static final String SEND_STATE = "Send current semaphore state";
    public static final String GENERATE_INFORMS = "Create files with informs";
    public static final String ASK_TOTAL_CARS = "Order simulator follow";
    
    public static final String WAIT_CROSSROAD = "wait_crossroad";
    public static final String FINISHING_ALL = "Finishing all agents";
    
}
