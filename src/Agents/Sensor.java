/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Utilities.AgentStates;
import Utilities.Constants;
import Utilities.JSON;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author Raúl López Arévalo
 */
public class Sensor extends Agent {

    private String crossroad_start;
    private String crossroad_end; // es a este al que le van a comunicar
    private JsonObject inform;

    public Sensor(AgentID aid, String crossroadStart, String crossroadEnd) throws Exception {
        super(aid);
        this.crossroad_start = crossroadStart;
        this.crossroad_end = crossroadEnd;
        this.state = AgentStates.WAIT_SUBSCRIBE;
        //this.state = AgentStates.FINALIZE;
        ////quitarSystem.out.println("Soy sensor " + getName()+ " con CStart " + 
          //      this.crossroadStart + " y CEnd "+ crossroadEnd);
        inform = null;
    }

    @Override
    public void execute() {
        while (!stop) {
            switch (this.state) {
                case AgentStates.WAIT_SUBSCRIBE: // de los cruces
                    waitSubsCrossroads();
                    break;
                case AgentStates.SUBSCRIBE: // al simulador 
                    subscribe();
                    break;
                case AgentStates.WAIT_TIME:
                    waitTime();
                    break;
                case AgentStates.INFORM_CROSSROAD:
                    inform();
                    break;
                case AgentStates.FINALIZE:
                    this.stop = true;
                    break;
            }
        }
    }
    
    public void waitSubsCrossroads(){
        if(!crossroad_start.equals(Constants.ROOT)){
            inbox = receiveMessage();
            //JSON.pintMessage(inbox);
            content = JSON.content(Constants.NOT_NOW);
            buildMessage(inbox.getSender(), content, ACLMessage.AGREE);
            this.send(outbox);
        }
        if(!crossroad_end.equals(Constants.LEAF)){
            inbox = receiveMessage();
            //JSON.pintMessage(inbox);
            content = JSON.content(Constants.NOT_NOW);
            buildMessage(inbox.getSender(), content, ACLMessage.AGREE);
            this.send(outbox);
        }
        this.state = AgentStates.SUBSCRIBE;
    }
    
    public void subscribe(){ // simulador
        content = JSON.content(Constants.TRAFFIC);
        buildMessage(new AgentID(Constants.SIMULATOR_NAME), content, ACLMessage.SUBSCRIBE);
        this.send(outbox);
        
        inbox = receiveMessage();

        this.state = AgentStates.WAIT_TIME;  
    }
    
    public void waitTime(){
        inbox = receiveMessage();
        content = JSON.readContent(inbox.getContent());
        
        if (content.equals(Constants.ENDS)) {
            content = JSON.content(Constants.ENDS);
            buildMessage(new AgentID(Constants.SWITCHBOARD_NAME), 
                    content, ACLMessage.AGREE);
            send(outbox);
            this.state = AgentStates.FINALIZE;
        } else { 
            // guardar informe para mandarlo al cruce en el siguiente estado
            JsonObject content_json = Json.parse(content).asObject();
            inform = content_json;

            ////quitarSystem.out.println(content_json);
            this.state = AgentStates.INFORM_CROSSROAD;
        }
    }
    
    public void inform(){
        // Solo informa al cruce del final
        if(!crossroad_end.equals(Constants.LEAF)){
            content = JSON.buildContent(Constants.TRAFFIC_INFORM, inform.toString());
            buildMessage(new AgentID(this.crossroad_end), content, ACLMessage.INFORM);
            this.send(outbox);
        }
        this.state = AgentStates.WAIT_TIME;
    }
}

