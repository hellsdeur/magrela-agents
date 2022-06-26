import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class MainStations {
    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();

        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "Station-Container");
        profile.setParameter(Profile.GUI, "true");

        ContainerController containerController = runtime.createAgentContainer(profile);

        ParserStation parserStation = new ParserStation("data/stations.csv", new String[] {"terminal", "name", "lat", "long", "dockcount"});

        for (String station: parserStation.getTerminals()) {
            AgentController stationAgentController;
            try {
                String[] argsStation = new String[4];
                argsStation[0] = parserStation.getData(station, "name");
                argsStation[1] = parserStation.getData(station, "dockcount");
                argsStation[2] = parserStation.getData(station, "lat");
                argsStation[3] = parserStation.getData(station, "long");

                stationAgentController = containerController.createNewAgent(station, "Station", argsStation);
                stationAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
