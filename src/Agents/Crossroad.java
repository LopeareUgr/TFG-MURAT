/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Utilities.AgentStates;
import Utilities.Constants;
import Utilities.JSON;
import SuperAgente2.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raúl López Arévalo
 */
public class Crossroad extends Agent {

    private Map<String, Map<String, String>> roads;
    private Map<String, Map<String, String>> states;
    private int current_state; // estado de configuración actual
    private int min_time_state; // tiempo mínimo de cada estado
    private int current_time_state; // tiempo que lleva el estado actual
    private int total_time_states;
    private boolean state_changes;

    Map<String, List<String>> roadSections; // No utilizar esta

    private Map<String, Map<String, String>> inform;
    private List<Map<String, Map<String, String>>> informs;
    private List<Map<String, Map<String, String>>> last_informs;
    //
    private JsonObject informs_prepared;
    private int time;

    public Crossroad(AgentID aid, Map<String, Map<String, String>> states, Map<String, List<String>> roadSections, Map<String, Map<String, String>> roads) throws Exception {
        super(aid);
        this.states = new HashMap();
        this.states = states;
        this.roadSections = new HashMap();
        this.roadSections = roadSections;
        this.roads = new HashMap();
        this.roads = roads;
        this.current_state = 1;
        this.min_time_state = 4;
        this.current_time_state = 0;
        this.state_changes = false;
        this.inform = new HashMap();
        this.informs = new ArrayList();
        this.informs_prepared = new JsonObject();
        last_informs = new ArrayList();
        this.time = 0;

        for (Map.Entry<String, Map<String, String>> state : this.states.entrySet()) {
            for (Map.Entry<String, String> properties : state.getValue().entrySet()) {
                if (properties.getKey().equals(Constants.TIME)) {
                    total_time_states += Integer.parseInt(properties.getValue());
                }
            }
        }

        this.state = AgentStates.WAIT_SUBSCRIBE;
    }

    @Override
    public void execute() {
        while (!this.stop) {
            switch (this.state) {
                case AgentStates.WAIT_SUBSCRIBE: // centralita para los informes
                    this.waitSubscribeSwitchboard();
                    break;
                case AgentStates.SUBSCRIBE: // sensores
                    subscribe();
                    break;
                case AgentStates.WAIT_SUBSCRIBE_SIMULATE: // simulate para los estados
                    waitSubscribeSimulate();
                    break;
                case AgentStates.LISTEN_SENSORS:
                    listenSensors();
                    break;
                case AgentStates.THINK:
                    think();
                    break;
                case AgentStates.CHANGE_SEMAPHORES:
                    changeSemaphores();
                    break;
                case AgentStates.SEND_STATE:
                    sendCurrentState();
                    break;
                case AgentStates.SEND_INFORM:
                    sendInforms();
                    break;
                case AgentStates.FINALIZE:
                    this.stop = true;
                    break;
            }
        }
    }

    public void waitSubscribeSwitchboard() {
        inbox = receiveMessage();
        content = JSON.content(Constants.NOT_NOW);
        buildMessage(inbox.getSender(), content, ACLMessage.AGREE);
        this.send(outbox);
        this.state = AgentStates.SUBSCRIBE;
    }

    // Sensores
    public void subscribe() {
        for (Map.Entry<String, List<String>> roadSections : roadSections.entrySet()) {
            for (String roadSection : roadSections.getValue()) {
                content = JSON.content(Constants.TRAFFIC_INFORM);
                buildMessage(new AgentID(roadSection + Constants.START), content, ACLMessage.SUBSCRIBE);
                this.send(outbox);

                inbox = receiveMessage();

                buildMessage(new AgentID(roadSection + Constants.END), content, ACLMessage.SUBSCRIBE);
                this.send(outbox);

                inbox = receiveMessage();
            }
        }
        this.state = AgentStates.WAIT_SUBSCRIBE_SIMULATE;
    }

    public void waitSubscribeSimulate() {

        inbox = receiveMessage();

        content = JSON.content(Integer.toString(current_state));
        buildMessage(new AgentID(Constants.SIMULATOR_NAME), content, ACLMessage.CONFIRM);
        this.send(outbox);
        this.state = AgentStates.LISTEN_SENSORS;
    }

    // meter este finalize en el propio método finalize
    // Espera los datos de los senosres o el aviso para finalizar
    public void listenSensors() {
        time++;
        //System.out.println("Constants: " + time);
        current_time_state++;
        inbox = receiveMessage();
        //System.out.println(getName() + " esperando sensores");
        // Distinguir si es el SWITCHBOARD diciendo que acabemos o sensores
        content = JSON.readContent(inbox.getContent());
        if (content.equals(Constants.ENDS)) {

            this.state = AgentStates.FINALIZE;

        } else {
            if (Constants.USE_SENSORS) {
                int numSensors = (roadSections.get(Constants.ROADS_IN).size()) * 2;
                // Nuevo inform para que no se pisotee
                buildCurrentInform(inbox);

                for (int sensor = 0; sensor < numSensors - 1; sensor++) {
                    inbox = receiveMessage();
                    buildCurrentInform(inbox);
                }
            } else {
                int numSensors = (roadSections.get(Constants.ROADS_IN).size());
                // Nuevo inform para que no se pisotee
                buildCurrentInformSim(inbox);

                for (int sensor = 0; sensor < numSensors - 1; sensor++) {
                    inbox = receiveMessage();
                    buildCurrentInformSim(inbox);
                }
            }

            calculateOcupation();
            this.state = AgentStates.THINK;
        }
    }

    public int processLastInforms() {
        // Analiza los informes del último ciclo de semáforos:
        // Calcular que calle ha tenido más cantidad de IN que de OUT.
        // Averiguar que estados de semáforos le dan prioridad y ajustar los tiempos.
        Map<String, Map<String, Integer>> roads_data = new HashMap();
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            if (road.getValue().get(Constants.CROSSROAD_END).equals(getName())) {
                Map<String, Integer> data = new HashMap();
                data.put(Constants.INTENSITY, 0);
                data.put(Constants.PER_OCCUPATION, 0);
                data.put(Constants.SCORE, 0);
                roads_data.put(road.getKey(), data);
            }
        }
        for (Map<String, Map<String, String>> inform : last_informs) {
            for (Map.Entry<String, Map<String, String>> road : inform.entrySet()) {
                int intensity = I(road.getValue().get(Constants.V_OUT));
                int occupation = I(road.getValue().get(Constants.PER_OCCUPATION));

                intensity += roads_data.get(road.getKey()).get(Constants.INTENSITY);
                occupation += roads_data.get(road.getKey()).get(Constants.PER_OCCUPATION);

                roads_data.get(road.getKey()).replace(Constants.INTENSITY, intensity);
                roads_data.get(road.getKey()).replace(Constants.PER_OCCUPATION, occupation);
            }
        }

        for (Map.Entry<String, Map<String, Integer>> road : roads_data.entrySet()) {
            int intensity = road.getValue().get(Constants.INTENSITY);
            int occupation = road.getValue().get(Constants.PER_OCCUPATION);

            if (intensity != 0 && occupation != 0) {
                occupation /= total_time_states;
                int value = intensity * 100 / occupation;
                road.getValue().replace(Constants.SCORE, value);
            }

        }
// mirar esto
        String worst_road = Constants.EMPTY;
        int last_score = Constants.INF;
        for (Map.Entry<String, Map<String, Integer>> road : roads_data.entrySet()) {
            if (road.getValue().get(Constants.SCORE) < last_score
                    && road.getValue().get(Constants.SCORE) != 0) {
                worst_road = road.getKey();
                last_score = road.getValue().get(Constants.SCORE);
            }
        }

        int status_choosen;
        if (!worst_road.equals(Constants.EMPTY)) {
            status_choosen = getBestState(worst_road);
        } else {
            status_choosen = 0;
        }

        return status_choosen;
    }

    // Lógica para decidir si cambiar de configuración o no.
    public void think() {

        List<String> cong_roads = getCongRoads();

        // Si hay alguna calle con congestión
        if (cong_roads.size() > 0) {
            int state_choosen;
            // Si no hay ninguna configuración en común
            state_choosen = getBestCommomState(cong_roads);

            if (state_choosen == 0) {
                // Calcular de las calles la que está peor
                String worst_road = getWorstRoad(cong_roads);
                state_choosen = getBestState(worst_road);
            }
            if (Constants.LOGIC) {
                adjustTimes(state_choosen);
            }
        }

        if (last_informs.size() == total_time_states) {
            int status_for_prio = processLastInforms();
            if (status_for_prio != 0) {
                if (Constants.LOGIC) {
                    adjustTimes(status_for_prio);
                }
            }
            last_informs.clear();
        }

        // Pasar de un estado al siguiente
        if (current_time_state >= getStatusTime()) {
            if (current_state + 1 > states.size()) {
                current_state = 1;
            } else {
                current_state++;
            }
            current_time_state = 0;
            state_changes = true;
            this.state = AgentStates.CHANGE_SEMAPHORES;
        } else {
            this.state = AgentStates.SEND_STATE;
        }

    }

    public void changeSemaphores() {
        if (state_changes) {
            for (Map.Entry<String, String> semaphore : states.get(Integer.toString(current_state)).entrySet()) {
                if (!semaphore.getValue().equals("") && !semaphore.getKey().equals(Constants.TIME)) {
                    content = JSON.content(semaphore.getValue());
                    buildMessage(new AgentID(this.getName() + semaphore.getKey()), content, ACLMessage.REQUEST);
                    send(outbox);
                    inbox = receiveMessage();
                }
            }
            state_changes = false;
        }
        this.state = AgentStates.SEND_STATE;
    }

    public void sendCurrentState() {
        String value = Integer.toString(current_state);
        content = JSON.buildContent(Constants.STATE, value);
        buildMessage(new AgentID(Constants.SIMULATOR_NAME), content, ACLMessage.INFORM);
        send(outbox);
        this.state = AgentStates.SEND_INFORM;
    }

    public void sendInforms() {

        try {
            String value;
            addInform(inform);
            //printCurrentInform();
            //printInforms();
            if (time % Constants.TIME_INFORM == 0) {
                prepareInforms();
                value = informs_prepared.toString();
                makeRow();
                System.out.println("Enviando informe " + getName());
            } else {
                value = Constants.EMPTY;
            }

            content = JSON.buildContent(Constants.INFORM, value);
            buildMessage(new AgentID(Constants.SWITCHBOARD_NAME), content, ACLMessage.INFORM);
            send(outbox);

            if (time % Constants.TIME_INFORM == 0) {
                cleanInforms();
            }

            this.state = AgentStates.LISTEN_SENSORS;
        } catch (Exception ex) {
            Logger.getLogger(SuperAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void finalize() {
        content = JSON.content(Constants.ENDS);
        buildMessage(new AgentID(Constants.SWITCHBOARD_NAME),
                content, ACLMessage.AGREE);
        send(outbox);
        ////quitarSystem.out.println("cruce terminado.");
        // Los agentes cruces van a finalizar también a los semáforos.
        content = JSON.content(Constants.ENDS);
        for (Map.Entry<String, String> semaphore : states.get("1").entrySet()) {
            if (!semaphore.getValue().equals("") && !semaphore.getKey().equals(Constants.TIME)) {
                buildMessage(new AgentID(this.getName() + semaphore.getKey()), content, ACLMessage.REQUEST);
                send(outbox);
                inbox = receiveMessage();
            }
        }
        super.finalize();

    }

    // **************************** Logic **************************************
    public List<String> getCongRoads() {
        List<String> roads_cong = new ArrayList();

        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            if (roads.get(road.getKey()).get(Constants.CROSSROAD_END).equals(getName())) {
                if (getPerOccupation(road.getKey()) > Constants.CONGESTION) {
                    roads_cong.add(road.getKey());
                }
            }
        }
        return roads_cong;
    }

    public int getBestCommomState(List<String> cong_roads) {
        Map<String, Integer> states_score = new HashMap();
        Map<String, List<String>> road_valid_state = new HashMap();
        // Guardamos para cada calle sus configuraciones válidas
        for (String road : cong_roads) {
            String dir = roads.get(road).get(Constants.DIR_END);
            List<String> valid_states = new ArrayList();
            for (Map.Entry<String, Map<String, String>> state : states.entrySet()) {
                states_score.put(state.getKey(), 0);
                if (state.getValue().get(dir).equals(Constants.ALLOWED)) {
                    valid_states.add(state.getKey());
                }
            }
            road_valid_state.put(road, valid_states);
        }
        // Puntuamos las configuraciones. Más coincidencia más puntos
        for (Map.Entry<String, List<String>> road1 : road_valid_state.entrySet()) {
            for (String state : road1.getValue()) {
                for (Map.Entry<String, List<String>> road2 : road_valid_state.entrySet()) {
                    // si la calle es distinta de ella misma
                    if (!road2.getKey().equals(road1.getKey())) {
                        for (String state2 : road2.getValue()) {
                            if (state.equals(state2)) {
                                int value = states_score.get(state);
                                states_score.replace(state, ++value);
                            }
                        }
                    }
                }
            }
        }
        int last_score = -1;
        int score;
        String state_choosen = Constants.ZERO;
        // Elegimos el estado que tenga más coincidencias sin ser 0
        for (Map.Entry<String, Integer> state : states_score.entrySet()) {
            score = state.getValue();
            if (score > last_score && score != 0) {
                last_score = score;
                state_choosen = state.getKey();
            }
        }
        return I(state_choosen);
    }

    public String getWorstRoad(List<String> cong_roads) {
        String worst_road = Constants.EMPTY;
        double last_perc = 0;
        for (Map.Entry<String, Map<String, String>> road : roads.entrySet()) {
            if (roads.get(road.getKey()).get(Constants.CROSSROAD_END).equals(getName())) {
                if (getPerOccupation(road.getKey()) > last_perc) {
                    last_perc = getPerOccupation(road.getKey());
                    worst_road = road.getKey();
                }
            }
        }
        return worst_road;
    }

    public int getBestState(String road) {
        List<String> road_valid_states = new ArrayList();
        Map<String, Integer> states_score = new HashMap();
        int state_choosen = 0;
        int i = 0;

        // Obtenemos las configuraciónes válidas
        String dir = roads.get(road).get(Constants.DIR_END);
        for (Map.Entry<String, Map<String, String>> state : states.entrySet()) {
            if (state.getValue().get(dir).equals(Constants.ALLOWED)) {
                road_valid_states.add(state.getKey());
            }
        }
        if (road_valid_states.size() == 1) {
            state_choosen = I(road_valid_states.get(0));
        } else {
            // Calcular la cercanía de cada estado
            for (String stat : road_valid_states) {
                states_score.put(stat, 0);
                int state = I(stat);
                i = 0;
                if (state == current_state) {
                    state_choosen = state;
                    break;
                }
                while (state != current_state) {
                    state--;
                    if (state == 0) {
                        state = states.size();
                    }
                    i++;
                }
                states_score.replace(stat, i);
            }

            if (state_choosen == 0) {
                // Elegimos el estado más cercano al actual
                int last_score = Constants.INF;
                for (Map.Entry<String, Integer> state : states_score.entrySet()) {
                    int score = state.getValue();
                    if (score < last_score) {
                        last_score = score;
                        state_choosen = I(state.getKey());
                    }
                }
            }
        }
        return state_choosen;
    }

    public void adjustTimes(int prio_state) {
        int time_added = 0;
        int stime;
        for (Map.Entry<String, Map<String, String>> state : states.entrySet()) {
            stime = I(state.getValue().get(Constants.TIME));
            if (stime > min_time_state
                    && I(state.getKey()) != prio_state) {
                time_added++;
                state.getValue().replace(Constants.TIME, Integer.toString(stime - 1));
            }
        }

        stime = I(states.get(Integer.toString(prio_state)).get(Constants.TIME));
        states.get(Integer.toString(prio_state)).replace(Constants.TIME, Integer.toString(stime + time_added));
    }

    // Obtener el tiempo establecido del estado actual de semáforos.
    public int getStatusTime() {
        String time = this.states.get(Integer.toString(current_state)).get(Constants.TIME);
        return Integer.parseInt(time);
    }

    // Get occupation percentage
    public double getPerOccupation(String roadName) {

        String occupation = roads.get(roadName).get(Constants.OCCUPATION);
        String capacity = roads.get(roadName).get(Constants.CAPACITY);
        String num_ways = roads.get(roadName).get(Constants.NUM_ROADS);

        return D(occupation) / (D(capacity) * D(num_ways)) * 100;

    }

    public Map<String, Map<String, String>> getRoadsIn() {
        return null;
    }

    // **************************** Informs ************************************
    // Guardar num vehiculos y ocupación de cada calle (util para cruces)
    public void calculateOcupation() {

        String v_in, v_out, oc_in, oc_out, road_name, current_v, current_oc;
        for (Map.Entry<String, Map<String, String>> road : inform.entrySet()) {
            v_in = road.getValue().get(Constants.V_IN);
            v_out = road.getValue().get(Constants.V_OUT);
            oc_in = road.getValue().get(Constants.OC_IN);
            oc_out = road.getValue().get(Constants.OC_OUT);

            road_name = road.getKey();

            current_v = roads.get(road_name).get(Constants.VEHICLES);
            current_oc = roads.get(road_name).get(Constants.OCCUPATION);

            current_v = plus(minus(current_v, v_out), v_in);
            current_oc = plus(minus(current_oc, oc_out), oc_in);

            roads.get(road_name).replace(Constants.VEHICLES, current_v);
            roads.get(road_name).replace(Constants.OCCUPATION, current_oc);
        }
    }

    public int I(String num) {
        return Integer.parseInt(num);
    }

    public double D(String num) {
        return Double.parseDouble(num);
    }

    public String minus(String x, String y) {
        int num_x = Integer.parseInt(x);
        int num_y = Integer.parseInt(y);

        return Integer.toString(num_x - num_y);
    }

    public String plus(String x, String y) {
        int num_x = Integer.parseInt(x);
        int num_y = Integer.parseInt(y);

        return Integer.toString(num_x + num_y);
    }

    // Formar fila para mandar informes
    public void makeRow() {
        int t = Integer.parseInt(getName().replace("C", ""));
        //System.out.println(getName() + " " + t);
        this.sleep(t * 100);
    }

    // Limpiar informes a mandar
    public void cleanInforms() {
        informs.clear();
        /*while (informs_prepared.names().size() > 0) {
        List<String> keys = informs_prepared.names();
        /*for (int i = 0; i < keys.size(); i++) {
        System.out.println(i);
        informs_prepared.remove(keys.get(i));
        }
        int i = 0;
        for(String f : informs_prepared.names()){
            i = Integer.parseInt(f);
            informs_prepared.remove(f);
        }
    }*/
        informs_prepared = new JsonObject();
    }

    // Prepara la lista de informes para ser mandada
    public void prepareInforms() {
        int index = 1;
        // All
        for (Map<String, Map<String, String>> inf : informs) {
            // Each inform
            JsonObject j_inf = new JsonObject();
            for (Map.Entry<String, Map<String, String>> road : inf.entrySet()) {
                // Inform data
                JsonObject jdata = new JsonObject();
                for (Map.Entry<String, String> data : road.getValue().entrySet()) {
                    jdata.add(data.getKey(), data.getValue());
                }
                j_inf.add(road.getKey(), jdata);
            }
            this.informs_prepared.add(Integer.toString(index), j_inf);
            index++;
        }
    }

    // Añade el informe actual a la lista de informes a mandar a la central
    public void addInform(Map<String, Map<String, String>> inform) {
        Map<String, Map<String, String>> inform_aux = new HashMap();
        for (Map.Entry<String, Map<String, String>> inf : inform.entrySet()) {
            Map<String, String> roadInform = new HashMap();
            for (Map.Entry<String, String> property : inf.getValue().entrySet()) {
                roadInform.put(property.getKey(), property.getValue());
            }
            inform_aux.put(inf.getKey(), roadInform);
        }
        informs.add(inform_aux);
        addInformInLast(inform_aux);
    }

    public void addInformInLast(Map<String, Map<String, String>> inform) {
        for (Map.Entry<String, Map<String, String>> road : inform.entrySet()) {
            String value = Integer.toString((int) getPerOccupation(road.getKey()));
            road.getValue().put(Constants.PER_OCCUPATION, value);
        }
        last_informs.add(inform);
    }

    // Mediante los mensajes de todos los sensores genera el informe actual 
    public void buildCurrentInform(ACLMessage inbox) {
        // cogemos el sender
        String sender_name = inbox.getSender().getLocalName();
        // procesamos contenido
        content = JSON.readContent(inbox.getContent());
        JsonObject content_json = Json.parse(content).asObject();
        for (String road : this.roadSections.get(Constants.ROADS_IN)) {

            Map<String, String> roadInform = new HashMap();
            // Damos con la calle
            if (sender_name.contains(road)) {
                // ¿Tiene ya la calle añadida?
                if (inform.containsKey(road)) {
                    if (sender_name.contains(Constants.START)) {
                        inform.get(road).put(Constants.V_IN,
                                content_json.get(Constants.V_IN).toString().replace("\"", ""));
                        inform.get(road).put(Constants.OC_IN,
                                content_json.get(Constants.OC_IN).toString().replace("\"", ""));
                    } else {
                        inform.get(road).put(Constants.V_OUT,
                                content_json.get(Constants.V_OUT).toString().replace("\"", ""));
                        inform.get(road).put(Constants.OC_OUT,
                                content_json.get(Constants.OC_OUT).toString().replace("\"", ""));
                    }
                } else {
                    if (sender_name.contains(Constants.START)) {
                        roadInform.put(Constants.V_IN,
                                content_json.get(Constants.V_IN).toString().replace("\"", ""));
                        roadInform.put(Constants.OC_IN,
                                content_json.get(Constants.OC_IN).toString().replace("\"", ""));
                    } else {
                        roadInform.put(Constants.V_OUT,
                                content_json.get(Constants.V_OUT).toString().replace("\"", ""));
                        roadInform.put(Constants.OC_OUT,
                                content_json.get(Constants.OC_OUT).toString().replace("\"", ""));
                    }
                    inform.put(road, roadInform);
                }
            }
        }
    }

    public void buildCurrentInformSim(ACLMessage inbox) {
        // procesamos contenido
        content = JSON.readContent(inbox.getContent());
        JsonObject content_json = Json.parse(content).asObject();
        Map<String, String> roadInform = new HashMap();
        roadInform.put(Constants.V_IN,
                content_json.get(Constants.V_IN).toString().replace("\"", ""));
        roadInform.put(Constants.OC_IN,
                content_json.get(Constants.OC_IN).toString().replace("\"", ""));
        roadInform.put(Constants.V_OUT,
                content_json.get(Constants.V_OUT).toString().replace("\"", ""));
        roadInform.put(Constants.OC_OUT,
                content_json.get(Constants.OC_OUT).toString().replace("\"", ""));
        inform.put(content_json.get(Constants.ROAD_NAME).toString().replace("\"", ""), roadInform);
    }

    public void printCurrentInform() {
        System.out.println("*************** INFORM " + getName() + " *********************");
        for (Map.Entry<String, Map<String, String>> road : inform.entrySet()) {
            System.out.println(getName() + road.getKey());
            System.out.println(getName() + road.getValue());
        }
        System.out.println("********************************************");
    }

    public void printInforms() {
        int i = 1;
        for (Map<String, Map<String, String>> informx : informs) {
            System.out.println("*************** INFORM " + i + getName() + " *********************");
            for (Map.Entry<String, Map<String, String>> road : informx.entrySet()) {
                System.out.println(getName() + road.getKey());
                System.out.println(getName() + road.getValue());
            }
            i++;
            System.out.println("********************************************");
        }
    }

}
