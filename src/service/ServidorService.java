package service;

import bean.ChatMessage;
import bean.ChatMessage.Action;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fabricio
 */
public class ServidorService {

    private ServerSocket serverSocket;
    private Socket socket;
    private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>();

    public ServidorService() {
        try {
            /**
             * Porta de conexão dos clientes com o servidor
             */
            serverSocket = new ServerSocket(8886);
            System.out.println("Servidor online!");
            /**
             * While true mantem o serverSocket sempre esperando por uma nova
             * conexão
             */
            while (true) {
                socket = serverSocket.accept();
                new Thread(new ListenerSocket(socket)).start();
            }

        } catch (IOException ex) {
            Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    private class ListenerSocket implements Runnable {

        /**
         * Output = saida e envio de mensagens do servidor Input = recebe as
         * mensagens enviadas pelos clientes
         */
        private ObjectOutputStream output;
        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void run() {
            ChatMessage message = null;
            try {

                while ((message = (ChatMessage) input.readObject()) != null) {
                    Action action = message.getAction();
                    /**
                     * Verifica o tipo de ação e direciona para os metodos corretos
                     */
                    if (action.equals(Action.CONNECT)) {
                        boolean isConnect = connect(message, output);
                        if (isConnect) {
                            mapOnlines.put(message.getName(), output);
                            sendOnlines();
                        }
                    } else if (action.equals(Action.DISCONNECT)) {
                        disconnect(message, output);
                        sendOnlines();
                        return;
                    } else if (action.equals(Action.SEND_ONE)) {
                        sendOne(message);
                    } else if (action.equals(Action.SEND_ALL)) {
                        sendAll(message);
                    } 
                }

            } catch (IOException ex) {
                disconnect(message, output);
                sendOnlines();
                System.out.println(message.getName() + " deixou o chat!!!");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private boolean connect(ChatMessage message, ObjectOutputStream output) {
        if (mapOnlines.size() == 0) {
            message.setText("YES");
            send(message, output);
            return true;
        }

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            if (kv.getKey().equals(message.getName())) {
                message.setText("NO");
                send(message, output);
                return false;
            } else {
                message.setText("YES");
                send(message, output);
                return true;
            }
        }

        return false;
    }

    private void disconnect(ChatMessage message, ObjectOutputStream output) {
        /**
         * Remove o usuário da lista
         */
        mapOnlines.remove(message.getName());

        message.setText("Desconectando...");

        message.setAction(Action.SEND_ONE);
        sendAll(message);

        System.out.println(message.getName() + " saiu da sala.");
    }

    private void sendOne(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            if (kv.getKey().equals(message.getNameReserved())) {
                try {
                    kv.getValue().writeObject(message);
                } catch (IOException ex) {
                    Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void send(ChatMessage message, ObjectOutputStream output) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void sendAll(ChatMessage message) {
        /**
         * Evita que a mensagem de saida seja enviada para o usuario que está
         * saindo
         */
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            if (!kv.getKey().equals(message.getName())) {
                message.setAction(Action.SEND_ONE);
                try {
                    kv.getValue().writeObject(message);
                } catch (IOException ex) {
                    Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void sendOnlines() {
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            setNames.add(kv.getKey());
        }

        ChatMessage message = new ChatMessage();
        message.setAction(Action.USERS_ONLINE);
        message.setSetOnlines(setNames);

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
