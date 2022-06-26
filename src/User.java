import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

public class User extends Agent{

    @Override
    protected void setup() {
        Object[] param = getArguments();
        double fromLatitude = Double.parseDouble(param[0].toString());
        double fromLongitude = Double.parseDouble(param[1].toString());
        double toLatitude = Double.parseDouble(param[2].toString());
        double toLongitude = Double.parseDouble(param[3].toString());
        int delay = Integer.parseInt(param[4].toString());
        final String[] bike = new String[1];

        // confirmation print (might delete later)
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                System.out.println("✓ [AGENT CREATED] User " + getAID().getLocalName());
            }
        });

        // bike request behavior
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // user requests bike to the central
                InfoUser infoUser = new InfoUser(null, fromLatitude, fromLongitude);
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                AID receiver = new AID("Central", AID.ISLOCALNAME);
                message.addReceiver(receiver);
                message.setOntology("BIKEREQUEST");
                try {
                    message.setContentObject(infoUser);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                myAgent.send(message);

                System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t→ " +  receiver.getLocalName() + "\t: BIKEREQUEST");
            }
        });

        // bike retrieval behavior
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {

                ACLMessage recvMessage = myAgent.receive();

                if (recvMessage != null) {
                    // if BIKEREQUEST-REPLY, then unpack bikeInfo and give bike to the user
                    if (recvMessage.getOntology().equalsIgnoreCase("BIKEREQUEST-REPLY")) {

                        InfoBike infoBike = null;

                        try {
                            infoBike = (InfoBike) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        bike[0] = infoBike.bike;

                        System.out.println("↑ [RENTAL] " + myAgent.getLocalName() + " received " + infoBike.bike + " at " + infoBike.station);
                    }
                    // if BIKEDEVOLUTION-REPLY, then unpack bikeInfo and retrieve bike from the user
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION-REPLY")) {

                        InfoBike infoBike = null;

                        try {
                            infoBike = (InfoBike) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        bike[0] = null;

                        System.out.println("↓ [DEVOLUTION] " + myAgent.getLocalName() + " returned " + infoBike.bike + " at " + infoBike.station);
                    }
                }
                else {
                    block();
                }
            }
        });

        // bike devolution behaviour
        addBehaviour(new WakerBehaviour(this, delay) {
            @Override
            protected void onWake() {
                // user returns bike to a station

                InfoUser infoUser = new InfoUser(bike[0], toLatitude, toLongitude);
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                AID receiver = new AID("Central", AID.ISLOCALNAME);
                message.addReceiver(receiver);
                message.setOntology("BIKEDEVOLUTION");
                try {
                    message.setContentObject(infoUser);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                myAgent.send(message);

                System.out.println("✉ [MESSAGE] " +  myAgent.getLocalName() + "\t→ " +  receiver.getLocalName() + "\t: BIKEDEVOLUTION");
            }
        });
    }
}
