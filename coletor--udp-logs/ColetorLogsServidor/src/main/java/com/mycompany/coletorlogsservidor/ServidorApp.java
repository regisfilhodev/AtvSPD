package com.mycompany.coletorlogsservidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorApp {
    private static final int PORTA = 9999;
    private static final RepositorioLogs repositorio = new RepositorioLogs();
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        try (DatagramSocket socket = new DatagramSocket(PORTA)) {
            System.out.println("Servidor Coletro de Logs UDP ativo na porta " + PORTA);
            byte[] buffer = new byte[65535];
            
            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                pacote.setLength(buffer.length);
                
                socket.receive(pacote);
                
                String jsonRecebido = new String(pacote.getData(), 0, pacote.getLength());
                InetAddress ipCliente = pacote.getAddress();
                int portaCliente = pacote.getPort();
                
                executor.submit(() -> {
                    processarRequisicao(socket, jsonRecebido, ipCliente, portaCliente);
                });
            }
        } catch (Exception e) {
            System.err.println("Erro no loop principal do Servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
    
    private static void processarRequisicao(DatagramSocket socket, String json, InetAddress ip, int porta) {
        try {
            Requisicao req = Requisicao.fromLinha(json);
            Resposta resp;
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String remetente = ip.getHostAddress() + ":" + porta;
            
            if ("REGISTRAR".equalsIgnoreCase(req.getOperacao())) {
                List<Evento> eventos = req.getEventos();
                int total = eventos.size();
                
                for (Evento evento : eventos) {
                    repositorio.adicionar(evento, remetente);
                    Thread.sleep(1000); // Simula o processo pesado de 1s por item
                }
                
                List<String> dadosResposta = new ArrayList<>();
                dadosResposta.add("OK - Lista de " + total + " evento(s) processada com sucesso.");
                resp = new Resposta("OK", timestamp, dadosResposta);
                
            } else if ("LISTAR".equalsIgnoreCase(req.getOperacao())) {
                List<String> logsFiltrados = repositorio.listaPorNivel(req.getNivel());
                resp = new Resposta("OK", timestamp, logsFiltrados);
                
            } else {
                List<String> erro = new ArrayList<>();
                erro.add("Operação desconhecida: " + req.getOperacao());
                resp = new Resposta("ERRO", timestamp, erro);
            }
            
            String jsonResposta = resp.paraLinha();
            byte[] dadosEnvio = jsonResposta.getBytes();
            DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnvio, dadosEnvio.length, ip, porta);
            
            synchronized (socket) {
                socket.send(pacoteEnvio);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar requisição do cliente " + ip + ":" + porta + ": " + e.getMessage());
        }
    }
}
