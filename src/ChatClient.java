import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "192.168.208.79";
    private static final int PORT = 12346;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor de chat.");

            System.out.print("Digite seu nome: ");
            String userName = scanner.nextLine();
            out.println(userName);

            Thread sendThread = new Thread(() -> {
                while (true) {
                    System.out.print("> ");
                    String message = scanner.nextLine();
                    if (message.equalsIgnoreCase("sair")) {
                        out.println("sair");
                        break;
                    }
                    out.println(message);
                }
            });

            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Conexão perdida com o servidor.");
                }
            });

            sendThread.start();
            receiveThread.start();

            sendThread.join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }
}
