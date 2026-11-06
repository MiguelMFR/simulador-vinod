# Simulador MIC-1

## Cache

A cache armazena palavras de 16 bits, cada uma representada por um vetor de
inteiros contendo apenas `0` ou `1`. Os endereços são índices de palavras,
começando em zero.

A `Cache` usa mapeamento direto: sua capacidade é o número de linhas, com uma
palavra por linha. `readWord` verifica se a palavra está na cache e retorna um
`Optional` vazio em caso de miss; não busca dados em memória principal.
`loadWord` insere uma palavra na cache, por exemplo após o componente que
gerenciar a memória resolver um miss.

Há classes próprias para cache de instruções e de dados. A cache de instruções
é consultada com `readWord` e preenchida com `loadWord`. A cache de dados também
oferece `writeWord`, que atualiza sua linha localmente, sem write-through,
write-back ou acesso a memória.

Exemplo de criação:

```java
InstructionCache instructionCache = new InstructionCache(8);
DataCache dataCache = new DataCache(8);
```

As caches são independentes. Neste estágio, elas não implementam nem dependem
de memória principal; a integração com a memória e o tratamento de misses ficam
para uma etapa posterior.