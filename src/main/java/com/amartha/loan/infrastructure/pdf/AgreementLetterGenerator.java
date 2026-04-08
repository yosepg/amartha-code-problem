package com.amartha.loan.infrastructure.pdf;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@ApplicationScoped
public class AgreementLetterGenerator {

    private final String storageDir;

    public AgreementLetterGenerator() {
        this.storageDir = "/tmp/amartha-loans/agreements";
    }

    public AgreementLetterGenerator(String storageDir) {
        this.storageDir = storageDir;
    }

    public String generateAgreementLetter(String loanId, String borrowerId, String principalAmount) throws IOException {
        Path dirPath = Paths.get(storageDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setLineWidth(1);
            contentStream.addRect(50, 50, 495, 742);
            contentStream.stroke();
        }

        String filename = UUID.randomUUID() + "_" + loanId + ".pdf";
        Path filePath = dirPath.resolve(filename);
        document.save(filePath.toFile());
        document.close();

        return filePath.toString();
    }
}
