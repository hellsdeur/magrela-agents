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

        String[] argsCentral = new String[] {"data/bikes.txt", "955"};

        try {
            AgentController centralAgentController = containerController.createNewAgent("Central", "Central", argsCentral);
            centralAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        StationParser stationParser = new StationParser("data/stations.csv", new String[] {"terminal", "name", "lat", "long", "dockcount"});

        for (String station: stationParser.getTerminals()) {
            AgentController stationAgentController;
            try {
                String[] argsStation = new String[4];
                argsStation[0] = stationParser.getData(station, "name");
                argsStation[1] = stationParser.getData(station, "dockcount");
                argsStation[2] = stationParser.getData(station, "lat");
                argsStation[3] = stationParser.getData(station, "long");

                stationAgentController = containerController.createNewAgent(station, "Station", argsStation);
                stationAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }



        String[] argsUser = new String[] {"UW-04","1.645","5.9897","5000"};

        try {
            AgentController userAgentController = containerController.createNewAgent("Helder", "User", argsUser);
            userAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
