var infinispan = require("infinispan");

var connected = infinispan.client(11222, "localhost");

connected.then(function(client) {
    console.log("connected.");

    var putted1 = client.put("key1", "value1");
    var getted1 = putted1.then(function() {
        return client.get("key1").then(function(value) {
            console.log("key1 = " + value);
        });
    });

    var replaceAndPut = getted1.then(function() {
        return client
            .replace("key1", "value100")
            .then(function() {
                return client.put("key2", "value2");
            });
    });

    var getted2 = replaceAndPut.then(function() {
        return client
            .get("key1")
            .then(function(value) {
                console.log("key1 = " + value);
            })
            .then(function() {
                return client.get("key2").then(function(value) {
                    console.log("key2 = " + value);
                });
            })
    });

    var putAll = getted2.then(function() {
        return client.putAll([
            { key: "key3", value: "value3" },
            { key: "key4", value: "value4" },
            { key: "key5", value: "value5" }
        ])
    });

    var getAll = putAll.then(function() {
        return client
            .getAll(["key1", "key2", "key3", "key4", "key5"])
            .then(function(entries) {
                console.log("entries = " + JSON.stringify(entries));
            });
    });

    return getAll.then(function() {
        console.log("clear");
        return client.clear();
    }).finally(function() {
        return client.disconnect().then(function() {
            console.log("disonnected");
        });
    });
});
