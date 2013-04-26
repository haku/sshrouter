SSH Router
==========

What is this?
-------------

TODO

How do I use it?
----------------

TODO SSH integration.

See [Amazon's docs for full filter API](http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/index.html?ApiReference-query-DescribeInstances.html).

Example SSH config (assumes ec2router script is on PATH):
```
Host aws_bastion
  Hostname 123.456.789.123
  User smith01
  IdentityFile ~/.ssh/aws.pem

Host aws-*
  User ec2-user

Host aws-app
  ProxyCommand ec2router aws_bastion 'tag:Name=*My%%20Cloud%%20App*'
```

How can I compile it?
---------------------

Its a Maven project.

```sh
mvn clean install assembly:single
java -jar target/sshrouter-1-SNAPSHOT-jar-with-dependencies.jar
```
