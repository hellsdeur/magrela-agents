import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
import java.util.Map;
import java.util.*;

public class Central extends ProntoAgent {

    Queue<String> bikes;
    int totalBikeCount;
    int numDocks;
    Map<String, InfoStation> stations = new HashMap<String, InfoStation>();

    @Override
    protected void setup() {
        Object[] param = getArguments();
        this.bikes = new ParserBike(param[0].toString()).bikes;
        this.totalBikeCount = bikes.size();
        this.numDocks = Integer.parseInt(param[1].toString());


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

                        InfoStation infoStation = (InfoStation) unpack(recvMessage);

                        float ratioDocks = (float) infoStation.dockcount / numDocks;
                        int numBikes = Math.round(ratioDocks * totalBikeCount);

                        InfoBikeBatch infoBikeBatch = new InfoBikeBatch();
                        while (numBikes > 0) {
                            infoBikeBatch.bikes.add(bikes.peek());
                            bikes.remove();
                            numBikes -= 1;
                        }
                        send(myAgent, recvMessage.getSender().getLocalName(), ACLMessage.INFORM, "BIKEALLOCATION-REPLY", infoBikeBatch, true);

                    }
                    // if UPDATESTATIONSINFO, refresh stations info
                    else if (recvMessage.getOntology().equalsIgnoreCase("UPDATESTATIONSINFO")) {
                        AID sender = recvMessage.getSender();

                        InfoStation infoStation = (InfoStation) unpack(recvMessage);

                        stations.putIfAbsent(sender.getLocalName(), infoStation);

                        InfoStation previousInfo = stations.get(sender.getLocalName());

                        if (previousInfo != null)
                            if (infoStation.bikeCount != previousInfo.bikeCount) {

                                totalBikeCount = totalBikeCount + (infoStation.bikeCount - previousInfo.bikeCount);

                                stations.put(sender.getLocalName(), infoStation);

                                //TODO identificar o erro

                                if (stations.get(sender.getLocalName()).bikeCount == 0) {
                                    System.out.println("Station "+ sender.getLocalName() +" need more bikes");

                                    String selectedStation = null;

                                    int distanceFromIdeal = Integer.MIN_VALUE;

                                    for (String station : stations.keySet()) {
                                        InfoStation info = stations.get(station);

                                        float ratioDocks = (float) info.dockcount / numDocks;
                                        int idealNumBikes = Math.round(ratioDocks * totalBikeCount);
                                        int bikeCountStation = info.bikeCount;

                                        if (distanceFromIdeal < (bikeCountStation - idealNumBikes)) {
                                            selectedStation = station;
                                            distanceFromIdeal = bikeCountStation - idealNumBikes;
                                            //System.out.println("estação da vez " + selectedStation + "/bikes enviadas:"+ distanceFromIdeal);
                                        }
                                    }


                                    if (selectedStation != null) {
                                        System.out.println("Sending request to "+ selectedStation + ": send " + distanceFromIdeal + " bike(s) to " + infoStation.station);

                                        InfoReallocate infoReallocate = new InfoReallocate(infoStation.station, infoStation.address, distanceFromIdeal);
                                        send(myAgent, selectedStation, ACLMessage.REQUEST, "REALLOCATEBIKES", infoReallocate, false);

                                    }
                                }
                        }
                    }
                    // if RENTALSTATIONREQUEST, then select closest station and send a RENTALSTATION
                    else if (recvMessage.getOntology().equalsIgnoreCase("RENTALSTATIONREQUEST")) {

                        InfoUser infoUser = (InfoUser) unpack(recvMessage);

                        String closestStation = null;
                        double shortestDistance = Double.MAX_VALUE;

                        for (Map.Entry<String, InfoStation> entry : stations.entrySet()) {
                            double currentDistance = Point2D.distance(infoUser.latitude, infoUser.longitude, entry.getValue().latitude, entry.getValue().longitude);
                            if ((currentDistance < shortestDistance) && (entry.getValue().bikeCount > 0)) {
                                closestStation = entry.getKey();
                                shortestDistance = currentDistance;
                            }
                        }

                        InfoStation infoStation = stations.get(closestStation);
                        send(myAgent, recvMessage.getSender().getLocalName(), ACLMessage.INFORM, "RENTALSTATIONREQUEST-REPLY", infoStation, false);

                    }
                    // if STATIONBIKEREQUEST-REPLY, then send bike to user
                    else if (recvMessage.getOntology().equalsIgnoreCase("STATIONBIKEREQUEST-REPLY")) {

                        InfoBike infoBike = (InfoBike) unpack(recvMessage);

                        send(myAgent, infoBike.user, ACLMessage.INFORM, "BIKEREQUEST-REPLY", infoBike, false);

                    }
                    // if DEVOLUTIONSTATIONREQUEST, then select a station and send a DEVOLUTIONSTATIONREQUEST-REPLY
                    else if (recvMessage.getOntology().equalsIgnoreCase("DEVOLUTIONSTATIONREQUEST")) {

                        InfoUser infoUser = (InfoUser) unpack(recvMessage);

                        String closestStation = null;
                        double shortestDistance = Double.MAX_VALUE;

                        for (Map.Entry<String, InfoStation> entry : stations.entrySet()) {
                            double currentDistance = Point2D.distance(infoUser.latitude, infoUser.longitude, entry.getValue().latitude, entry.getValue().longitude);
                            if (currentDistance < shortestDistance  && (entry.getValue().bikeCount < entry.getValue().dockcount)) {
                                closestStation = entry.getKey();
                                shortestDistance = currentDistance;
                            }
                        }

                        InfoStation infoStation = stations.get(closestStation);
                        send(myAgent, recvMessage.getSender().getLocalName(), ACLMessage.INFORM, "DEVOLUTIONSTATIONREQUEST-REPLY", infoStation, false);

                    }
                } else {
                    block();
                }
            }
        });
    }
}
