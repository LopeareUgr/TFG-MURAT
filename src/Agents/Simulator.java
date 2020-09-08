/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Utilities.AgentStates;
import Utilities.Constants;
import Utilities.JSON;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Raúl López Arévalo
 */
public class Simulator extends Agent {
    // Todas las calles en Map con sus propiedades. Para acceder rápidamente.
    private Map<String, Map<String, String>> roads;
    private List<Map<String, String>> roadSections;
    // Todos los cruces con sus calles de entrada y salida
    private Map<String, Map<String, List<String>>> crossroads;
    // Todos los estados de los cruces. Accede al actual
    private Map<String, Map<String, Map<String, String>>> crossroads_states;
    // Actual estado de semáforos de cada cruce, solamente el número.
    private Map<String, String> current_cross_state;
    // Ocupaciones de las vías de cada calle
    private Map<String, Map<String, List<Map<String, String>>>> traffic;
    
    // Time driving
    private int total_time_driving = 0;
    private int max_time_driving = 0;
    private int min_time_driving = Constants.INF;
    private int total_cars = 0;
    private int time = Constants.TIME_START;
    private int total_in = 0;
    private int total_out = 0;

    public Simulator(AgentID aid, List<Map<String, String>> roadSections,
            Map<String, Map<String, List<String>>> crossroads,
            Map<String, Map<String, Map<String, String>>> crossroadsStates,
            Map<String, Map<String, String>> allRoads
    ) throws Exception {
        super(aid);
        this.roads = new HashMap();
        this.roads = allRoads;
        this.crossroads = new HashMap();
        this.crossroads = crossroads;
        
        this.crossroads_states = new HashMap();
        this.crossroads_states = crossroadsStates;
        this.current_cross_state = new HashMap();
        this.traffic = new HashMap();
        this.roadSections = new ArrayList();
        this.roadSections = roadSections;
        total_time_driving = 0;
        max_time_driving = 0;
        min_time_driving = Constants.INF;
        this.state = AgentStates.WAIT_SUBSCRIBE;
        //Map para acceder rápidamente al estado de un cruce
        for (Map.Entry<String, Map<String, List<String>>> crossroad : crossroads.entrySet()) {
            current_cross_state.put(crossroad.getKey(), "1");
        }
        //Map para gestionar la ocupación de cada calle
        for (Map<String, String> roadSection : roadSections) {
            // Añadimos cada calle
            Map<String, List<Map<String, String>>> road = new HashMap();
            int numVias = Integer.parseInt(roadSection.get(Constants.NUM_ROADS));
            // Por cada calle, su número de vías con una lista de vehiculos vacía por ahora
            for (int way = 1; way < numVias + 1; way++) {
                List<Map<String, String>> vehicles = new ArrayList();
                road.put(Integer.toString(way), vehicles);
                traffic.put(roadSection.get(Constants.ID), road);
            }
        }

    }

    @Override
    public void execute() {
        while (!this.stop) {
            switch (this.state) {
                case AgentStates.WAIT_SUBSCRIBE: // de los sensores
                    waitSensors();
                    break;
                case AgentStates.SUBSCRIBE: // Estados de semáforos
                    subscribeCrossroadsState();
                    break;
                case AgentStates.SIMULATE:
                    simulate();
                    break;
                case AgentStates.INFORM_SENSORS:
                    informSensors();
                    break;
                case AgentStates.LISTEN_STATES:
                    listenStates();
                    break;
                case AgentStates.SEND_VEH_DATA:
                    sendVehicleData();
                    break;
                case AgentStates.FINALIZE:
                    this.stop = true;
                    break;
            }
        }
    }

    public void waitSensors() {
        for (int sensor = 0; sensor < roadSections.size() * 2; sensor++) {
            inbox = receiveMessage();
            content = JSON.content(Constants.NOT_NOW);
            buildMessage(inbox.getSender(), content, ACLMessage.AGREE);
            this.send(outbox);
        }
        this.state = AgentStates.SUBSCRIBE;
    }

    public void subscribeCrossroadsState() {
        for (Map.Entry<String, Map<String, List<String>>> crossroad : crossroads.entrySet()) {
            content = JSON.content(Constants.CROSSROAD_STATE);
            buildMessage(new AgentID(crossroad.getKey()), content, ACLMessage.SUBSCRIBE);
            this.send(outbox);
            inbox = receiveMessage();
        }
        this.state = AgentStates.SIMULATE;
    }

    public void simulate() {
        time++;
        System.out.println(time);

        canalizeTraffic();
        if (Constants.USE_SENSORS) {
            if (Constants.WORKABLE_DAY) {
                if (time > 0 && time < 250) {
                    if (time % Constants.VERY_LITTLE == 0) {
                        addTraffic();
                    }
                } else if (time >= 250 && time < 400) {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 400 && time < 550) {
                    if (time % Constants.NORMAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 550 && time < 650) {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 650 && time < 1000) {
                    if (time % Constants.LITTLE == 0) {
                        addTraffic();
                    }
                } else {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                }
            } else {

            }
        } else {
            if (Constants.WORKABLE_DAY) {
                
                if (time > 0 && time < 7*3600) {
                    if (time % Constants.LITTLE == 0) {
                        addTraffic();
                    }
                } else if (time >= 7*3600 && time < 9*3600) {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 9*3600 && time < 14*3600) {
                    if (time % Constants.NORMAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 14*3600 && time < 16*3600) {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 16*3600 && time < 22*3600) {
                    if (time % Constants.NORMAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 22*3600 && time < 24*3600) {
                    if (time % Constants.LITTLE == 0) {
                        addTraffic();
                    }
                } 
            
            } else {

            }
        }
/*if (Constants.WORKABLE_DAY) {
                if (time > 0 && time < 7*3600) {
                    if (time % Constants.LITTLE == 0) {
                        addTraffic();
                    }
                } else if (time >= 7*3600 && time < 9*3600) {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 9*3600 && time < 14*3600) {
                    if (time % Constants.NORMAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 14*3600 && time < 16*3600) {
                    if (time % Constants.SEVERAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 16*3600 && time < 22*3600) {
                    if (time % Constants.NORMAL == 0) {
                        addTraffic();
                    }
                } else if (time >= 22*3600 && time < 24*3600) {
                    if (time % Constants.LITTLE == 0) {
                        addTraffic();
                    }
                } 
            }*/
        

        //printOccupation();
        //printTraffic();

        
        
        if (time == (Constants.TIME_END+1)) {
            this.state = AgentStates.FINALIZE;
        } else {
            this.state = AgentStates.INFORM_SENSORS;
        }
    }

    public void informSensors() {

        content = JSON.content(Integer.toString(this.time));
        //System.out.println("simulador informando sensores");
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            
            //aquí podemos poner que solo mande a sensores que terminan en cruce
            // en el switchboard evitar la creación de estos sensores
            if(Constants.USE_SENSORS){
                JsonObject data_start = new JsonObject();

                data_start.add(Constants.V_IN, road.getValue().get(Constants.V_IN));
                data_start.add(Constants.OC_IN, road.getValue().get(Constants.OC_IN));
                content = data_start.toString();
                //content = JSON.content(content);
                content = JSON.buildContent(Constants.TRAFFIC_INFORM, content);
                ////quitarSystem.out.println(buildContent);
                buildMessage(new AgentID(road.getKey() + Constants.START), content, ACLMessage.INFORM);
                send(outbox);

                JsonObject data_end = new JsonObject();

                data_end.add(Constants.V_OUT, road.getValue().get(Constants.V_OUT));
                data_end.add(Constants.OC_OUT, road.getValue().get(Constants.OC_OUT));
                content = data_end.toString();
                ////quitarSystem.out.println(buildContent);
                //content = JSON.content(content);
                content = JSON.buildContent(Constants.TRAFFIC_INFORM, content);
                buildMessage(new AgentID(road.getKey() + Constants.END), content, ACLMessage.INFORM);
                send(outbox);
            }else if(!road.getValue().get(Constants.CROSSROAD_END).equals(Constants.LEAF)){
                String crossroad = road.getValue().get(Constants.CROSSROAD_END);
                JsonObject data = new JsonObject();

                data.add(Constants.ROAD_NAME, road.getKey());
                data.add(Constants.V_IN, road.getValue().get(Constants.V_IN));
                data.add(Constants.OC_IN, road.getValue().get(Constants.OC_IN));
                data.add(Constants.V_OUT, road.getValue().get(Constants.V_OUT));
                data.add(Constants.OC_OUT, road.getValue().get(Constants.OC_OUT));
                content = data.toString();
                //content = JSON.content(content);
                content = JSON.buildContent(Constants.TRAFFIC_INFORM, content);
                ////quitarSystem.out.println(buildContent);
                buildMessage(new AgentID(crossroad), content, ACLMessage.INFORM);
                send(outbox);
            }
        }

        cleanVehiclesInOut();

        // //.out.println("end informs sensors");
        this.state = AgentStates.LISTEN_STATES;

    }

    public void listenStates() {
        // Esto ya es otro estado diferente del simulador
        // El simulador espera para recibir los estados
        //System.out.println("simulador recibiendo estados de semáforos");
        for (int cross_num = 0; cross_num < this.crossroads.size(); cross_num++) {
            inbox = receiveMessage();
            content = JSON.readContent(inbox.getContent());
            // Actualizar el estado actual de cada cruce
            for (Map.Entry<String, Map<String, Map<String, String>>> crossroad : crossroads_states.entrySet()) {
                if (inbox.getSender().toString().contains(crossroad.getKey())) {
                    current_cross_state.replace(crossroad.getKey(), content);
                    //quitarSystem.out.println(current_cross_state);
                }
            }
        }
        
        this.state = AgentStates.SEND_VEH_DATA;
    }
    
    public void sendVehicleData(){
        //System.out.println("sim esperando next");
        //Esperar a que alguien le diga que puede seguir, porque todos han terminado.
        
        //quitarSystem.out.println("Simulador esperando nueva iteración");
        inbox = receiveMessage();
        //System.out.println("orden de continuar recibida");
        
        JsonObject vehicle_data = new JsonObject();
        vehicle_data.add(Constants.TOTAL_CARS, total_cars);
        vehicle_data.add(Constants.TOTAL_TIME_DRIV, total_time_driving);
        vehicle_data.add(Constants.MAX_TIME_DRIV, max_time_driving);
        vehicle_data.add(Constants.MIN_TIME_DRIV, min_time_driving);
        vehicle_data.add("total_in",total_in);
        vehicle_data.add("total_out",total_out);
        
        content = JSON.buildContent(Constants.VEHICLES_DATA, vehicle_data.toString());
        if(time%400 == 0){
            //System.out.println("debug");
        }
        buildMessage(new AgentID(Constants.SWITCHBOARD_NAME), content, ACLMessage.INFORM);
        send(outbox);
        
        //total_in = 0;
        //total_out = 0;
        //total_time_driving = 0;
        this.state = AgentStates.SIMULATE;
    }

    @Override
    public void finalize() {
        // Manda finalizar al switchboard
        content = JSON.buildContent(Constants.ENDS,null);
        buildMessage(new AgentID(Constants.SWITCHBOARD_NAME),
                content, ACLMessage.REQUEST);
        send(outbox);
        inbox = receiveMessage();
        //quitarSystem.out.println("simulador llama al super");

        super.finalize();
    }

    //**************************************************** Methods to simulation
    //****************************************************//********************
    // View all roads traffic
    public void printTraffic() {
        String in, out;

        System.out.println("");
        System.out.println("******************** ALL TRAFFIC ********************");
        System.out.println("");
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            System.out.println("Tráfico en calle: " + road.getKey());
            in = road.getValue().get(Constants.V_IN);
            out = road.getValue().get(Constants.V_OUT);

            System.out.println("Calle " + road.getKey());
            System.out.println("IN: " + in + " - Out: " + out);
            for (int way = 1; way <= getNumWays(road.getKey()); way++) {
                System.out.println("Via " + way);
                for (Map<String, String> vehicle : getVehicles(road.getKey(), way)) {
                    System.out.println(vehicle);
                }
            }
            System.out.println("");
        }
    }

    // View all occupation
    public void printOccupation() {
        String in, out, vin, vout;
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            in = road.getValue().get(Constants.OC_IN);
            out = road.getValue().get(Constants.OC_OUT);
            vin = road.getValue().get(Constants.V_IN);
            vout = road.getValue().get(Constants.V_OUT);
            System.out.println("Calle " + road.getKey());
            System.out.println("IN: " + in + " - Out: " + out);
            System.out.println("VIN: " + vin + " - VOut: " + vout);
        }
    }

    //Avanzar todos los coches existentes
    public void canalizeTraffic() {
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            moveCars(road.getKey());
        }
    }

    public void noteDownTime(Map<String, String> vehicle) {
        int start_driving = Integer.parseInt(vehicle.get(Constants.START_DRIVING));
        int t = time - start_driving;

        if(t>7000){
            //System.out.println("debug");
        }
        
        total_out++;
        
        total_time_driving += t;
        if (t < min_time_driving) {
            min_time_driving = t;
        }
        if (t > max_time_driving) {
            max_time_driving = t;
        }
    }
    
    // Avanzar coches en una calle (todas sus vías)
    public void moveCars(String roadName) {
        List<Map<String, String>> carsToDelete = new ArrayList();
        String new_road;
        //Para cada vía
        for (int way = 1; way <= getNumWays(roadName); way++) {
            // For each vehicle           
            Map<String, String> lastVehicle = new HashMap();
            for (Map<String, String> vehicle : getVehicles(roadName, way)) {

                if (lastVehicle.isEmpty()) {
                    if (getPos(vehicle) > Constants.DEFAULT_SPEED) {
                        //Avanzar libremente con su velocidad
                        moveCar(vehicle, Constants.DEFAULT_SPEED);
                        lastVehicle = vehicle;
                    } else if (isLeafRoad(roadName)) { // Se saca directamente
                        carsToDelete.add(vehicle);
                        noteDownTime(vehicle);
                        detectVehicleOut(roadName, vehicle);
                        total_cars--;
                    } else {
                        //Tomar una salida si es posible. Si no hay que dejarlo en la posición 0
                        if (isAllowedToExit(roadName) && isAnyExitFree(roadName)) {

                            //Lo eliminamos de esta calle
                            carsToDelete.add(vehicle);
                            detectVehicleOut(roadName, vehicle);
                            //Lo introducimos en la calle nueva
                            new_road = chooseRoadToExit(roadName);
                            addCar(new_road, vehicle);
                            detectVehicleIn(new_road, vehicle);
                        } else {
                            moveCar(vehicle);
                            lastVehicle = vehicle;
                        }
                    }
                } else {
                    //Avanzar teniendo en cuenta el coche de alante. No puede salir
                    moveCar(lastVehicle, vehicle);
                    lastVehicle = vehicle;
                }
            }
            // Borrado permanente del vehiculo
            for (Map<String, String> vehicle : carsToDelete) {
                removeCar(roadName, way, vehicle);
            }
            carsToDelete.clear();
        }
    }

    // Return if is allowed exit from this road. taken into account semaphore
    public boolean isAllowedToExit(String roadName) {
        String direction = roads.get(roadName).get(Constants.DIR_END);
        String c_end = roads.get(roadName).get(Constants.CROSSROAD_END);

        ////quitarSystem.out.println("Road: " + roadName);
        ////quitarSystem.out.println("Direction status: " + semaphoreStatus(c_end, direction));
        ////quitarSystem.out.println(!semaphoreStatus(c_end, direction).equals(Constants.NOT_ALLOWED));
        return !semaphoreStatus(c_end, direction).equals(Constants.NOT_ALLOWED);
    }

    // Return if any possible exit is free. 
    public boolean isAnyExitFree(String roadName) {
        return getAllowedExits(roadName).size() > 0;
    }

    // Return allowed exits. Can be null
    public List<String> getAllowedExits(String roadName) {
        List<String> allowedDir = getAllowedDir(roadName);
        String c_end = getCEnd(roadName);
        List<String> allowedExits = new ArrayList();

        List<String> allExits = crossroads.get(c_end).get(Constants.ROADS_OUT);

        for (String road : allExits) {
            if (allowedDir.contains(getDirStart(road)) && !isRoadFull(road)) {
                allowedExits.add(road);
            }
        }
        return allowedExits;
    }

    // Choose a road to enter.
    public String chooseRoadToExit(String roadName) {
        List<String> allowedExits = getAllowedExits(roadName);

        // Siempre va a tener al menos un valor.
        // Si se llama a este método es porque se ha hecho el chequeo de que existe alguna salida válida.
        int index = (int) (Math.random() * allowedExits.size());
        return allowedExits.get(index);
    }

    // Get end crossroad
    public String getCEnd(String roadName) {
        return roads.get(roadName).get(Constants.CROSSROAD_END);
    }

    // Get start direction road
    public String getDirStart(String roadName) {
        return roads.get(roadName).get(Constants.DIR_START);
    }

    // Get end direction road
    public String getDirEnd(String roadName) {
        return roads.get(roadName).get(Constants.DIR_END);
    }

    // get all allowed directions to exit
    public List<String> getAllowedDir(String roadName) {
        List<String> directions = new ArrayList<>(Arrays.asList(Constants.NORTH, Constants.WEAST,
                Constants.SOUTH, Constants.EAST));

        String direction = getDirEnd(roadName);
        List<String> allowedDirs = new ArrayList();

        int v = getGreenLights(roadName);
        if (time == 18) {
            //quitarSystem.out.println("punto ruptura");
        }
        if (v == 1) { // Puede tomar cualquier dirección
            allowedDirs = directions;
            allowedDirs.remove(getDirEnd((roadName)));
        } else if (v == 2) { // Solamente puede ir recto o derecha
            allowedDirs.add(directions.get((directions.indexOf(direction) + 1) % 4));
            allowedDirs.add(directions.get((directions.indexOf(direction) + 2) % 4));
        }

        return allowedDirs;
    }

    // Devuelve el número de semáforos en verde en la configuración del cruce.
    /* Si hay solamente un semáforo en verde, puede tomar cualquier salida
    siempre y cuando el campo de dirección permitida de la base de datos lo permita.
    En caso de devolver 2 semáforos en verde, significa que puede continuar recto
    o girar a la derecha.*/
    public int getGreenLights(String roadName) {
        List<String> roads = null;
        String c_end = this.roads.get(roadName).get(Constants.CROSSROAD_END);
        String currentState = current_cross_state.get(c_end);
        int v = 0;

        for (Map.Entry<String, String> state : crossroads_states.get(c_end).get(currentState).entrySet()) {
            if (state.getValue().equals(Constants.ALLOWED)) {
                v++;
            }
        }
        return v;
    }

    // Devuelve el estado de un semáforo 
    public String semaphoreStatus(String crossroad, String direction) {
        String currentState = current_cross_state.get(crossroad);
        return crossroads_states.get(crossroad).get(currentState).get(direction);
    }

    // Añadir coches en todas las calles root
    public void addTraffic() {
        
        if(total_cars < Constants.LIMIT_CARS){
            for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
                if (isRootRoad(road.getKey()) && !isRoadFull(road.getKey())) {
                    addCar(road.getKey());
                    total_cars++;
                }
            }
        }
        
    }

    // Añadir un coche en una Calle Root
    // Aquí es donde se pueden poner propiedades aleatorias, como la velocidad
    public void addCar(String roadName) {
        Map<String, String> vehicle = new HashMap();
        String capacity = roads.get(roadName).get(Constants.CAPACITY);
        String length = Constants.DEFAULT_LENGTH;
        String pos = Integer.toString(Integer.parseInt(capacity) - Integer.parseInt(length));
        vehicle.put(Constants.POSISITION, pos);
        vehicle.put(Constants.LENGHT, Constants.DEFAULT_LENGTH);
        vehicle.put(Constants.TYPE, Constants.DEFAULT_TYPE);
        vehicle.put(Constants.START_DRIVING, Integer.toString(time));
        
        
        String way = chooseWay(roadName);
        traffic.get(roadName).get(way).add(vehicle);
        detectVehicleIn(roadName, vehicle);
        
        total_in++;
    }

    // Añadir un coche que sale de un tramo a otro
    public void addCar(String roadName, Map<String, String> vehicle) {
        String way = chooseWay(roadName);
        String new_position = Integer.toString(
                getRoadCapacity(roadName) - getLength(vehicle));
        vehicle.replace(Constants.POSISITION, new_position);
        traffic.get(roadName).get(way).add(vehicle);
    }

    // Sensor captando un vehículo que entra
    public void detectVehicleIn(String roadName, Map<String, String> vehicle) {
        int current = Integer.parseInt(roads.get(roadName).get(Constants.OC_IN));
        current += getLength(vehicle) + Constants.DEFAULT_SEPARATION;
        roads.get(roadName).replace(Constants.OC_IN, Integer.toString(current));

        current = Integer.parseInt(roads.get(roadName).get(Constants.V_IN));
        current++;
        roads.get(roadName).replace(Constants.V_IN, Integer.toString(current));
        ////quitarSystem.out.println(current);

        ////quitarSystem.out.println("Calle: "+roadName+ " con " +roads.get(roadName).get(Constants.V_IN)+ " coches");
        ////quitarSystem.out.println("Calle: "+roadName+ " con " +roads.get(roadName).get(Constants.OC_IN)+ " ESPACIO OCUPADO");
    }

    // Sensor captando un vehículo que sale
    public void detectVehicleOut(String roadName, Map<String, String> vehicle) {
        int current = Integer.parseInt(roads.get(roadName).get(Constants.OC_OUT));
        current += getLength(vehicle) + Constants.DEFAULT_SEPARATION;
        roads.get(roadName).replace(Constants.OC_OUT, Integer.toString(current));

        current = Integer.parseInt(roads.get(roadName).get(Constants.V_OUT));
        current++;
        roads.get(roadName).replace(Constants.V_OUT, Integer.toString(current));

        ////quitarSystem.out.println("Calle: "+roadName+ " con " +roads.get(roadName).get(Constants.V_IN)+ " coches");
        ////quitarSystem.out.println("Calle: "+roadName+ " con " +roads.get(roadName).get(Constants.OC_IN)+ " ESPACIO OCUPADO");
    }

    // Clean number of vehicles in and out from all roads
    public void cleanVehiclesInOut() {
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            road.getValue().replace(Constants.OC_IN, Constants.ZERO);
            road.getValue().replace(Constants.OC_OUT, Constants.ZERO);
            road.getValue().replace(Constants.V_IN, Constants.ZERO);
            road.getValue().replace(Constants.V_OUT, Constants.ZERO);
        }
    }

    // Elegimos la via que tenga el último coche más lejano
    public String chooseWay(String roadName) {
        int farthest = Constants.INF;
        int wayChoosen = 1;
        int lastPosition;
        // Para cada via
        for (int way = 1; way <= getNumWays(roadName); way++) {
            // Se elije la primera que esté vacía
            if (isWayEmpty(roadName, way)) {
                wayChoosen = way;
                break;
            } else {
                // Si ninguna está vacía, se tiene en cuenta en último coche de cada una
// esto de calcular la ultima posición se puede modularizar. y otras cosas tmbn revisar.
                lastPosition = getPos(getVehicles(roadName, way).
                        get(getVehicles(roadName, way).size() - 1));
                if (lastPosition < farthest) {
                    farthest = lastPosition;
                    wayChoosen = way;
                }
            }
        }
        return Integer.toString(wayChoosen);
    }

    // Eliminar un coche de una calle
    public void removeCar(String roadName, int way, Map<String, String> vehicle) {
        traffic.get(roadName).get(Integer.toString(way)).remove(vehicle);
    }

    // Get vehicles list from a street way
    public List<Map<String, String>> getVehicles(String roadName, int way) {
        //for test
        List<Map<String, String>> test = traffic.get(roadName).get(Integer.toString(way));
        //end for test
        return traffic.get(roadName).get(Integer.toString(way));
    }

    // About Vehicles
    public int getPos(Map<String, String> vehicle) {
        return Integer.parseInt(vehicle.get(Constants.POSISITION));
    }

    public int getLength(Map<String, String> vehicle) {
        return Integer.parseInt(vehicle.get(Constants.LENGHT));
    }

    public int howMuchMove(Map<String, String> vehicleFirst, Map<String, String> vehicleSecond) {
        return Math.min(getPos(vehicleSecond) - getPos(vehicleFirst)
                - getLength(vehicleFirst) - Constants.DEFAULT_SEPARATION,
                Constants.DEFAULT_SPEED);
    }

    // se guardan los cambios porque se considera el map un objeto.
    public void moveCar(Map<String, String> vehicleFirst, Map<String, String> vehicleSecond) {
        int distance = howMuchMove(vehicleFirst, vehicleSecond);
        int newPos = Integer.parseInt(vehicleSecond.get(Constants.POSISITION)) - distance;
        vehicleSecond.replace(Constants.POSISITION, Integer.toString(newPos));
    }

    public void moveCar(Map<String, String> vehicle, int distance) {
        int newPos = Integer.parseInt(vehicle.get(Constants.POSISITION)) - distance;
        vehicle.replace(Constants.POSISITION, Integer.toString(newPos));
    }

    public void moveCar(Map<String, String> vehicle) {
        vehicle.replace(Constants.POSISITION, Integer.toString(0));
    }

    // About Roads
    public boolean isRootRoad(String roadName) {
        return roads.get(roadName).get(Constants.CROSSROAD_START).equals(Constants.ROOT);
    }

    public boolean isLeafRoad(String roadName) {
        return roads.get(roadName).get(Constants.CROSSROAD_END).equals(Constants.LEAF);
    }

    public int getRoadCapacity(String roadName) {
        String capacity = roads.get(roadName).get(Constants.CAPACITY);
        return Integer.parseInt(capacity);
    }

    // Si todas sus vías están llenas, la calle lo está
    // Estar llena = No se puede añadir otro coche teniendo en cuenta el último
    public boolean isRoadFull(String roadName) {
        boolean full = true;
        for (int way = 1; way <= getNumWays(roadName); way++) {
            if (!isWayFull(roadName, way)) {
                full = false;
                break;
            }
        }
        return full;
    }

    // Vemos si en una via es posible añadir un coche
    // Dependería del coche que se va a meter
    public boolean isWayFull(String roadName, int way) {
        boolean full = false;
        int lastPosition;
        if (!isWayEmpty(roadName, way)) {
            // Tomamos la posición de su último coche

            List<Map<String, String>> vehicles = getVehicles(roadName, way);
            Map<String, String> last_car = vehicles.get(vehicles.size() - 1);
            lastPosition = getPos(last_car);

            //Map<String,String> coche = getVehicles(roadName, way).
            //get(getVehicles(roadName, way).size()-1);
            //lastPosition = getPos(coche);
            //endtest
            //lastPosition = getPos(getVehicles(roadName, way).
            //     get(getVehicles(roadName, way).size()-1));
            if (lastPosition >= Integer.parseInt(roads.get(roadName).get(Constants.CAPACITY))
                    - Integer.parseInt(Constants.DEFAULT_LENGTH)) {
                full = true;
            }
        }
        return full;
    }

    // Vemos si una via está vacía
    public boolean isWayEmpty(String raodName, int way) {
        return getVehicles(raodName, way).isEmpty();
    }

    public int getNumWays(String roadName) {
        return Integer.parseInt(roads.get(roadName).get(Constants.NUM_ROADS));
    }

}
