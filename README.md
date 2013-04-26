SSH Router
==========

What is this?
-------------

Every time my Cloud Formation the IP address of my instances change.
So every time I want to ssh to one I have to look in the EC2 console for the new IP and paste it into my .ssh/config.
If you do a lot of deploys this gets annoying really quickly.

This is a tool to automate this process by having a query in .ssh/config - effectively a dynamic HostName field.
A provided script referenced via ProxyCommand makes a local HTTP call
to a Java process on localhost that handles the actual EC2 API queries, caches reposes (TODO), etc.

Warning: This code is a proof of concept and liable to break or explode at any time.

How do I use it?
----------------

Integration with ssh is by slightly abusing the ProxyCommnand directive.
The sample .ssh/config below shows example usage.
Note that it assumes ec2router script is on PATH.
It also assumes that you are using a VPC with bastions style setup.
The Java server must be running when the connection is estabilished.

```
Host aws_bastion
  HostName 123.456.789.123
  User smith01
  IdentityFile ~/.ssh/aws.pem

Host aws-*
  User ec2-user

Host aws-app
  ProxyCommand ec2router aws_bastion 'tag:Name=*My%%20Cloud%%20App*'
```

The filter string is per the AWS EC2 filter API.
See [Amazon's docs for filter API](http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/index.html?ApiReference-query-DescribeInstances.html).
Currently the query must already be URI escaped.
Note that as this is the ProxyCommand, % must be escaped as %%.

How can I compile and run the server?
-------------------------------------

Its a Maven project.

```sh
mvn clean install assembly:single
java -jar target/sshrouter-1-SNAPSHOT-jar-with-dependencies.jar
```

I think I found a bug?!
-----------------------

Almost certainly.  Please raise a GitHub issue.
