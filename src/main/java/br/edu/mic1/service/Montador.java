package br.edu.mic1.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Montador de macroinstrucoes de 16 bits para a arquitetura didatica baseada
 * em MIC-1.
 *
 * Sintaxe esperada:
 *   LODD &variavel
 *   LOCO 10
 *   JUMP inicio
 *   inicio: ADDD 1
 *
 * Comentarios: linhas cujo primeiro caractere nao branco seja '/' sao ignoradas.
 * Labels ocupam o indice da instrucao correspondente (nao endereco em bytes).
 * Variaveis iniciadas por '&' recebem enderecos a partir da quantidade de
 * instrucoes montadas. Cada nome de variavel recebe um endereco unico.
 *
 * O resultado e uma lista de palavras de 16 bits representadas por int (0..65535).
 * A classe nao escreve diretamente na memoria: o Controller deve transferir
 * o resultado para o componente de memoria do Model.
 */
public class Montador {

    private final Encoder encoder;
    private int lastInstructionCount;

    private static final Set<String> BRANCH_INSTRUCTIONS = new HashSet<String>();
    private static final Set<String> DATA_INSTRUCTIONS = new HashSet<String>();

    static {
        BRANCH_INSTRUCTIONS.add("JPOS");
        BRANCH_INSTRUCTIONS.add("JZER");
        BRANCH_INSTRUCTIONS.add("JUMP");
        BRANCH_INSTRUCTIONS.add("JNEG");
        BRANCH_INSTRUCTIONS.add("JNZE");
        BRANCH_INSTRUCTIONS.add("CALL");
        DATA_INSTRUCTIONS.add("LODD");
        DATA_INSTRUCTIONS.add("STOD");
        DATA_INSTRUCTIONS.add("ADDD");
        DATA_INSTRUCTIONS.add("SUBD");
        DATA_INSTRUCTIONS.add("LODL");
        DATA_INSTRUCTIONS.add("STOL");
        DATA_INSTRUCTIONS.add("ADDL");
        DATA_INSTRUCTIONS.add("SUBL");
    }

    public Montador() {
        this.encoder = new Encoder();
    }

    public Montador(Encoder encoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("Encoder nao pode ser nulo.");
        }
        this.encoder = encoder;
    }

    /**
     * Monta todo o programa e retorna as palavras de maquina.
     */
    public List<Integer> montar(String sourceCode) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("O codigo-fonte nao pode ser nulo.");
        }

        Map<String, Integer> dataDeclarations = new LinkedHashMap<String, Integer>();
        String executableSource = extractDataDeclarations(sourceCode, dataDeclarations);
        List<SourceInstruction> instructions = parseSource(executableSource);
        lastInstructionCount = 0;
        for (SourceInstruction instruction : instructions) {
            if (!instruction.labelOnly) {
                lastInstructionCount++;
            }
        }
        Map<String, Integer> labels = collectLabels(instructions);

        // Variaveis sao alocadas depois da ultima instrucao.
        Map<String, Integer> variables = new HashMap<String, Integer>();
        int nextVariableAddress = lastInstructionCount;
        for (String name : dataDeclarations.keySet()) {
            variables.put(name, nextVariableAddress++);
        }

        List<Integer> machineCode = new ArrayList<Integer>();

        for (int i = 0; i < instructions.size(); i++) {
            SourceInstruction instruction = instructions.get(i);
            if (instruction.labelOnly) {
                continue;
            }
            int word;

            if (instruction.operands.isEmpty()) {
                if ("HALT".equals(instruction.mnemonic)) {
                    word = encoder.encode("JUMP", lastInstructionCount);
                } else {
                    word = encoder.encode(instruction.mnemonic);
                }
            } else {
                if (instruction.operands.size() != 1) {
                    throw error(instruction,
                        "Esta arquitetura espera zero ou um operando por instrucao.");
                }

                String operandText = instruction.operands.get(0);
                int operand;

                if (isVariable(operandText)) {
                    String variableName = operandText.substring(1);
                    if (variableName.isEmpty()) {
                        throw error(instruction, "Nome de variavel vazio.");
                    }

                    if (!variables.containsKey(variableName)) {
                        variables.put(variableName, nextVariableAddress++);
                    }
                    operand = variables.get(variableName);

                } else if (BRANCH_INSTRUCTIONS.contains(instruction.mnemonic)
                           && labels.containsKey(operandText)) {
                    operand = labels.get(operandText);

                } else {
                    try {
                        operand = Integer.parseInt(operandText);
                        if (DATA_INSTRUCTIONS.contains(instruction.mnemonic)
                                && operand >= 0
                                && operand < lastInstructionCount) {
                            throw error(instruction,
                                "Endereco numerico " + operand +
                                " colide com uma instrucao (intervalo 0 a " +
                                (lastInstructionCount - 1) + "). Use um endereco maior ou variavel iniciada por &.");
                        }
                    } catch (NumberFormatException ex) {
                        if (BRANCH_INSTRUCTIONS.contains(instruction.mnemonic)) {
                            throw error(instruction,
                                "Label ou endereco numerico nao encontrado: " + operandText);
                        }
                        throw error(instruction,
                            "Operando deve ser decimal ou variavel iniciada por &: " +
                            operandText);
                    }
                }

                try {
                    word = encoder.encode(instruction.mnemonic, operand);
                } catch (IllegalArgumentException ex) {
                    throw error(instruction, ex.getMessage());
                }
            }

            machineCode.add(word);
        }

        int[] variableData = new int[nextVariableAddress - lastInstructionCount];
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            int address = entry.getValue();
            if (address >= lastInstructionCount) {
                Integer initial = dataDeclarations.get(entry.getKey());
                variableData[address - lastInstructionCount] =
                        initial == null ? 0 : initial;
            }
        }
        for (int initial : variableData) {
            if (initial < -2048 || initial > 4095) {
                throw new IllegalArgumentException(
                    "Valor inicial de variavel fora do intervalo permitido (-2048 a 4095): " +
                    initial);
            }
            machineCode.add(initial & 0x0FFF);
        }

        return machineCode;
    }

    /**
     * Versao auxiliar que devolve cada palavra como uma String de 16 bits.
     */
    public List<String> montarBinario(String sourceCode) {
        List<Integer> words = montar(sourceCode);
        List<String> binary = new ArrayList<String>();

        for (Integer word : words) {
            binary.add(String.format("%16s", Integer.toBinaryString(word))
                              .replace(' ', '0'));
        }
        return binary;
    }

    public int getLastInstructionCount() {
        return lastInstructionCount;
    }

    /**
     * Primeira etapa: remove comentarios, identifica labels e separa instrucoes.
     * Aceita label isolado ou label seguido de instrucao na mesma linha.
     */
    private String extractDataDeclarations(String sourceCode,
                                           Map<String, Integer> declarations) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("O codigo-fonte nao pode ser nulo.");
        }
        String[] lines = sourceCode.split("\\R");
        StringBuilder executable = new StringBuilder();
        Pattern declaration = Pattern.compile(
                "^([A-Za-z_][A-Za-z0-9_]*)\\s*:\\s*([+-]?\\d+)\\s*$");

        Pattern nameDeclaration = Pattern.compile(
                "^([A-Za-z_][A-Za-z0-9_]*)\\s*:\\s*$");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String sourceLine = lines[lineIndex];
            String line = sourceLine.trim();
            Matcher matcher = declaration.matcher(line);
            if (matcher.matches()) {
                String name = matcher.group(1);
                int initial;
                try {
                    initial = Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Valor inicial invalido para a variavel " + name + ".");
                }
                if (declarations.containsKey(name)) {
                    throw new IllegalArgumentException(
                            "Variavel declarada duas vezes: " + name);
                }
                declarations.put(name, initial);
                continue;
            }

            Matcher nameMatcher = nameDeclaration.matcher(line);
            if (nameMatcher.matches() && lineIndex + 1 < lines.length) {
                String name = nameMatcher.group(1);
                String nextLine = lines[lineIndex + 1].trim();
                if (nextLine.matches("[+-]?\\d+")) {
                    int initial;
                    try {
                        initial = Integer.parseInt(nextLine);
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                "Valor inicial invalido para a variavel " + name + ".");
                    }
                    if (declarations.containsKey(name)) {
                        throw new IllegalArgumentException(
                                "Variavel declarada duas vezes: " + name);
                    }
                    declarations.put(name, initial);
                    lineIndex++;
                    continue;
                }
            }
            executable.append(sourceLine).append('\n');
        }
        return executable.toString();
    }

    private List<SourceInstruction> parseSource(String sourceCode) {
        List<SourceInstruction> result = new ArrayList<SourceInstruction>();
        String[] lines = sourceCode.split("\\R");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex].trim();

            if (line.isEmpty() || line.startsWith("/")) {
                continue;
            }

            String label = null;

            int colonIndex = line.indexOf(':');
            if (colonIndex >= 0) {
                label = line.substring(0, colonIndex).trim();
                if (!label.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    throw new IllegalArgumentException(
                        "Linha " + (lineIndex + 1) + ": label invalido: " + label);
                }
                line = line.substring(colonIndex + 1).trim();
            }

            if (line.isEmpty()) {
                // Label sem instrucao: sera associado a proxima instrucao.
                result.add(new SourceInstruction(label, null,
                    new ArrayList<String>(), lineIndex + 1, true));
                continue;
            }

            String[] pieces = line.split("\\s+", 2);
            String mnemonic = pieces[0].toUpperCase(Locale.ROOT);
            if ("GOTO".equals(mnemonic)) {
                mnemonic = "JUMP";
            }
            List<String> operands = new ArrayList<String>();

            if (pieces.length > 1) {
                String operandPart = pieces[1].trim();
                if (!operandPart.isEmpty()) {
                    // Remove virgulas para aceitar "LOCO, 10" ou "LOCO 10".
                    operandPart = operandPart.replace(",", " ").trim();
                    if (!operandPart.isEmpty()) {
                        String[] tokens = operandPart.split("\\s+");
                        for (String token : tokens) {
                            if (!token.isEmpty()) {
                                operands.add(token);
                            }
                        }
                    }
                }
            }

            result.add(new SourceInstruction(label, mnemonic, operands,
                                             lineIndex + 1, false));
        }

        return result;
    }

    /**
     * Segunda etapa: resolve labels para indices de instrucoes.
     */
    private Map<String, Integer> collectLabels(List<SourceInstruction> parsed) {
        Map<String, Integer> labels = new HashMap<String, Integer>();
        int instructionIndex = 0;

        for (SourceInstruction item : parsed) {
            if (item.label != null) {
                if (labels.containsKey(item.label)) {
                    throw new IllegalArgumentException(
                        "Linha " + item.lineNumber + ": label duplicado: " + item.label);
                }
                labels.put(item.label, instructionIndex);
            }

            if (!item.labelOnly) {
                instructionIndex++;
            }
        }

        // Associa labels isolados ao indice da proxima instrucao.
        // O indice ja foi contado corretamente na passagem acima.
        return labels;
    }

    private boolean isVariable(String operand) {
        return operand != null && operand.startsWith("&");
    }

    private IllegalArgumentException error(SourceInstruction instruction,
                                           String message) {
        return new IllegalArgumentException(
            "Linha " + instruction.lineNumber + ": " + message);
    }

    private static class SourceInstruction {
        private final String label;
        private final String mnemonic;
        private final List<String> operands;
        private final int lineNumber;
        private final boolean labelOnly;

        private SourceInstruction(String label, String mnemonic,
                                  List<String> operands, int lineNumber,
                                  boolean labelOnly) {
            this.label = label;
            this.mnemonic = mnemonic;
            this.operands = operands;
            this.lineNumber = lineNumber;
            this.labelOnly = labelOnly;
        }
    }
}
