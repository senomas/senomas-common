Camel Router Project for Blueprint (OSGi)
=========================================

To build this project use

    mvn install

To run the project you can execute the following Maven goal

    mvn camel:run

To deploy the project in OSGi. For example using Apache ServiceMix
or Apache Karaf. You can run the following command from its shell:

    osgi:install -s mvn:com.senomas/common/1.0.0-SNAPSHOT

For more help see the Apache Camel documentation

    http://camel.apache.org/

    
    
MAVEN REPOSITORIES

http://docker:7081/content/repositories/central/
http://docker:7081/content/repositories/releases/
http://docker:7081/content/repositories/snapshots/
http://docker:7081/content/repositories/release.fusesource.org/
http://docker:7081/content/repositories/ea.fusesource.org/
http://docker:7081/content/repositories/jboss.repository/
http://docker:7081/content/repositories/central/



config:edit org.ops4j.pax.url.mvn

org.ops4j.pax.url.mvn.repositories= \
	http://docker:7081/content/repositories/snapshots@id=senomas.snapshot.repo@snapshot, \
	http://docker:7081/content/repositories/releases@id=senomas.repo, \
    http://docker:7081/content/repositories/central@id=maven.central.repo, \
    http://docker:7081/content/repositories/release.fusesource.org@id=fusesource.release.repo, \
    http://docker:7081/content/repositories/ea.fusesource.org@id=fusesource.ea.repo, \
    http://docker:7081/content/repositories/servicemix.repo@id=servicemix.repo, \
    http://docker:7081/content/repositories/springsource.release.repo@id=springsource.release.repo, \
    http://docker:7081/content/repositories/springsource.external.repo/@id=springsource.external.repo, \
    http://docker:7081/content/repositories/scala.repo/@id=scala.repo

    
    
    
org.ops4j.pax.url.mvn.repositories= \
    http://repo1.maven.org/maven2@id=maven.central.repo, \
    https://repo.fusesource.com/nexus/content/repositories/releases@id=fusesource.release.repo, \
    https://repo.fusesource.com/nexus/content/groups/ea@id=fusesource.ea.repo, \
    http://svn.apache.org/repos/asf/servicemix/m2-repo@id=servicemix.repo, \
    http://repository.springsource.com/maven/bundles/release@id=springsource.release.repo, \
    http://repository.springsource.com/maven/bundles/external@id=springsource.external.repo, \
    https://oss.sonatype.org/content/groups/scala-tools@id=scala.repo
    