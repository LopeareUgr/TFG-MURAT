/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Utilities.AgentStates;
import Utilities.Constants;
import Utilities.JSON;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author Raúl López Arévalo
 */
public class Semaphore extends Agent {
    private String light;
    public Semaphore(AgentID aid) throws Exception {
        super(aid);
        this.state = AgentStates.WAIT_CROSSROAD;
    }

    @Override
    public void execute() {
        while (!stop) {
            switch (this.state) {
                case AgentStates.WAIT_CROSSROAD:
                    waitChange();
                    break;
                case AgentStates.FINALIZE:
                    this.stop = true;
                    break;
            }
        }
    }

    public void waitChange() {
        inbox = receiveMessage();
        content = JSON.readContent(inbox.getContent());
        if (content.equals(Constants.ENDS)) {
            content = JSON.content(Constants.ENDS);
            buildMessage(inbox.getSender(), content, ACLMessage.AGREE);
            send(outbox);
            this.state = AgentStates.FINALIZE;
        } else {
            light = content;
            // comprobar la recepción de esto
            content = JSON.content("cambio estado");
            buildMessage(inbox.getSender(), content, ACLMessage.CONFIRM);
            send(outbox);
            this.state = AgentStates.WAIT_CROSSROAD;
        }
    }
}
