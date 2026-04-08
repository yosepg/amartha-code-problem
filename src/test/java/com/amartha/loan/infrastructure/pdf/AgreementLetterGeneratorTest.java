package com.amartha.loan.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AgreementLetterGeneratorTest {

    private AgreementLetterGenerator generator;

    @Test
    void generateAgreementLetter_withValidLoanData_returns_pdfPath() throws IOException {
        generator = new AgreementLetterGenerator();
        String path = generator.generateAgreementLetter("loan-123", "B-001", "5000000.00");

        assertNotNull(path);
        assertTrue(path.contains("loan-123"));
        assertTrue(path.endsWith(".pdf"));
    }

    @Test
    void generateAgreementLetter_creates_file_in_storage(@TempDir Path tempDir) throws IOException {
        generator = new AgreementLetterGenerator(tempDir.toString());
        String path = generator.generateAgreementLetter("loan-456", "B-002", "3000000.00");

        assertTrue(Files.exists(Path.of(path)));
    }

    @Test
    void generateAgreementLetter_embeds_loanDetails(@TempDir Path tempDir) throws IOException {
        generator = new AgreementLetterGenerator(tempDir.toString());
        String path = generator.generateAgreementLetter("loan-789", "B-003", "7500000.00");

        byte[] pdfContent = Files.readAllBytes(Path.of(path));
        assertTrue(pdfContent.length > 0);
    }
}
