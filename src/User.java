import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class User extends Agent{

    @Override
    protected void setup() {
        Object[] param = getArguments();
        String name = param[0].toString();
        String station = param[1].toString();
        float latitude = Float.parseFloat(param[2].toString());
        float longitude = Float.parseFloat(param[3].toString());

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("✓ User " + getAID().getLocalName() + " created successfully.");
            }
        });

        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // bike allocation request message
                ACLMessage bikeAllocMsg = new ACLMessage(ACLMessage.REQUEST);
                bikeAllocMsg.addReceiver(new AID(station, AID.ISLOCALNAME));
                bikeAllocMsg.setOntology("Bike-Request");
                bikeAllocMsg.setContent("bike");
                myAgent.send(bikeAllocMsg);
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage bikeRecvMsg = myAgent.receive();
                if (bikeRecvMsg != null) {
                    String content = bikeRecvMsg.getContent();
                    System.out.println("--->" + bikeRecvMsg.getSender().getName() + ": " + content);
                }
                else {
                    block();
                }
            }
        });
    }
}
