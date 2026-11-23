# Simulador MIC-1

Este projeto implementa um simulador didatico da arquitetura MIC-1 com
interface Swing, montador de assembly, memoria principal, memoria de controle,
caches e execucao baseada em microinstrucoes.

O projeto usa Java 17 e Maven.

## Estrutura MVP

```text
src/main/java/br/edu/mic1/
├── Main.java
├── controller/
│   └── SimulatorController.java
├── model/
│   ├── Mic1Model.java
│   ├── UnidadeControle.java
│   ├── Microinstruction.java
│   ├── SpecialMicroinstructionExecutor.java
│   ├── alu/
│   ├── cache/
│   ├── memory/
│   └── register/
├── service/
│   ├── Montador.java
│   ├── Encoder.java
│   ├── AssemblyDecoder.java
│   └── InstructionDecoder.java
└── view/
    ├── SimulatorView.java
    ├── ControlUnitPanel.java
    ├── RegisterPanel.java
    └── MemoryPanel.java
```

### Model

Contem o estado e o comportamento do simulador:

- memoria principal e memoria de controle;
- unidade de controle;
- microinstrucoes;
- registradores;
- ULA e datapath;
- caches e operacoes de memoria.

### View

Contem a interface Swing:

- exibicao dos registradores;
- exibicao de MPC, MIR e barramento C;
- exibicao da memoria;
- exibicao das estatisticas das caches;
- entrada de codigo assembly.

### Controller

`SimulatorController` conecta a interface ao modelo. Suas responsabilidades
sao montar o codigo, carregar o programa na memoria, avancar ciclos, reiniciar
o estado e informar a View sobre mudancas.

## Execucao

A simulacao comeca em `Main` com uma memoria principal de 4096 palavras e uma
memoria de controle de 256 microinstrucoes:

```java
Mic1Model model = new Mic1Model(4096, 256);
SimulatorController controller = new SimulatorController(model);
new SimulatorView(controller).setVisible(true);
```

Cada chamada de `step()` executa um ciclo da unidade de controle.

```java
controller.step();
```

O MPC aponta para a proxima microinstrucao. Quando uma instrucao macro do
programa entra em execucao, o dispatcher seleciona o endereco do microprograma
correspondente.

## Unidade de controle

A [UnidadeControle](src/main/java/br/edu/mic1/model/UnidadeControle.java)
coordena o ciclo de execucao:

1. obtem o endereco atual do MPC;
2. busca a microinstrucao em `MemoriaControle`;
3. atualiza o MIR;
4. delega microinstrucoes especiais;
5. executa o datapath;
6. interpreta sinais de leitura, escrita e registrador destino;
7. atualiza MPC, halt ou proxima microinstrucao.

Ela nao implementa diretamente toda a logica interna do simulador. Essa logica
esta distribuida nos componentes abaixo.

## Componentes especializados

### RegisterFile

Local: `model/register/RegisterFile.java`.

Gerencia os 16 registradores e suas posicoes:

| Indice | Registrador |
|---:|---|
| 0 | MAR |
| 1 | MBR |
| 2 | PC |
| 3 | IR |
| 4 | SP |
| 5 | AC |
| 6 | TIR |
| 7 | H |
| 8 | R8 constante igual a 1 |
| 9-15 | Registradores gerais |

O registrador R8 nao pode ser alterado pela interface ou pelas microinstrucoes.

### StatusRegister

Local: `model/register/StatusRegister.java`.

Guarda as flags N e Z produzidas pela ULA:

- `Z = 1` representa resultado zero;
- `N = 1` representa resultado negativo;
- `JZER` utiliza Z;
- `JNEG` utiliza N.

### Datapath

Local: `model/alu/Datapath.java`.

Responsavel pelas operacoes do caminho de dados:

- AMux;
- MMux;
- selecao das entradas A e B;
- ULA;
- Shifter;
- mascaramento de operandos de 12 bits;
- barramento C;
- escrita no registrador destino.

A ULA possui as seguintes operacoes:

- `ADD`;
- `AND`;
- `PASS_A`;
- `NOT_A`.

### ULA

Local: `model/alu/ULA.java`.

Recebe A, B e F, executa a operacao selecionada e produz:

- `R`, contendo o resultado de 16 bits;
- `D[0]`, indicando N;
- `D[1]`, indicando Z.

A soma usa carry propagando da posicao menos significativa para a mais
significativa.

### MemoryController

Local: `model/memory/MemoryController.java`.

Gerencia o acesso a `MemoriaPrincipal`, `InstructionCache` e `DataCache`.

Leituras recebem uma indicacao do tipo de acesso:

```java
memoryController.read(address, true);  // cache de instrucoes
memoryController.read(address, false); // cache de dados
```

Escritas atualizam a memoria principal e a linha correspondente na cache de
dados.

Isso vale tanto para microinstrucoes convencionais quanto para microinstrucoes
especiais usadas por `PUSH`, `POP`, `CALL`, `RETN` e instrucoes relacionadas.

### InstructionDecoder

Local: `service/InstructionDecoder.java`.

Identifica o opcode da instrucao em IR e retorna o endereco do microprograma:

- instrucoes com nibble de 4 bits usam os quatro bits superiores;
- `CALL` usa opcode `0xE`;
- opcodes de 8 bits e especiais usam o valor correspondente;
- opcoes nao registradas geram `Opcode sem microprograma`.

Exemplos:

- `LODD` possui opcode `0`;
- `LOCO` possui opcode `7`;
- `PUSH` possui opcode `0xF4`;
- `POP` possui opcode `0xF6`.

### AssemblyDecoder

Local: `service/AssemblyDecoder.java`.

Converte uma palavra de 16 bits em texto assembly para a interface. Por
exemplo:

```text
0x7005 -> LOCO 5
0xF400 -> PUSH
```

### SpecialMicroinstructionExecutor

Local: `model/SpecialMicroinstructionExecutor.java`.

Executa os casos especiais que nao podem ser representados pelo microprograma
convencional atual. Os enderecos especiais continuam preservados:

- `96`: JZER;
- `128`: JPOS;
- `144`: JNEG;
- `160`: JNZE;
- `176`: RETN;
- `192-203`: CALL;
- `208-212`: microprogramas de retorno e pilha;
- `224-231`: PUSH/POP;
- `232-236`: INSP/DESP;
- `240-255`: LODL/STOL/ADDL/SUBL.

## Cache

`Cache` usa mapeamento direto e linhas de uma palavra de 16 bits.

Cada linha usa:

- indice calculado por `address % capacidade`;
- tag calculada por `address / capacidade`;
- vetor de 16 bits.

### Cache de instrucoes

`InstructionCache` recebe a palavra buscada da memoria durante o ciclo de
instrucao.

### Cache de dados

`DataCache` recebe leituras e escritas de dados. Uma escrita atualiza a memoria
principal e a linha correspondente da cache.

As duas caches possuem contadores separados de hit e miss, exibidos pela
interface Swing.

## Montador

`Montador` converte o codigo assembly em uma lista de palavras de 16 bits.

### Labels

Labels representam enderecos de instrucoes:

```text
LOOP:
LODD &SOMA
GOTO LOOP
```

No `GOTO`, o montador converte a instrucao em `JUMP` e substitui o label pelo
indice da instrucao correspondente.

### Variaveis

Variaveis comecam com `&` e sao alocadas depois das instrucoes:

```text
LOCO 5
STOD &LIMITE

LIMITE:
5
```

Os dados iniciais sao carregados na memoria principal junto ao programa.

## Instrucoes suportadas

| Instrucao | Formato |
|---|---|
| `LODD` | LODD &variavel |
| `STOD` | STOD &variavel |
| `ADDD` | ADDD &variavel |
| `SUBD` | SUBD &variavel |
| `LOCO` | LOCO valor |
| `JUMP` | JUMP label |
| `GOTO` | GOTO label |
| `JPOS` | JPOS label |
| `JZER` | JZER label |
| `JNEG` | JNEG label |
| `JNZE` | JNZE label |
| `CALL` | CALL endereco |
| `PSHI` | PSHI |
| `POPI` | POPI |
| `PUSH` | PUSH |
| `POP` | POP |
| `RETN` | RETN |
| `SWAP` | SWAP |
| `INSP` | INSP valor |
| `DESP` | DESP valor |
| `HALT` | HALT |

## SUBD

`SUBD` implementa a subtracao por complemento de dois.

Para calcular `10 - 3`:

```text
NOT_A:  3 -> complemento de um
ADD:    complemento de um + 1 -> complemento de dois de 3
ADD:    AC + complemento de dois -> 7
```

O valor `1` vem do registrador constante R8.

## Busca de instrucoes

O microprograma inicial executa:

```text
MAR <- PC
MBR <- Memoria[MAR]
PC <- PC + 1
IR <- MBR
dispatch
```

A leitura da instrucao passa pela cache de instrucoes. Leituras e escritas
convencionais de dados passam pela cache de dados.

## Ciclos especiais

Microinstrucoes especiais manipulam pilha e registradores diretamente:

- `PUSH` escreve AC na pilha;
- `POP` incrementa SP e recupera o valor da pilha em AC;
- `CALL` grava PC e muda o fluxo para o endereco chamado;
- `RETN` restaura o endereco de retorno;
- `SWAP` troca AC e SP;
- `INSP` incrementa SP pelo operando;
- `DESP` decrementa SP pelo operando.

Essas operacoes usam `MemoryController`, portanto nao bypassam mais a cache
de dados.

## Reset

`reset()`:

- reinicia o MPC;
- limpa MIR e barramento C;
- limpa o estado de halt;
- reinicia os registradores;
- restaura SP;
- limpa as caches.

O conteudo de dados escrito pelo usuario na memoria principal nao precisa ser
destruido por todas as operacoes de reset. Ao carregar um novo programa,
`MemoriaPrincipal.clear()` e executado antes da carga.

## Compilacao

Com Maven instalado:

```powershell
mvn compile
mvn test
mvn exec:java
```

Sem Maven, o projeto pode ser compilado com o Java 17:

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
