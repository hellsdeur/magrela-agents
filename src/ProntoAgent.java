import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProntoAgent extends Agent {

    public ProntoAgent() {
    }

    public void send(Agent myAgent, String name, int performative, String ontology, Serializable content, boolean print) {
        AID receiver = new AID(name, AID.ISLOCALNAME);
        ACLMessage message = new ACLMessage(performative);
        message.addReceiver(receiver);
        message.setOntology(ontology);
        try {
            message.setContentObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myAgent.send(message);

        if (print) {
            String senderName = myAgent.getLocalName();
            int spacesSender = 1;
            if (myAgent instanceof Station) spacesSender = 7 - senderName.length();
            else if (myAgent instanceof User) spacesSender = 11 - senderName.length();
            String receivName = receiver.getLocalName();
            int spacesReceiv = 11 - receivName.length();

            System.out.println("✉ [MES] " + senderName + " ".repeat(spacesSender) + "→ " +  receivName + " ".repeat(spacesReceiv) + ": " + ontology);
        }
    }

    public void send(Agent myAgent, String name, int performative, String ontology, String content, boolean print) {
        AID receiver = new AID(name, AID.ISLOCALNAME);
        ACLMessage message = new ACLMessage(performative);
        message.addReceiver(receiver);
        message.setOntology(ontology);
        message.setContent(content);
        myAgent.send(message);

        if (print) {
            String senderName = myAgent.getLocalName();
            int spacesSender = 1; // central, max 7 chars
            if (myAgent instanceof Station) spacesSender = 7 - senderName.length(); // station, max 6 chars
            else if (myAgent instanceof User) spacesSender = 11 - senderName.length(); // user, max 10 chars
            String receivName = receiver.getLocalName();
            int spacesReceiv = 11 - receivName.length();

            System.out.println("✉ [MES] " + senderName + " ".repeat(spacesSender) + "→ " +  receivName + " ".repeat(spacesReceiv) + ": " + ontology);
        }
    }

    public Serializable unpack(ACLMessage message) {
        Serializable serializable;

        try {
            serializable = (Serializable) message.getContentObject();
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

        return serializable;
    }

    public Serializable unpackReply(ACLMessage message, Agent myAgent, int performative, String ontology, Serializable content, boolean print) {
        Serializable serializable;

        try {
            serializable = (Serializable) message.getContentObject();
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

        AID receiver = new AID(message.getSender().getLocalName(), AID.ISLOCALNAME);
        ACLMessage messageReply = new ACLMessage(performative);
        message.addReceiver(receiver);
        message.setOntology(ontology);
        try {
            message.setContentObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myAgent.send(messageReply);

        if (print) {
            String senderName = myAgent.getLocalName();
            int spacesSender = 1;
            if (myAgent instanceof Station) spacesSender = 7 - senderName.length();
            else if (myAgent instanceof User) spacesSender = 11 - senderName.length();
            String receivName = receiver.getLocalName();
            int spacesReceiv = 11 - receivName.length();

            System.out.println("✉ [MES] " + senderName + " ".repeat(spacesSender) + "→ " +  receivName + " ".repeat(spacesReceiv) + ": " + ontology);
        }

        return serializable;
    }

    public Serializable unpackReply(ACLMessage message, Agent myAgent, int performative, String ontology, String content, boolean print) {
        Serializable serializable;

        try {
            serializable = (Serializable) message.getContentObject();
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

        AID receiver = new AID(message.getSender().getLocalName(), AID.ISLOCALNAME);
        ACLMessage messageReply = new ACLMessage(performative);
        message.addReceiver(receiver);
        message.setOntology(ontology);
        message.setContent(content);
        myAgent.send(messageReply);

        if (print) {
            String senderName = myAgent.getLocalName();
            int spacesSender = 1;
            if (myAgent instanceof Station) spacesSender = 7 - senderName.length();
            else if (myAgent instanceof User) spacesSender = 11 - senderName.length();
            String receivName = receiver.getLocalName();
            int spacesReceiv = 11 - receivName.length();

            System.out.println("✉ [MES] " + senderName + " ".repeat(spacesSender) + "→ " +  receivName + " ".repeat(spacesReceiv) + ": " + ontology);
        }

        return serializable;
    }
}
