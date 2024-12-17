import groovy.json.JsonSlurperClassic

def parseJsonToMap(String json) {
    final slurper = new JsonSlurperClassic()
    return new HashMap<>(slurper.parseText(json))
}
