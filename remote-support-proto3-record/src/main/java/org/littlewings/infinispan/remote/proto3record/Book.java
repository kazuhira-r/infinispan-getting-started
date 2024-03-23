package org.littlewings.infinispan.remote.proto3record;

import org.infinispan.protostream.annotations.Proto;

@Proto
public record Book(
        String isbn,
        String title,
        int price
) {
}
