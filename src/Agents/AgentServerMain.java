/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import SuperAgente2.ServerAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raúl López Arévalo
 */
public class AgentServerMain extends ServerAgent{
    protected boolean stop;
    protected String state;
    protected ACLMessage inbox, outbox;
    protected String content;
    
    public AgentServerMain(AgentID aid) throws Exception {
        super(aid);
        this.stop = false;
        this.inbox = null;
        this.outbox = null;
        System.out.println("Agent " + this.getName() + " initializing.");
    }
    
    public void buildMessage(AgentID receiver, String content, int performative){
        outbox = new ACLMessage();
        outbox.setReceiver(receiver);
        outbox.setSender(new AgentID(this.getName()));
        outbox.setContent(content);
        outbox.setPerformative(performative);
        outbox.setEncoding("JSON");
    }

    
    public ACLMessage receiveMessage(){
        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(AgentServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inbox;
    }
    
    public void finalize(){
        System.out.println("Agent " + this.getName() + " finalizing");
    }
}
