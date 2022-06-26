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
                    // if RECEIVEBIKE-CONFIRMATION, refresh stations data
                    else if (recvMessage.getOntology().equalsIgnoreCase("RECEIVEBIKE-CONFIRMATION")) {
                        AID sender = recvMessage.getSender();

                        InfoStation infoStation = null;
                        try {
                            infoStation = (InfoStation) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw  new RuntimeException(e);
                        }
                        stations.put(sender.getLocalName(), infoStation);
                    }
                    // if BIKEREQUEST, then select closest station and send a STATIONNIKEREQUEST
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEREQUEST")) {

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

                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

                        InfoBike infoBike = new InfoBike(null, recvMessage.getSender().getLocalName(), closestStation);

                        message.addReceiver(new AID(closestStation, AID.ISLOCALNAME));
                        message.setOntology("STATIONBIKEREQUEST");
                        try {
                            message.setContentObject(infoBike);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);

                        System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t → " +  recvMessage.getSender().getLocalName() + "\t: BIKEALLOCATION-REPLY");
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
                    // if BIKEDEVOLUTION, then select a station and send a STATIONBIKEDEVOLUTION
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION")) {

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

                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                        InfoBike infoBike = new InfoBike(infoUser.bike, recvMessage.getSender().getLocalName(), closestStation);
                        message.addReceiver(new AID(closestStation, AID.ISLOCALNAME));
                        message.setOntology("STATIONBIKEDEVOLUTION");
                        try {
                            message.setContentObject(infoBike);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        myAgent.send(message);
                    }
                    // if STATIONBIKEDEVOLUTION-REPLY, then inform user where to return bike
                    else if (recvMessage.getOntology().equalsIgnoreCase("STATIONBIKEDEVOLUTION-REPLY")) {

                        InfoBike infoBike = null;
                        try {
                            infoBike = (InfoBike) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);

                        message.addReceiver(new AID(infoBike.user, AID.ISLOCALNAME));
                        message.setOntology("BIKEDEVOLUTION-REPLY");
                        try {
                            message.setContentObject(infoBike);
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
