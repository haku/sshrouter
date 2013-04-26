SSH Router
==========

What is this?
-------------

TODO

How do I use it?
----------------

TODO SSH integration.

Make a HTTP request and get an IP back if query only matched one instance.
See [Amazon's docs for full filter API](http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/index.html?ApiReference-query-DescribeInstances.html).
```
curl 'http://127.0.0.1:47782/ip?instance-state-name=running&tag:Name=*My%20Cloud%20App*'
```

How can I compile it?
---------------------

Its a Maven project.

```sh
mvn clean install assembly:single
java -jar target/sshrouter-1-SNAPSHOT-jar-with-dependencies.jar
```
