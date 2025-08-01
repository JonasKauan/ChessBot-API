package com.zika.chessbot.bot.testSuites;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zika.chessbot.bot.ChessBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class TestSuiteService {
    private static final String TEST_SUITE_REPORTS_DIR_PATH = "src/main/resources/testSuiteReports/";
    private static final String TEST_SUITE_PATH = "src/main/resources/test-suites.txt";
    private static final char INCOMPLETE = '░';
    private static final char COMPLETE = '█';
    private final DateTimeFormatter formatter;
    private final List<EpdEntry> epdEntries;
    private final ObjectMapper objectMapper;
    private final ChessBot chessBot;

    public TestSuiteService(ChessBot chessBot) {
        this.formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss");
        this.epdEntries = getEpdEntries();
        this.objectMapper = new ObjectMapper();
        this.chessBot = chessBot;
    }

    public ReportTestSuite rodarSuitesTeste() {
        List<Case> successCases = new ArrayList<>();
        List<Case> failedCases = new ArrayList<>();
        int totalCases = epdEntries.size();

        StringBuilder progressBar = new StringBuilder();
        Stream.generate(() -> INCOMPLETE).limit(totalCases).forEach(progressBar::append);
        log.info("Iniciando suítes de teste");

        for (EpdEntry epdEntry : epdEntries) {
            int i = successCases.size() + failedCases.size();
            progressBar.replace(i, i + 1, String.valueOf(COMPLETE));

            log.info("Progresso: {} - {}%", progressBar, 100 * i / totalCases);

            String botMove = chessBot.decideMove(epdEntry.fen(), false);
            Case testCase = new Case(epdEntry, botMove);

            if (testCase.success()) {
                successCases.add(testCase);
            } else {
                failedCases.add(testCase);
            }
        }

        String successRate = 100 * successCases.size() / totalCases + "%";
        ReportTestSuite report = new ReportTestSuite(successRate, failedCases, successCases);

        log.info("[TestSuiteService.rodarSuitesTeste] Suítes de teste concluídas");
        saveTestSuiteReport(report);

        return report;
    }

    private void saveTestSuiteReport(ReportTestSuite report) {
        File reportFile = new File(
            TEST_SUITE_REPORTS_DIR_PATH + "suite-" + formatter.format(LocalDateTime.now()) + ".json"
        );

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile, report);
            log.info("Arquivo gerado: {}", reportFile.getAbsolutePath());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private List<EpdEntry> getEpdEntries() {
        File testSuites =  new File(TEST_SUITE_PATH);
        List<EpdEntry> epdEntries = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(testSuites))) {
            String line;

            while((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(";");
                String[] fenSlashBm = lineSplit[0].split("bm");
                String fen = fenSlashBm[0].substring(0, fenSlashBm[0].length() - 1);
                String bestMove = fenSlashBm[1].trim();
                String id = lineSplit[1].substring(lineSplit[1].indexOf("\"") + 1, lineSplit[1].lastIndexOf("\""));

                epdEntries.add(new EpdEntry(id, fen, bestMove));
            }

        } catch(IOException e) {
            e.fillInStackTrace();
        }

        return epdEntries;
    }

}
