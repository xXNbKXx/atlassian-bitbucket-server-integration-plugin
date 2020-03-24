package it.com.atlassian.bitbucket.jenkins.internal.applink.oauth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import it.com.atlassian.bitbucket.jenkins.internal.applink.oauth.client.JenkinsOAuthClient;
import it.com.atlassian.bitbucket.jenkins.internal.applink.oauth.pageobjects.AuthorizeTokenPage;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.loggedInAs;
import static org.junit.Assert.assertNotNull;

@WithPlugins({"mailer", "matrix-auth", "atlassian-bitbucket-server-integration"})
public class Applinks3LOAuthTest extends AbstractJUnitTest {

    public static final String CONSUMER_KEY = "test-consumer";
    public static final String CONSUMER_SECRET = "abc123";

    @Inject
    private JenkinsController controller;
    private JenkinsOAuthClient client;
    private User bbsUser;

    @Before
    public void setup() throws Exception {
        client = new JenkinsOAuthClient(getBaseUrl(), CONSUMER_KEY, CONSUMER_SECRET);

        // Enable security
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.configure();
        JenkinsDatabaseSecurityRealm realm = sc.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        sc.save();

        bbsUser = realm.signup().password("bbs-user")
                .email("user@bbs.com")
                .signup("bbs-user");

        controller.populateJenkinsHome(jenkinsHomeZip().toByteArray(), false);
    }

    @Test
    public void testOAuth() throws Exception {
        login(bbsUser);

        OAuth1RequestToken requestToken = client.getRequestToken();
        assertNotNull(requestToken);
        assertThat(requestToken.getToken(), not(isEmptyOrNullString()));

        String authzUrl = client.getAuthorizationUrl(requestToken);
        String oAuthVerifier = new AuthorizeTokenPage(jenkins, URI.create(authzUrl).toURL(), requestToken.getToken())
                .authorize();

        OAuth1AccessToken accessToken = client.getAccessToken(requestToken, oAuthVerifier);
        assertNotNull(accessToken);
        assertThat(accessToken.getToken(), not(isEmptyOrNullString()));
    }

    private ByteArrayOutputStream jenkinsHomeZip() throws IOException {
        ByteArrayOutputStream jenkinsHomeZipBytes = new ByteArrayOutputStream();
        try (ZipOutputStream jenkinsHomeZipStream = new ZipOutputStream(jenkinsHomeZipBytes)) {
            Resource consumersXml = new Resource(getClass().getClassLoader().getResource("oauth-consumers.xml"));
            jenkinsHomeZipStream.putNextEntry(new ZipEntry(consumersXml.getName()));
            jenkinsHomeZipStream.write(consumersXml.asByteArray());
            jenkinsHomeZipStream.closeEntry();
        }
        return jenkinsHomeZipBytes;
    }

    private void login(User user) {
        Login login = jenkins.login().doLogin(user);
        waitFor(login)
                .withTimeout(10, SECONDS)
                .until(loggedInAs(user.fullName()));
    }

    private String getBaseUrl() {
        return controller.getUrl().toString();
    }
}
