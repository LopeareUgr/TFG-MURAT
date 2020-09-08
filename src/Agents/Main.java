/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Utilities.Constants;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author Raúl López Arévalo
 */
public class Main {              // Actualizar los semáforos(ambar) y los coches (generación automatica) y las calles con sus direcciones obligadas y los semáforos a la entrada de una calle del cruce (peatonales)

    /**
     * @param args thecommand line arguments
     */
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        
        Switchboard switchboard = new Switchboard(new AgentID(Constants.SWITCHBOARD_NAME));
        switchboard.start();
        
        JsonObject json = new JsonObject();
        JsonObject dentro = new JsonObject();
        dentro.add("prop", "value");
        json.add("1", dentro);
        
        String json_string = json.toString();
        
        json = Json.parse(json_string).asObject();
        
        
        System.out.println(json.get("1").getClass().getName());
    
        dentro = (JsonObject) json.get("1");
    }
    
}
