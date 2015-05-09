// mode=collator,language=groovy
def list = reducedResults.entrySet().toList()
def sorted = list.sort(false, new org.littlewings.infinispan.scripting.EntryComparatorInGroovy())

def results = [:]
def limit = sorted.size() > 20 ? 20 : sorted.size()

sorted.take(limit).each { entry ->
    results[entry.key] = entry.value
}

results
