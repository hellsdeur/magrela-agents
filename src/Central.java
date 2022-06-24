import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Queue;

public class Central extends Agent {

    @Override
    protected void setup() {
        Object[] param = getArguments();
        Queue<String> bikes = new BikeParser(param[0].toString()).bikes;
        int numDocks = Integer.parseInt(param[1].toString());

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("✓ Central " + getAID().getLocalName() + " created successfully.");
                System.out.println("There are " + bikes.size() + " bikes.");
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage receivedMessage = myAgent.receive();
                if (receivedMessage != null) {
                    if (receivedMessage.getOntology().equalsIgnoreCase("BIKEALLOCATION")) {
                        ACLMessage reply = receivedMessage.createReply();
                        int dockcount = Integer.parseInt(receivedMessage.getContent());
                        float ratioBikes = (float) dockcount / numDocks;
                        int numBikes = Math.round(ratioBikes * bikes.size());
                        String content = "";
                        while (numBikes > 0) {
                            content = content + bikes.peek() + " ";
                            bikes.remove();
                            numBikes -= 1;
                        }
                        reply.setOntology("BIKEALLOCATION-REPLY");
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(content);
                        myAgent.send(reply);
                        System.out.println("Central enviando para " + receivedMessage.getSender().getLocalName());
                    }
                    else {
                        block();
                    }
                }else {
                    block();
                }

            }
        });
    }
}
