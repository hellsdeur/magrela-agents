import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Map;
import java.util.*;

public class Central extends Agent {

    @Override
    protected void setup() {
        Object[] param = getArguments();
        Queue<String> bikes = new BikeParser(param[0].toString()).bikes;
        int totalBikeCount = bikes.size();
        int numDocks = Integer.parseInt(param[1].toString());
        Map<String, StationInfo> stations = new HashMap<String, StationInfo>();

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("✓ [AGENT CREATED] Central " + getAID().getLocalName());
            }
        });

        // listen to any transaction behaviour
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage recvMessage = myAgent.receive();

                if (recvMessage != null) {
                    // if BIKEALLOCATION, then calculate necessary bikes and allocate
                    if (recvMessage.getOntology().equalsIgnoreCase("BIKEALLOCATION")) {

                        StationInfo stationInfo = null;
                        try {
                            stationInfo = (StationInfo) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        float ratioDocks = (float) stationInfo.dockcount / numDocks;
                        int numBikes = Math.round(ratioDocks * bikes.size()); // TODO totalBikeCount???

                        ACLMessage reply = recvMessage.createReply();
                        BikeBatchInfo bikeBatchInfo = new BikeBatchInfo();
                        while (numBikes > 0) {
                            bikeBatchInfo.bikes.add(bikes.peek());
                            bikes.remove();
                            numBikes -= 1;
                        }
                        reply.setOntology("BIKEALLOCATION-REPLY");
                        reply.setPerformative(ACLMessage.INFORM);
                        try {
                            reply.setContentObject(bikeBatchInfo);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(reply);
                        System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t → " +  recvMessage.getSender().getLocalName() + "\t: BIKEALLOCATION-REPLY");
                    }
                    // if RECEIVEBIKE-CONFIRMATION, refresh stations data
                    else if (recvMessage.getOntology().equalsIgnoreCase("RECEIVEBIKE-CONFIRMATION")) {
                        AID sender = recvMessage.getSender();

                        StationInfo stationInfo = null;
                        try {
                            stationInfo = (StationInfo) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw  new RuntimeException(e);
                        }
                        stations.put(sender.getLocalName(), stationInfo);
                    }
                    // if BIKEREQUEST, then select a station and send a STATIONNIKEREQUEST
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEREQUEST")) {

//                        Random rand = new Random();
//                        List<String> stationsList = new ArrayList<>(stations.keySet());
//                        String selectedStation = stationsList.get(rand.nextInt(stationsList.size()));
                        String selectedStation = "CH-15";

                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

                        BikeInfo bikeInfo = new BikeInfo(null, recvMessage.getSender().getLocalName(), selectedStation);

                        message.addReceiver(new AID(selectedStation, AID.ISLOCALNAME));
                        message.setOntology("STATIONBIKEREQUEST");
                        try {
                            message.setContentObject(bikeInfo);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);

                        System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t → " +  recvMessage.getSender().getLocalName() + "\t: BIKEALLOCATION-REPLY");
                    }
                    // if STATIONBIKEREQUEST-REPLY, then send bike to user
                    else if (recvMessage.getOntology().equalsIgnoreCase("STATIONBIKEREQUEST-REPLY")) {

                        BikeInfo bikeInfo = null;
                        try {
                            bikeInfo = (BikeInfo) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);

                        message.addReceiver(new AID(bikeInfo.user, AID.ISLOCALNAME));
                        message.setOntology("BIKEREQUEST-REPLY");
                        try {
                            message.setContentObject(bikeInfo);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);
                    }
                    // if BIKEDEVOLUTION, then select a station and send a STATIONBIKEDEVOLUTION
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION")) {
//                        Random rand = new Random();
//                        List<String> stationsList = new ArrayList<>(stations.keySet());
//                        String selectedStation = stationsList.get(rand.nextInt(stationsList.size()));
                        String selectedStation = "CH-15";

                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

                        BikeInfo bikeInfo = new BikeInfo(recvMessage.getContent(), recvMessage.getSender().getLocalName(), selectedStation);

                        message.addReceiver(new AID(selectedStation, AID.ISLOCALNAME));
                        message.setOntology("STATIONBIKEDEVOLUTION");

                        try {
                            message.setContentObject(bikeInfo);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        myAgent.send(message);
                    }
                    // if STATIONBIKEDEVOLUTION-REPLY, then inform user where to return bike
                    else if (recvMessage.getOntology().equalsIgnoreCase("STATIONBIKEDEVOLUTION-REPLY")) {

                        BikeInfo bikeInfo = null;
                        try {
                            bikeInfo = (BikeInfo) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);

                        message.addReceiver(new AID(bikeInfo.user, AID.ISLOCALNAME));
                        message.setOntology("BIKEDEVOLUTION-REPLY");
                        try {
                            message.setContentObject(bikeInfo);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);
                    }
                } else {
                    block();
                }
            }
        });
    }
}
