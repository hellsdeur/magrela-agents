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

import java.io.IOException;
import java.util.*;


public class Station extends Agent {


    @Override
    protected void setup() {
        Object[] param = getArguments();
        String address = param[0].toString();
        int dockcount = Integer.parseInt(param[1].toString());
        float latitude = Float.parseFloat(param[2].toString());
        float longitude = Float.parseFloat(param[3].toString());
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
                System.out.println("✓ [AGENT CREATED] Station " + getAID().getLocalName());
            }
        });

        // bike batch allocation behaviour
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // station requests bikes from central

                InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                AID receiver = new AID("Central", AID.ISLOCALNAME);
                message.addReceiver(receiver);
                message.setOntology("BIKEALLOCATION");
                try {
                    message.setContentObject(infoStation);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                myAgent.send(message);

                System.out.println("✉ [MESSAGE] " +  myAgent.getLocalName() + "\t → " +  receiver.getLocalName() + "\t: BIKEALLOCATION");
            }
        });

        // bike transfer behavior
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {

                ACLMessage recvMessage = myAgent.receive();

                if (recvMessage != null) {
                    // if BIKEALLOCATION-REPLY, then receive bikes and enqueue
                    if (recvMessage.getOntology().equalsIgnoreCase("BIKEALLOCATION-REPLY")){

                        InfoBikeBatch infoBikeBatch = null;
                        try {
                            infoBikeBatch = (InfoBikeBatch) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        bikes.addAll(infoBikeBatch.bikes);

                        ACLMessage reply = recvMessage.createReply();

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setOntology("UPDATESTATIONSINFO");
                        try {
                            reply.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(reply);

                        System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t → " +  recvMessage.getSender().getLocalName() + "\t: UPDATESTATIONSINFO-CONFIRMATION (" + infoBikeBatch.bikes.size() + " bikes)");

                    }
                    // if STATIONBIKEREQUEST, unpack request and send bike to user
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEREQUEST")) {

                        ACLMessage reply = recvMessage.createReply();

                        InfoUser infoUser = null;
                        try {
                            infoUser = (InfoUser) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        String bikeToSend;

                        if (bikes.size() == 0) {
                            bikeToSend = null;
                        }
                        else {
                            bikeToSend = bikes.peek();
                            bikes.remove();
                        }

                        InfoBike infoBikeReply = new InfoBike(bikeToSend, recvMessage.getSender().getLocalName(), getAID().getLocalName());

                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setOntology("BIKEREQUEST-REPLY");
                        try {
                            reply.setContentObject(infoBikeReply);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(reply);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.setOntology("UPDATESTATIONSINFO");
                        message.addReceiver(new AID("Central", AID.ISLOCALNAME));
                        try {
                            message.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);

                        System.out.println("✉ [MESSAGE] " + myAgent.getLocalName() + "\t → " +  recvMessage.getSender().getLocalName() + "\t: BIKEREQUEST-REPLY (" + infoBikeReply.bike + ")");

                    }
                    // if BIKEDEVOLUTION, then unpack bikeInfo
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION")) {

                        InfoBike infoBike = null;
                        try {
                            infoBike = (InfoBike) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        bikes.add(infoBike.bike);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.setOntology("UPDATESTATIONSINFO");
                        message.addReceiver(new AID("Central", AID.ISLOCALNAME));
                        try {
                            message.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);
                    }
                    else if (recvMessage.getOntology().equalsIgnoreCase("REALLOCATEBIKES")) {

                        InfoReallocate infoReallocate = null;
                        try {
                            infoReallocate = (InfoReallocate) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        InfoBikeBatch bikesSent = new InfoBikeBatch();

                        System.out.println(myAgent.getLocalName() + " QUANTIDADE PARA REALOCAR: " + infoReallocate.sendNumBikes);

                        for (int i = 0; i < infoReallocate.sendNumBikes; i++){
                            bikesSent.bikes.add(bikes.peek());
                            bikes.remove();
                        }

                        ACLMessage sendingBikes = new ACLMessage(ACLMessage.INFORM);

                        sendingBikes.addReceiver(new AID(infoReallocate.station, AID.ISLOCALNAME));
                        sendingBikes.setOntology("REALLOCATEBIKES-REPLY");
                        try {
                            sendingBikes.setContentObject(bikesSent);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        myAgent.send(sendingBikes);


                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.setOntology("UPDATESTATIONSINFO");
                        message.addReceiver(new AID("Central", AID.ISLOCALNAME));
                        try {
                            message.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);

                    }
                    else if (recvMessage.getOntology().equalsIgnoreCase("REALLOCATEBIKES-REPLY")){
                        System.out.println("recebi bike "+ myAgent.getLocalName());

                        InfoBikeBatch infoBikeBatch = null;
                        try {
                            infoBikeBatch = (InfoBikeBatch) recvMessage.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }

                        while (infoBikeBatch.bikes.size() > 0){
                            bikes.add(infoBikeBatch.bikes.peek());
                            infoBikeBatch.bikes.remove();
                        }


                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.setOntology("UPDATESTATIONSINFO");
                        message.addReceiver(new AID("Central", AID.ISLOCALNAME));
                        try {
                            message.setContentObject(infoStation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        myAgent.send(message);

                    }
                }
                else {
                    block();
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
