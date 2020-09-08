/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Agents.AgentServerMain;
import Agents.Simulator;
import Agents.Sensor;
import Agents.Semaphore;
import Agents.Crossroad;
import Utilities.AgentStates;
import Utilities.Constants;
import Utilities.JSON;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Raúl López Arévalo
 */
public class Switchboard extends AgentServerMain {

    private Map<String, Map<String, String>> roads;
    private List<Map<String, String>> roadSections; // eliminar
    private Map<String, Map<String, List<String>>> crossroads;
    private Map<String, Map<String, Map<String, String>>> crossroads_states;
    private Map<String, List<List<Map<String, Map<String, String>>>>> informs;
    private Map<String, Map<String, List<Integer>>> data_output;
    private List<JsonObject> vehicles_data;
    private List<Integer> total_cars;
    int time = 0;

    public Switchboard(AgentID aid) throws Exception {
        super(aid);
        roads = new HashMap();
        roadSections = new ArrayList();
        crossroads = new HashMap();
        crossroads_states = new HashMap();
        informs = new HashMap();
        data_output = new HashMap();
        total_cars = new ArrayList();
        vehicles_data = new ArrayList();
        this.state = AgentStates.GET_DATA;
    }

    public void buildRoadSections() { // dividir aquí la obtención de datos (crear interfaz)
        BufferedReader bufferRead = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            bufferRead = new BufferedReader(new FileReader(Constants.ROAD_SECTIONS_PATH));
            line = bufferRead.readLine();
            String[] header = line.split(cvsSplitBy);

            while ((line = bufferRead.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);

                // Eliminar esto
                Map<String, String> roadSection = new HashMap();
                for (int i = 0; i < header.length; i++) {
                    roadSection.put(header[i], values[i]);
                }
                roadSections.add(roadSection);
                //------------------------------------------------

                //Construcción de allRoads para el simulador (Más eficiente)
                Map<String, String> properties = new HashMap();
                for (int i = 1; i < header.length; i++) {
                    properties.put(header[i], values[i]);
                }
                properties.put(Constants.OC_IN, Constants.ZERO);
                properties.put(Constants.OC_OUT, Constants.ZERO);
                properties.put(Constants.V_IN, Constants.ZERO);
                properties.put(Constants.V_OUT, Constants.ZERO);
                properties.put(Constants.VEHICLES, Constants.ZERO);
                properties.put(Constants.OCCUPATION, Constants.ZERO);
                roads.put(values[0], properties);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferRead != null) {
                try {
                    bufferRead.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void buildCrossroads() {
        String cStart = "";
        String cEnd = "";

        // Usar la estructura de map en lugar de la lista
        for (Map.Entry<String, Map<String, String>> entry : roads.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();

        }
        //----------------------------------------------------------------------

        for (Map<String, String> roadSection : roadSections) {
            cStart = roadSection.get(Constants.CROSSROAD_START);
            if (!cStart.equals(Constants.ROOT)) {
                if (!crossroads.containsKey(cStart)) {
                    
                    Map<String, List<String>> roadSectionsES = new HashMap();
                    List<String> roadSectionsS = new ArrayList();
                    List<String> roadSectionsE = new ArrayList();

                    roadSectionsS.add(roadSection.get(Constants.ID));

                    roadSectionsES.put(Constants.ROADS_OUT, roadSectionsS);
                    roadSectionsES.put(Constants.ROADS_IN, roadSectionsE);
                    crossroads.put(cStart, roadSectionsES);
                } else {
                    crossroads.get(cStart).get(Constants.ROADS_OUT).add(roadSection.get(Constants.ID));
                }
            }
            cEnd = roadSection.get(Constants.CROSSROAD_END);
            if (!cEnd.equals(Constants.LEAF)) {
                if (!crossroads.containsKey(cEnd)) {
                    
                    Map<String, List<String>> roadSectionsES = new HashMap();
                    List<String> roadSectionsS = new ArrayList();
                    List<String> roadSectionsE = new ArrayList();

                    roadSectionsE.add(roadSection.get(Constants.ID));

                    roadSectionsES.put(Constants.ROADS_OUT, roadSectionsS);
                    roadSectionsES.put(Constants.ROADS_IN, roadSectionsE);
                    crossroads.put(cEnd, roadSectionsES);
                } else {
                    crossroads.get(cEnd).get(Constants.ROADS_IN).add(roadSection.get(Constants.ID));
                }
            }
        }
    }

    public void buildCrossroadStates() {
        BufferedReader bufferRead = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            bufferRead = new BufferedReader(new FileReader(Constants.CROSSROAD_STATES_PATH));
            line = bufferRead.readLine();
            String[] header = line.split(cvsSplitBy);

            while ((line = bufferRead.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                Map<String, String> semaphoreStates = new HashMap();
                Map<String, Map<String, String>> States = new HashMap();
                for (int i = 2; i < values.length; i++) {
                    semaphoreStates.put(header[i], values[i]);
                }
                if (!this.crossroads_states.containsKey(values[0])) {
                    States.put(values[1], semaphoreStates);
                    crossroads_states.put(values[0], States);
                } else {
                    crossroads_states.get(values[0]).put(values[1], semaphoreStates);
                }
            }
        } catch (FileNotFoundException e) {
            //.out.println("File not found");
            //.out.println(e);
            e.printStackTrace();
        } catch (IOException e) {
            //.out.println("Exception: ");
            //.out.println(e);
            e.printStackTrace();
        } finally {
            if (bufferRead != null) {
                try {
                    bufferRead.close();
                } catch (IOException e) {
                    //.out.println("Exception closing file");
                    //.out.println(e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void createCrossroads() {
        Crossroad[] agentcrossroads = new Crossroad[crossroads.size()];
        
        int i = 0;
        String in, out;
        for (Map.Entry<String, Map<String, List<String>>> cross : crossroads.entrySet()) {
            
            // Construcción que pretende sustituir al roadSections:
            Map<String, Map<String, String>> crossroad_roads = new HashMap();
            for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
                in = road.getValue().get(Constants.CROSSROAD_END);
                out = road.getValue().get(Constants.CROSSROAD_START);
                
                if(cross.getKey().equals(in) || cross.getKey().equals(out)){
                    crossroad_roads.put(road.getKey(), road.getValue());
                }
            }
            // ****************************************************
            try {
                agentcrossroads[i] = new Crossroad(new AgentID(cross.getKey()), crossroads_states.get(cross.getKey()), cross.getValue(), crossroad_roads);
                agentcrossroads[i].start();
                i++;
            } catch (Exception ex) {
                Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void createSensors() {
        Sensor[] sensors = new Sensor[roadSections.size() * 2];
        String id;
        int i = 0;
        for (Map<String, String> roadSection : roadSections) {
            try {
                id = roadSection.get("ID") + "Start";
               
                sensors[i] = new Sensor(new AgentID(id), roadSection.get("CrossroadStart"), roadSection.get("CrossroadEnd"));
                id = roadSection.get("ID") + "End";
            
                sensors[i + 1] = new Sensor(new AgentID(id), roadSection.get("CrossroadStart"), roadSection.get("CrossroadEnd"));
                sensors[i].start();
                sensors[i + 1].start();
            } catch (Exception ex) {
                Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void createSemaphores() { // Se puede hacer del tirón cuando se está leyendo el csv de los cruces
        //Semaphore [] semaphores = new Semaphore[roadSections.size()];
        ArrayList<Semaphore> semaphores = new ArrayList<>();
        String currentCrossroad = "";
        String lastCrossroad = "";

        for (Map.Entry<String, Map<String, Map<String, String>>> crossroad : crossroads_states.entrySet()) {
            currentCrossroad = crossroad.getKey();
            if (!lastCrossroad.equals(currentCrossroad)) {
                for (Map.Entry<String, String> semaphore : crossroad.getValue().get("1").entrySet()) {
                    if (!semaphore.getValue().equals("") && !semaphore.getKey().equals(Constants.TIME)) {
                        try {
                            semaphores.add(new Semaphore(new AgentID(currentCrossroad + semaphore.getKey())));
                        } catch (Exception ex) {
                            Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                lastCrossroad = currentCrossroad;
            }
        }
        for (int semaphore = 0; semaphore < semaphores.size(); semaphore++) {
            semaphores.get(semaphore).start();
        }
    }

    public void createSimulator() {
        try {
            Simulator simulator = new Simulator(new AgentID(Constants.SIMULATOR_NAME), roadSections, crossroads, crossroads_states, roads);
            simulator.start();
        } catch (Exception ex) {
            Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initInforms() {
        for (Map.Entry<String, Map<String, List<String>>> crossroad : crossroads.entrySet()) {
            informs.put(crossroad.getKey(), new ArrayList());
        }
    }

    public void buildData() {
        this.buildRoadSections();
        this.buildCrossroads();
        this.buildCrossroadStates();
        this.initInforms();
        this.state = AgentStates.CREATE_AGENTS;
    }

    public void createAgents() {
        this.createSimulator();
        this.createSensors();
        this.createCrossroads();
        this.createSemaphores();

        this.state = AgentStates.SUBSCRIBE;
    }

    @Override
    public void execute() {

        while (!this.stop) {
            switch (this.state) {
                case AgentStates.GET_DATA:
                    this.buildData();
                    break;
                case AgentStates.CREATE_AGENTS:
                    this.createAgents();
                    break;
                case AgentStates.SUBSCRIBE: // informes de cruces
                    this.subscribe();
                    break;
                case AgentStates.WAIT_INFORMS:
                    this.waitInforms();
                    break;
                case AgentStates.ASK_TOTAL_CARS:
                    this.askVehicleData();
                    break;
                case AgentStates.PROCESS_INFORMS:
                    this.generateDataInform();
                    break;
                case AgentStates.GENERATE_INFORMS:
                    this.generateInforms();
                    break;

                case AgentStates.FINISHING_ALL:
                    this.finishingAll(); // este podría ser el finalize
                    break;
                case AgentStates.FINALIZE:
                    this.stop = true;
                    break;
            }
        }
    }

    public void subscribe() {
        // Subscribe all crossroads
        crossroads.entrySet().forEach((crossroad) -> {
            content = JSON.content(Constants.TRAFFIC_INFORM);
            this.buildMessage(new AgentID(crossroad.getKey()), content, ACLMessage.SUBSCRIBE);
            this.send(outbox);
        });
        // Wait response
        for (int cruce = 0; cruce < crossroads.size(); cruce++) {
            inbox = receiveMessage();
        }

        state = AgentStates.WAIT_INFORMS;
    }

    public void waitInforms() {
        time++;
        //System.out.println("S: " + time);
        inbox = receiveMessage();
        if (JSON.readLabel(inbox.getContent()).equals(Constants.ENDS)) {
            state = AgentStates.PROCESS_INFORMS;
        } else {
            checkData(inbox);
            for (int crossr = 0; crossr < crossroads.size() - 1; crossr++) {
                //System.out.println("Escucho posibles informes");
                inbox = receiveMessage();
                checkData(inbox);
            }
            state = AgentStates.ASK_TOTAL_CARS;
        }

        // 
    }

    public void askVehicleData() {

        content = JSON.buildContent(Constants.VEHICLES_DATA, Constants.EMPTY);
        buildMessage(new AgentID(Constants.SIMULATOR_NAME), content, ACLMessage.REQUEST);
        send(outbox);
        inbox = receiveMessage();
        content = JSON.readContent(inbox.getContent());
        
        JsonObject vehicle_data = Json.parse(content).asObject();
        vehicles_data.add(vehicle_data);
        if(time%400 == 0){
            //System.out.println("debug");
        }
        
        //total_cars.add(Integer.parseInt(content));
        state = AgentStates.WAIT_INFORMS;
    }

    public void checkData(ACLMessage inbox) {
        content = JSON.readContent(inbox.getContent());
        if (!content.equals(Constants.EMPTY)) {

            System.out.println("recibo informe valido");
            prepareDataInform(inbox);
        }
    }

    // Transforma el contenido a HashMap.
    // Guarda en informs para cada agente sus informes de cada TIME_INFORM
    public void prepareDataInform(ACLMessage inbox) {
        // si peta aquí dentro significa que entra

        //asdf
        String sender = inbox.getSender().getLocalName();
        String string_data = JSON.readContent(Constants.INFORM, inbox.getContent());
        JsonObject j_time = Json.parse(string_data).asObject();

        //System.out.println("Esto es lo que le llega a la central: " + j_time);
        // Cruce: 1-TIME_INFORM : Calle : properties
        List<String> j_time_k = j_time.names();

        String key_time, key_road, key_prop;
        JsonObject j_road = new JsonObject();
        JsonObject j_properties = new JsonObject();
        List<Map<String, Map<String, String>>> roadsList = new ArrayList();
        Map<String, Map<String, Map<String, String>>> times = new HashMap();
        // Each second
        for (int i = 0; i < j_time.size(); i++) {
            //System.out.println("Time: " + j_time_k.get(i));

            key_time = j_time_k.get(i);
            j_road = j_time.get(key_time).asObject();
            List<String> j_road_k = j_road.names();

            // Each road
            Map<String, Map<String, String>> roads = new HashMap();

            for (int j = 0; j < j_road_k.size(); j++) {
                //System.out.println("Road: " + j_road_k.get(j));

                key_road = j_road_k.get(j);
                j_properties = j_road.get(key_road).asObject();
                List<String> j_properties_k = j_properties.names();

                // Each property
                Map<String, String> properties = new HashMap();
                for (int k = 0; k < j_properties.size(); k++) {
                    key_prop = j_properties_k.get(k);
                    //System.out.println(key_prop + " = " + j_properties.get(key_prop).toString().replace("\"", ""));

                    properties.put(key_prop, j_properties.get(key_prop).toString().replace("\"", ""));
                }
                roads.put(key_road, properties);

            }
            roadsList.add(roads);
            times.put(key_time, roads);
        }
        informs.get(sender).add(roadsList);

        //System.out.println(informs);
    }

    // Prepara los datos a imprimir en el csv
    public void generateDataInform() {
        //Map<String,Map<String,List<Integer>>>
        int v_out = 0;
        double v_in = 0;

        for (Map.Entry<String, List<List<Map<String, Map<String, String>>>>> cross : informs.entrySet()) {
            data_output.put(cross.getKey(), new HashMap());

            List<Integer> l_v_out = new ArrayList();
            List<Integer> l_v_relation = new ArrayList();
            // Todos los informes
            for (List<Map<String, Map<String, String>>> informs : cross.getValue()) {

                // Cada informe. Cada tiempo.
                for (Map<String, Map<String, String>> inform : informs) {

                    // Cada Calle
                    for (Map.Entry<String, Map<String, String>> road : inform.entrySet()) {

                        // Con sus propiedades
                        for (Map.Entry<String, String> prop : road.getValue().entrySet()) {
                            if (prop.getKey().equals(Constants.V_OUT)) {
                                v_out += Integer.parseInt(prop.getValue());
                            } else if (prop.getKey().equals(Constants.V_IN)) {
                                v_in += Integer.parseInt(prop.getValue());
                            }
                        }
                    }

                }
                v_in = v_out / v_in *100;
                l_v_relation.add((int)v_in);
                l_v_out.add(v_out);
                v_out = 0;
                v_in = 0;
            }
            // Tiempo
            List<Integer> l_time = new ArrayList();
            int aux = Constants.TIME_START + Constants.TIME_INFORM;
            while (aux <= Constants.TIME_END) {
                l_time.add(aux);
                aux += Constants.TIME_INFORM;
            }
            
            
            /*for (int i = 1; i <= total_cars.size(); i++) {
                if (i % Constants.TIME_INFORM == 0) {
                    l_cars.add(total_cars.get(i - 1));
                }
            }*/
            
            // Vehicles Data
            List<Integer> l_time_driving = new ArrayList();
            List<Integer> l_cars = new ArrayList();
            List<Integer> l_max_time = new ArrayList();
            List<Integer> l_min_time = new ArrayList();
            int total_time_driving = 0;
            int aux_total_time_driving = 0;
            int total_cars = 0;
            
            int max_time = 0;
            int min_time = Constants.INF;
            int aux_time = 0;
            int total_in = 0;
            int total_out = 0;
            List<Integer> l_total_in = new ArrayList();
            List<Integer> l_total_out = new ArrayList();
            int i = 1;
            
            for (JsonObject v_data : vehicles_data) {
                // Max
                aux_time = v_data.get(Constants.MAX_TIME_DRIV).asInt();
                if(aux_time > max_time){
                    max_time = aux_time;
                }
                // Min
                aux_time = v_data.get(Constants.MIN_TIME_DRIV).asInt();
                if(aux_time < min_time){
                    min_time = aux_time;
                }
                // Total time and cars
                
                
                //aux_total_time_driving = v_data.get(Constants.TOTAL_TIME_DRIV).asInt();
                //total_time_driving += aux_total_time_driving;
                if((i) % Constants.TIME_INFORM == 0){
                    total_cars = v_data.get(Constants.TOTAL_CARS).asInt();
                    l_cars.add(total_cars);
                    
                    total_time_driving = v_data.get(Constants.TOTAL_TIME_DRIV).asInt();
                    l_time_driving.add(total_time_driving);
                    //total_time_driving = 0;
                    
                    // Acumulado
                    total_in = v_data.get("total_in").asInt();
                    total_out = v_data.get("total_out").asInt();
                    l_total_in.add(total_in);
                    l_total_out.add(total_out);
                }
                i++;
            }
            l_max_time.add(max_time);
            l_min_time.add(min_time);
            
            //int a = vehicles_data.get((vehicles_data.size()-1)).get(Constants.MIN_TIME_DRIV).asInt();
            //int b = vehicles_data.get((vehicles_data.size()-1)).get(Constants.MAX_TIME_DRIV).asInt();
            
            //data_output.get(cross.getKey()).put(Constants.V_RELATION, l_v_relation);
            // Se calcula aquí su acumulado
            data_output.get(cross.getKey()).put(Constants.TIME, l_time); 
            data_output.get(cross.getKey()).put(Constants.V_OUT, l_v_out);
            // Se recoge el valor puntual
            data_output.get(cross.getKey()).put(Constants.TOTAL_CARS, l_cars);
            // De todos los informes, volvemos a coger aquí el menor y mayor
            data_output.get(cross.getKey()).put(Constants.MAX_TIME_DRIV, l_max_time);
            data_output.get(cross.getKey()).put(Constants.MIN_TIME_DRIV, l_min_time);
            // Se recibe del SIM ya acumulado
            data_output.get(cross.getKey()).put(Constants.TOTAL_TIME_DRIV, l_time_driving);
            data_output.get(cross.getKey()).put("total_in", l_total_in);
            data_output.get(cross.getKey()).put("total_out", l_total_out);

        }
        state = AgentStates.GENERATE_INFORMS;
    }

    // Crear el fichero al final de la ejecución
    public void generateInforms() {
        int row_count = 0;
        int col_count = 0;

        for (Map.Entry<String, Map<String, List<Integer>>> crossroad : data_output.entrySet()) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(crossroad.getKey());

            for (Map.Entry<String, List<Integer>> data : crossroad.getValue().entrySet()) {
                Row r = sheet.createRow(row_count);
                Cell cell = r.createCell(col_count);

                cell.setCellValue(data.getKey());
                for (Integer value : data.getValue()) {
                    col_count++;
                    cell = r.createCell(col_count);
                    cell.setCellValue(value);
                }
                col_count = 0;
                row_count++;
            }

            row_count = 0;
            FileOutputStream outputStream;

            try {
                outputStream = new FileOutputStream(Constants.INFORMS_PATH + crossroad.getKey() + Constants.EXTENSION);
                workbook.write(outputStream);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /*XSSFWorkbook workbook1 = new XSSFWorkbook();
        XSSFSheet sheet1 = workbook1.createSheet("Java Books");
        Object[][] bookData = {
        {"Head First Java", "Kathy Serria", 79},
        {"Effective Java", "Joshua Bloch", 36},
        {"Clean Code", "Robert martin", 42},
        {"Thinking in Java", "Bruce Eckel", 35},};
        int rowCount1 = 0;
        
        for (Object[] aBook : bookData) {
        Row row = sheet1.createRow(++rowCount1);
        
        int columnCount = 0;
        
        for (Object field : aBook) {
        Cell cell = row.createCell(++columnCount);
        if (field instanceof String) {
        cell.setCellValue((String) field);
        } else if (field instanceof Integer) {
        cell.setCellValue((Integer) field);
        }
        }
        
        }
        
        FileOutputStream outputStream;
        try {
        outputStream = new FileOutputStream("Informes\\JavaBooks" + time + ".xlsx");
        workbook1.write(outputStream);
        } catch (FileNotFoundException ex) {
        Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        state = AgentStates.FINISHING_ALL;
    }



    public void finishingAll() {
        // Responde al simulador. Va a finalizar pero no ahora.
        content = JSON.content(Constants.ENDS);
        buildMessage(new AgentID(Constants.SIMULATOR_NAME),
                content, ACLMessage.AGREE);
        send(outbox);
        // Manda terminar a todos los sensores
        //quitarSystem.out.println("termina todos los sensores");
        content = JSON.content(Constants.ENDS);
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            buildMessage(new AgentID(road.getKey() + Constants.START),
                    content, ACLMessage.REQUEST);
            send(outbox);
            inbox = receiveMessage();

            buildMessage(new AgentID(road.getKey() + Constants.END),
                    content, ACLMessage.REQUEST);
            send(outbox);
            inbox = receiveMessage();
        }

        // Manda terminar a todos los cruces.
        // Estos a su vez terminan sus semáforos.
        //quitarSystem.out.println("Termina a todos los cruces");
        for (Map.Entry<String, Map<String, List<String>>> crossroad : crossroads.entrySet()) {
            buildMessage(new AgentID(crossroad.getKey()),
                    content, ACLMessage.REQUEST);
            send(outbox);
            inbox = receiveMessage();
        }
        //quitarSystem.out.println("switchboard antes de llamar al super");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Switchboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        //quitarSystem.out.println("despues de dormir");
        this.state = AgentStates.FINALIZE;
    }
}
