import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class User extends ProntoAgent {

    double fromLatitude;
    double fromLongitude;
    double toLatitude;
    double toLongitude;
    String bike;

    int delay;

    @Override
    protected void setup() {
        Object[] param = getArguments();
        fromLatitude = Double.parseDouble(param[0].toString());
        fromLongitude = Double.parseDouble(param[1].toString());
        toLatitude = Double.parseDouble(param[2].toString());
        toLongitude = Double.parseDouble(param[3].toString());
        delay = Integer.parseInt(param[4].toString());
        bike = null;

        // confirmation print (might delete later)
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                System.out.println("✓ [NEW] User " + getAID().getLocalName());
            }
        });

        // bike request behavior
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                // user requests a station name to the central
                InfoUser infoUser = new InfoUser(myAgent.getLocalName(), null, fromLatitude, fromLongitude);
                send(myAgent, "Central", ACLMessage.REQUEST, "RENTALSTATIONREQUEST", infoUser, true);
            }
        });

        // bike retrieval behavior
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {

                ACLMessage recvMessage = myAgent.receive();

                if (recvMessage != null) {
                    // if RENTALSTATIONREQUEST-REPLY, then request bike on the given station and send a BIKEREQUEST
                    if (recvMessage.getOntology().equalsIgnoreCase("RENTALSTATIONREQUEST-REPLY")) {

                        InfoStation infoStation = (InfoStation) unpack(recvMessage);

                        InfoUser infoUser = new InfoUser(myAgent.getLocalName(), null, fromLatitude, fromLongitude);

                        send(myAgent, infoStation.station, ACLMessage.REQUEST, "BIKEREQUEST", infoUser, true);

                    }
                    // if BIKEREQUEST-REPLY, then unpack bikeInfo and give bike to the user
                    else if (recvMessage.getOntology().equalsIgnoreCase("BIKEREQUEST-REPLY")) {

                        InfoBike infoBike = (InfoBike) unpack(recvMessage);

                        // if no bike was received, ask for another station
                        if (infoBike.bike == null) {
                            InfoUser infoUser = new InfoUser(myAgent.getLocalName(), null, fromLatitude, fromLongitude);
                            send(myAgent, "Central", ACLMessage.REQUEST, "RENTALSTATIONREQUEST", infoUser, true);
                        }
                        // if bike was received, get bike, sleep according to delay, then return
                        else {
                            bike = infoBike.bike;
                            System.out.println("↑ [REN] " + myAgent.getLocalName() + " ".repeat(11 - myAgent.getLocalName().length()) + "received \uD83D\uDEB2 " + infoBike.bike + " @ " +  infoBike.station);

                            // wait here
                            try {
                                TimeUnit.MILLISECONDS.sleep(delay);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            // waker behaviour moved here
                            InfoUser infoUser = new InfoUser(myAgent.getLocalName(), bike, toLatitude, toLongitude);
                            send(myAgent, "Central", ACLMessage.REQUEST, "DEVOLUTIONSTATIONREQUEST", infoUser, true);
                        }
                    }
                    // if DEVOLUTIONSTATIONREQUEST-REPLY, then
                    else if (recvMessage.getOntology().equalsIgnoreCase("DEVOLUTIONSTATIONREQUEST-REPLY")) {

                        InfoStation infoStation = (InfoStation) unpack(recvMessage);

                        InfoBike infoBike = new InfoBike(bike, getAID().getLocalName(), infoStation.station);

                        send(myAgent, infoStation.station, ACLMessage.INFORM, "BIKEDEVOLUTION", infoBike, false);

                        System.out.println("↓ [DEV] " + myAgent.getLocalName() + " ".repeat(11 - myAgent.getLocalName().length()) + "returned \uD83D\uDEB2 " + infoBike.bike + " @ " +  infoBike.station);

                        bike = null;

                        myAgent.doDelete();

                    }
                }
                else {
                    block();
                }
            }
        });
    }
    @Override
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("✕ [DEL] User " + getAID().getLocalName());
    }
}
