import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12346;
    private static Map<String, PrintWriter> clientMap = new HashMap<>(); 

    public static void main(String[] args) {
        System.out.println("Servidor de chat iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                BufferedReader in = new BufferedReader(isr);
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Digite seu nome:");
                clientName = in.readLine();
                if (clientName == null || clientName.isEmpty()) {
                    clientName = "Anônimo";
                }

                synchronized (clientMap) {
                    clientMap.put(clientName, out);
                }

                broadcastMessage("Servidor", clientName + " entrou no chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(clientName, message);
                }
            } catch (IOException e) {
                System.err.println("Erro no cliente: " + e.getMessage());
            } finally {
                if (clientName != null) {
                    synchronized (clientMap) {
                        clientMap.remove(clientName);
                    }
                    broadcastMessage("Servidor", clientName + " saiu do chat.");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket: " + e.getMessage());
                }
            }
        }

        private void broadcastMessage(String sender, String message) {
            synchronized (clientMap) {
                for (PrintWriter writer : clientMap.values()) {
                    writer.println(sender + ": " + message);
                }
            }
        }
    }
}
