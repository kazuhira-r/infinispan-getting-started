// mode=local,language=javascript
var cachedValue3 = cache.get(marshaller.objectToByteBuffer("key3"));
var cachedValue5 = cache.get(marshaller.objectToByteBuffer("key5"));

var value1 = marshaller.objectFromByteBuffer(cachedValue3) * a;
var value2 = marshaller.objectFromByteBuffer(cachedValue5) * b;
java.lang.Double.valueOf(value1 + value2).intValue();

