import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class User extends Agent{

    @Override
    protected void setup() {
        Object[] param = getArguments();
        String name = param[0].toString();
        String station = param[1].toString();
        float latitude = Float.parseFloat(param[2].toString());
        float longitude = Float.parseFloat(param[3].toString());
        int delay = Integer.parseInt(param[4].toString());
        final String[] bike = new String[1];

        // confirmation print (might delete later)
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                System.out.println("✓ User " + getAID().getLocalName() + " created successfully.");
            }
        });

        // requesting a bike
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // bike allocation request message
                ACLMessage bikeAllocMsg = new ACLMessage(ACLMessage.REQUEST);
                bikeAllocMsg.addReceiver(new AID("Central", AID.ISLOCALNAME));
                bikeAllocMsg.setOntology("BIKEREQUEST");
                bikeAllocMsg.setContent("request");
                myAgent.send(bikeAllocMsg);

                System.out.println(myAgent.getLocalName()+" request a bike");
            }
        });

        // listen for the bike confirmation
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage bikeRecvMsg = myAgent.receive();
                if (bikeRecvMsg != null) {
                    if (bikeRecvMsg.getOntology().equalsIgnoreCase("BIKEREQUEST-REPLY")) {

                        MessageBikeForUser msg = null;
                        try {
                            msg = (MessageBikeForUser) bikeRecvMsg.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println(myAgent.getLocalName()+" is going to "+msg.station);

                        bike[0] = msg.bike;
                        System.out.println(myAgent.getLocalName()+ " received " + msg.bike);
                }}
                else {
                    block();
                }
            }
        });

        // bike devolution after a given delay
        addBehaviour(new WakerBehaviour(this, delay) {
            @Override
            protected void onWake() {
                ACLMessage bikeDevolutionMessage = new ACLMessage(ACLMessage.REQUEST);
                bikeDevolutionMessage.addReceiver(new AID("Central", AID.ISLOCALNAME));
                bikeDevolutionMessage.setOntology("BIKEDEVOLUTION");
                bikeDevolutionMessage.setContent("devolution");
                myAgent.send(bikeDevolutionMessage);
                System.out.println(myAgent.getAID().getLocalName()+" is trying to return the bike");
            }
        });
    }
}
