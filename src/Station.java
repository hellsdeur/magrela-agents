import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

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
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Bike Service");
        sd.setName("Rental");
        registerService(sd);

        // create a bike devolution service
        sd = new ServiceDescription();
        sd.setType("Bike Service");
        sd.setName("Devolution");
        registerService(sd);

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

                ACLMessage bikesReceived = myAgent.receive();
                if (bikesReceived != null) {
                    String content = bikesReceived.getContent();
                    String[] arrStringBikes = content.split(" ");
                    for (String stringBike: arrStringBikes) {
                        bikes.add(stringBike);
                    }
                    System.out.println(bikes.size() + " bikes were received.");
                }
                else {
                    block();
                }
            }
        });

        // receive bikes from central
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage bikesReceived = myAgent.receive();
                if (bikesReceived != null) {
                    String content = bikesReceived.getContent();
                    String[] arrStringBikes = content.split(" ");
                    for (String stringBike: arrStringBikes) {
                        bikes.add(stringBike);
                    }
                    System.out.println(bikes.size() + " bikes were received.");
                }
                else {
                    block();
                }
            }
        });

        // listen for requests or devolution
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage receivedMessage = myAgent.receive();
                if (receivedMessage != null) {
                    // create reply
                    ACLMessage reply = receivedMessage.createReply();
                    String content = receivedMessage.getContent();

                    if (content.equalsIgnoreCase("request")) {
                        System.out.println("User " + receivedMessage.getSender().getName() + " wants a bike.");
                        System.out.println("Bike allocated!");

                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(bikes.peek());
                        bikes.remove();
                        myAgent.send(reply);
                    }
                    else if (content.equalsIgnoreCase("devolution")) {
                        System.out.println("User " + receivedMessage.getSender().getName() + " returned a bike.");
                        System.out.println("Bike returned!");

                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Devolution accepted.");
                        myAgent.send(reply);
                    }
                    else {
                        // TODO the problem is right here, this behaviour is catching messages from another one
                        System.out.println(content);
                        block();
                    }
                }
            }
        });
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
