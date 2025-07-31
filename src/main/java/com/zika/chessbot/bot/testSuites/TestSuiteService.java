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

@Slf4j
@Service
public class TestSuiteService {
    private static final String TEST_SUITE_REPORTS_DIR_PATH = "src/main/resources/testSuiteReports/";
    private static final String TEST_SUITE_PATH = "src/main/resources/test-suites.txt";
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

        for (EpdEntry epdEntry : epdEntries) {
            String botMove = chessBot.decideMove(epdEntry.fen());
            Case testCase = new Case(epdEntry, botMove);

            if (testCase.success()) {
                successCases.add(testCase);
            } else {
                failedCases.add(testCase);
            }
        }

        int totalCases = successCases.size() + failedCases.size();
        String successRate = 100 * successCases.size() / totalCases + "%";
        ReportTestSuite report = new ReportTestSuite(failedCases, successCases, successRate);
        saveTestSuiteReport(report);

        return report;
    }

    private void saveTestSuiteReport(ReportTestSuite report) {
        File reportFile = new File(
            TEST_SUITE_REPORTS_DIR_PATH + "suite-" + formatter.format(LocalDateTime.now()) + ".json"
        );

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile, report);
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

                String fen = fenSlashBm[0];
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
