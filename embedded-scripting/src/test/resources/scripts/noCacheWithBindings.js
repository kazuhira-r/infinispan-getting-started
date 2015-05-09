var c = cacheManager.getCache(cacheName);

var list = new java.util.ArrayList();
list.add(java.lang.Double.valueOf(c.get("key4") + c.get("key5")).intValue());
list.add(scriptingManager.getClass().getName());

list;
