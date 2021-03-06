import com.sun.tools.javac.Main;
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

public class Station extends ProntoAgent {

    String address;
    int dockcount;
    float latitude;
    float longitude;
    Queue<String> bikes = new LinkedList<String>();

    @Override
    protected void setup() {
        Object[] param = getArguments();
        address = param[0].toString();
        dockcount = Integer.parseInt(param[1].toString());
        latitude = Float.parseFloat(param[2].toString());
        longitude = Float.parseFloat(param[3].toString());


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
                System.out.println("✓ [NEW] Station " + getAID().getLocalName());
            }
        });

        // bike batch allocation behaviour
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // station requests bikes from central

                InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                send(myAgent, "Central", ACLMessage.REQUEST, "BIKEALLOCATION", infoStation, true);
            }
        });

        // bike transfer behavior
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {

                ACLMessage recvMessage = myAgent.receive();

                if (recvMessage != null) {
                    // if BIKEALLOCATION-REPLY, then receive bikes, enqueue bikes, and update central
                    if (recvMessage.getOntology().equalsIgnoreCase("BIKEALLOCATION-REPLY")){

                        InfoBikeBatch infoBikeBatch = (InfoBikeBatch) unpack(recvMessage);

                        bikes.addAll(infoBikeBatch.bikes);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);

                        send(myAgent, "Central", ACLMessage.CONFIRM, "UPDATESTATIONSINFO", infoStation, true);

                        System.out.println("⥮ [ALC] " + myAgent.getLocalName() + "\t❮❮❮ " + infoBikeBatch.bikes.size() + "\t\uD83D\uDEB2 ❮❮❮ " + recvMessage.getSender().getLocalName());

                    }
                    // if BIKEREQUEST, unpack request and send bike to user
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEREQUEST")) {

                        InfoUser infoUser = (InfoUser) unpack(recvMessage);

                        String bikeToSend = null;

                        if (bikes.size() > 0) {
                            bikeToSend = bikes.peek();
                            bikes.remove();
                        }

                        InfoBike infoBikeReply = new InfoBike(bikeToSend, recvMessage.getSender().getLocalName(), getAID().getLocalName());
                        send(myAgent, infoUser.name, ACLMessage.INFORM, "BIKEREQUEST-REPLY", infoBikeReply, true);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        send(myAgent, "Central", ACLMessage.INFORM, "UPDATESTATIONSINFO", infoStation, true);

                    }
                    // if BIKEDEVOLUTION, then unpack bikeInfo
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEDEVOLUTION")) {

                        InfoBike infoBike = (InfoBike) unpack(recvMessage);
                        bikes.add(infoBike.bike);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        send(myAgent, "Central", ACLMessage.INFORM, "UPDATESTATIONSINFO", infoStation, true);

                    }
                    // if REALLOCATEBIKES, then send requested number of bikes, and update Central
                    else if (recvMessage.getOntology().equalsIgnoreCase("REALLOCATEBIKES")) {

                        InfoReallocate infoReallocate = (InfoReallocate) unpack(recvMessage);

//                        System.out.println("⥮[ALC] " + myAgent.getLocalName() + " ❯❯❯ " + infoReallocate.sendNumBikes + " \uD83D\uDEB2 ❯❯❯ " + infoReallocate.station);

                        InfoBikeBatch infoBikeBatch = new InfoBikeBatch();

                        for (int i = 0; i < infoReallocate.sendNumBikes; i++) {
                            infoBikeBatch.bikes.add(bikes.peek());
                            bikes.remove();
                        }
                        send(myAgent, infoReallocate.station, ACLMessage.INFORM, "REALLOCATEBIKES-REPLY", infoBikeBatch, true);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        send(myAgent, "Central", ACLMessage.INFORM, "UPDATESTATIONSINFO", infoStation, true);

                    }
                    else if (recvMessage.getOntology().equalsIgnoreCase("REALLOCATEBIKES-REPLY")){

                        InfoBikeBatch infoBikeBatch = (InfoBikeBatch) unpack(recvMessage);

                        System.out.println("⥮ [ALC] " + myAgent.getLocalName() + "\t❮❮❮ " + infoBikeBatch.bikes.size() + "\t\uD83D\uDEB2 ❮❮❮ " + recvMessage.getSender().getLocalName());

                        bikes.addAll(infoBikeBatch.bikes);

                        InfoStation infoStation = new InfoStation(getAID().getLocalName(), address, latitude, longitude, bikes.size(), dockcount);
                        send(myAgent, "Central", ACLMessage.INFORM, "UPDATESTATIONSINFO", infoStation, true);

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
