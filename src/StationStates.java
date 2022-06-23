import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class StationStates extends CyclicBehaviour {
    private Integer state = 0;

    public StationStates(Station agent) {
        super(agent);
    }

    @Override
    public void action() {
        if (state == 0) {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setProtocol("mensagem");
            message.addReceiver(new AID("station", AID.ISLOCALNAME));
            myAgent.send(message);
        }

        state = 1;
    }
}
