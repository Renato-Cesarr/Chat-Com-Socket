import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORTA = 12346;
    private static final Map<String, PrintWriter> clientesConectados = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor de chat iniciado na porta " + PORTA);

        try (ServerSocket servidorSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket socketCliente = servidorSocket.accept();
                new GerenciadorDeCliente(socketCliente).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    private static class GerenciadorDeCliente extends Thread {
        private final Socket socketCliente;
        private PrintWriter saida;
        private String nomeCliente;

        public GerenciadorDeCliente(Socket socketCliente) {
            this.socketCliente = socketCliente;
        }

        @Override
        public void run() {
            try {
                configurarConexao();
                processarMensagens();
            } catch (IOException e) {
                System.err.println("Erro na conexão com cliente: " + e.getMessage());
            } finally {
                finalizarConexao();
            }
        }

        private void configurarConexao() throws IOException {
            BufferedReader leitor = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            saida = new PrintWriter(socketCliente.getOutputStream(), true);

            saida.println("Digite seu nome:");
            nomeCliente = leitor.readLine();
            if (nomeCliente == null || nomeCliente.isBlank()) {
                nomeCliente = "Anônimo";
            }

            synchronized (clientesConectados) {
                clientesConectados.put(nomeCliente, saida);
            }

            enviarMensagemParaTodos("Servidor", nomeCliente + " entrou no chat!");
        }

        private void processarMensagens() throws IOException {
            BufferedReader leitor = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

            String mensagem;
            while ((mensagem = leitor.readLine()) != null) {
                enviarMensagemParaTodos(nomeCliente, mensagem);
            }
        }

        private void finalizarConexao() {
            if (nomeCliente != null) {
                synchronized (clientesConectados) {
                    clientesConectados.remove(nomeCliente);
                }
                enviarMensagemParaTodos("Servidor", nomeCliente + " saiu do chat.");
            }

            try {
                socketCliente.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar conexão com cliente: " + e.getMessage());
            }
        }

        private void enviarMensagemParaTodos(String remetente, String mensagem) {
            synchronized (clientesConectados) {
                for (PrintWriter escritor : clientesConectados.values()) {
                    escritor.println(remetente + ": " + mensagem);
                }
            }
        }
    }
}
