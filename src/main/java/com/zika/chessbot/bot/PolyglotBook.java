package com.zika.chessbot.bot;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class PolyglotBook {
    private final List<BookEntry> entries;
    private static final int ENTRY_SIZE = 16;

    public PolyglotBook() {
        this.entries = new ArrayList<>();

        try(DataInputStream dis = new DataInputStream(new FileInputStream("src/main/resources/opbook.bin"))) {
            byte[] entryData = new byte[ENTRY_SIZE];

            while(dis.read(entryData) == ENTRY_SIZE) {
                entries.add(parseDataToEntry(entryData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BookEntry parseDataToEntry(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        long key = buffer.getLong();
        short move = buffer.getShort();
        short weight = buffer.getShort();
        int learn = buffer.getInt();

        return new BookEntry(key, move, weight, learn);
    }

}
