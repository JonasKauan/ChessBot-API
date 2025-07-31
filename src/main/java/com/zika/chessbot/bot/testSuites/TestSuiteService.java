package com.zika.chessbot.bot.testSuites;

import com.zika.chessbot.bot.ChessBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TestSuiteService {
    private static final String TEST_SUITE_PATH = "src/main/resources/test-suites.txt";
    private final List<EpdEntry> epdEntries;
    private final ChessBot chessBot;

    public TestSuiteService(ChessBot chessBot) {
        this.epdEntries = getEpdEntries();
        this.chessBot = chessBot;
    }

    public ReportTestSuite rodarSuitesTeste() {
        List<Case> successCases = new ArrayList<>();
        List<Case> failedCases = new ArrayList<>();

        for(EpdEntry epdEntry : epdEntries) {
            String botMove = chessBot.decideMove(epdEntry.fen());
            Case testCase = new Case(epdEntry, botMove);

            if(testCase.success()) {
                successCases.add(testCase);
            } else {
                failedCases.add(testCase);
            }
        }

        int totalCases = successCases.size() + failedCases.size();
        String successRate = 100 * successCases.size() / totalCases + "%";

        return new ReportTestSuite(failedCases, successCases, successRate);
    }

    public List<EpdEntry> getEpdEntries() {
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
