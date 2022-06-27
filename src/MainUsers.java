import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainUsers {
    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();

        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "User-Container");
        profile.setParameter(Profile.GUI, "true");

        ContainerController containerController = runtime.createAgentContainer(profile);

//        String[] argsUser = new String[] {"47.60434980640431", "-122.34153194248867", "47.61332530577333", "-122.35054072688345", "5000"};
//
//        try {
//            AgentController userAgentController = containerController.createNewAgent("Adam100183", "User", argsUser);
//            userAgentController.start();
//        } catch (StaleProxyException e) {
//            e.printStackTrace();
//        }

        ParserMap parserMap = new ParserMap("data/users_popular.csv", new String[] {"name", "from_lat", "from_long", "to_lat", "to_long", "delay"});

        List<String> names = new ArrayList<>(parserMap.getNames());
        for (int i = 0; i < 30; ++i) {
            String name = names.get(i);
            AgentController stationAgentController;
            try {
                String[] argsUser = new String[5];
                argsUser[0] = parserMap.getData(name, "from_lat");
                argsUser[1] = parserMap.getData(name, "from_long");
                argsUser[2] = parserMap.getData(name, "to_lat");
                argsUser[3] = parserMap.getData(name, "to_long");
                argsUser[4] = parserMap.getData(name, "delay");

                stationAgentController = containerController.createNewAgent(name, "User", argsUser);
                stationAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
//        for (String name: parserMap.getNames()) {
//            AgentController stationAgentController;
//            try {
//                String[] argsUser = new String[5];
//                argsUser[0] = parserMap.getData(name, "from_lat");
//                argsUser[1] = parserMap.getData(name, "from_long");
//                argsUser[2] = parserMap.getData(name, "to_lat");
//                argsUser[3] = parserMap.getData(name, "to_long");
//                argsUser[4] = parserMap.getData(name, "delay");
//
//                stationAgentController = containerController.createNewAgent(name, "User", argsUser);
//                stationAgentController.start();
//            } catch (StaleProxyException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
