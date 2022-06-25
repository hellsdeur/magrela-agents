import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import javax.management.StringValueExp;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class Station extends Agent {


    @Override
    protected void setup() {
        Object[] param = getArguments();
        String station = param[0].toString();
        String name = param[1].toString();
        int dockcount = Integer.parseInt(param[2].toString());
        float latitude = Float.parseFloat(param[3].toString());
        float longitude = Float.parseFloat(param[4].toString());
        Queue<String> bikes = new LinkedList<String>();

        // create a bike rental service
//        ServiceDescription sd = new ServiceDescription();
//        sd.setType("Bike Service");
//        sd.setName("Rental");
//        registerService(sd);
//
//        // create a bike devolution service
//        sd = new ServiceDescription();
//        sd.setType("Bike Service");
//        sd.setName("Devolution");
//        registerService(sd);

        // confirmation print (might delete later)
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                System.out.println("✓ Station " + getAID().getLocalName() + " created successfully.");
            }
        });

        // request bikes from central
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                ACLMessage bikesRequest = new ACLMessage(ACLMessage.REQUEST);
                bikesRequest.addReceiver(new AID("Central", AID.ISLOCALNAME));
                bikesRequest.setOntology("BIKEALLOCATION");
                bikesRequest.setContent(Integer.toString(dockcount));
                myAgent.send(bikesRequest);

            }
        });

        // receive bikes from central
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage receivedMessage = myAgent.receive();
                if (receivedMessage != null) {
                    if (receivedMessage.getOntology().equalsIgnoreCase("BIKEALLOCATION-REPLY")){
                        String content = receivedMessage.getContent();
                        String[] arrStringBikes = content.split(" ");
                        for (String stringBike: arrStringBikes) {
                            bikes.add(stringBike);
                        }
                        System.out.println(bikes.size() + " bikes were received by "+ myAgent.getAID().getLocalName());

                        ACLMessage reply = receivedMessage.createReply();

                        String lat = String.valueOf(latitude);
                        String lon = String.valueOf(longitude);
                        String numbike = String.valueOf(bikes.size());
                        String dockcount_str = String.valueOf(dockcount);

                        String contentInfo = lat + ' ' + lon + ' ' + numbike + ' ' + dockcount_str;

                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setOntology("RECEIVEBIKE-CONFIRMATION");
                        reply.setContent(contentInfo);
                        myAgent.send(reply);

                    } else if (receivedMessage.getOntology().equalsIgnoreCase("STATIONBIKEREQUEST")) {

                        System.out.println("Allocating bike for user ---" + myAgent.getLocalName());
                        ACLMessage reply = receivedMessage.createReply();


                        MessageBikeForUser msg = null;
                        try {
                            msg = (MessageBikeForUser) receivedMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        MessageBikeForUser content = new MessageBikeForUser(bikes.peek(), msg.user, msg.station);

                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setOntology("STATIONBIKEREQUEST-REPLY");
                        try {
                            reply.setContentObject(content);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        bikes.remove();
                        myAgent.send(reply);

                    }
                    else if (receivedMessage.getOntology().equalsIgnoreCase("STATIONBIKEDEVOLUTION")) {

                        System.out.println("Allocating space for user ---" + myAgent.getLocalName());
                        ACLMessage reply = receivedMessage.createReply();


                        MessageBikeForUser msg = null;
                        try {
                            msg = (MessageBikeForUser) receivedMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println(bikes.size());

                        bikes.add(msg.bike);

                        System.out.println(bikes.size());


                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setOntology("STATIONBIKEDEVOLUTION-REPLY");
                        try {
                            reply.setContentObject(msg);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        myAgent.send(reply);

                    }
                }
                else {
                    block();
                }
            }
        });

        // listen for requests or devolution
//        addBehaviour(new CyclicBehaviour(this) {
//            @Override
//            public void action() {
//                ACLMessage receivedMessage = myAgent.receive();
//                if (receivedMessage != null) {
//
//                    // create reply
//                    ACLMessage reply = receivedMessage.createReply();
//                    String ontology = receivedMessage.getOntology();
//                    String content = receivedMessage.getContent();
//
//
//
//                    if (ontology.equalsIgnoreCase("BIKEREQUEST")) {
//                        System.out.println("Bike allocated to "+ receivedMessage.getSender().getName() + "!"
//                                              +"---message from "+ myAgent.getAID().getLocalName()  );
//
//                        reply.setPerformative(ACLMessage.INFORM);
//                        reply.setOntology("BIKEREQUEST-REPLY");
//                        reply.setContent(bikes.peek());
//                        bikes.remove();
//                        myAgent.send(reply);
//
//                    } else if (ontology.equalsIgnoreCase("BIKEDEVOLUTION")) {
//                        System.out.println("Bike returned successfully by "+ receivedMessage.getSender().getName()  +"!"
//                                                     +"---message from "+ myAgent.getAID().getLocalName()  );
//
//                        reply.setPerformative(ACLMessage.INFORM);
//                        reply.setOntology("BIKEDEVOLUTION-REPLY");
//                        reply.setContent("Devolution accepted.");
//                        myAgent.send(reply);
//                    }
//                    else {
//                        // TODO the problem is right here, this behaviour is catching messages from another one
//                        System.out.println(content +"---message from "+ myAgent.getAID().getLocalName());
//                        block();
//                    }
//                }
//                else {
//                    block();
//                }
//            }
//        });

    }







    // DF registration
    protected void registerService(ServiceDescription sd) {
        // create a new entry at DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // add service
        dfd.addServices(sd);

        // register service at DF
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    // DF take down
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
