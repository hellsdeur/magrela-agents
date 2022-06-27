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

        ParserMap parserMap = new ParserMap("data/stations.csv", new String[] {"terminal", "address", "lat", "long", "dockcount"});

        for (String station: parserMap.getNames()) {
            AgentController stationAgentController;
            try {
                String[] argsStation = new String[4];
                argsStation[0] = parserMap.getData(station, "address");
                argsStation[1] = parserMap.getData(station, "dockcount");
                argsStation[2] = parserMap.getData(station, "lat");
                argsStation[3] = parserMap.getData(station, "long");

                stationAgentController = containerController.createNewAgent(station, "Station", argsStation);
                stationAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
