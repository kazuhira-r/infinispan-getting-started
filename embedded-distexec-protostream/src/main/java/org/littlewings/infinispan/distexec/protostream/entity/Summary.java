package org.littlewings.infinispan.distexec.protostream.entity;

public class Summary {
    int value;

    public static Summary create(int value) {
        Summary summary = new Summary();

        summary.setValue(value);

        return summary;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
