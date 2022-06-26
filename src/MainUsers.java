import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class MainUsers {
    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();

        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "User-Container");
        profile.setParameter(Profile.GUI, "true");

        ContainerController containerController = runtime.createAgentContainer(profile);

        String[] argsUser = new String[] {"47.61311", "-122.344208", "47.602103","-122.316923", "5000"};

        for (int i = 0; i < 10; ++i) {
            try {
                AgentController userAgentController = containerController.createNewAgent("User"+i, "User", argsUser);
                userAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

//        try {
//            AgentController userAgentController = containerController.createNewAgent("Helder", "User", argsUser);
//            userAgentController.start();
//        } catch (StaleProxyException e) {
//            e.printStackTrace();
//        }
    }
}
