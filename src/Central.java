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

public class Central extends Agent {

    @Override
    protected void setup() {
        Object[] param = getArguments();
        Queue<String> bikes = new ParserBike(param[0].toString()).bikes;
        int totalBikeCount = bikes.size();
        int numDocks = Integer.parseInt(param[1].toString());
        Map<String, InfoStation> stations = new HashMap<String, InfoStation>();

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

                        InfoStation infoStation = null;
                        try {
                            infoStation = (InfoStation) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        float ratioDocks = (float) infoStation.dockcount / numDocks;
                        int numBikes = Math.round(ratioDocks * bikes.size()); // TODO totalBikeCount???

                        ACLMessage reply = recvMessage.createReply();
                        InfoBikeBatch infoBikeBatch = new InfoBikeBatch();
                        while (numBikes > 0) {
                            infoBikeBatch.bikes.add(bikes.peek());
                            bikes.remove();
                            numBikes -= 1;
                        }
                        reply.setOntology("BIKEALLOCATION-REPLY");
                        reply.setPerformative(ACLMessage.INFORM);
                        try {
                            reply.setContentObject(infoBikeBatch);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(reply);
                        System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t → " +  recvMessage.getSender().getLocalName() + "\t: BIKEALLOCATION-REPLY");
                    }
                    // if UPDATESTATIONSINFO, refresh stations info
                    else if (recvMessage.getOntology().equalsIgnoreCase("UPDATESTATIONSINFO")) {
                        AID sender = recvMessage.getSender();

                        InfoStation infoStation = null;
                        try {
                            infoStation = (InfoStation) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        stations.put(sender.getLocalName(), infoStation);

                        //TODO se a estação possuir 0 bikes, enviar para ela mais bikes

                        if (infoStation.bikeCount == 0){
                            String selectedStation = null;

                            int distanceFromIdeal = 0;

                            for (String station : stations.keySet()) {
                                InfoStation info = stations.get(station);

                                float ratioDocks = (float) info.dockcount / numDocks;
                                int idealNumBikes = Math.round(ratioDocks * bikes.size());
                                int bikeCountStation = info.bikeCount;



                                if (distanceFromIdeal<(bikeCountStation - idealNumBikes)) {
                                    selectedStation = station;
                                }

                            }

                            if (selectedStation != null) {
                                ACLMessage sendBikesToAnotherStation = new ACLMessage(ACLMessage.REQUEST);
                                sendBikesToAnotherStation.addReceiver(new AID(selectedStation, AID.ISLOCALNAME));
                                sendBikesToAnotherStation.setOntology("REALLOCATEBIKES");

                                InfoReallocate content = new InfoReallocate(infoStation.station, infoStation.address, distanceFromIdeal);

                                try {
                                    sendBikesToAnotherStation.setContentObject(content);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                myAgent.send(sendBikesToAnotherStation);

                            }
                        }
                    }
                    // if RENTALSTATIONREQUEST, then select closest station and send a RENTALSTATION
                    else if (recvMessage.getOntology().equalsIgnoreCase("RENTALSTATIONREQUEST")) {

                        InfoUser infoUser = null;

                        try {
                            infoUser = (InfoUser) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        String closestStation = null;
                        double shortestDistance = Double.MAX_VALUE;
                        // TODO verificar se a estação possui bikes
                        for (Map.Entry<String, InfoStation> entry : stations.entrySet()) {
                            double currentDistance = Point2D.distance(infoUser.latitude, infoUser.longitude, entry.getValue().latitude, entry.getValue().longitude);
                            if (currentDistance < shortestDistance) {
                                closestStation = entry.getKey();
                                shortestDistance = currentDistance;
                            }
                        }

                        ACLMessage reply = recvMessage.createReply();
                        InfoStation infoStation = stations.get(closestStation);
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setOntology("RENTALSTATIONREQUEST-REPLY");
                        try {
                            reply.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(reply);
                    }
                    // if STATIONBIKEREQUEST-REPLY, then send bike to user
                    else if (recvMessage.getOntology().equalsIgnoreCase("STATIONBIKEREQUEST-REPLY")) {

                        InfoBike infoBike = null;
                        try {
                            infoBike = (InfoBike) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);

                        message.addReceiver(new AID(infoBike.user, AID.ISLOCALNAME));
                        message.setOntology("BIKEREQUEST-REPLY");
                        try {
                            message.setContentObject(infoBike);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);
                    }
                    // if DEVOLUTIONSTATIONREQUEST, then select a station and send a DEVOLUTIONSTATIONREQUEST-REPLY
                    else if (recvMessage.getOntology().equalsIgnoreCase("DEVOLUTIONSTATIONREQUEST")) {

                        InfoUser infoUser = null;

                        try {
                            infoUser = (InfoUser) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        String closestStation = null;
                        double shortestDistance = Double.MAX_VALUE;

                        for (Map.Entry<String, InfoStation> entry : stations.entrySet()) {
                            double currentDistance = Point2D.distance(infoUser.latitude, infoUser.longitude, entry.getValue().latitude, entry.getValue().longitude);
                            if (currentDistance < shortestDistance) {
                                closestStation = entry.getKey();
                                shortestDistance = currentDistance;
                            }
                        }

                        ACLMessage reply = recvMessage.createReply();
                        InfoStation infoStation = stations.get(closestStation);
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setOntology("DEVOLUTIONSTATIONREQUEST-REPLY");
                        try {
                            reply.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(reply);
                    }
                } else {
                    block();
                }
            }
        });
    }
}
