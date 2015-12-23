// mode=distributed,language=javascript
var scriptCache = cache.getCacheManager().getCache("___script_cache");
var marshaller = scriptCache.getCacheConfiguration().compatibility().marshaller();

var cachedValue3 = cache.get(marshaller.objectToByteBuffer("key3"));
var cachedValue5 = cache.get(marshaller.objectToByteBuffer("key5"));

var value1 = marshaller.objectFromByteBuffer(cachedValue3);
var value2 = marshaller.objectFromByteBuffer(cachedValue5);
java.lang.Double.valueOf(value1 + value2).intValue();
