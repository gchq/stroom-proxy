package stroom.proxy.util.spring;

import java.io.File;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class StroomResourceLoaderUtil {
//    private static String configReplace = null;
//    private static final String TEST_CONFIG_PATH = "stroom-config";
//
//    static {
//        try {
//            File parent = new File(".");
//            parent = new File(parent.getCanonicalPath());
//
//            // Go back up through parents to try and find the config directory.
//            File configDir = null;
//            while (parent != null && (configDir == null || !configDir.isDirectory())) {
//                configDir = new File(parent, TEST_CONFIG_PATH);
//                parent = parent.getParentFile();
//            }
//
//            if (configDir != null && configDir.isDirectory()) {
//                configReplace = "file://" + configDir.getCanonicalPath() + "/";
//            }
//        } catch (final Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public static Resource getResource(ResourceLoader resourceLoader, String path) {
//        if (configReplace != null) {
//            String newPath = path.replace("classpath:", configReplace);
//            Resource resource = resourceLoader.getResource(newPath);
//            if (resource.exists()) {
//                return resource;
//            }
//        }
        return resourceLoader.getResource(path);
    }
}
