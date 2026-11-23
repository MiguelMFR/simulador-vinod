# Simulador MIC-1

Simulador didático da arquitetura MIC-1 desenvolvido em Java 17, com interface gráfica Swing, montador Assembly, memória principal, memória de controle, caches e execução baseada em microinstruções.

## Tecnologias

- Java 17
- Maven
- Java Swing

## Arquitetura

O projeto utiliza o padrão MVC:

- **Model:** gerencia registradores, memória, unidade de controle, ULA, datapath e caches.
- **View:** apresenta a interface gráfica, incluindo registradores, memória, microinstruções e estatísticas das caches.
- **Controller:** conecta a interface ao modelo, gerencia a montagem e controla a execução.

## Funcionalidades

- Montagem de código Assembly.
- Gerenciamento de memória principal e memória de controle.
- Execução passo a passo de microinstruções.
- Unidade de controle com MPC e MIR.
- ULA com operações ADD, AND, PASS_A e NOT_A.
- Registradores e flags de estado N e Z.
- Cache de instruções e cache de dados.
- Cache com mapeamento direto e linhas de uma palavra de 16 bits.
- Estatísticas de hit e miss.
- Operações de pilha e controle de fluxo.
- Interface gráfica para acompanhamento da execução.

## Instruções Assembly suportadas

| Instrução | Função |
|---|---|
| LODD | Carrega valor da memória |
| STOD | Armazena valor na memória |
| ADDD | Soma valor da memória |
| SUBD | Subtrai valor da memória |
| LOCO | Carrega constante |
| JUMP / GOTO | Salto incondicional |
| JPOS | Salto se positivo |
| JZER | Salto se zero |
| JNEG | Salto se negativo |
| JNZE | Salto se diferente de zero |
| CALL | Chamada de sub-rotina |
| RETN | Retorno de sub-rotina |
| PUSH / POP | Operações de pilha |
| PSHI / POPI | Operações indiretas de pilha |
| SWAP | Troca AC e SP |
| INSP / DESP | Ajuste do ponteiro da pilha |
| HALT | Finaliza a execução |

O montador também oferece suporte a labels e variáveis.

## Cache

O simulador possui duas caches independentes:

- **InstructionCache:** utilizada na busca de instruções.
- **DataCache:** utilizada na leitura e escrita de dados.

Ambas utilizam mapeamento direto, com uma palavra de 16 bits por linha e contadores individuais de hits e misses.

As escritas utilizam a política *write-through*, atualizando a memória principal e a cache de dados.

## Execução

A simulação utiliza uma memória principal de 4096 palavras e uma memória de controle com 256 microinstruções.

Inicialização:

```java
Mic1Model model = new Mic1Model(4096, 256);
SimulatorController controller = new SimulatorController(model);
new SimulatorView(controller).setVisible(true);
```

Cada chamada de `step()` executa um ciclo da unidade de controle:

```java
controller.step();
```

## Compilação e execução

Com Maven instalado:

```powershell
mvn compile
mvn test
mvn exec:java
```

Alternativamente, utilizando apenas o Java 17:

```powershell
$sources = Get-ChildItem -Recurse -Filter *.java src/main/java |
    ForEach-Object -ExpandProperty FullName

javac -encoding UTF-8 -d target/classes $sources
```

## Docker

Para executar a interface Swing em um navegador, use Docker Compose:

```powershell
docker compose up --build
```

Depois, abra [http://localhost:6080/vnc.html](http://localhost:6080/vnc.html).
Clique em `Connect` na pagina noVNC para visualizar o simulador.

Para ajustar a resolução da área virtual mostrada pelo navegador, altere
`SCREEN_WIDTH` e `SCREEN_HEIGHT` no `docker-compose.yml`. A configuração
padrão usa uma tela de 1600x900. Também é possível abrir noVNC com o ajuste
visual ativo usando:

```text
http://localhost:6080/vnc.html?autoconnect=true&resize=scale
```

Para executar sem o Compose:

```powershell
docker build -t simulador-mic1 .
docker run --rm -p 6080:6080 --name simulador-mic1 simulador-mic1
```
