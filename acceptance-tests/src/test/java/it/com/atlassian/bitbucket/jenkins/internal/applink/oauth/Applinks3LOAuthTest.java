package it.com.atlassian.bitbucket.jenkins.internal.applink.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import io.restassured.RestAssured;
import it.com.atlassian.bitbucket.jenkins.internal.applink.oauth.client.JenkinsOAuthClient;
import it.com.atlassian.bitbucket.jenkins.internal.applink.oauth.docker.BitbucketServerContainer;
import it.com.atlassian.bitbucket.jenkins.internal.applink.oauth.pageobjects.AuthorizeTokenPage;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.http.ContentType.URLENC;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.loggedInAs;
import static org.junit.Assert.assertNotNull;

@WithDocker
@WithPlugins({"mailer", "matrix-auth", "atlassian-bitbucket-server-integration"})
public class Applinks3LOAuthTest extends AbstractJUnitTest {

    private static String CONSUMER_KEY_FIELD = "consumerKey";
    private static String CONSUMER_NAME_FIELD = "consumerName";
    private static String CONSUMER_SECRET_FIELD = "consumerSecret";
    private static String CONSUMER_CALLBACKURL_FIELD = "callbackUrl";

    private final ObjectMapper jsonSerializer = new ObjectMapper();

    @Inject
    public DockerContainerHolder<BitbucketServerContainer> bbsContainerHolder;

    @Inject
    private JenkinsController controller;

    private BitbucketServerContainer bbsContainer;
    private JenkinsOAuthClient client;
    private User stashUser;

    @Before
    public void setup() throws Exception {
        bbsContainer = bbsContainerHolder.get();

        Map<String, String> consumerData = newConsumerData();
        String consumerCreateUri = absoluteUrl("/bbs-oauth/create/performCreate");
        RestAssured.given()
                .contentType(URLENC)
                .formParam("json", jsonSerializer.writeValueAsString(consumerData))
                .when()
                .post(consumerCreateUri)
                .then()
                .statusCode(302);

        client = new JenkinsOAuthClient(getBaseUrl(), consumerData.get(CONSUMER_KEY_FIELD),
                consumerData.get(CONSUMER_SECRET_FIELD));

        // Enable security
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.configure();
        JenkinsDatabaseSecurityRealm realm = sc.useRealm(JenkinsDatabaseSecurityRealm.class);
        realm.allowUsersToSignUp(true);
        sc.save();

        stashUser = realm.signup("bbsuser" + randomNumeric(8));
    }

    @Test
    public void testOAuth() throws Exception {
        driver.get(bbsContainer.getBaseURL());
        waitFor().withTimeout(ofSeconds(120))
                .withMessage("Bitbucket didn't start within 2 minutes")
                .until(ignored -> bbsContainer.isBitbucketRunning());

        waitFor().withTimeout(ofSeconds(10)).until(cpl -> cpl.find(By.name("j_username"))).sendKeys("admin");
        waitFor().withTimeout(ofSeconds(10)).until(cpl -> cpl.find(By.name("j_password"))).sendKeys("admin");
        waitFor().withTimeout(ofSeconds(10)).until(cpl -> cpl.find(By.name("submit"))).click();

        //todo: wait for login page (flakey)
        login(stashUser);

        OAuth1RequestToken requestToken = client.getRequestToken();
        assertNotNull(requestToken);
        assertThat(requestToken.getToken(), not(isEmptyOrNullString()));

        String authzUrl = client.getAuthorizationUrl(requestToken);
        String oAuthVerifier = new AuthorizeTokenPage(jenkins, URI.create(authzUrl).toURL(), requestToken.getToken())
                .authorize();

        OAuth1AccessToken accessToken = client.getAccessToken(requestToken, oAuthVerifier);
        assertNotNull(accessToken);
        assertThat(accessToken.getToken(), not(isEmptyOrNullString()));

        driver.get(bbsContainer.getUrl("dashboard") + "?accessToken=" + accessToken.getToken());
        return;
    }

    private Map<String, String> newConsumerData() {
        String uniqueConsumerIdentifier = randomNumeric(8);
        Map<String, String> consumer = new HashMap<>();
        consumer.put(CONSUMER_KEY_FIELD, "stash-consumer" + uniqueConsumerIdentifier);
        consumer.put(CONSUMER_NAME_FIELD, "Stash Consumer " + uniqueConsumerIdentifier);
        consumer.put(CONSUMER_SECRET_FIELD, randomAlphanumeric(10));
        consumer.put(CONSUMER_CALLBACKURL_FIELD, "http://whatever.com/redirect" + uniqueConsumerIdentifier);
        return consumer;
    }

    private void login(User user) {
        // todo: flakey as fuck
        Login login = jenkins.login().doLogin(user);
        waitFor(login)
                .withTimeout(10, SECONDS)
                .until(loggedInAs(user.fullName()));
    }

    private String absoluteUrl(String relativeUrl) {
        return removeEnd(getBaseUrl(), "/") + "/" + removeStart(relativeUrl, "/");
    }

    private String getBaseUrl() {
        return controller.getUrl().toString();
    }
}
