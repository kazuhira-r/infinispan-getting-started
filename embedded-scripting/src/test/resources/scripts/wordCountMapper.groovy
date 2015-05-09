// mode=mapper,reducer=wordCountReducer.groovy,collator=wordCountCollator.groovy,language=groovy
def words = value.split(/[\W]+/)

words
    .grep { it && it.length() > 5 }
    .each { collector.emit(it.toLowerCase(), 1) }
