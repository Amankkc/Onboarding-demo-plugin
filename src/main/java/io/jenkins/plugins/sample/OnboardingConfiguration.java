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
import jenkins.model.GlobalConfiguration;

@Extension
@Symbol("onboardingPlugin")
public class OnboardingConfiguration extends GlobalConfiguration {

    private static final Log LOGGER = LogFactory.getLog(OnboardingConfiguration.class);

    private String name;
    private String description;

    public OnboardingConfiguration() {
        load();
        LOGGER.info("Loaded configuration: name=" + name + ", description=" + description);
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
}
