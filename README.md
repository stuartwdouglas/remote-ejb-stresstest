# remote-ejb-stresstest
A project to measure remote EJB invocations...

- compile the project using Maven and copy the 'ear' artifact to $JBOSS_HOME/standalone/deployments

- run the client:

  mvn -f client/pom.xml exec:exec -Druntime=6 -DpoolSize=20 -Dtype=slsb

  Possible '-D' parameter:

  -Dhost
  -Dport

  -Druntime (in seconds)
  -Ddelay (in milliseconds - delay between client invocations)
  -DpoolSize (client side pool size used for parallel calls)
  -Diterations (# of iterations to perform - will override '-Druntime' if provided)
  -Dtype (one of 'slsb' Stateless Session Bean or 'sfsb' Stateful Session Bean)
  -Dcalls (the number of executions on each SLSB)