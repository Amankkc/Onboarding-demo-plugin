package io.jenkins.plugins.sample;

import java.util.UUID;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public final class CategoryEntry extends AbstractDescribableImpl<CategoryEntry> {

    private final String name;

    public String getUuid() {
        return uuid;
    }

    private final String uuid;
    @DataBoundConstructor
    public CategoryEntry(String name, String uuid) {
        //generate the UUID.

        this.name = name;
        this.uuid = UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<CategoryEntry> {
        @Override public String getDisplayName() {
            return "Category";
        }
    }

}
