# 📋 Coletor de Logs Centralizado com Sockets UDP

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![NetBeans](https://img.shields.io/badge/NetBeans-IDE-1B6AC6?style=for-the-badge&logo=apachenetbeans)
![UDP](https://img.shields.io/badge/Protocolo-UDP-blue?style=for-the-badge)
![License](https://img.shields.io/badge/Licen%C3%A7a-MIT-green?style=for-the-badge)

Aplicação cliente/servidor concorrente desenvolvida em **Java** para a disciplina de **Sistemas Paralelos e Distribuídos**. O sistema utiliza **Sockets UDP**, **Threads (`ExecutorService`)**, **Exclusão Mútua (`synchronized`)** e **Interface Gráfica em Swing** com execução assíncrona (`SwingWorker`).

---

## 🎯 Sobre o Projeto

O **Coletor de Logs Centralizado** permite que múltiplas aplicações clientes acumulem eventos de log localmente (nível, origem e mensagem) e os enviem em lote ao servidor central através de datagramas UDP em formato JSON.

### 💡 Conceitos Aplicados e Desafios Técnicos

* **Concorrência no Servidor:** O servidor utiliza um pool com 10 threads (`ExecutorService`) para processar lotes de logs em paralelo, garantindo que o Socket UDP permaneça desocupado para novas requisições.
* **Proteção de Região Crítica:** Para simular uma operação I/O custosa, cada evento leva 1 segundo para ser persistido no repositório compartilhado. O acesso a essa estrutura de dados é protegido via bloco `synchronized`, prevenindo **Condição de Corrida (Race Condition)**.
* **Interface Não-Bloqueante (`SwingWorker`):** A interface do cliente utiliza threads em segundo plano para enviar e receber pacotes UDP, mantendo a GUI responsiva durante as chamadas de rede.

---

## ⚙️ Arquitetura do Protocolo JSON

A comunicação entre Cliente e Servidor ocorre via pacotes UDP utilizando a biblioteca **Google Gson** para serialização/desserialização de objetos.

### ✉️ Estrutura da Requisição (`Requisicao.java`)

```json
{
  "operacao": "REGISTRAR",
  "nivel": "TODOS",
  "eventos": [
    {
      "origem": "API-Autenticacao",
      "nivel": "ERROR",
      "mensagem": "Falha na conexao com o banco de dados"
    }
  ]
}

```

* `operacao`: Define a ação (`"REGISTRAR"` ou `"LISTAR"`).
* `nivel`: Filtro para consulta (`"TODOS"`, `"INFO"`, `"WARN"`, `"ERROR"`).
* `eventos`: Lista de objetos `Evento` (utilizado na operação de registro).

### 📩 Estrutura da Resposta (`Resposta.java`)

```json
{
  "status": "OK",
  "timestamp": "2026-09-11T19:30:00Z",
  "dados": [
    "[INFO] [API-Autenticacao] Usuario logado com sucesso",
    "[ERROR] [API-Pagamento] Timeout no gateway"
  ]
}

```

* `status`: Indicador do resultado (`"OK"` ou `"ERRO"`).
* `timestamp`: Carimbo de data/hora gerado pelo servidor.
* `dados`: Registros retornados ou confirmações de processamento.

---

## 📁 Estrutura do Projeto

```text
.
├── ColetorLogsServidor/
│   ├── src/main/java/com/mycompany/coletorlogsservidor/
│   │   ├── Evento.java
│   │   ├── Requisicao.java
│   │   ├── Resposta.java
│   │   ├── RepositorioLogs.java    # Região Crítica (synchronized)
│   │   └── ServidorApp.java        # Socket UDP (Porta 9999) + ThreadPool
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

## 🚀 Como Executar

### Pré-requisitos

* **Java JDK 17** ou superior instalado.
* **NetBeans IDE** (ou Apache Maven para execução via terminal).

### 1. Clonar o Repositório

```bash
git clone [https://github.com/regisfilhodev/AtvSPD.git](https://github.com/regisfilhodev/AtvSPD.git)
cd AtvSPD

```

### 2. Executar via NetBeans IDE

1. Abra o NetBeans e acesse **File > Open Project...** (`Ctrl + Shift + O`).
2. Selecione e abra os dois projetos: **`ColetorLogsServidor`** e **`ColetorLogsCliente`**.

#### **Iniciando o Servidor:**

1. No painel **Projects**, expanda o projeto `ColetorLogsServidor`.
2. Clique com o botão direito em `ServidorApp.java` e escolha **Run File** (`Shift + F6`).
3. Confirme na aba de Output a mensagem:
> `Servidor Coletor de Logs UDP ativo na porta 9999`



#### **Iniciando o Cliente:**

1. Expanda o projeto `ColetorLogsCliente`.
2. Clique com o botão direito em `TelaCliente.java` e escolha **Run File** (`Shift + F6`).

---

## 🧪 Validando o Processamento Concorrente

Para observar o comportamento das threads e da região crítica no servidor:

1. **Abra duas instâncias do cliente:** Execute o arquivo `TelaCliente.java` duas vezes consecutivas.
2. **Monte a fila no Cliente 1:** Adicione 3 eventos (ex: `Origem: API-A` | Mensagens: `A1`, `A2`, `A3`).
3. **Monte a fila no Cliente 2:** Adicione 3 eventos (ex: `Origem: API-B` | Mensagens: `B1`, `B2`, `B3`).
4. **Dispare as requisições:** Clique em **"Enviar Fila ao Servidor"** em ambas as janelas quase simultaneamente.
5. **Consulte o histórico:** Em qualquer cliente, selecione o filtro `"TODOS"` e clique em **"Consultar Servidor"**.
6. **Resultado Esperado:** Devido à pausa simulada de 1 segundo por registro no servidor, os eventos dos dois clientes aparecerão **intercalados** (ex: `A1`, `B1`, `A2`, `B2...`), demonstrando que o servidor está tratando as requisições concorrentemente.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Comunicação em Rede:** Java Sockets (`DatagramSocket`, `DatagramPacket`)
* **Concorrência:** Threads (`ExecutorService`), `SwingWorker`, `synchronized`
* **Interface Gráfica:** Java Swing
* **Serialização:** Google Gson
* **Gerenciador de Dependências:** Apache Maven

```

```
