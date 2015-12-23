// mode=distributed,language=javascript
var scriptCache = cache.getCacheManager().getCache("___script_cache");
var marshaller = scriptCache.getCacheConfiguration().compatibility().marshaller();

var cachedValue3 = cache.get(marshaller.objectToByteBuffer("key3"));
var value1;
if (cachedValue3) {
    value1 = marshaller.objectFromByteBuffer(cachedValue3);
} else {
    cachedValue3 = cache.get("key3");
    value1 = cachedValue3;
}

var cachedValue5 = cache.get(marshaller.objectToByteBuffer("key5"));
var value2;
if (cachedValue5) {
    value2 = marshaller.objectFromByteBuffer(cachedValue5);
} else {
    cachedValue5 = cache.get("key5");
    value2 = cachedValue5;
}

java.lang.Double.valueOf(value1 + value2).intValue();
