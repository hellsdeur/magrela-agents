import jade.core.Agent;

public class Station extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new StationStates(this));
    }
}
