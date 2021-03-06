package com.codebutler.farebot.card.ultralight;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.support.annotation.NonNull;

import com.codebutler.farebot.card.TagReader;
import com.codebutler.farebot.card.UnsupportedTagException;
import com.codebutler.farebot.card.ultralight.raw.RawUltralightCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UltralightTagReader extends TagReader<MifareUltralight, RawUltralightCard> {

    public UltralightTagReader(@NonNull byte[] tagId, @NonNull Tag tag) {
        super(tagId, tag);
    }

    @NonNull
    @Override
    protected MifareUltralight getTech(@NonNull Tag tag) {
        return MifareUltralight.get(tag);
    }

    @NonNull
    @Override
    protected RawUltralightCard readTag(
            @NonNull byte[] tagId,
            @NonNull Tag tag,
            @NonNull MifareUltralight tech) throws Exception {
        int size;
        switch (tech.getType()) {
            case MifareUltralight.TYPE_ULTRALIGHT:
                size = UltralightCard.ULTRALIGHT_SIZE;
                break;
            case MifareUltralight.TYPE_ULTRALIGHT_C:
                size = UltralightCard.ULTRALIGHT_C_SIZE;
                break;

            // unknown
            default:
                throw new UnsupportedTagException(new String[]{"Ultralight"},
                        "Unknown Ultralight type " + tech.getType());
        }

        // Now iterate through the pages and grab all the datas
        int pageNumber = 0;
        byte[] pageBuffer = new byte[0];
        List<UltralightPage> pages = new ArrayList<>();
        while (pageNumber <= size) {
            if (pageNumber % 4 == 0) {
                // Lets make a new buffer of data. (16 bytes = 4 pages * 4 bytes)
                pageBuffer = tech.readPages(pageNumber);
            }

            // Now lets stuff this into some pages.
            pages.add(UltralightPage.create(pageNumber, Arrays.copyOfRange(
                    pageBuffer,
                    (pageNumber % 4) * MifareUltralight.PAGE_SIZE,
                    ((pageNumber % 4) + 1) * MifareUltralight.PAGE_SIZE)));
            pageNumber++;
        }

        // Now we have pages to stuff in the card.
        return RawUltralightCard.create(
                tagId,
                new Date(),
                pages,
                tech.getType());
    }
}
