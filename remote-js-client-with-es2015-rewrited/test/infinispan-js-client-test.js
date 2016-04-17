const infinispan = require("infinispan");
const should = require("chai").should();

describe("Infinispan JavaScript Client, Getting Started Test", () => {
    it("minimal.", () => {
        return infinispan.client({ host: "localhost", port: 11222 }).then(client => {
            console.log("connected.");

            // put
            return client.put("key1", "value1")
                // get and verify
                .then(() => client.get("key1").then(value => value.should.equal("value1")))
                // replace
                .then(() => client.replace("key1", "value100"))
                // put
                .then(() => client.put("key2", "value2"))
                // get and verify
                .then(() => client.get("key1").then(value => value.should.equal("value100")))
                // get and verify
                .then(() => client.get("key2").then(value => value.should.equal("value2")))
                // put all
                .then(() => client.putAll([
                    { key: "key3", value: "value3" },
                    { key: "key4", value: "value4" },
                    { key: "key5", value: "value5" }
                ]))
                // get all and verify
                .then(() => client.getAll(["key1", "key2", "key3", "key4", "key5"]).then(entries => {
                    entries.length.should.equal(5);

                    const sorted = entries.sort((a, b) => a.key.substring(3) - b.key.substring(3));
                    sorted[0].should.deep.equal({ key: "key1", value: "value100" });
                    sorted[1].should.deep.equal({ key: "key2", value: "value2" });
                    sorted[2].should.deep.equal({ key: "key3", value: "value3" });
                    sorted[3].should.deep.equal({ key: "key4", value: "value4" });
                    sorted[4].should.deep.equal({ key: "key5", value: "value5" });
                }))
                // clear
                .then(() => client.clear())
                // size and verify
                .then(() => client.size().then(size => size.should.equal(0)))
                // disconnect
                .finally(() => {
                    console.log("disconnect.");
                    return client.disconnect();
                });
        });
    });

    it("pass callback value.", () => {
        return infinispan.client({ host: "localhost", port: 11222 }).then(client => {
            console.log("connected.");

            // put
            return client.put("key1", "value1")
                // get
                .then(() => client.get("key1"))
                // get result verify
                .then(value => value.should.equal("value1"))
                // replace
                .then(() => client.replace("key1", "value100"))
                // put
                .then(() => client.put("key2", "value2"))
                // get
                .then(() => client.get("key1"))
                // get result verify
                .then(value => value.should.equal("value100"))
                // get
                .then(() => client.get("key2"))
                // get result verify
                .then(value => value.should.equal("value2"))
                // put all
                .then(() => client.putAll([
                    { key: "key3", value: "value3" },
                    { key: "key4", value: "value4" },
                    { key: "key5", value: "value5" }
                ]))
                // get all
                .then(() => client.getAll(["key1", "key2", "key3", "key4", "key5"]))
                // get all result verify
                .then(entries => {
                    entries.length.should.equal(5);

                    const sorted = entries.sort((a, b) => a.key.substring(3) - b.key.substring(3));
                    sorted[0].should.deep.equal({ key: "key1", value: "value100" });
                    sorted[1].should.deep.equal({ key: "key2", value: "value2" });
                    sorted[2].should.deep.equal({ key: "key3", value: "value3" });
                    sorted[3].should.deep.equal({ key: "key4", value: "value4" });
                    sorted[4].should.deep.equal({ key: "key5", value: "value5" });
                })
                // clear
                .then(() => client.clear())
                // size
                .then(() => client.size())
                // size result verify
                .then(size => size.should.equal(0))
                // disconnect
                .finally(() => {
                    console.log("disconnect.");
                    return client.disconnect();
                });
        });
    });
});
