// mode=collator,language=javascript
var entrySet = reducedResults.entrySet();
var list = new java.util.ArrayList(entrySet);
java.util.Collections.sort(list, new org.littlewings.infinispan.scripting.EntryComparator())

var results = new java.util.LinkedHashMap();
var limit = list.size() > 20 ? 20 : list.size();

for (var i = 0; i < limit; i++) {
    var entry = list.get(i);
    results.put(entry.getKey(), entry.getValue());
}

results;
