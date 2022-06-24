import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Station extends Agent {

    @Override
    protected void setup() {
        Object[] param = getArguments();
        String station = param[0].toString();
        String name = param[1].toString();
        int dockcount = Integer.parseInt(param[2].toString());
        float latitude = Float.parseFloat(param[3].toString());
        float longitude = Float.parseFloat(param[4].toString());

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("✓ Station " + getAID().getLocalName() + " created successfully.");
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage bikeAllocMsg = myAgent.receive();
                if (bikeAllocMsg != null) {
                    // create reply
                    ACLMessage bikeReplyMsg = bikeAllocMsg.createReply();
                    String content = bikeAllocMsg.getContent();
                    if (content.equalsIgnoreCase("Bike")) {
                        System.out.println("User " + bikeAllocMsg.getSender().getName() + " wants a bike.");
                        System.out.println("Bike allocated!");

                        bikeReplyMsg.setPerformative(ACLMessage.INFORM);
                        bikeReplyMsg.setContent("Request accepted. Your bike ID is KKKKKK");
                        myAgent.send(bikeReplyMsg);
                    }
                    else {
                        block();
                    }
                }
            }
        });
    }
}
