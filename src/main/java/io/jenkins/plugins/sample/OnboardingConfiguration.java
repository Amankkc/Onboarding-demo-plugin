package io.jenkins.plugins.sample;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

@Extension
@Symbol("onboardingPlugin")
public class OnboardingConfiguration extends GlobalConfiguration {

    private static final Log LOGGER = LogFactory.getLog(OnboardingConfiguration.class);

    private String name;
    private String description;
    private String url;
    private String username;
    private Secret password;
    private CategoryConfig categoryConfig;

    public OnboardingConfiguration() {
        load();
        LOGGER.info("Loaded configuration: name=" + name + ", description=" + description + " url=" +url+ " username"
                    + "=" + username + " password="+password);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        try {
            // Call doCheckName to validate the name
            FormValidation validation = doCheckName(name);
            if (validation.kind == FormValidation.Kind.OK) {
                this.name = name;
                save(); // Save only if valid
                LOGGER.info("Saved name: " + name);
            } else {
                LOGGER.warn("Attempted to save invalid name: " + name + ". Reason: " + validation.getMessage());
            }
        } catch (IOException | ServletException e) {
            LOGGER.error("Error validating name: " + e.getMessage(), e);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        save();
        LOGGER.info("Saved description: " + description);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        save(); // Save the URL
        LOGGER.info("Saved URL: " + url);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        try {
            // Call doCheckUsername to validate the username
            FormValidation validation = doCheckUsername(username);
            if (validation.kind == FormValidation.Kind.OK) {
                this.username = username;
                save(); // Save only if valid
                LOGGER.info("Saved username: " + username);
            } else {
                LOGGER.warn("Attempted to save invalid username: " + username + ". Reason: " + validation.getMessage());
            }
        } catch (IOException | ServletException e) {
            LOGGER.error("Error validating username: " + e.getMessage(), e);
        }
    }

    public Secret getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Secret.fromString(password);
        save();
        LOGGER.info("Saved password.");
    }

    public CategoryConfig getCategoryConfig() {
        return categoryConfig;
    }

    public void setCategoryConfig(CategoryConfig categoryConfig) {
        this.categoryConfig = categoryConfig;
    }

    public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
        if (value.isEmpty()) {
            return FormValidation.warning("value is empty");
        } else {
            Pattern pattern = Pattern.compile("^[A-Za-z\\s]+$");
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                return FormValidation.error("Invalid data in value");
            } else {
                return FormValidation.ok("All good!");
            }
        }
    }

    public FormValidation doCheckUsername(@QueryParameter String value) throws IOException, ServletException {
        if (value.isEmpty()) {
            return FormValidation.warning("Username cannot be empty.");
        } else {
            Pattern pattern = Pattern.compile("^[A-Za-z]+$");
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                return FormValidation.error("Username can only contain letters.");
            }
        }
        return FormValidation.ok("Username is valid.");
    }

    public FormValidation doCheckUrl(@QueryParameter String value) throws IOException, ServletException {
        if (value.isEmpty()) {
            return FormValidation.warning("URL cannot be empty.");
        } else {
            // Basic URL validation (consider using a more robust regex for production)
            Pattern pattern = Pattern.compile("^(http|https)://.*$");
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                return FormValidation.error("Invalid URL format.");
            }
        }
        return FormValidation.ok("URL is valid.");
    }

    @POST
    public FormValidation doTestConnection(@QueryParameter String url, @QueryParameter String username, @QueryParameter Secret password) throws IOException, InterruptedException {
        String credentials = String.join(":", username, password.getPlainText());
        String headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                                 .uri(URI.create(url))
                                 .header("Authorization", headerValue)
                                 .GET()
                                 .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return FormValidation.error("Connection Failed: Provided configuration details are not correct. Response Code: " + response.statusCode());
            }

            return FormValidation.ok("Connection Success!!!");
        } catch (IOException | InterruptedException e) {
            return FormValidation.error("Connection Failed: " + e.getMessage());
        }
    }


    /**
     * Using credentials plugin i.e SecretText
     *
     */
    @POST
    public FormValidation doTestPayload(@AncestorInPath Item item, @QueryParameter String url,
                                        @QueryParameter String username,
                                        @QueryParameter String credentialsId) throws IOException, InterruptedException {
        DomainRequirement domainRequirement = new DomainRequirement();
        List<StandardCredentials> credentials =
                CredentialsProvider.lookupCredentials(StandardCredentials.class, item, ACL.SYSTEM, domainRequirement);
        Optional<StringCredentials> optStandardCredentials = credentials.stream().filter(standardCredentials ->
                                                                                                 standardCredentials instanceof StringCredentials && standardCredentials.getId().matches(credentialsId))
                                                                        .map(standardCredentials -> (StringCredentials) standardCredentials)
                                                                        .findFirst();
        if (optStandardCredentials.isPresent()) {
            Secret password = optStandardCredentials.get().getSecret();
            String headerValue = "Basic "+ username+":"+ password.getEncryptedValue();
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder().uri(URI.create(url))
                                     .header("Authorization", headerValue)
                                     .POST(HttpRequest.BodyPublishers.ofString(optStandardCredentials.get().getSecret().getPlainText())).build();
            var responseFuture = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseFuture.statusCode() != 200) {
                return FormValidation.error("Payload test Failed: Provided configuration details are not correct. Response Code: " + responseFuture.statusCode());
            }
        }

        return FormValidation.ok("Test connection successful");
    }

    //Fills the dropdown list when any credentials get added.
    @POST
    public ListBoxModel doFillCredentialsIdItems(
            @AncestorInPath Item item, @QueryParameter String credentialsId,
            @QueryParameter String url) {

        StandardListBoxModel result = new StandardListBoxModel();
        DomainRequirement domainRequirement = new DomainRequirement();
        List<StandardCredentials> credentials =
                CredentialsProvider.lookupCredentials(StandardCredentials.class, item, ACL.SYSTEM, domainRequirement);

        for (StandardCredentials c : credentials) {
            if (c instanceof StringCredentials) {
                result.add(c.getId(), c.getId());
            }
        }
        return result.includeCurrentValue(credentialsId);
    }



}
