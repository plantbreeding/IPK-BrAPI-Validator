package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Generate HTML based on a template file and variables.
 */
public class TemplateHTML {
    private String template;
    private Map<String, String> variables;


    public TemplateHTML(String template, Map<String, String> variables) throws IOException, URISyntaxException {
        setTemplate(template);
        this.variables = variables;
    }

    public TemplateHTML(String template) throws IOException, URISyntaxException {
        setTemplate(template);
    }

    public void setTemplate(String path) throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        this.template = new String(Files.readAllBytes(Paths.get(classLoader.getResource(path).toURI())));
    }

    public String generateBody() throws IOException, URISyntaxException {
        StrSubstitutor sub = new StrSubstitutor(this.variables, "{", "}");
        return sub.replace(this.template);
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}
