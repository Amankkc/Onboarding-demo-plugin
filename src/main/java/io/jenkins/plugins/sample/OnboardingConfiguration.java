package io.jenkins.plugins.sample;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;

@Extension
@Symbol("onboardingPlugin")
public class OnboardingConfiguration extends GlobalConfiguration {

    private static final Log LOGGER = LogFactory.getLog(OnboardingConfiguration.class);

    private String name;
    private String description;
    private String url;
    private String username;
    private Secret password;

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
}
