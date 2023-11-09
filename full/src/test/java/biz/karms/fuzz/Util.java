package biz.karms.fuzz;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Util {

    public static String BASE_DIR = getBaseDir();

    public static String getBaseDir() {
        final String env = System.getenv().get("basedir");
        final String sys = System.getProperty("basedir");
        if (StringUtils.isNotBlank(env)) {
            return new File(env).getAbsolutePath();
        }
        if (StringUtils.isBlank(sys)) {
            throw new IllegalArgumentException("Unable to determine project.basedir.");
        }
        return new File(sys).getAbsolutePath();
    }
}
