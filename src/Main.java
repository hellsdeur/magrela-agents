import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Profile;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();

        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");

        ContainerController containerController = runtime.createMainContainer(profile);

        CSVParser parser = new CSVParser("data/stations.csv", new String[] {"terminal", "name", "lat", "long", "dockcount"});

        for (String station: parser.getTerminals()) {
            AgentController stationAgentController;
            try {
                String[] param = new String[5];
                param[0] = station;
                param[1] = parser.getData(station, "name");
                param[2] = parser.getData(station, "dockcount");
                param[3] = parser.getData(station, "lat");
                param[4] = parser.getData(station, "long");

                stationAgentController = containerController.createNewAgent(station, "Station", param);
                stationAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
