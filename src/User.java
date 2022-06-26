import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class User extends Agent{

    @Override
    protected void setup() {
        Object[] param = getArguments();
        String station = param[0].toString();
        float latitude = Float.parseFloat(param[1].toString());
        float longitude = Float.parseFloat(param[2].toString());
        int delay = Integer.parseInt(param[3].toString());
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
                // user requests bike to the station

                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                AID receiver = new AID("Central", AID.ISLOCALNAME);
                message.addReceiver(receiver);
                message.setOntology("BIKEREQUEST");
                message.setContent("request");
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

                        BikeInfo bikeInfo = null;

                        try {
                            bikeInfo = (BikeInfo) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        bike[0] = bikeInfo.bike;

                        System.out.println("↑ [RENTAL] " + myAgent.getLocalName() + " received " + bikeInfo.bike + " at " + bikeInfo.station);
                    }
                    // if BIKEDEVOLUTION-REPLY, then unpack bikeInfo and retrieve bike from the user
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION-REPLY")) {

                        BikeInfo bikeInfo = null;

                        try {
                            bikeInfo = (BikeInfo) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        bike[0] = null;

                        System.out.println("↓ [DEVOLUTION] " + myAgent.getLocalName() + " returned " + bikeInfo.bike + " at " + bikeInfo.station);
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

                ACLMessage bikeDevolutionMessage = new ACLMessage(ACLMessage.REQUEST);
                AID receiver = new AID("Central", AID.ISLOCALNAME);
                bikeDevolutionMessage.addReceiver(receiver);
                bikeDevolutionMessage.setOntology("BIKEDEVOLUTION");
                bikeDevolutionMessage.setContent(bike[0]);
                myAgent.send(bikeDevolutionMessage);

                System.out.println("✉ [MESSAGE] " +  myAgent.getLocalName() + "\t→ " +  receiver.getLocalName() + "\t: BIKEDEVOLUTION");
            }
        });
    }
}
