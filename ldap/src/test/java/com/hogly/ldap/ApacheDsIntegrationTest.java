package com.hogly.ldap;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.annotations.LoadSchema;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.integ.ServerIntegrationUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(FrameworkRunner.class)
@CreateLdapServer(
  transports = { @CreateTransport(protocol = "LDAP") }
  )
@CreateDS(
  allowAnonAccess = true,
  partitions = { @CreatePartition(name = "Xtiva", suffix = "dc=example,dc=com")}
)
@ApplyLdifFiles("ldap-data.ldif")
public class ApacheDsIntegrationTest extends AbstractLdapTestUnit {

  private static ActorSystem system;

  @BeforeClass
  public static void beforeAll() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void afterClass() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void shouldFindAllPersons() throws Exception {
    LdapContext ctx = (LdapContext) ServerIntegrationUtils
      .getWiredContext(ldapServer, null)
      .lookup("ou=Users,dc=example,dc=com");
    ctx.setRequestControls(new Control[] {new SortControl("cn", Control.CRITICAL)});
    NamingEnumeration<SearchResult> res = ctx.search("", "(objectClass=person)", new SearchControls());

    assertThat(res.hasMore(), equalTo(true));
    assertThat(res.next().getName(), equalTo("cn=John Steinbeck"));
    assertThat(res.next().getName(), equalTo("cn=Micha Kops"));
    assertThat(res.next().getName(), equalTo("cn=Santa Claus"));
  }

}
