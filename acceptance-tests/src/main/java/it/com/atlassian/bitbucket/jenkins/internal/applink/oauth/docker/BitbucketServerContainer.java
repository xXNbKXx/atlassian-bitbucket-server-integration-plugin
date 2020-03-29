package it.com.atlassian.bitbucket.jenkins.internal.applink.oauth.docker;

import io.restassured.RestAssured;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.removeStart;

/**
 * Runs the BitbucketServer Docker container
 */
@DockerFixture(id = "bbs", ports = 7990)
public class BitbucketServerContainer extends DockerContainer {

    public String getBaseURL() {
        return "http://" + ipBound(7990) + ":" + port(7990);
    }

    public boolean isBitbucketRunning() {
        String status = RestAssured.given()
                .when()
                .get(getUrl("status"))
                .jsonPath()
                .get("state");
        return "RUNNING".equalsIgnoreCase(status);
    }

    public String getUrl(String relativeUrl) {
        requireNonNull(relativeUrl, "relativeUrl");
        return getBaseURL() + "/" + removeStart(relativeUrl, "/");
    }
}
