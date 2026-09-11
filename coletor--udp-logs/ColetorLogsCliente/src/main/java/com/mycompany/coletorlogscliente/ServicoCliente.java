package com.mycompany.coletorlogscliente;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServicoCliente {
    private static final String HOST_SERVIDOR = "localhost";
    private static final int PORTA_SERVIDOR = 9999;
    
    public Resposta enviar(Requisicao req) throws Exception {
        String jsonEnvio = req.paraLinha();
        byte[] dadosEnvio = jsonEnvio.getBytes();
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress enderecoServidor = InetAddress.getByName(HOST_SERVIDOR);
            
            DatagramPacket pacoteEnvio = new DatagramPacket(
                    dadosEnvio,
                    dadosEnvio.length,
                    enderecoServidor,
                    PORTA_SERVIDOR
            );
            socket.send(pacoteEnvio);
            
            byte[] bufferResposta = new byte[65535];
            DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
            
            socket.receive(pacoteResposta);
            
            String jsonResposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
            return Resposta.fromLinha(jsonResposta);
        }
    }
}
