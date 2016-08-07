package org.littlewings.infinispan.wordcount;

import java.io.Serializable;

public class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    String value;
    String partOfSpeech;

    public Token(String value, String partOfSpeech) {
        this.value = value;
        this.partOfSpeech = partOfSpeech;
    }

    public String getValue() {
        return value;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    @Override
    public String toString() {
        return "token = " + value + ", partOfSpeech = " + partOfSpeech;
    }
}
