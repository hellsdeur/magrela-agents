import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Profile;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class MainCentral {
    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();

        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");

        ContainerController containerController = runtime.createMainContainer(profile);

        String[] argsCentral = new String[]{"data/bikes.txt", "955"};

        try {
            AgentController centralAgentController = containerController.createNewAgent("Central", "Central", argsCentral);
            centralAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
