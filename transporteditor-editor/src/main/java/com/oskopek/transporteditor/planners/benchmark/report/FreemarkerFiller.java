package com.oskopek.transporteditor.planners.benchmark.report;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * Freemarker utility class for filling in report templates.
 */
public final class FreemarkerFiller {

    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);

    static {
        configuration.setClassForTemplateLoading(FreemarkerFiller.class, "");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Private empty constructor.
     */
    private FreemarkerFiller() {
        // intentionally empty
    }

    /**
     * Generate the filled-in report with the template from {@code templateFile} using the {@code input} data map.
     *
     * @param templateFile the template file
     * @param input the information to pass to Freemarker
     * @return the filled-in report
     */
    public static String generate(String templateFile, Map<String, Object> input) {
        Template template;
        try {
            template = configuration.getTemplate(templateFile);
        } catch (IOException e) {
            throw new IllegalStateException("Error occurred during reading template file.", e);
        }

        StringWriter writer = new StringWriter();
        try {
            template.process(input, writer);
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Error occurred during processing template.", e);
        }
        return writer.toString().replaceAll("\\r\\n", "\n");
    }

}


