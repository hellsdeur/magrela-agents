import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
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
        int numDocks = Integer.parseInt(param[1].toString());
        Map<String, String[]> stations = new HashMap<String, String[]>();

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("✓ Central " + getAID().getLocalName() + " created successfully.");
                System.out.println("There are " + bikes.size() + " bikes.");
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage receivedMessage = myAgent.receive();

                if (receivedMessage != null) {
                    if (receivedMessage.getOntology().equalsIgnoreCase("BIKEALLOCATION")) {
                        ACLMessage reply = receivedMessage.createReply();
                        int dockcount = Integer.parseInt(receivedMessage.getContent());
                        float ratioBikes = (float) dockcount / numDocks;
                        int numBikes = Math.round(ratioBikes * bikes.size());
                        String content = "";
                        while (numBikes > 0) {
                            content = content + bikes.peek() + " ";
                            bikes.remove();
                            numBikes -= 1;
                        }
                        reply.setOntology("BIKEALLOCATION-REPLY");
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(content);
                        myAgent.send(reply);
                        System.out.println("Central enviando para " + receivedMessage.getSender().getLocalName());

                    } else if (receivedMessage.getOntology().equalsIgnoreCase("RECEIVEBIKE-CONFIRMATION")) {
                        AID sender = receivedMessage.getSender();
                        String stationContent = receivedMessage.getContent();
                        String[] stationInfo = stationContent.split(" ");

                        System.out.println(sender.getLocalName()+" confirmou recebimento--- Central");


                        stations.put(sender.getLocalName(), stationInfo);

                        /*
                        station info
                        [0] => latitude
                        [1] => longitude
                        [2] => bikecount
                        [3] => dockcount
                         */


                    } else if (receivedMessage.getOntology().equalsIgnoreCase("BIKEREQUEST")) {

                        String nameSender = receivedMessage.getSender().getLocalName();

                        System.out.println("Request from "+nameSender+" received by Central");

                        //Select random station
                        List<String> keysAsArray = new ArrayList<String>(stations.keySet());
                        Random random = new Random();
                        String nameStationSelected = keysAsArray.get(random.nextInt(keysAsArray.size()));



                        ACLMessage requestStation = new ACLMessage(ACLMessage.REQUEST);



                        MessageBikeForUser msgContent = new MessageBikeForUser(null, nameSender, nameStationSelected);

                        requestStation.addReceiver(new AID(nameStationSelected, AID.ISLOCALNAME));
                        requestStation.setOntology("STATIONBIKEREQUEST");

                        try {
                            requestStation.setContentObject(msgContent);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        myAgent.send(requestStation);

                        System.out.println("Central request bike from "+ nameStationSelected);



                    } else if (receivedMessage.getOntology().equalsIgnoreCase("STATIONBIKEREQUEST-REPLY")) {

                        MessageBikeForUser msg = null;
                        try {
                            msg = (MessageBikeForUser) receivedMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        ACLMessage bikeForUser = new ACLMessage(ACLMessage.INFORM);

                        bikeForUser.addReceiver(new AID(msg.user, AID.ISLOCALNAME));
                        bikeForUser.setOntology("BIKEREQUEST-REPLY");
                        try {
                            bikeForUser.setContentObject(msg);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(bikeForUser);

                        System.out.println("Sending "+ msg.bike+ " for "+ msg.user+"---message from Central");


                    } else if (receivedMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION")) {
                        String nameSender = receivedMessage.getSender().getLocalName();

                        System.out.println("Devolution request from "+nameSender+" received by Central");


                        //Select random station
                        List<String> keysAsArray = new ArrayList<String>(stations.keySet());
                        Random random = new Random();
                        String nameStationSelected = keysAsArray.get(random.nextInt(keysAsArray.size()));


                        String Userbike = receivedMessage.getContent();
                        ACLMessage requestStation = new ACLMessage(ACLMessage.REQUEST);

                        MessageBikeForUser msgContent = new MessageBikeForUser(Userbike, nameSender, nameStationSelected);

                        requestStation.addReceiver(new AID(nameStationSelected, AID.ISLOCALNAME));
                        requestStation.setOntology("STATIONBIKEDEVOLUTION");

                        try {
                            requestStation.setContentObject(msgContent);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        myAgent.send(requestStation);

                        System.out.println("Central request bike devolution for "+ nameStationSelected);
                    }
                    else if (receivedMessage.getOntology().equalsIgnoreCase("STATIONBIKEDEVOLUTION-REPLY")) {

                        MessageBikeForUser msg = null;
                        try {
                            msg = (MessageBikeForUser) receivedMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        ACLMessage devolution = new ACLMessage(ACLMessage.INFORM);

                        devolution.addReceiver(new AID(msg.user, AID.ISLOCALNAME));
                        devolution.setOntology("BIKEDEVOLUTION-REPLY");
                        try {
                            devolution.setContentObject(msg);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(devolution);

                        System.out.println("Informing "+ msg.user+" available space for devolution in "
                                + msg.station +"---message from Central");


                    }

                }else {
                    block();
                }

            }
        });


    }
}
