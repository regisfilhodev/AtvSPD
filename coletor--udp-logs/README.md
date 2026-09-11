# 📋 Coletor de Logs Centralizado com Sockets UDP

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![NetBeans](https://img.shields.io/badge/NetBeans-IDE-1B6AC6?style=for-the-badge&logo=apachenetbeans)
![UDP](https://img.shields.io/badge/Protocolo-UDP-blue?style=for-the-badge)

Projeto desenvolvido para a disciplina de **Sistemas Paralelos e Distribuídos**. Aplicação cliente/servidor concorrente em Java utilizando **Sockets UDP**, **Threads (ExecutorService)**, **Exclusão Mútua (`synchronized`)** e **Interface Gráfica em Swing** com atualização assíncrona (`SwingWorker`).

---

## 🎯 Sobre o Projeto

O **Coletor de Logs Centralizado** permite que múltiplas aplicações clientes acumulem eventos de log localmente (nível, origem e mensagem) e os enviem em lote ao servidor central através de datagramas UDP em formato JSON.

### 💡 Principais Desafios e Conceitos Aplicados:
* **Concorrência e Multithreading no Servidor:** O servidor utiliza um pool de 10 threads (`ExecutorService`) para processar as listas de logs recebidas em paralelo sem bloquear a porta UDP.
* **Proteção de Região Crítica:** Para simular um processamento pesado, cada evento da lista leva 1 segundo para ser gravado no repositório compartilhado. O acesso a essa lista central é protegido via `synchronized` para evitar **Condição de Corrida (Race Condition)**.
* **Comunicação Não-Bloqueante na Interface (SwingWorker):** O cliente Swing despacha requisições de rede em segundo plano, mantendo a interface responsiva durante o envio dos pacotes.

---

## ⚙️ Arquitetura do Protocolo JSON

A comunicação entre Cliente e Servidor ocorre via pacotes UDP utilizando a biblioteca **Gson** para serialização e desserialização dos objetos em JSON:

### ✉️ `Requisicao.java`
* **`operacao`**: `"REGISTRAR"` ou `"LISTAR"`.
* **`nivel`**: Filtro para consultas (`"TODOS"`, `"INFO"`, `"WARN"`, `"ERROR"`).
* **`eventos`**: Lista de objetos `Evento` no modo de registro.

### 📩 `Resposta.java`
* **`status`**: `"OK"` ou `"ERRO"`.
* **`timestamp`**: Data/hora do servidor no momento do processamento.
* **`dados`**: Lista de textos contendo confirmações, registros retornados ou mensagens de erro.

---

## 📁 Estrutura dos Projetos

```text
├── ColetorLogsServidor/
│   ├── src/main/java/com/mycompany/coletorlogsservidor/
│   │   ├── Evento.java
│   │   ├── Requisicao.java
│   │   ├── Resposta.java
│   │   ├── RepositorioLogs.java    # Região Crítica (synchronized)
│   │   └── ServidorApp.java        # Socket UDP (porta 9999) + ThreadPool
│   └── pom.xml
│
└── ColetorLogsCliente/
    ├── src/main/java/com/mycompany/coletorlogscliente/
    │   ├── Evento.java
    │   ├── Requisicao.java
    │   ├── Resposta.java
    │   ├── ServicoCliente.java     # Camada de comunicação UDP
    │   └── TelaCliente.java        # Interface Swing + SwingWorker
    └── pom.xml
```

---

## 🚀 Como Executar no NetBeans IDE

### 1. Clonar o Repositório

Abra o terminal e baixe o projeto: 
```bash
git clone https://github.com/hugbrl09/coletor-logs-udp.git
cd coletor-logs-udp
```

### 2. Abrir os Projetos
1. Abra o **NetBeans IDE**.
2. Vá em **File > Open Project...** (ou `Ctrl + Shift + O`).
3. Selecione as duas pastas: `ColetorLogsServidor` e `ColetorLogsCliente`.

---

### 3. Iniciar o Servidor
1. Na aba **Projects** à esquerda, expanda o projeto **`ColetorLogsServidor`**.
2. Navegue até o pacote `com.mycompany.coletorlogsservidor`.
3. Clique com o botão direito no arquivo **`ServidorApp.java`** e selecione **Run File** (ou pressione `Shift + F6`).
4. Verifique na aba de saída (*Output*) do NetBeans a mensagem:
   > `Servidor Coletor de Logs UDP ativo na porta 9999`

---

### 4. Iniciar o Cliente (Interface Gráfica)
1. Expanda o projeto **`ColetorLogsCliente`**.
2. Navegue até o pacote `com.mycompany.coletorlogscliente`.
3. Clique com o botão direito no arquivo **`TelaCliente.java`** e selecione **Run File** (ou pressione `Shift + F6`).
4. A janela da interface gráfica do cliente será exibida.

---

## 🧪 Testando a Concorrência (Intercalação de Logs)

Para validar a concorrência e o uso de threads na Região Crítica do servidor:

1. Abra **duas janelas do cliente**: no NetBeans, clique com o botão direito em **`TelaCliente.java`** e selecione **Run File** duas vezes consecutivas.
2. Na **Janela 1 do Cliente**, adicione 3 eventos à fila local (ex: `Origem: API-A` com mensagens `A1`, `A2`, `A3`).
3. Na **Janela 2 do Cliente**, adicione 3 eventos à fila local (ex: `Origem: API-B` com mensagens `B1`, `B2`, `B3`).
4. Clique no botão **"Enviar Fila ao Servidor"** em ambos os clientes quase ao mesmo tempo.
5. No painel de consulta de qualquer cliente, selecione o filtro `"TODOS"` e clique em **"Consultar Servidor"**.
6. **Resultado Esperado:** Como cada thread do servidor processa um evento por segundo, os logs das duas janelas aparecerão **intercalados no histórico central** (ex: `A1`, `B1`, `A2`, `B2...`), comprovando o atendimento concorrente no servidor!

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17
* **IDE:** NetBeans IDE
* **Redes:** `java.net.DatagramSocket`, `DatagramPacket`
* **Concorrência:** `java.util.concurrent.ExecutorService`, `SwingWorker`, `synchronized`
* **Interface:** Java Swing (GUI)
* **Serialização:** Google Gson