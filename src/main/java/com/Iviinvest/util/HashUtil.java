package com.Iviinvest.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe utilitária para operações de hash.
 * <p>
 * Fornece métodos para geração de hashes seguros usando algoritmos criptográficos.
 * Atualmente implementa SHA-256 para hash de strings.
 * <p>
 * Utility class for hash operations.
 * Provides methods for generating secure hashes using cryptographic algorithms.
 * Currently implements SHA-256 for string hashing.
 */
public class HashUtil {

    /**
     * Gera um hash SHA-256 de uma string de entrada.
     * <p>
     * O hash é retornado como uma string hexadecimal de 64 caracteres.
     * <p>
     * Generates a SHA-256 hash of an input string.
     * The hash is returned as a 64-character hexadecimal string.
     *
     * @param input A string a ser hasheada | The string to be hashed
     * @return Hash SHA-256 em formato hexadecimal | SHA-256 hash in hexadecimal format
     * @throws RuntimeException Se o algoritmo SHA-256 não estiver disponível
     *                        | If SHA-256 algorithm is not available
     */
    public static String sha256(String input) {
        try {
            // 1. Obtém instância do algoritmo SHA-256
            // Gets instance of SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 2. Aplica o hash aos bytes da string (UTF-8)
            // Applies hash to string bytes (UTF-8)
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // 3. Converte o hash para representação hexadecimal
            // Converts hash to hexadecimal representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // 4. Tratamento de erro para algoritmo indisponível
            // Error handling for unavailable algorithm
            throw new RuntimeException("Erro ao gerar hash SHA-256 | Error generating SHA-256 hash", e);
        }
    }
}